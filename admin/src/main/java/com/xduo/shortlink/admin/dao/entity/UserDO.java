package com.xduo.shortlink.admin.dao.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.xduo.shortlink.admin.common.database.BaseDO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

/**
 * 用户持久层实体
 * @TableName t_user
 */
@Data
@TableName("t_user")
@AllArgsConstructor
@RequiredArgsConstructor
public class UserDO extends BaseDO {
    /**
     * ID
     */
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码

     */
    private String password;

    /**
     * 真实姓名

     */
    private String realName;

    /**
     * 手机号

     */
    private String phone;

    /**
     * 邮箱

     */
    private String mail;

    /**
     * 注销时间戳
     */
    private Long deletionTime;


}