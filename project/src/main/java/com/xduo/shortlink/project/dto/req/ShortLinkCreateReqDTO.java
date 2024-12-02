package com.xduo.shortlink.project.dto.req;

import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 短链接创建请求对象
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ShortLinkCreateReqDTO {

    /**
     * 域名
     */
    @TableField(value = "domain")
    private String domain;
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
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @TableField(value = "valid_date")
    private Date validDate;

    /**
     * 描述
     */
    @TableField(value = "describe")
    private String describe;

}
