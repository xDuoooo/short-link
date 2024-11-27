package com.nageoffer.shortlink.project.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 
 * @TableName t_link_browser_stats
 */
@TableName(value ="t_link_browser_stats")
@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class LinkBrowserStats implements Serializable {
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
     * 浏览器
     */
    @TableField(value = "browser")
    private String browser;

    /**
     * 创建时间
     */
    @TableField(value = "create_time")
    private Date createTime;

    /**
     * 修改时间
     */
    @TableField(value = "update_time")
    private Date updateTime;

    /**
     * 删除标识 0：未删除 1：已删除
     */
    @TableField(value = "del_flag")
    private Integer delFlag;

    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}