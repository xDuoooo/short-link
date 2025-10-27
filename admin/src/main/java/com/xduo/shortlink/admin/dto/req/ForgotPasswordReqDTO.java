package com.xduo.shortlink.admin.dto.req;

import lombok.Data;

/**
 * 找回密码请求参数
 */
@Data
public class ForgotPasswordReqDTO {
    
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 邮箱
     */
    private String email;
    
    /**
     * 新密码
     */
    private String newPassword;
    
    /**
     * 邮箱验证码
     */
    private String emailCode;
}
