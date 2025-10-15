package com.xduo.shortlink.admin.dto.resp;

import lombok.Data;

/**
 * 用户返回参数实体
 */
@Data
public class UserActualRespDTO {
    /**
     * ID
     */
    private Long id;

    /**
     * 用户名
     */
    private String username;

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
     * 头像URL
     */
    private String avatar;

}
