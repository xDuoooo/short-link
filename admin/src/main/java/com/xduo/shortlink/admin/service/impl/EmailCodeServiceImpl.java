package com.xduo.shortlink.admin.service.impl;

import com.xduo.shortlink.admin.service.EmailCodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * 邮箱验证码服务实现
 */
@Service
@RequiredArgsConstructor
public class EmailCodeServiceImpl implements EmailCodeService {
    
    private final StringRedisTemplate stringRedisTemplate;
    
    private static final String EMAIL_CODE_KEY_PREFIX = "short-link:email-code:";
    private static final int CODE_LENGTH = 4;
    private static final int CODE_EXPIRE_MINUTES = 5; // 验证码5分钟过期
    
    @Override
    public String sendEmailCode(String email, String username) {
        // 生成4位数字验证码
        String code = generateCode();
        
        // 存储到Redis，设置5分钟过期
        String key = EMAIL_CODE_KEY_PREFIX + email;
        stringRedisTemplate.opsForValue().set(key, code, CODE_EXPIRE_MINUTES, TimeUnit.MINUTES);
        
        return code;
    }
    
    @Override
    public boolean verifyEmailCode(String email, String code) {
        String key = EMAIL_CODE_KEY_PREFIX + email;
        String storedCode = stringRedisTemplate.opsForValue().get(key);
        
        if (storedCode == null) {
            return false; // 验证码不存在或已过期
        }
        
        return storedCode.equals(code);
    }
    
    @Override
    public void deleteEmailCode(String email) {
        String key = EMAIL_CODE_KEY_PREFIX + email;
        stringRedisTemplate.delete(key);
    }
    
    /**
     * 生成4位数字验证码
     */
    private String generateCode() {
        Random random = new Random();
        int code = random.nextInt(9000) + 1000; // 生成1000-9999之间的数字
        return String.valueOf(code);
    }
}
