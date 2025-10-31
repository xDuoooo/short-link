package com.xduo.shortlink.project.mq.producer;

import com.alibaba.fastjson2.JSON;
import com.xduo.shortlink.project.dto.req.ShortLinkStatsRecordDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

/**
 * 短链接统计Kafka生产者
 * 替代原来的Redis Stream生产者
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ShortLinkStatsKafkaProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;

    @Value("${spring.kafka.topic.short-link-stats:short-link-stats}")
    private String topic;

    /**
     * 发送短链接统计记录到Kafka
     */
    public void send(ShortLinkStatsRecordDTO statsRecord) {
        try {
            String message = JSON.toJSONString(statsRecord);
            String key = statsRecord.getFullShortUrl(); // 使用完整短链接作为key，保证同一短链接的消息有序
            
            CompletableFuture<SendResult<String, String>> future = kafkaTemplate.send(topic, key, message);
            
            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("短链接统计消息发送成功: topic={}, key={}, offset={}", 
                        topic, key, result.getRecordMetadata().offset());
                } else {
                    log.error("短链接统计消息发送失败: topic={}, key={}, error={}", 
                        topic, key, ex.getMessage(), ex);
                }
            });
            
        } catch (Exception e) {
            log.error("发送短链接统计消息异常: {}", e.getMessage(), e);
        }
    }

    /**
     * 同步发送消息（用于重要消息）
     */
    public void sendSync(ShortLinkStatsRecordDTO statsRecord) {
        try {
            String message = JSON.toJSONString(statsRecord);
            String key = statsRecord.getFullShortUrl();
            
            SendResult<String, String> result = kafkaTemplate.send(topic, key, message).get();
            log.info("短链接统计消息同步发送成功: topic={}, key={}, offset={}", 
                topic, key, result.getRecordMetadata().offset());
                
        } catch (Exception e) {
            log.error("同步发送短链接统计消息异常: {}", e.getMessage(), e);
        }
    }
}
