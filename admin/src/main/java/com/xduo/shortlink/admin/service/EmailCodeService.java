package com.xduo.shortlink.admin.service;

/**
 * 邮箱验证码服务接口
 */
public interface EmailCodeService {
    
    /**
     * 发送邮箱验证码
     * @param email 邮箱地址
     * @param username 用户名
     * @return 验证码
     */
    String sendEmailCode(String email, String username);
    
    /**
     * 验证邮箱验证码
     * @param email 邮箱地址
     * @param code 验证码
     * @return 验证结果
     */
    boolean verifyEmailCode(String email, String code);
    
    /**
     * 删除邮箱验证码
     * @param email 邮箱地址
     */
    void deleteEmailCode(String email);
}
