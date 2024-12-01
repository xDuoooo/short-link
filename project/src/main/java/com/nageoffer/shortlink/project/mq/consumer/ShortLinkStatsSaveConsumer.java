//package com.nageoffer.shortlink.project.mq.consumer;
//
//import cn.hutool.core.date.DateUtil;
//import cn.hutool.core.date.Week;
//import cn.hutool.core.lang.UUID;
//import cn.hutool.core.util.ArrayUtil;
//import cn.hutool.core.util.StrUtil;
//import cn.hutool.http.HttpUtil;
//import com.alibaba.fastjson2.JSON;
//import com.alibaba.fastjson2.JSONObject;
//import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
//import com.baomidou.mybatisplus.core.toolkit.Wrappers;
//import com.nageoffer.shortlink.project.dao.entity.*;
//import com.nageoffer.shortlink.project.dao.mapper.*;
//import com.nageoffer.shortlink.project.util.LinkUtil;
//import jakarta.servlet.http.Cookie;
//import jodd.util.StringUtil;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.redisson.api.RedissonClient;
//import org.springframework.data.redis.core.StringRedisTemplate;
//import org.springframework.stereotype.Component;
//
//import java.util.*;
//import java.util.concurrent.atomic.AtomicBoolean;
//import java.util.concurrent.atomic.AtomicReference;
//
//import static com.nageoffer.shortlink.project.common.constant.ShortLinkConstant.AMOP_REMOTE_URL;
//@Slf4j
//@Component
//@RequiredArgsConstructor
//public class ShortLinkStatsSaveConsumer {
//
//    private final LinkMapper linkMapper;
//
//    private final LinkGoToMapper linkGotoMapper;
//
//    private final RedissonClient redissonClient;
//
//    private final LinkAccessStatsMapper linkAccessStatsMapper;
//
//    private final LinkLocaleStatsMapper linkLocaleStatsMapper;
//
//    private final LinkOsStatsMapper linkOsStatsMapper;
//
//    private final LinkBrowserStatsMapper linkBrowserStatsMapper;
//
//    private final LinkAccessLogsMapper linkAccessLogsMapper;
//
//    private final LinkDeviceStatsMapper linkDeviceStatsMapper;
//
//    private final LinkNetworkStatsMapper linkNetworkStatsMapper;
//
//    private final LinkStatsTodayMapper linkStatsTodayMapper;
//
//    private final StringRedisTemplate stringRedisTemplate;
//
//
//
//
//
//    public void actualSaveShortLinkStats(String fullShortUrl, String gid, ShortLinkStatsRecordDTO statsRecord){
//
//
//            Long uvAdded = stringRedisTemplate.opsForSet().add("short-link:stats:uip" + fullShortUrl, ip);
//            boolean uipFirstFlag = uvAdded != null && uvAdded > 0L;
//            if (StrUtil.isBlank(gid)) {
//                LambdaQueryWrapper<LinkGoToDO> queryWrapper = Wrappers.lambdaQuery(LinkGoToDO.class)
//                        .eq(LinkGoToDO::getFullShortUrl, fullShortUrl);
//                LinkGoToDO shortLinkGotoDO = linkGoToMapper.selectOne(queryWrapper);
//                gid = shortLinkGotoDO.getGid();
//            }
//            int hour = DateUtil.hour(date, true);
//            Week week = DateUtil.dayOfWeekEnum(date);
//            int weekValue = week.getIso8601Value();
//            LinkAccessStatsDO linkAccessStatsDO = LinkAccessStatsDO.builder()
//                    .pv(1)
//                    .uv(uvFirstFlag.get() ? 1 : 0)
//                    .uip(uipFirstFlag ? 1 : 0)
//                    .hour(hour)
//                    .weekday(weekValue)
//                    .fullShortUrl(fullShortUrl)
//                    .gid(gid)
//                    .date(date)
//                    .build();
//            linkAccessStatsDO.setDelFlag(0);
//            linkAccessStatsDO.setCreateTime(date);
//            linkAccessStatsDO.setUpdateTime(date);
//            linkAccessStatsMapper.shortLinkStats(linkAccessStatsDO);
//
//
//            Map<String, Object> localeGetParam = new HashMap<>();
//            localeGetParam.put("ip", ip);
//            localeGetParam.put("key", statsAmapKey);
//            String localeResultStr = HttpUtil.get(AMOP_REMOTE_URL, localeGetParam);
//            JSONObject localeResultObj = JSON.parseObject(localeResultStr);
//            String infoCode = localeResultObj.getString("infocode");
//            String actualProvince = "未知";
//            String actualCity = "未知";
//            if (StrUtil.isNotBlank(infoCode) && StrUtil.equals(infoCode, "10000")) {
//                String province = localeResultObj.getString("province");
//                boolean unknownProvinceFlag = StrUtil.equals(province, "[]");
//                LinkLocaleStatsDO linkLocaleStatsDO = LinkLocaleStatsDO.builder()
//                        .province(actualProvince = unknownProvinceFlag ? "未知" : province)
//                        .city(actualCity = unknownProvinceFlag ? "未知" : localeResultObj.getString("city"))
//                        .adcode(unknownProvinceFlag ? "未知" : localeResultObj.getString("adcode"))
//                        .cnt(1)
//                        .fullShortUrl(fullShortUrl)
//                        .country("中国")
//                        .gid(gid)
//                        .date(date).build();
//                linkLocaleStatsDO.setCreateTime(date);
//                linkLocaleStatsDO.setUpdateTime(date);
//                linkLocaleStatsDO.setDelFlag(0);
//                linkLocaleStatsMapper.shortLinkStats(linkLocaleStatsDO);
//            }
//            String os = LinkUtil.getOs(request);
//            LinkOsStatsDO linkOsStatsDO = LinkOsStatsDO.builder()
//                    .os(os)
//                    .cnt(1)
//                    .fullShortUrl(fullShortUrl)
//                    .gid(gid)
//                    .date(date)
//                    .createTime(date)
//                    .updateTime(date)
//                    .delFlag(0)
//                    .build();
//            linkOsStatsMapper.shortLinkStats(linkOsStatsDO);
//            String browser = LinkUtil.getBrowser(request);
//            LinkBrowserStatsDO linkBrowserStatsDO = LinkBrowserStatsDO.builder()
//                    .browser(browser)
//                    .cnt(1)
//                    .fullShortUrl(fullShortUrl)
//                    .gid(gid)
//                    .date(date)
//                    .createTime(date)
//                    .updateTime(date)
//                    .delFlag(0)
//                    .build();
//            linkBrowserStatsMapper.shortLinkStats(linkBrowserStatsDO);
//            String device = LinkUtil.getDevice(request);
//            String network = LinkUtil.getNetwork(request);
//            LinkAccessLogsDO linkAccessLogsDO = LinkAccessLogsDO.builder()
//                    .ip(ip)
//                    .user(uv.get())
//                    .gid(gid)
//                    .locale(StrUtil.join("-", "中国", actualProvince, actualCity))
//                    .device(device)
//                    .network(network)
//                    .os(os)
//                    .fullShortUrl(fullShortUrl)
//                    .browser(browser)
//                    .build();
//            linkAccessLogsMapper.insert(linkAccessLogsDO);
//            LinkDeviceStatsDO linkDeviceStatsDO = LinkDeviceStatsDO.builder()
//                    .device(device)
//                    .cnt(1)
//                    .gid(gid)
//                    .fullShortUrl(fullShortUrl)
//                    .date(date)
//                    .createTime(date)
//                    .updateTime(date)
//                    .delFlag(0)
//                    .build();
//            linkDeviceStatsMapper.shortLinkStats(linkDeviceStatsDO);
//            LinkNetworkStatsDO linkNetworkStatsDO = LinkNetworkStatsDO.builder()
//                    .network(network)
//                    .cnt(1)
//                    .gid(gid)
//                    .fullShortUrl(fullShortUrl)
//                    .date(date)
//                    .createTime(date)
//                    .updateTime(date)
//                    .delFlag(0)
//                    .build();
//            linkNetworkStatsMapper.shortLinkStats(linkNetworkStatsDO);
//            LinkStatsTodayDO linkStatsTodayDO = LinkStatsTodayDO.builder()
//                    .todayPv(1)
//                    .todayUv(uvFirstFlag.get() ? 1 : 0)
//                    .todayUip(uipFirstFlag ? 1 : 0)
//                    .gid(gid)
//                    .fullShortUrl(fullShortUrl)
//                    .createTime(date)
//                    .updateTime(date)
//                    .delFlag(0)
//                    .date(date)
//                    .build();
//            linkStatsTodayMapper.shortLinkTodayState(linkStatsTodayDO);
//
//            linkMapper.incrementStats(gid, fullShortUrl, 1, uvFirstFlag.get() ? 1 : 0, uipFirstFlag ? 1 : 0);
//
//        } catch (Throwable ex) {
//            log.error("短链接访问量统计异常", ex);
//        }
//    }
//}
