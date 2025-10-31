package com.xduo.shortlink.admin.service.impl;

import com.xduo.shortlink.admin.config.MinIOConfig;
import com.xduo.shortlink.admin.service.MinIOService;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.SetBucketPolicyArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

/**
 * MinIO服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MinIOServiceImpl implements MinIOService {

    private final MinioClient minioClient;
    private final MinIOConfig minIOConfig;

    @Override
    public String uploadFile(MultipartFile file, String objectName) {
        try {
            // 确保存储桶存在
            ensureBucketExists();
            
            // 获取文件输入流
            InputStream inputStream = file.getInputStream();
            
            // 上传文件
            minioClient.putObject(
                PutObjectArgs.builder()
                    .bucket(minIOConfig.getBucketName())
                    .object(objectName)
                    .stream(inputStream, file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build()
            );
            
            // 返回文件访问URL
            return String.format("%s/%s/%s", minIOConfig.getEndpoint(), minIOConfig.getBucketName(), objectName);
            
        } catch (Exception e) {
            log.error("文件上传失败: {}", e.getMessage(), e);
            throw new RuntimeException("文件上传失败", e);
        }
    }

    @Override
    public void deleteFile(String objectName) {
        try {
            minioClient.removeObject(
                RemoveObjectArgs.builder()
                    .bucket(minIOConfig.getBucketName())
                    .object(objectName)
                    .build()
            );
        } catch (Exception e) {
            log.error("文件删除失败: {}", e.getMessage(), e);
            throw new RuntimeException("文件删除失败", e);
        }
    }

    @Override
    public void ensureBucketExists() {
        try {
            boolean bucketExists = minioClient.bucketExists(
                BucketExistsArgs.builder()
                    .bucket(minIOConfig.getBucketName())
                    .build()
            );
            
            if (!bucketExists) {
                minioClient.makeBucket(
                    MakeBucketArgs.builder()
                        .bucket(minIOConfig.getBucketName())
                        .build()
                );
                
                // 设置存储桶策略为公开读取
                setBucketPublicReadPolicy();
                
                log.info("创建存储桶: {}", minIOConfig.getBucketName());
            }
        } catch (Exception e) {
            log.error("检查/创建存储桶失败: {}", e.getMessage(), e);
            throw new RuntimeException("存储桶操作失败", e);
        }
    }

    @Override
    public void setBucketPublicReadPolicy() {
        try {
            String bucketName = minIOConfig.getBucketName();
            String policy = String.format("""
                {
                    "Version": "2012-10-17",
                    "Statement": [
                        {
                            "Effect": "Allow",
                            "Principal": "*",
                            "Action": "s3:GetObject",
                            "Resource": "arn:aws:s3:::%s/avatars/*"
                        }
                    ]
                }
                """, bucketName);
            
            minioClient.setBucketPolicy(
                SetBucketPolicyArgs.builder()
                    .bucket(bucketName)
                    .config(policy)
                    .build()
            );
            
            log.info("设置存储桶 {} 的 avatars/ 目录为公开读取", bucketName);
        } catch (Exception e) {
            log.error("设置存储桶策略失败: {}", e.getMessage(), e);
            throw new RuntimeException("设置存储桶策略失败", e);
        }
    }
}
