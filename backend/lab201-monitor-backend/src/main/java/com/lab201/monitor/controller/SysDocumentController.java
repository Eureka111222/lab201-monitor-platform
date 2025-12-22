package com.lab201.monitor.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lab201.monitor.common.result.Result;
import com.lab201.monitor.entity.SysDocument;
import com.lab201.monitor.service.ISysDocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "04. 知识库文档管理")
@RestController
@RequestMapping("/api/v1/sys-document")
public class SysDocumentController {

    @Autowired
    private ISysDocumentService sysDocumentService;

    @Operation(summary = "上传并解析文档 (RAG核心)")
    @PostMapping("/upload")
    public Result<String> upload(@RequestParam("file") MultipartFile file) {
        // 1. 上传 MinIO
        // 2. 插入数据库
        // 3. 异步调用 AI 解析
        return Result.success("上传成功，后台解析中...");
    }

    @Operation(summary = "分页查询文档")
    @GetMapping("/page")
    public Result<Page<SysDocument>> page(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(sysDocumentService.page(new Page<>(page, size)));
    }

    @Operation(summary = "删除文档 (同时删除向量库)")
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.success(sysDocumentService.removeById(id));
    }
}