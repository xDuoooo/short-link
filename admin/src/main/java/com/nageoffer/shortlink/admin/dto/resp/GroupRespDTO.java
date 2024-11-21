package com.nageoffer.shortlink.admin.dto.resp;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

@Data
public class GroupRespDTO {

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

}
