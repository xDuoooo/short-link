package com.xduo.shortlink.project.dto.resp;

import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * 短链接分页返回DTO
 */
@Data
public class ShortLinkPageRespDTO {


    /**
     * 域名
     */
    @TableField(value = "domain")
    private String domain;

    /**
     * 短链接
     */
    @TableField(value = "short_uri")
    private String shortUri;

    /**
     * 完整短链接
     */
    @TableField(value = "full_short_url")
    private String fullShortUrl;

    /**
     * 原始链接
     */
    @TableField(value = "origin_url")
    private String originUrl;

    /**
     * 分组标识
     */
    @TableField(value = "gid")
    private String gid;

    /**
     * 启用标识 0：未启用 1：已启用
     */
    @TableField(value = "enable_status")
    private Integer enableStatus;

    /**
     * 创建类型 0：控制台 1：接口
     */
    @TableField(value = "created_type")
    private Integer createdType;

    /**
     * 有效期类型 0：永久有效 1：用户自定义
     */
    @TableField(value = "valid_date_type")
    private Integer validDateType;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    @TableField(value = "create_time")
    private Date createTime;
    /**
     * 有效期
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    @TableField(value = "valid_date")
    private Date validDate;

    /**
     * 描述
     */
    @TableField(value = "`describe`")
    private String describe;

    /**
     * 网站标识图片
     */
    @TableField(value = "favicon")
    private String favicon;


}
