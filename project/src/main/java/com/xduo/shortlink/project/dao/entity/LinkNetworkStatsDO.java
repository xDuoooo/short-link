package com.xduo.shortlink.project.dao.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.xduo.shortlink.project.common.database.BaseDO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 
 * @TableName t_link_network_stats
 */
@TableName(value ="t_link_network_stats")
@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class LinkNetworkStatsDO extends BaseDO implements Serializable {
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
    @TableField(value = "network")
    private String network;

    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}