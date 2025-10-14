package com.xduo.shortlink.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

/**
 * CORS跨域配置
 */
@Configuration
public class CorsConfig {

    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        
        // 允许所有来源
        corsConfiguration.addAllowedOriginPattern("*");
        
        // 允许所有请求头
        corsConfiguration.addAllowedHeader("*");
        
        // 允许所有请求方法
        corsConfiguration.addAllowedMethod("*");
        
        // 允许携带凭证
        corsConfiguration.setAllowCredentials(true);
        
        // 预检请求的缓存时间（秒）
        corsConfiguration.setMaxAge(3600L);
        
        // 允许的响应头
        corsConfiguration.addExposedHeader("Authorization");
        corsConfiguration.addExposedHeader("Content-Type");
        corsConfiguration.addExposedHeader("X-Requested-With");
        corsConfiguration.addExposedHeader("Accept");
        corsConfiguration.addExposedHeader("Origin");
        corsConfiguration.addExposedHeader("Access-Control-Request-Method");
        corsConfiguration.addExposedHeader("Access-Control-Request-Headers");
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfiguration);
        
        return new CorsWebFilter(source);
    }
}
