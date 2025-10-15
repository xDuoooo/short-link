package com.xduo.shortlink.project.common.constant;

/**
 * Redis Key 常量类
 */
public class RedisKeyConstant {
    /**
     * 短链接完整信息Hash前缀Key
     */
    public static final String GOTO_SHORT_LINK_HASH_KEY = "short-link:hash:%s";

    /**
     * 短链接跳转锁前缀Key
     */
    public static final String LOCK_GOTO_SHORT_LINK_KEY = "short-link:lock_goto:%s";    /**

     * 短链接空值跳转锁前缀Key
     */
    public static final String GOTO_IS_NULL_SHORT_LINK_KEY = "short-link:is-null_lock_goto:%s";
    /**
     * 短链接统计uv前缀key
     */
    public static final String STATS_UV_KEY_PREFIX = "short-link:stats:uv:%s";    /**
     * 短链接统计uip前缀key
     */
    public static final String STATS_UIP_KEY_PREFIX = "short-link:stats:uip:";
    /**
     * 短链接修改分组 ID 锁前缀 Key
     */
    public static final String LOCK_GID_UPDATE_KEY = "short-link:lock_update-gid:%s";

    /**
     * 短链接延迟队列消费统计 Key
     */
    public static final String DELAY_QUEUE_STATS_KEY = "short-link:delay-queue:stats";
    /**
     * 短链接监控消息保存队列 Topic 缓存标识
     */
    public static final String SHORT_LINK_STATS_STREAM_TOPIC_KEY = "short-link:stats-stream";

    /**
     * 短链接监控消息保存队列 Group 缓存标识
     */
    public static final String SHORT_LINK_STATS_STREAM_GROUP_KEY = "short-link:stats-stream:only-group";
}
