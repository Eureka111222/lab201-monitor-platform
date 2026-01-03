package com.lab201.monitor.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lab201.monitor.common.config.MinioConfig;
import com.lab201.monitor.common.event.RagParseEvent;
import com.lab201.monitor.common.login.LoginUser;
import com.lab201.monitor.common.login.LoginUserHolder;
import com.lab201.monitor.common.utils.MinioUtil;
import com.lab201.monitor.entity.SysDocument;
import com.lab201.monitor.entity.dto.DocumentQueryDto;
import com.lab201.monitor.mapper.SysDocumentMapper;
import com.lab201.monitor.service.ISysDocumentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class SysDocumentServiceImpl extends ServiceImpl<SysDocumentMapper, SysDocument> implements ISysDocumentService {

    @Autowired
    private MinioUtil minioUtil;
    @Autowired
    private MinioConfig minioConfig;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void upload(MultipartFile file, String title, Integer viewScope, Integer isRag) {
        // 1. 获取当前用户
        LoginUser user = LoginUserHolder.getLoginUser();

        // 2. 上传到 MinIO
        String objectName = minioUtil.upload(minioConfig.getBucketName(), file);

        // 3. 保存到数据库
        SysDocument doc = new SysDocument();
        doc.setTitle(title); // 前端填写的标题
        doc.setFileName(file.getOriginalFilename());

        // 获取后缀
        String originalName = file.getOriginalFilename();
        String ext = originalName != null && originalName.contains(".") ?
                originalName.substring(originalName.lastIndexOf(".") + 1) : "unknown";
        doc.setFileExt(ext);

        doc.setFileSize(file.getSize());
        doc.setBucketName(minioConfig.getBucketName());
        doc.setObjectName(objectName);
        doc.setViewScope(viewScope); // 0私有 1公开
        doc.setIsRag(isRag);

        if(isRag==1){
            doc.setParseStatus(0);
        }else{
            doc.setParseStatus(0);
        }

        // 记录上传者
        doc.setUploaderId(user.getUserId());
        doc.setUploaderName(user.getUsername());

        this.save(doc);

        if(isRag!=null&&isRag==1){
            eventPublisher.publishEvent(new RagParseEvent(this, doc.getId()));
        }
    }

    @Override
    public Page<SysDocument> pageQuery(DocumentQueryDto query) {
        LoginUser user = LoginUserHolder.getLoginUser();
        Page<SysDocument> pageParam = new Page<>(query.getPage(), query.getSize());
        LambdaQueryWrapper<SysDocument> wrapper = new LambdaQueryWrapper<>();

        // === 权限隔离核心逻辑 ===
        if ("admin".equals(user.getRole())) {
            // Admin: 看所有 (不需要过滤 viewScope)
        } else {
            // User: 看 (公开) OR (自己的)
            wrapper.and(w -> w
                    .eq(SysDocument::getViewScope, 1)
                    .or()
                    .eq(SysDocument::getUploaderId, user.getUserId())
            );
        }

        // === 搜索条件 ===
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.and(w -> w
                    .like(SysDocument::getTitle, query.getKeyword())
                    .or()
                    .like(SysDocument::getFileName, query.getKeyword())
            );
        }

        // === 前端筛选 (比如只看私有) ===
        if (query.getViewScope() != null) {
            wrapper.eq(SysDocument::getViewScope, query.getViewScope());
        }

        wrapper.orderByDesc(SysDocument::getCreateTime);
        return this.page(pageParam, wrapper);
    }

    @Override
    public String getDownloadUrl(Long id) {
        SysDocument doc = this.getById(id);
        if (doc == null) throw new RuntimeException("文件不存在");

        // 简单鉴权：如果是私有且不是本人也不是admin，拦截
        LoginUser user = LoginUserHolder.getLoginUser();
        if (doc.getViewScope() == 0
                && !user.getUserId().equals(doc.getUploaderId())
                && !"admin".equals(user.getRole())) {
            throw new RuntimeException("无权下载此文件");
        }

        // 生成临时链接
        return minioUtil.getPresignedUrl(doc.getBucketName(), doc.getObjectName());
    }
}