package com.lab201.monitor.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lab201.monitor.common.result.Result;
import com.lab201.monitor.entity.ServerNode;
import com.lab201.monitor.service.IServerNodeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "02. 服务器资产管理")
@RestController
@RequestMapping("/api/v1/server-node")
public class ServerNodeController {

    @Autowired
    private IServerNodeService serverNodeService;

    @Operation(summary = "获取所有服务器列表 (大屏用)")
    @GetMapping("/list")
    public Result<List<ServerNode>> listAll() {
        return Result.success(serverNodeService.list());
    }

    @Operation(summary = "分页查询服务器")
    @GetMapping("/page")
    public Result<Page<ServerNode>> pageList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(serverNodeService.page(new Page<>(page, size)));
    }

    @Operation(summary = "新增/注册服务器")
    @PostMapping
    public Result<Boolean> add(@RequestBody ServerNode serverNode) {
        return Result.success(serverNodeService.save(serverNode));
    }

    @Operation(summary = "修改服务器信息")
    @PutMapping
    public Result<Boolean> update(@RequestBody ServerNode serverNode) {
        return Result.success(serverNodeService.updateById(serverNode));
    }

    @Operation(summary = "删除服务器")
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.success(serverNodeService.removeById(id));
    }
}