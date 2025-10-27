package com.xduo.shortlink.admin.dto.req;

import lombok.Data;

/**
 * 发送找回密码邮箱验证码请求参数
 */
@Data
public class SendForgotPasswordEmailReqDTO {
    
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 邮箱
     */
    private String email;
}
