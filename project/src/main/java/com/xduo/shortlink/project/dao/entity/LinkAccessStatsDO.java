package com.xduo.shortlink.project.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

import com.xduo.shortlink.project.common.database.BaseDO;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * 
 * @TableName t_link_access_stats
 */
/**
 * 基础访问实体
 */
@TableName(value ="t_link_access_stats")
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class LinkAccessStatsDO extends BaseDO implements Serializable {
    /**
     * ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 分组标识
     */
    @TableField(value = "gid")
    private String gid;

    /**
     * 完整短链接
     */
    @TableField(value = "full_short_url")
    private String fullShortUrl;

    /**
     * 日期
     */
    @TableField(value = "date")
    private Date date;

    /**
     * 访问量
     */
    @TableField(value = "pv")
    private Integer pv;

    /**
     * 独立访问数
     */
    @TableField(value = "uv")
    private Integer uv;

    /**
     * 独立IP数
     */
    @TableField(value = "uip")
    private Integer uip;

    /**
     * 小时
     */
    @TableField(value = "hour")
    private Integer hour;

    /**
     * 星期
     */
    @TableField(value = "weekday")
    private Integer weekday;

    /**
     * 删除标识：0 未删除 1 已删除
     */
    @TableField(value = "del_flag")
    private Integer delFlag;

    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}