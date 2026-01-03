package com.lab201.monitor.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lab201.monitor.common.result.Result;
import com.lab201.monitor.entity.SysDocument;
import com.lab201.monitor.entity.dto.DocumentQueryDto;
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

    @Operation(summary = "上传文档")
    @PostMapping("/upload")
    public Result<Boolean> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam("title") String title,
            @RequestParam("viewScope") Integer viewScope,
            @RequestParam("isRag") Integer isRag
    ) {
        // 调用 Service 处理上传逻辑
        sysDocumentService.upload(file, title, viewScope, isRag);
        return Result.success(true);
    }

    @Operation(summary = "分页查询文档")
    @GetMapping("/page")
    // 使用 DTO 接收前端传来的 page, size, keyword, viewScope
    public Result<Page<SysDocument>> page(DocumentQueryDto queryDto) {
        return Result.success(sysDocumentService.pageQuery(queryDto));
    }

    @Operation(summary = "获取下载链接")
    @GetMapping("/download/{id}")
    public Result<String> getDownloadUrl(@PathVariable Long id) {
        // 返回 MinIO 的 http 链接，前端直接 window.open 打开
        String url = sysDocumentService.getDownloadUrl(id);
        return Result.success(url);
    }

    @Operation(summary = "删除文档")
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        // Service 里记得补上：先删 MinIO 文件，再删数据库记录
        return Result.success(sysDocumentService.removeById(id));
    }
}