package com.xduo.shortlink.aggregation;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 短链接聚合应用
 */
@EnableDiscoveryClient
@SpringBootApplication(scanBasePackages = {
        "com.xduo.shortlink.admin",
        "com.xduo.shortlink.project",
})
@MapperScan(value = {
        "com.xduo.shortlink.project.dao.mapper",
        "com.xduo.shortlink.admin.dao.mapper"
})
public class AggregationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AggregationServiceApplication.class, args);
    }
}