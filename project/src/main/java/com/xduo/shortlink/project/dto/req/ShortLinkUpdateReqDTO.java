package com.xduo.shortlink.project.dto.req;

import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;
@Data
public class ShortLinkUpdateReqDTO {


    /**
     * 原始链接
     */
    @TableField(value = "origin_url")
    private String originUrl;
    /**
     * 完整短链接
     */
    private String fullShortUrl;

    /**
     * 分组标识
     */
    @TableField(value = "gid")
    private String gid;
    /**
     * 原始分组标识
     */
    @TableField
    private String originGid;
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
