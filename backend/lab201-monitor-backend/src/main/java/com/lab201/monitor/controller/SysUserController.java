package com.lab201.monitor.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lab201.monitor.common.login.LoginUser;
import com.lab201.monitor.common.login.LoginUserHolder;
import com.lab201.monitor.common.result.Result;
import com.lab201.monitor.common.result.ResultCodeEnum;
import com.lab201.monitor.common.utils.JwtUtil;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    // ⚠️ 注意：返回值泛型改成了 Map，因为我们要返回 token 和 userInfo 两样东西
    public Result<Map<String, Object>> login(@RequestBody LoginDto loginDto) {
        // 1. 查用户 (带上密码字段以便校验)
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUsername, loginDto.getUsername());
        // 必须select密码，否则默认select=false查出来是null
        wrapper.select(SysUser::getId, SysUser::getUsername, SysUser::getPassword, SysUser::getStatus, SysUser::getRealName, SysUser::getAvatar);
        SysUser user = sysUserService.getOne(wrapper);

        // 2. 校验账号密码
        if (user == null) {
            return Result.fail(ResultCodeEnum.USER_NOT_EXIST); // 确保你有这个枚举，或者直接写 Result.fail("用户不存在")
        }
        // 如果数据库是明文密码：
        if (!user.getPassword().equals(loginDto.getPassword())) {
            return Result.fail(ResultCodeEnum.PASSWORD_ERROR);
        }
        if (user.getStatus() == 0) {
            return Result.fail(ResultCodeEnum.USER_DISABLE);
        }

        // 3. 获取角色 (逻辑保持不变)
        String role = "user"; // 默认
        LambdaQueryWrapper<SysUserRole> urWrapper = new LambdaQueryWrapper<>();
        urWrapper.eq(SysUserRole::getUserId, user.getId());
        List<SysUserRole> userRoles = sysUserRoleService.list(urWrapper);

        if (!userRoles.isEmpty()) {
            SysRole sysRole = sysRoleService.getById(userRoles.get(0).getRoleId());
            if (sysRole != null && "ROLE_ADMIN".equals(sysRole.getRoleCode())) {
                role = "admin";
            }
        }
        // 把查到的角色塞回 user 对象（给前端展示用）
        user.setRole(role);
        user.setPassword(null); // 擦除密码

        // 4. 🔥 生成 JWT Token (核心修改)
        // 将 userId, username, role 写入 Token
        String token = JwtUtil.createToken(user.getId(), user.getUsername(), role);

        // 5. 封装返回值
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("userInfo", user);

        return Result.success(result);
    }

    @Operation(summary = "获取当前登录用户信息(测试JWT)")
    @GetMapping("/me")
    public Result<LoginUser> getCurrentUser() {
        // 🔥 直接从 ThreadLocal 拿，不需要查数据库，也不用解析 Token（拦截器已经做完了）
        LoginUser currentUser = LoginUserHolder.getLoginUser();
        return Result.success(currentUser);
    }
    @Data
    public static class LoginDto {
        private String username;
        private String password;
    }
}