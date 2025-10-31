package com.xduo.shortlink.admin.service.impl;

import com.xduo.shortlink.admin.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * 邮箱发送服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {
    
    private final JavaMailSender mailSender;
    
    @Value("${email.from:noreply@shortlink.com}")
    private String fromEmail;
    
    @Value("${email.from-name:短链接系统}")
    private String fromName;
    
    @Override
    public void sendEmailCode(String email, String code, String username) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(email);
            message.setSubject("【" + fromName + "】密码修改验证码");
            
            String content = String.format(
                "亲爱的 %s，\n\n" +
                "您正在修改密码，验证码为：%s\n\n" +
                "验证码有效期为5分钟，请及时使用。\n\n" +
                "如非本人操作，请忽略此邮件。\n\n" +
                "此邮件由系统自动发送，请勿回复。",
                username, code
            );
            
            message.setText(content);
            
            mailSender.send(message);
            log.info("验证码邮件发送成功，邮箱：{}，验证码：{}", email, code);
            
        } catch (Exception e) {
            log.error("验证码邮件发送失败，邮箱：{}，错误信息：{}", email, e.getMessage(), e);
            throw new RuntimeException("邮件发送失败：" + e.getMessage());
        }
    }

    @Override
    public void sendForgotPasswordEmailCode(String email, String code, String username) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(email);
            message.setSubject("【" + fromName + "】找回密码验证码");
            
            String content = String.format(
                "亲爱的 %s，\n\n" +
                "您正在找回密码，验证码为：%s\n\n" +
                "验证码有效期为5分钟，请及时使用。\n\n" +
                "如非本人操作，请忽略此邮件。\n\n" +
                "此邮件由系统自动发送，请勿回复。",
                username, code
            );
            
            message.setText(content);
            
            mailSender.send(message);
            log.info("找回密码验证码邮件发送成功，邮箱：{}，验证码：{}", email, code);
            
        } catch (Exception e) {
            log.error("找回密码验证码邮件发送失败，邮箱：{}，错误信息：{}", email, e.getMessage(), e);
            throw new RuntimeException("邮件发送失败：" + e.getMessage());
        }
    }

    @Override
    public void sendRegisterEmailCode(String email, String code) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(email);
            message.setSubject("【" + fromName + "】注册验证码");
            
            String content = String.format(
                "亲爱的用户，\n\n" +
                "欢迎注册%s，您的验证码为：%s\n\n" +
                "验证码有效期为5分钟，请及时使用。\n\n" +
                "如非本人操作，请忽略此邮件。\n\n" +
                "此邮件由系统自动发送，请勿回复。",
                fromName, code
            );
            
            message.setText(content);
            
            mailSender.send(message);
            log.info("注册验证码邮件发送成功，邮箱：{}，验证码：{}", email, code);
            
        } catch (Exception e) {
            log.error("注册验证码邮件发送失败，邮箱：{}，错误信息：{}", email, e.getMessage(), e);
            throw new RuntimeException("邮件发送失败：" + e.getMessage());
        }
    }

    @Override
    public void sendChangeEmailCode(String email, String code, String username) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(email);
            message.setSubject("【" + fromName + "】变更邮箱验证码");
            String content = String.format(
                "亲爱的 %s，\n\n" +
                "您正在变更账号绑定的邮箱，新邮箱验证码为：%s\n\n" +
                "验证码有效期为5分钟，请及时使用。\n\n" +
                "如非本人操作，请忽略此邮件。\n\n" +
                "此邮件由系统自动发送，请勿回复。",
                username, code
            );
            message.setText(content);
            mailSender.send(message);
            log.info("变更邮箱验证码邮件发送成功，邮箱：{}，验证码：{}", email, code);
        } catch (Exception e) {
            log.error("变更邮箱验证码邮件发送失败，邮箱：{}，错误信息：{}", email, e.getMessage(), e);
            throw new RuntimeException("邮件发送失败：" + e.getMessage());
        }
    }
}
