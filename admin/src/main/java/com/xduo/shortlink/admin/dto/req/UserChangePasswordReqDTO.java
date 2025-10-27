package com.xduo.shortlink.admin.dto.req;

import lombok.Data;

/**
 * 用户修改密码请求参数
 */
@Data
public class UserChangePasswordReqDTO {
    
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 当前密码（使用当前密码修改时必填）
     */
    private String currentPassword;
    
    /**
     * 新密码
     */
    private String newPassword;
    
    /**
     * 邮箱验证码（使用邮箱验证码修改时必填）
     */
    private String emailCode;
    
    /**
     * 修改方式：PASSWORD-使用当前密码，EMAIL-使用邮箱验证码
     */
    private String changeType;
}
