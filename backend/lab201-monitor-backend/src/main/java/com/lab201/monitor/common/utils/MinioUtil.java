package com.lab201.monitor.common.utils;

import io.minio.*;
import io.minio.errors.*;
import io.minio.http.Method;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class MinioUtil {

    @Autowired
    private MinioClient minioClient;

    /**
     * 上传文件
     * @return 返回生成的唯一对象名 (UUID.pdf)
     */
    public String upload(String bucketName, MultipartFile file) {

        try {
            if (!minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build())) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
                // 可选：设置 Bucket 公共读权限（根据业务需求）
                //minioClient.setBucketPolicy(
                //        SetBucketPolicyArgs.builder()
                //                .bucket(bucketName)
                //                .config("{\"Version\":\"2026-01-01\",\"Statement\":[{\"Effect\":\"Allow\",\"Principal\":\"*\",\"Action\":\"s3:GetObject\",\"Resource\":\"arn:aws:s3:::" + bucketName + "/*\"}]}")
                //                .build()
                //);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) originalFilename = "unknown.file";
        
        // 生成唯一文件名，防止重名覆盖
        String suffix = originalFilename.contains(".") ? 
                        originalFilename.substring(originalFilename.lastIndexOf(".")) : "";
        String objectName = UUID.randomUUID().toString().replace("-", "") + suffix;

        try {
            InputStream inputStream = file.getInputStream();
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .stream(inputStream, file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );
            return objectName;
        } catch (Exception e) {
            log.error("MinIO上传失败", e);
            throw new RuntimeException("文件上传MinIO失败");
        }
    }

    /**
     * 获取文件预览/下载链接 (签名URL，有效期默认1小时)
     * 这样前端拿到的是一个 http://192.168.31.130:9000/... 的临时链接
     */
    public String getPresignedUrl(String bucketName, String objectName) {
        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucketName)
                            .object(objectName)
                            .expiry(1, TimeUnit.HOURS) // 1小时有效
                            .build()
            );
        } catch (Exception e) {
            log.error("获取MinIO链接失败", e);
            return null;
        }
    }

    /**
     * 删除文件
     */
    public void remove(String bucketName, String objectName) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder().bucket(bucketName).object(objectName).build()
            );
            log.info("文件已删除: bucket={}, fileName={}", bucketName, objectName);
        } catch (Exception e) {
            log.error("MinIO删除失败", e);
        }
    }

    public InputStream getObject(String bucketName, String objectName) {
        try {
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build()
            );
        } catch (Exception e) {
            log.error("MinIO下载失败", e);
            throw new RuntimeException("文件读取失败");
        }
    }
}