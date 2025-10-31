package com.xduo.shortlink.admin.dto.req;

import lombok.Data;

/**
 * 用户变更邮箱请求参数
 */
@Data
public class UserChangeEmailReqDTO {
    /**
     * 用户名
     */
    private String username;
    /**
     * 新邮箱
     */
    private String newEmail;
    /**
     * 验证码（发到新邮箱）
     */
    private String emailCode;
}
