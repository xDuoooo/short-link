package com.xduo.shortlink.project.dao.entity;


import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

import com.xduo.shortlink.project.common.database.BaseDO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 短链接监控操作系统访问状态
 * @TableName t_link_os_stats
 */
@TableName(value ="t_link_os_stats")
@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class LinkOsStatsDO extends BaseDO implements Serializable {
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
     * 操作系统
     */
    @TableField(value = "os")
    private String os;

    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}