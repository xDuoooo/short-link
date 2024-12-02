package com.xduo.shortlink.project.dao.entity;


import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;

import com.xduo.shortlink.project.common.database.BaseDO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 
 * @TableName t_link_locale_stats
 */
@TableName(value ="t_link_locale_stats")
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class LinkLocaleStatsDO extends BaseDO implements Serializable {
    /**
     * ID
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 完整短链接
     */
    @TableField(value = "full_short_url")
    private String fullShortUrl;

    /**
     * 分组标识
     */
    @TableField(value = "gid")
    private String gid;

    /**
     * 日期
     */
    @TableField(value = "date")
    private Date date;

    /**
     * 访问量
     */
    @TableField(value = "cnt")
    private Integer cnt;

    /**
     * 省份名称
     */
    @TableField(value = "province")
    private String province;

    /**
     * 市名称
     */
    @TableField(value = "city")
    private String city;

    /**
     * 城市编码
     */
    @TableField(value = "adcode")
    private String adcode;

    /**
     * 国家标识
     */
    @TableField(value = "country")
    private String country;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}