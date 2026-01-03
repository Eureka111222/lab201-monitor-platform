package com.lab201.monitor.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lab201.monitor.entity.SysDocument;
import com.lab201.monitor.entity.dto.DocumentQueryDto;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/**
 * <p>
 * 知识库文档 服务类
 * </p>
 *
 * @author lab201
 * @since 2025-12-22
 */
public interface ISysDocumentService extends IService<SysDocument> {

    @Transactional(rollbackFor = Exception.class)
    void upload(MultipartFile file, String title, Integer viewScope, Integer isRag);

    Page<SysDocument> pageQuery(DocumentQueryDto query);

    String getDownloadUrl(Long id);
}
