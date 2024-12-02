package com.xduo.shortlink.project.mq.consumer;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.Week;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.xduo.shortlink.project.common.convention.exception.ServiceException;
import com.xduo.shortlink.project.dao.entity.*;
import com.xduo.shortlink.project.dao.mapper.*;
import com.xduo.shortlink.project.dto.req.ShortLinkStatsRecordDTO;
import com.xduo.shortlink.project.mq.idempotent.MessageQueueIdempotentHandler;
import com.xduo.shortlink.project.mq.producer.DelayShortLinkStatsProducer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.stream.StreamListener;
import org.springframework.stereotype.Component;

import java.util.*;

import static com.xduo.shortlink.project.common.constant.RedisKeyConstant.LOCK_GID_UPDATE_KEY;
import static com.xduo.shortlink.project.common.constant.ShortLinkConstant.AMOP_REMOTE_URL;

@Slf4j
@Component
@RequiredArgsConstructor
public class ShortLinkStatsSaveConsumer  implements StreamListener<String, MapRecord<String, String, String>> {


    @Value("${short-link.stats.locale.amap-key}")
    private String statsAmapKey;

    private final LinkMapper linkMapper;

    private final LinkGoToMapper linkGoToMapper;

    private final RedissonClient redissonClient;

    private final LinkAccessStatsMapper linkAccessStatsMapper;

    private final LinkLocaleStatsMapper linkLocaleStatsMapper;

    private final LinkOsStatsMapper linkOsStatsMapper;

    private final LinkBrowserStatsMapper linkBrowserStatsMapper;

    private final LinkAccessLogsMapper linkAccessLogsMapper;

    private final LinkDeviceStatsMapper linkDeviceStatsMapper;

    private final LinkNetworkStatsMapper linkNetworkStatsMapper;

    private final LinkStatsTodayMapper linkStatsTodayMapper;

    private final StringRedisTemplate stringRedisTemplate;

    private final DelayShortLinkStatsProducer delayShortLinkStatsProducer;

    private final MessageQueueIdempotentHandler messageQueueIdempotentHandler;


    @Value("${spring.data.redis.channel-topic.short-link-stats-group}")
    private String group;

    public void actualSaveShortLinkStats(String fullShortUrl, String gid, ShortLinkStatsRecordDTO statsRecord) {
        Date date = new Date();
        String ip = statsRecord.getRemoteAddr();
        String device = statsRecord.getDevice();
        String network = statsRecord.getNetwork();
        String uv = statsRecord.getUv();
        String os = statsRecord.getOs();
        String browser = statsRecord.getBrowser();
        Boolean uipFirstFlag = statsRecord.getUipFirstFlag();
        Boolean uvFirstFlag = statsRecord.getUvFirstFlag();
        fullShortUrl = Optional.ofNullable(fullShortUrl).orElse(statsRecord.getFullShortUrl());
        RReadWriteLock readWriteLock = redissonClient.getReadWriteLock(String.format(LOCK_GID_UPDATE_KEY, fullShortUrl));
        RLock rLock = readWriteLock.readLock();
        if (!rLock.tryLock()) {
            //如果数据正在修改过程中，给延迟队列生产者读
            delayShortLinkStatsProducer.send(statsRecord);
            return;
        }
        try {
            if (StrUtil.isBlank(gid)) {
                LambdaQueryWrapper<LinkGoToDO> queryWrapper = Wrappers.lambdaQuery(LinkGoToDO.class)
                        .eq(LinkGoToDO::getFullShortUrl, fullShortUrl);
                LinkGoToDO shortLinkGotoDO = linkGoToMapper.selectOne(queryWrapper);
                gid = shortLinkGotoDO.getGid();
            }
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


    @Override
    public void onMessage(MapRecord<String, String, String> message) {
        String stream = message.getStream();
        RecordId id = message.getId();
        Map<String, String> producerMap = message.getValue();
        String fullShortUrl = producerMap.get("fullShortUrl");
        if(messageQueueIdempotentHandler.isMessageProcessed(id.toString())){
            if (messageQueueIdempotentHandler.isAccomplish(id.toString())){
                return;
            }
            throw new ServiceException("消息消费异常:流程未完成");


        }
        try{
            if (StrUtil.isNotBlank(fullShortUrl)) {
                String gid = producerMap.get("gid");
                ShortLinkStatsRecordDTO statsRecord = JSON.parseObject(producerMap.get("statsRecord"), ShortLinkStatsRecordDTO.class);
                actualSaveShortLinkStats(fullShortUrl, gid, statsRecord);
                if (stream != null) {
                    stringRedisTemplate.opsForStream().acknowledge(stream, group, id.getValue());
                    stringRedisTemplate.opsForStream().delete(stream, id.getValue());
                }

            }
        }catch (Throwable throwable){
            throw new ServiceException("消息消费异常：出现异常");
        }finally {
            messageQueueIdempotentHandler.delMessageProcessed(id.toString());
        }
        messageQueueIdempotentHandler.setAccomplish(id.toString());
    }


}
