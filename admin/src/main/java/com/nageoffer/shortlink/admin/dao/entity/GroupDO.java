package com.nageoffer.shortlink.admin.dao.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.nageoffer.shortlink.admin.common.database.BaseDO;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;

/**
 * 
 * @TableName t_group
 */
@EqualsAndHashCode(callSuper = true)
@TableName(value ="t_group")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GroupDO extends BaseDO implements Serializable{
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
    private Integer sortOrder;

    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;


}