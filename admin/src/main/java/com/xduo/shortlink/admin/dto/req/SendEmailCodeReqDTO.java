package com.xduo.shortlink.admin.dto.req;

import lombok.Data;

/**
 * 发送邮箱验证码请求参数
 */
@Data
public class SendEmailCodeReqDTO {
    
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 邮箱地址
     */
    private String email;
}
