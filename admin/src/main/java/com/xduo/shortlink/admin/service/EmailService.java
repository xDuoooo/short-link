package com.xduo.shortlink.admin.service;

/**
 * 邮箱发送服务接口
 */
public interface EmailService {
    
    /**
     * 发送邮箱验证码
     * @param email 邮箱地址
     * @param code 验证码
     * @param username 用户名
     */
    void sendEmailCode(String email, String code, String username);

    /**
     * 发送找回密码邮箱验证码
     * @param email 邮箱地址
     * @param code 验证码
     * @param username 用户名
     */
    void sendForgotPasswordEmailCode(String email, String code, String username);
}
