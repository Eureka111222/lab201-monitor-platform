package com.lab201.monitor.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lab201.monitor.common.result.Result;
import com.lab201.monitor.entity.ServerNode;
import com.lab201.monitor.service.IServerNodeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
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

    //@Operation(summary = "分页查询服务器")
    //@GetMapping("/page")
    //public Result<Page<ServerNode>> pageList(
    //        @RequestParam(defaultValue = "1") Integer page,
    //        @RequestParam(defaultValue = "10") Integer size) {
    //    return Result.success(serverNodeService.page(new Page<>(page, size)));
    //}

    @Operation(summary = "分页查询服务器")
    @GetMapping("/page")
    public Result<Page<ServerNode>> page(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword // 👈 1. 接收前端传来的 keyword
    ) {
        // 1. 创建分页对象
        Page<ServerNode> pageParam = new Page<>(page, size);

        // 2. 构建查询条件
        LambdaQueryWrapper<ServerNode> wrapper = new LambdaQueryWrapper<>();

        // 3. 核心搜索逻辑：如果 keyword 不为空，则拼接查询 SQL
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w
                    .like(ServerNode::getServerName, keyword) // 主机名包含 keyword
                    .or()
                    .like(ServerNode::getServerIp, keyword)   // 或者 IP 包含 keyword
                    .or()
                    .like(ServerNode::getGpuModel, keyword)   // 或者 显卡型号 包含 keyword
            );
        }

        // 4. 按创建时间倒序（新加的在前面）
        wrapper.orderByDesc(ServerNode::getCreateTime);

        // 5. 执行查询
        return Result.success(serverNodeService.page(pageParam, wrapper));
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