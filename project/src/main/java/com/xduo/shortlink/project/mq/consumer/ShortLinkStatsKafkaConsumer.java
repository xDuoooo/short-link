package com.xduo.shortlink.project.mq.consumer;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.Week;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.xduo.shortlink.project.dao.entity.*;
import com.xduo.shortlink.project.dao.mapper.*;
import com.xduo.shortlink.project.dto.req.ShortLinkStatsRecordDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.*;

import static com.xduo.shortlink.project.common.constant.RedisKeyConstant.LOCK_GID_UPDATE_KEY;
import static com.xduo.shortlink.project.common.constant.ShortLinkConstant.AMOP_REMOTE_URL;

/**
 * 短链接统计Kafka消费者
 * 替代原来的Redis Stream消费者
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ShortLinkStatsKafkaConsumer {

    @Value("${short-link.stats.locale.amap-key}")
    private String statsAmapKey;

    private final LinkMapper linkMapper;
    private final RedissonClient redissonClient;
    private final LinkAccessStatsMapper linkAccessStatsMapper;
    private final LinkLocaleStatsMapper linkLocaleStatsMapper;
    private final LinkOsStatsMapper linkOsStatsMapper;
    private final LinkBrowserStatsMapper linkBrowserStatsMapper;
    private final LinkAccessLogsMapper linkAccessLogsMapper;
    private final LinkDeviceStatsMapper linkDeviceStatsMapper;
    private final LinkNetworkStatsMapper linkNetworkStatsMapper;
    private final LinkStatsTodayMapper linkStatsTodayMapper;

    /**
     * 消费短链接统计消息
     */
    @KafkaListener(
        topics = "${spring.kafka.topic.short-link-stats:short-link-stats}",
        groupId = "${spring.kafka.consumer.group-id:short-link-stats-group}",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void onMessage(
            @Payload String message,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {
        
        try {
            log.info("接收到短链接统计消息: topic={}, partition={}, offset={}, message={}", 
                topic, partition, offset, message);
            
            // 解析消息
            ShortLinkStatsRecordDTO statsRecord = JSON.parseObject(message, ShortLinkStatsRecordDTO.class);
            
            // 直接使用消息中的gid，避免额外数据库查询
            String gid = statsRecord.getGid();
            if (gid == null) {
                log.warn("消息中gid为空，使用备用查询方法: fullShortUrl={}", statsRecord.getFullShortUrl());
                gid = getGidByFullShortUrl(statsRecord.getFullShortUrl());
            }
            
            // 处理统计逻辑
            actualSaveShortLinkStats(statsRecord.getFullShortUrl(), gid, statsRecord);
            
            // 手动提交offset
            acknowledgment.acknowledge();
            
            log.info("短链接统计消息处理成功: topic={}, partition={}, offset={}", 
                topic, partition, offset);
                
        } catch (Exception e) {
            log.error("处理短链接统计消息异常: topic={}, partition={}, offset={}, message={}, error={}", 
                topic, partition, offset, message, e.getMessage(), e);
            
            // 根据业务需求决定是否提交offset
            // 这里选择提交，避免重复处理同一条消息
            acknowledgment.acknowledge();
        }
    }
//
//    /**
//     * 批量消费消息（可选）
//     */
//    @KafkaListener(
//        topics = "${spring.kafka.topic.short-link-stats:short-link-stats}",
//        groupId = "${spring.kafka.consumer.group-id:short-link-stats-group}-batch",
//        containerFactory = "kafkaListenerContainerFactory"
//    )
//    public void onBatchMessage(
//            @Payload List<String> messages,
//            Acknowledgment acknowledgment) {
//
//        try {
//            log.info("接收到批量短链接统计消息: 数量={}", messages.size());
//
//            for (String message : messages) {
//                try {
//                    // 清理消息格式，确保是完整的JSON
//                    String cleanMessage = message.trim();
//                    if (!cleanMessage.startsWith("{")) {
//                        log.warn("跳过格式不正确的消息: {}", cleanMessage);
//                        continue;
//                    }
//
//                    ShortLinkStatsRecordDTO statsRecord = JSON.parseObject(cleanMessage, ShortLinkStatsRecordDTO.class);
//
//                    // 直接使用消息中的gid，避免额外数据库查询
//                    String gid = statsRecord.getGid();
//                    if (gid == null) {
//                        log.warn("批量消息中gid为空，使用备用查询方法: fullShortUrl={}", statsRecord.getFullShortUrl());
//                        gid = getGidByFullShortUrl(statsRecord.getFullShortUrl());
//                    }
//
//                    actualSaveShortLinkStats(statsRecord.getFullShortUrl(), gid, statsRecord);
//                } catch (Exception e) {
//                    log.error("处理批量消息中的单条记录异常: message={}, error={}",
//                        message, e.getMessage(), e);
//                }
//            }
//
//            acknowledgment.acknowledge();
//            log.info("批量短链接统计消息处理完成: 数量={}", messages.size());
//
//        } catch (Exception e) {
//            log.error("处理批量短链接统计消息异常: 数量={}, error={}", messages.size(), e.getMessage(), e);
//            acknowledgment.acknowledge();
//        }
//    }

    /**
     * 根据完整短链接获取分组ID
     */
    private String getGidByFullShortUrl(String fullShortUrl) {
        try {
            LambdaQueryWrapper<LinkDO> queryWrapper = Wrappers.lambdaQuery(LinkDO.class)
                    .eq(LinkDO::getFullShortUrl, fullShortUrl)
                    .eq(LinkDO::getDelFlag, 0)
                    .select(LinkDO::getGid);
            
            LinkDO linkDO = linkMapper.selectOne(queryWrapper);
            return linkDO != null ? linkDO.getGid() : null;
        } catch (Exception e) {
            log.error("查询短链接分组ID失败: fullShortUrl={}, error={}", fullShortUrl, e.getMessage(), e);
            return null;
        }
    }

    /**
     * 实际保存短链接统计数据
     */
    public void actualSaveShortLinkStats(String fullShortUrl, String gid, ShortLinkStatsRecordDTO statsRecord) {
        Date date = new Date();
        String ip = statsRecord.getRemoteAddr();
        String device = statsRecord.getDevice();
        String network = statsRecord.getNetwork();
        String uv = statsRecord.getUv();
        String os = statsRecord.getOs();
        String browser = statsRecord.getBrowser();
        Boolean uipFirstFlag = statsRecord.getUipFirstFlag() != null ? statsRecord.getUipFirstFlag() : false;
        Boolean uvFirstFlag = statsRecord.getUvFirstFlag() != null ? statsRecord.getUvFirstFlag() : false;
        fullShortUrl = Optional.ofNullable(fullShortUrl).orElse(statsRecord.getFullShortUrl());
        RReadWriteLock readWriteLock = redissonClient.getReadWriteLock(String.format(LOCK_GID_UPDATE_KEY, fullShortUrl));
        RLock rLock = readWriteLock.readLock();
        if (!rLock.tryLock()) {
            log.warn("获取锁失败，跳过统计: {}", fullShortUrl);
            return;
        }
        try {
            // gid 现在直接从消息中获取，不需要查询数据库
            int hour = DateUtil.hour(date, true);
            Week week = DateUtil.dayOfWeekEnum(date);
            int weekValue = week.getIso8601Value();
            LinkAccessStatsDO linkAccessStatsDO = LinkAccessStatsDO.builder()
                    .pv(1)
                    .uv(statsRecord.getUvFirstFlag() ? 1 : 0)
                    .uip(statsRecord.getUipFirstFlag() ? 1 : 0)
                    .hour(hour)
                    .weekday(weekValue)
                    .fullShortUrl(fullShortUrl)
                    .gid(gid)
                    .date(date)
                    .build();
            linkAccessStatsDO.setDelFlag(0);
            linkAccessStatsDO.setCreateTime(date);
            linkAccessStatsDO.setUpdateTime(date);
            linkAccessStatsMapper.shortLinkStats(linkAccessStatsDO);
            Map<String, Object> localeGetParam = new HashMap<>();
            localeGetParam.put("ip", ip);
            localeGetParam.put("key", statsAmapKey);
            String localeResultStr = HttpUtil.get(AMOP_REMOTE_URL, localeGetParam);
            JSONObject localeResultObj = JSON.parseObject(localeResultStr);
            String infoCode = localeResultObj.getString("infocode");
            String actualProvince = "未知";
            String actualCity = "未知";
            if (StrUtil.isNotBlank(infoCode) && StrUtil.equals(infoCode, "10000")) {
                String province = localeResultObj.getString("province");
                boolean unknownProvinceFlag = StrUtil.equals(province, "[]");
                LinkLocaleStatsDO linkLocaleStatsDO = LinkLocaleStatsDO.builder()
                        .province(actualProvince = unknownProvinceFlag ? "未知" : province)
                        .city(actualCity = unknownProvinceFlag ? "未知" : localeResultObj.getString("city"))
                        .adcode(unknownProvinceFlag ? "未知" : localeResultObj.getString("adcode"))
                        .cnt(1)
                        .fullShortUrl(fullShortUrl)
                        .country("中国")
                        .gid(gid)
                        .date(date).build();
                linkLocaleStatsDO.setCreateTime(date);
                linkLocaleStatsDO.setUpdateTime(date);
                linkLocaleStatsDO.setDelFlag(0);
                linkLocaleStatsMapper.shortLinkStats(linkLocaleStatsDO);
            }
            LinkOsStatsDO linkOsStatsDO = LinkOsStatsDO.builder()
                    .os(os)
                    .cnt(1)
                    .fullShortUrl(fullShortUrl)
                    .gid(gid)
                    .date(date)
                    .createTime(date)
                    .updateTime(date)
                    .delFlag(0)
                    .build();
            linkOsStatsMapper.shortLinkStats(linkOsStatsDO);
            LinkBrowserStatsDO linkBrowserStatsDO = LinkBrowserStatsDO.builder()
                    .browser(browser)
                    .cnt(1)
                    .fullShortUrl(fullShortUrl)
                    .gid(gid)
                    .date(date)
                    .createTime(date)
                    .updateTime(date)
                    .delFlag(0)
                    .build();
            linkBrowserStatsMapper.shortLinkStats(linkBrowserStatsDO);

            LinkAccessLogsDO linkAccessLogsDO = LinkAccessLogsDO.builder()
                    .ip(ip)
                    .user(uv)
                    .gid(gid)
                    .locale(StrUtil.join("-", "中国", actualProvince, actualCity))
                    .device(device)
                    .network(network)
                    .os(os)
                    .fullShortUrl(fullShortUrl)
                    .browser(browser)
                    .build();
            linkAccessLogsMapper.insert(linkAccessLogsDO);
            LinkDeviceStatsDO linkDeviceStatsDO = LinkDeviceStatsDO.builder()
                    .device(device)
                    .cnt(1)
                    .gid(gid)
                    .fullShortUrl(fullShortUrl)
                    .date(date)
                    .createTime(date)
                    .updateTime(date)
                    .delFlag(0)
                    .build();
            linkDeviceStatsMapper.shortLinkStats(linkDeviceStatsDO);
            LinkNetworkStatsDO linkNetworkStatsDO = LinkNetworkStatsDO.builder()
                    .network(network)
                    .cnt(1)
                    .gid(gid)
                    .fullShortUrl(fullShortUrl)
                    .date(date)
                    .createTime(date)
                    .updateTime(date)
                    .delFlag(0)
                    .build();
            linkNetworkStatsMapper.shortLinkStats(linkNetworkStatsDO);
            LinkStatsTodayDO linkStatsTodayDO = LinkStatsTodayDO.builder()
                    .todayPv(1)
                    .todayUv(uvFirstFlag ? 1 : 0)
                    .todayUip(uipFirstFlag ? 1 : 0)
                    .gid(gid)
                    .fullShortUrl(fullShortUrl)
                    .createTime(date)
                    .updateTime(date)
                    .delFlag(0)
                    .date(date)
                    .build();
            linkStatsTodayMapper.shortLinkTodayState(linkStatsTodayDO);

            linkMapper.incrementStats(gid, fullShortUrl, 1, uvFirstFlag ? 1 : 0, uipFirstFlag ? 1 : 0);

        } catch (Throwable ex) {
            log.error("短链接访问量统计异常", ex);
        } finally {
            rLock.unlock();
        }
    }
}
