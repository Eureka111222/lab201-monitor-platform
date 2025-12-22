package com.lab201.monitor.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lab201.monitor.common.result.Result;
import com.lab201.monitor.entity.SysRole;
import com.lab201.monitor.service.ISysRoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Tag(name = "05. 角色管理")
@RestController
@RequestMapping("/api/v1/sys-role")
public class SysRoleController {

    @Autowired
    private ISysRoleService sysRoleService;

    @Operation(summary = "获取角色列表")
    @GetMapping("/list")
    public Result<Page<SysRole>> list(@RequestParam(defaultValue = "1") Integer page,
                                      @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(sysRoleService.page(new Page<>(page, size)));
    }

    @Operation(summary = "新增角色")
    @PostMapping
    public Result<Boolean> add(@RequestBody SysRole sysRole) {
        return Result.success(sysRoleService.save(sysRole));
    }

    @Operation(summary = "修改角色")
    @PutMapping
    public Result<Boolean> update(@RequestBody SysRole sysRole) {
        return Result.success(sysRoleService.updateById(sysRole));
    }

    @Operation(summary = "删除角色")
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.success(sysRoleService.removeById(id));
    }
}