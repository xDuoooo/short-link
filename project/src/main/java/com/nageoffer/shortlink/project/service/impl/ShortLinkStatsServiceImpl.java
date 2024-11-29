package com.nageoffer.shortlink.project.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.nageoffer.shortlink.project.dao.entity.*;
import com.nageoffer.shortlink.project.dao.mapper.*;
import com.nageoffer.shortlink.project.dto.req.SelectUvTypeByUserReqDTO;
import com.nageoffer.shortlink.project.dto.req.ShortLinkStatsAccessRecordReqDTO;
import com.nageoffer.shortlink.project.dto.req.ShortLinkStatsReqDTO;
import com.nageoffer.shortlink.project.dto.resp.*;
import com.nageoffer.shortlink.project.service.ShortLinkStatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
public class ShortLinkStatsServiceImpl implements ShortLinkStatsService {

    private final LinkAccessStatsMapper linkAccessStatsMapper;

    private final LinkLocaleStatsMapper linkLocaleStatsMapper;

    private final LinkAccessLogsMapper linkAccessLogsMapper;
    private final LinkNetworkStatsMapper linkNetworkStatsMapper;
    private final LinkDeviceStatsMapper linkDeviceStatsMapper;
    private final LinkBrowserStatsMapper linkBrowserStatsMapper;
    private final LinkOsStatsMapper linkOsStatsMapper;

