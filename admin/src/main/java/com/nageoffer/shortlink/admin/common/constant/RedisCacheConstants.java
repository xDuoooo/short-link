package com.nageoffer.shortlink.admin.common.constant;

/**
 * 短链接后管 Redis 缓存常量类
 */
public class RedisCacheConstants {
    /**
     * 注册分布式锁
     */
    public static final String LOCK_USER_REGISTER_KEY = "short-link:lock:user-register:";

    /**
     * 分组创建分布式锁
     */
    public static final String LOCK_GROUP_CREATE_KEY = "short-link:lock:group-create:%s";
    /**
     * 用户登录token
     */
    public static final String USER_LOGIN_KEY = "short-link:login:";
}
