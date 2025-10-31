package com.xduo.shortlink.admin.dto.req;

import lombok.Data;

/**
 * 发送注册验证码请求参数
 */
@Data
public class SendRegisterEmailCodeReqDTO {
    
    /**
     * 邮箱地址
     */
    private String email;
}
