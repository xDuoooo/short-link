package com.xduo.shortlink.admin.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * MinIO服务接口
 */
public interface MinIOService {

    /**
     * 上传文件
     * @param file 文件
     * @param objectName 对象名称
     * @return 文件访问URL
     */
    String uploadFile(MultipartFile file, String objectName);

    /**
     * 删除文件
     * @param objectName 对象名称
     */
    void deleteFile(String objectName);

    /**
     * 检查存储桶是否存在，不存在则创建
     */
    void ensureBucketExists();

    /**
     * 设置存储桶为公开读取
     */
    void setBucketPublicReadPolicy();
}