    @Override
    public ShortLinkStatsRespDTO getOneShortLinkStats(ShortLinkStatsReqDTO shortLinkStatsReqDTO) {
        //封装每天访问数据
        List<LinkAccessStatsDO> linkAccessStatsDOS = linkAccessStatsMapper.getOneLinkBaseDateBetweenDate(shortLinkStatsReqDTO);
        List<ShortLinkStatsAccessDailyRespDTO> shortLinkStatsAccessDailyRespDTOList = new ArrayList<>();
        for (LinkAccessStatsDO linkAccessStatsDO : linkAccessStatsDOS) {
            ShortLinkStatsAccessDailyRespDTO shortLinkStatsAccessDailyRespDTO = ShortLinkStatsAccessDailyRespDTO.builder()
                    .uv(linkAccessStatsDO.getUv())
                    .uip(linkAccessStatsDO.getUip())
                    .pv(linkAccessStatsDO.getPv())
                    .date(linkAccessStatsDO.getDate()).build();
            shortLinkStatsAccessDailyRespDTOList.add(shortLinkStatsAccessDailyRespDTO);
        }


        //封装访问地区数量及其占比
        List<ShortLinkStatsLocaleCNRespDTO> shortLinkStatsLocaleCNRespDTOSList = linkLocaleStatsMapper.getOneLinkLocaleCntBetweenDateGroupByProvince(shortLinkStatsReqDTO);
        int localeCnSum = shortLinkStatsLocaleCNRespDTOSList.stream()
                .mapToInt(ShortLinkStatsLocaleCNRespDTO::getCnt)
                .sum();
        for (ShortLinkStatsLocaleCNRespDTO shortLinkStatsLocaleCNRespDTO : shortLinkStatsLocaleCNRespDTOSList) {
            double ratio = (double) shortLinkStatsLocaleCNRespDTO.getCnt() / localeCnSum;
            double actualRatio = Math.round(ratio * 100.0) / 100.0;
            shortLinkStatsLocaleCNRespDTO.setRatio(actualRatio);
        }
        //封装按小时统计的数据
        List<LinkAccessStatsDO> listHourStatsByShortLinkDO = linkAccessStatsMapper.listHourStatsByShortLink(shortLinkStatsReqDTO);
        List<Integer> hourStats = new ArrayList<>();
        for(int i = 0 ; i < 24 ; i++){
            AtomicInteger hour = new AtomicInteger(i);
            int hourCnt = listHourStatsByShortLinkDO.stream()
                    .filter(each -> Objects.equals(each.getHour(), hour.get()))
                    .findFirst()
                    .map(LinkAccessStatsDO::getPv)
                    .orElse(0);
            hourStats.add(hourCnt);
        }
        //封装高频ip访问
        List<ShortLinkStatsTopIpRespDTO> topIpStats = new ArrayList<>();
        List<HashMap<String, Object>> listTopIpByShortLink = linkAccessLogsMapper.listTopIpByShortLink(shortLinkStatsReqDTO);
        listTopIpByShortLink.forEach(each -> {
            ShortLinkStatsTopIpRespDTO statsTopIpRespDTO = ShortLinkStatsTopIpRespDTO.builder()
                    .ip(each.get("ip").toString())
                    .cnt(Integer.parseInt(each.get("count").toString()))
                    .build();
            topIpStats.add(statsTopIpRespDTO);
        });
        //封装星期day统计量
        List<LinkAccessStatsDO> listWeekdayStatsByShortLinkDO = linkAccessStatsMapper.listWeekdayStatsByShortLink(shortLinkStatsReqDTO);
        List<Integer> weekdayStats = new ArrayList<>();
        for(int i = 1 ; i < 8 ; i++){
            AtomicInteger weekday = new AtomicInteger(i);
            int weekdayCnt = listWeekdayStatsByShortLinkDO.stream()
                    .filter(each -> Objects.equals(each.getWeekday(), weekday.get()))
                    .findFirst()
                    .map(LinkAccessStatsDO::getPv)
                    .orElse(0);
            weekdayStats.add(weekdayCnt);
        }

        //封装浏览器统计量
        List<LinkBrowserStatsDO> linkBrowserStatsDOList = linkBrowserStatsMapper.listBrowserStatsByShortLink(shortLinkStatsReqDTO);
        int browserSum = linkBrowserStatsDOList.stream().mapToInt(LinkBrowserStatsDO::getCnt).sum();
        List<ShortLinkStatsBrowserRespDTO> shortLinkStatsBrowserRespDTOList = new ArrayList<>();
        linkBrowserStatsDOList.forEach(
                x-> {
                    double ratio = (double) x.getCnt() / browserSum;
                    double actualRatio = Math.round(ratio * 100.0) / 100.0;
                    ShortLinkStatsBrowserRespDTO shortLinkStatsBrowserRespDTO = ShortLinkStatsBrowserRespDTO.builder().browser(x.getBrowser()).cnt(x.getCnt()).ratio(actualRatio).build();
                    shortLinkStatsBrowserRespDTOList.add(shortLinkStatsBrowserRespDTO);
                }
        );
        //封装操作系统统计量
        List<LinkOsStatsDO> linkOsStatsDOList = linkOsStatsMapper.listOsStatsByShortLink(shortLinkStatsReqDTO);
        int osSum = linkOsStatsDOList.stream().mapToInt(LinkOsStatsDO::getCnt).sum();
        List<ShortLinkStatsOsRespDTO> shortLinkStatsOsrespDTOList = new ArrayList<>();
        linkOsStatsDOList.forEach(
                x-> {
                    double ratio = (double) x.getCnt() / osSum;
                    double actualRatio = Math.round(ratio * 100.0) / 100.0;
                    ShortLinkStatsOsRespDTO shortLinkStatsOsRespDTO = ShortLinkStatsOsRespDTO.builder().os(x.getOs()).cnt(x.getCnt()).ratio(actualRatio).build();
                    shortLinkStatsOsrespDTOList.add(shortLinkStatsOsRespDTO);
                }
        );
        //封装访客类型统计量
        List<ShortLinkStatsUvRespDTO> uvTypeStats = new ArrayList<>();
        HashMap<String, Object> findUvTypeByShortLink = linkAccessLogsMapper.findUvTypeCntByShortLink(shortLinkStatsReqDTO);
        int oldUserCnt = Integer.parseInt(findUvTypeByShortLink.get("oldUserCnt").toString());
        int newUserCnt = Integer.parseInt(findUvTypeByShortLink.get("newUserCnt").toString());
        int uvSum = oldUserCnt + newUserCnt;
        double oldRatio = (double) oldUserCnt / uvSum;
        double actualOldRatio = Math.round(oldRatio * 100.0) / 100.0;
        double newRatio = (double) newUserCnt / uvSum;
        double actualNewRatio = Math.round(newRatio * 100.0) / 100.0;
        ShortLinkStatsUvRespDTO newUvRespDTO = ShortLinkStatsUvRespDTO.builder()
                .uvType("newUser")
                .cnt(newUserCnt)
                .ratio(actualNewRatio)
                .build();
        uvTypeStats.add(newUvRespDTO);
        ShortLinkStatsUvRespDTO oldUvRespDTO = ShortLinkStatsUvRespDTO.builder()
                .uvType("oldUser")
                .cnt(oldUserCnt)
                .ratio(actualOldRatio)
                .build();
        uvTypeStats.add(oldUvRespDTO);
        //封装设备类型统计量
        List<LinkDeviceStatsDO> linkDeviceStatsDOList = linkDeviceStatsMapper.listDeviceStatsByShortLink(shortLinkStatsReqDTO);
        int deviceSum = linkDeviceStatsDOList.stream().mapToInt(LinkDeviceStatsDO::getCnt).sum();
        List<ShortLinkStatsDeviceRespDTO> shortLinkStatsDeviceRespDTOList = new ArrayList<>();
        linkDeviceStatsDOList.forEach(
                x-> {
                    double ratio = (double) x.getCnt() / deviceSum;
                    double actualRatio = Math.round(ratio * 100.0) / 100.0;
                    ShortLinkStatsDeviceRespDTO shortLinkStatsDeviceRespDTO = ShortLinkStatsDeviceRespDTO.builder().device(x.getDevice()).cnt(x.getCnt()).ratio(actualRatio).build();
                    shortLinkStatsDeviceRespDTOList.add(shortLinkStatsDeviceRespDTO);
                }
        );


        //封装网络类型统计量
        List<LinkNetworkStatsDO> linkNetworkStatsDOList = linkNetworkStatsMapper.listNetworkStatsByShortLink(shortLinkStatsReqDTO);
        int networkSum = linkNetworkStatsDOList.stream().mapToInt(LinkNetworkStatsDO::getCnt).sum();
        List<ShortLinkStatsNetworkRespDTO> shortLinkStatsNetworkRespDTOList = new ArrayList<>();
        linkNetworkStatsDOList.forEach(
                x-> {
                    double ratio = (double) x.getCnt() / networkSum;
                    double actualRatio = Math.round(ratio * 100.0) / 100.0;
                    ShortLinkStatsNetworkRespDTO shortLinkStatsNetworkRespDTO = ShortLinkStatsNetworkRespDTO.builder().network(x.getNetwork()).cnt(x.getCnt()).ratio(actualRatio).build();
                    shortLinkStatsNetworkRespDTOList.add(shortLinkStatsNetworkRespDTO);
                }
        );
        return ShortLinkStatsRespDTO.builder()
                .daily(shortLinkStatsAccessDailyRespDTOList)
                .localeCnStats(shortLinkStatsLocaleCNRespDTOSList)
                .hourStats(hourStats)
                .uvTypeStats(uvTypeStats)
                .osStats(shortLinkStatsOsrespDTOList)
                .topIpStats(topIpStats)
                .weekdayStats(weekdayStats)
                .browserStats(shortLinkStatsBrowserRespDTOList)
                .networkStats(shortLinkStatsNetworkRespDTOList)
                .deviceStats(shortLinkStatsDeviceRespDTOList)
                .build();


    }

