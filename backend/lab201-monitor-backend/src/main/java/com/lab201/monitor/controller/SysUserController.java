package com.lab201.monitor.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lab201.monitor.common.result.Result;
import com.lab201.monitor.entity.SysUser;
import com.lab201.monitor.service.ISysUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Tag(name = "03. 用户管理")
@RestController
@RequestMapping("/api/v1/sys-user")
public class SysUserController {

    @Autowired
    private ISysUserService sysUserService;

    @Operation(summary = "分页查询用户")
    @GetMapping("/page")
    public Result<Page<SysUser>> page(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(sysUserService.page(new Page<>(page, size)));
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
}