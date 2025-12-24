package com.lab201.monitor.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lab201.monitor.common.result.Result;
import com.lab201.monitor.common.result.ResultCodeEnum;
import com.lab201.monitor.entity.SysRole;
import com.lab201.monitor.entity.SysUser;
import com.lab201.monitor.entity.SysUserRole;
import com.lab201.monitor.service.ISysRoleService;
import com.lab201.monitor.service.ISysUserRoleService;
import com.lab201.monitor.service.ISysUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "03. 用户管理")
@RestController
@RequestMapping("/api/v1/sys-user")
public class SysUserController {

    @Autowired
    private ISysUserService sysUserService;

    @Autowired
    private ISysUserRoleService sysUserRoleService; // 记得注入关联表 Service
    @Autowired
    private ISysRoleService sysRoleService;         // 记得注入角色表 Service

    @Operation(summary = "分页查询用户")
    @GetMapping("/page")
    public Result<Page<SysUser>> page(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword) {
        Page<SysUser> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();

        if(StringUtils.hasText(keyword)){
            wrapper.and(w->w
                    .like(SysUser::getUsername,keyword)
                    .or()
                    .like(SysUser::getRealName,keyword));
        }
        return Result.success(sysUserService.page(pageParam,wrapper));
    }

    @Operation(summary = "新增用户")
    @PostMapping
    public Result<Boolean> save(@RequestBody SysUser sysUser) {
        // 实际开发中需要在这里加密密码
        // sysUser.setPassword(passwordEncoder.encode(sysUser.getPassword()));
        return Result.success(sysUserService.save(sysUser));
    }

    @Operation(summary = "修改用户")
    @PutMapping
    public Result<Boolean> update(@RequestBody SysUser sysUser) {
        return Result.success(sysUserService.updateById(sysUser));
    }

    @Operation(summary = "删除用户")
    @DeleteMapping("/{id}")
    public Result<Boolean> remove(@PathVariable Long id) {
        return Result.success(sysUserService.removeById(id));
    }

    @Operation(summary = "根据ID获取详情")
    @GetMapping("/{id}")
    public Result<SysUser> getInfo(@PathVariable Long id) {
        return Result.success(sysUserService.getById(id));
    }

    @Operation(summary = "登录功能")
    @PostMapping("/login")
    public Result<SysUser> login(@RequestBody LoginDto loginDto) {
        // 1. 查用户
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUsername, loginDto.getUsername());
        wrapper.select(SysUser::getId, SysUser::getUsername, SysUser::getPassword, SysUser::getStatus, SysUser::getRealName, SysUser::getAvatar);
        SysUser user = sysUserService.getOne(wrapper);

        // 2. 校验用户 & 密码
        if (user == null) {
            return Result.fail(ResultCodeEnum.USER_NOT_EXIST);
        }

        if(!user.getPassword().equals(loginDto.getPassword())){
            return Result.fail(ResultCodeEnum.PASSWORD_ERROR);
        }

        if (user.getStatus() == 0) {
            return Result.fail(ResultCodeEnum.USER_DISABLE);
        }

        // =======================================================
        // 🔥🔥🔥 核心修改：从三张表里查角色 🔥🔥🔥
        // =======================================================

        // 3.1 先去关联表查 role_id
        LambdaQueryWrapper<SysUserRole> urWrapper = new LambdaQueryWrapper<>();
        urWrapper.eq(SysUserRole::getUserId, user.getId());
        List<SysUserRole> userRoles = sysUserRoleService.list(urWrapper);

        // 3.2 如果没配置角色，默认给 user
        String finalRole = "user";

        // 3.3 如果有角色，去角色表查 role_code
        if (!userRoles.isEmpty()) {
            // 这里为了简单，假设一个用户只有一个角色。如果有多个，你可以取第一个。
            Long roleId = userRoles.get(0).getRoleId();
            SysRole sysRole = sysRoleService.getById(roleId);

            if (sysRole != null) {
                // 数据库存的是 'ROLE_ADMIN'，但前端判断逻辑是 'admin'
                // 所以我们这里做一个转换
                if ("ROLE_ADMIN".equals(sysRole.getRoleCode())) {
                    finalRole = "admin";
                } else {
                    finalRole = "user";
                }
            }
        }

        // 4. 把查到的角色塞进 user 对象传给前端
        user.setRole(finalRole);
        user.setPassword(null); // 隐藏密码

        return Result.success(user);
    }
    @Data
    public static class LoginDto {
        private String username;
        private String password;
    }
}