    @Override
    public IPage<ShortLinkStatsAccessRecordRespDTO> shortLinkStatsAccessRecord(ShortLinkStatsAccessRecordReqDTO requestParam) {
        LambdaQueryWrapper<LinkAccessLogsDO> queryWrapper = Wrappers.lambdaQuery(LinkAccessLogsDO.class)
                .eq(LinkAccessLogsDO::getGid, requestParam.getGid())
                .eq(LinkAccessLogsDO::getFullShortUrl, requestParam.getFullShortUrl())
                .between(LinkAccessLogsDO::getCreateTime, requestParam.getStartDate(), requestParam.getEndDate())
                .eq(LinkAccessLogsDO::getDelFlag, 0)
                .orderByDesc(LinkAccessLogsDO::getCreateTime);
        IPage<LinkAccessLogsDO> linkAccessLogsDOIPage = linkAccessLogsMapper.selectPage(requestParam,queryWrapper);
        IPage<ShortLinkStatsAccessRecordRespDTO> actualResult = linkAccessLogsDOIPage.convert(each -> BeanUtil.toBean(each, ShortLinkStatsAccessRecordRespDTO.class));
        List<String> userAccessLogsList = actualResult.getRecords().stream()
                .map(ShortLinkStatsAccessRecordRespDTO::getUser)
                .toList();
        SelectUvTypeByUserReqDTO selectUvTypeByUserReqDTO = BeanUtil.toBean(requestParam, SelectUvTypeByUserReqDTO.class);
        selectUvTypeByUserReqDTO.setUserAccessLogsList(userAccessLogsList);
        List<Map<String,Object>> uvTypeList = linkAccessLogsMapper.selectUvTypeByUser(selectUvTypeByUserReqDTO);
        actualResult.getRecords().forEach(
                eachResult ->{
                    String uvType = uvTypeList.stream().filter(eachType-> Objects.equals(eachType.get("user"),eachResult.getUser()))
                            .findFirst().map(item -> item.get("uvType").toString())
                            .orElse("旧访客");
                    eachResult.setUvType(uvType);
                }
        );

        return actualResult;
    }
}
