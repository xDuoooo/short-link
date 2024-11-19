package com.nageoffer.shortlink.admin.dao.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 
 * @TableName t_group
 */
@TableName(value ="t_group")
@Data
public class GroupDO implements Serializable {
    /**
     * ID
     */
    private Long id;

    /**
     * 分组标识
     */
    @TableField(value = "gid")
    private String gid;

    /**
     * 分组名称
     */
    @TableField(value = "name")
    private String name;

    /**
     * 创建分组用户名
     */
    @TableField(value = "username")
    private String username;

    /**
     * 分组排序
     */
    @TableField(value = "sort_order")
    private Integer sort_order;

    /**
     * 创建时间
     */
    @TableField(value = "create_time",fill = FieldFill.INSERT)
    private Date create_time;

    /**
     * 修改时间
     */
    @TableField(value = "update_time",fill = FieldFill.INSERT_UPDATE)
    private Date update_time;

    /**
     * 删除标识 0：未删除 1：已删除
     */
    @TableField(value = "del_flag",fill = FieldFill.INSERT)
    private Integer del_flag;

    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}