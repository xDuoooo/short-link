package com.nageoffer.shortlink.project.config;


import lombok.AllArgsConstructor;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * 布隆过滤器配置
 */
@Configuration
public class RBloomFilterConfiguration {


    /**
     * 防止短链接创建查询数据库的布隆过滤器
     */
    @Bean
    public RBloomFilter<String> shortUriCreateCachePenetrationBloomFilter(RedissonClient redissonClient) {
        RBloomFilter<String> cachePenetrationBloomFilter = redissonClient.getBloomFilter("shortUriCreateCachePenetrationBloomFilter");
        cachePenetrationBloomFilter.tryInit(100000000L, 0.001  );
        return cachePenetrationBloomFilter;
    }
}