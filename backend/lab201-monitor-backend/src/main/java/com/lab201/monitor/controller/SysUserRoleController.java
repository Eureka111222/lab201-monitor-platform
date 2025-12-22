package com.lab201.monitor.controller;

import com.lab201.monitor.common.result.Result;
import com.lab201.monitor.entity.SysUserRole;
import com.lab201.monitor.service.ISysUserRoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Tag(name = "06. 用户授权管理")
@RestController
@RequestMapping("/api/v1/sys-user-role")
public class SysUserRoleController {

    @Autowired
    private ISysUserRoleService sysUserRoleService;

    @Operation(summary = "给用户分配角色")
    @PostMapping("/assign")
    public Result<Boolean> assignRole(@RequestBody SysUserRole sysUserRole) {
        return Result.success(sysUserRoleService.save(sysUserRole));
    }
    
    // 更多逻辑如：查询某用户的所有角色，通常在 Service 层封装好后由 SysUserController 调用
}