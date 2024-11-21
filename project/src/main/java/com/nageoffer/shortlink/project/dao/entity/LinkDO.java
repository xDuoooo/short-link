package com.nageoffer.shortlink.project.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.nageoffer.shortlink.project.common.database.BaseDO;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 
 * @TableName t_link
 */
@EqualsAndHashCode(callSuper = true)
@TableName(value ="t_link")
@Data
@RequiredArgsConstructor
@AllArgsConstructor
@Builder
public class LinkDO extends BaseDO implements Serializable {
    /**
     * ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

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
     * 点击量
     */
    @TableField(value = "click_num")
    private Integer clickNum;

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

    /**
     * 有效期
     */
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

    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}