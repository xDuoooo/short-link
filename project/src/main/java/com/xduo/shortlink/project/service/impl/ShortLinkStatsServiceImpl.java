package com.xduo.shortlink.project.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.xduo.shortlink.project.dao.entity.*;
import com.xduo.shortlink.project.dao.mapper.*;
import com.xduo.shortlink.project.dto.req.*;
import com.xduo.shortlink.project.dto.resp.*;
import com.xduo.shortlink.project.service.ShortLinkStatsService;
import com.xduo.shortlink.project.service.ShortLinkUvStatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
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

    private final ShortLinkUvStatsService shortLinkUvStatsService;
    
    private final LinkMapper linkMapper;



    /**
     * 获取一个短链接指定日期内的监控记录
     * @param shortLinkStatsReqDTO
     * @return
     */
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
        //封装pv uv uip
        Integer pv = 0;
        Integer uv = 0;
        Integer uip = 0;
        for (LinkAccessStatsDO linkAccessStatsDO : linkAccessStatsDOS) {
            pv += linkAccessStatsDO.getPv();
            uv += linkAccessStatsDO.getUv();
            uip += linkAccessStatsDO.getUip();
        }


        //封装访问地区数量及其占比
        List<ShortLinkStatsLocaleCNRespDTO> shortLinkStatsLocaleCNRespDTOSList = linkLocaleStatsMapper.getOneLinkLocaleCntBetweenDateByProvince(shortLinkStatsReqDTO);
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
        for (int i = 0; i < 24; i++) {
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
            Object ipObj = each.get("ip");
            Object countObj = each.get("count");
            if (ipObj != null && countObj != null) {
                ShortLinkStatsTopIpRespDTO statsTopIpRespDTO = ShortLinkStatsTopIpRespDTO.builder()
                        .ip(ipObj.toString())
                        .cnt(Integer.parseInt(countObj.toString()))
                        .build();
                topIpStats.add(statsTopIpRespDTO);
            }
        });
        //封装星期day统计量
        List<LinkAccessStatsDO> listWeekdayStatsByShortLinkDO = linkAccessStatsMapper.listWeekdayStatsByShortLink(shortLinkStatsReqDTO);
        List<Integer> weekdayStats = new ArrayList<>();
        for (int i = 1; i < 8; i++) {
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
                x -> {
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
                x -> {
                    double ratio = (double) x.getCnt() / osSum;
                    double actualRatio = Math.round(ratio * 100.0) / 100.0;
                    ShortLinkStatsOsRespDTO shortLinkStatsOsRespDTO = ShortLinkStatsOsRespDTO.builder().os(x.getOs()).cnt(x.getCnt()).ratio(actualRatio).build();
                    shortLinkStatsOsrespDTOList.add(shortLinkStatsOsRespDTO);
                }
        );
        //封装访客类型统计量
        List<ShortLinkStatsUvRespDTO> uvTypeStats = new ArrayList<>();
        Map<String, Object> findUvTypeByShortLink = shortLinkUvStatsService.getUvTypeCntByShortLink(shortLinkStatsReqDTO);
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
                x -> {
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
                x -> {
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
                .pv(pv)
                .uip(uip)
                .uv(uv)
                .build();


    }

    @Override
    public ShortLinkStatsRespDTO getGroupShortLinkStats(ShortLinkGroupStatsReqDTO shortLinkGroupStatsReqDTO) {
        //封装每天访问数据
        List<LinkAccessStatsDO> linkAccessStatsDOS = linkAccessStatsMapper.getGroupLinkBaseDateBetweenDate(shortLinkGroupStatsReqDTO);
        List<ShortLinkStatsAccessDailyRespDTO> shortLinkGroupStatsAccessDailyRespDTOList = new ArrayList<>();
        for (LinkAccessStatsDO linkAccessStatsDO : linkAccessStatsDOS) {
            ShortLinkStatsAccessDailyRespDTO shortLinkStatsAccessDailyRespDTO = ShortLinkStatsAccessDailyRespDTO.builder()
                    .uv(linkAccessStatsDO.getUv())
                    .uip(linkAccessStatsDO.getUip())
                    .pv(linkAccessStatsDO.getPv())
                    .date(linkAccessStatsDO.getDate()).build();
            shortLinkGroupStatsAccessDailyRespDTOList.add(shortLinkStatsAccessDailyRespDTO);
        }
        //封装pv uv uip
        Integer pv = 0;
        Integer uv = 0;
        Integer uip = 0;
        for (LinkAccessStatsDO linkAccessStatsDO : linkAccessStatsDOS) {
            pv += linkAccessStatsDO.getPv();
            uv += linkAccessStatsDO.getUv();
            uip += linkAccessStatsDO.getUip();
        }


        //封装访问地区数量及其占比
        List<ShortLinkStatsLocaleCNRespDTO> shortLinkStatsLocaleCNRespDTOSList = linkLocaleStatsMapper.getGroupLinkLocaleCntBetweenDateByProvince(shortLinkGroupStatsReqDTO);
        int localeCnSum = shortLinkStatsLocaleCNRespDTOSList.stream()
                .mapToInt(ShortLinkStatsLocaleCNRespDTO::getCnt)
                .sum();
        for (ShortLinkStatsLocaleCNRespDTO shortLinkStatsLocaleCNRespDTO : shortLinkStatsLocaleCNRespDTOSList) {
            double ratio = (double) shortLinkStatsLocaleCNRespDTO.getCnt() / localeCnSum;
            double actualRatio = Math.round(ratio * 100.0) / 100.0;
            shortLinkStatsLocaleCNRespDTO.setRatio(actualRatio);
        }
        //封装按小时统计的数据
        List<LinkAccessStatsDO> listGroupHourStatsByShortLink = linkAccessStatsMapper.listGroupHourStatsByShortLink(shortLinkGroupStatsReqDTO);
        List<Integer> hourStats = new ArrayList<>();
        for (int i = 0; i < 24; i++) {
            AtomicInteger hour = new AtomicInteger(i);
            int hourCnt = listGroupHourStatsByShortLink.stream()
                    .filter(each -> Objects.equals(each.getHour(), hour.get()))
                    .findFirst()
                    .map(LinkAccessStatsDO::getPv)
                    .orElse(0);
            hourStats.add(hourCnt);
        }
        //封装高频ip访问
        List<ShortLinkStatsTopIpRespDTO> topIpStats = new ArrayList<>();
        List<HashMap<String, Object>> listGroupTopIpByShortLink = linkAccessLogsMapper.listGroupTopIpByShortLink(shortLinkGroupStatsReqDTO);
        listGroupTopIpByShortLink.forEach(each -> {
            Object ipObj = each.get("ip");
            Object countObj = each.get("count");
            if (ipObj != null && countObj != null) {
                ShortLinkStatsTopIpRespDTO statsTopIpRespDTO = ShortLinkStatsTopIpRespDTO.builder()
                        .ip(ipObj.toString())
                        .cnt(Integer.parseInt(countObj.toString()))
                        .build();
                topIpStats.add(statsTopIpRespDTO);
            }
        });
        //封装星期day统计量
        List<LinkAccessStatsDO> listWeekdayStatsByShortLinkDO = linkAccessStatsMapper.listGroupWeekdayStatsByShortLink(shortLinkGroupStatsReqDTO);
        List<Integer> weekdayStats = new ArrayList<>();
        for (int i = 1; i < 8; i++) {
            AtomicInteger weekday = new AtomicInteger(i);
            int weekdayCnt = listWeekdayStatsByShortLinkDO.stream()
                    .filter(each -> Objects.equals(each.getWeekday(), weekday.get()))
                    .findFirst()
                    .map(LinkAccessStatsDO::getPv)
                    .orElse(0);
            weekdayStats.add(weekdayCnt);
        }

        //封装浏览器统计量
        List<LinkBrowserStatsDO> linkBrowserStatsDOList = linkBrowserStatsMapper.listGroupBrowserStatsByShortLink(shortLinkGroupStatsReqDTO);
        int browserSum = linkBrowserStatsDOList.stream().mapToInt(LinkBrowserStatsDO::getCnt).sum();
        List<ShortLinkStatsBrowserRespDTO> shortLinkStatsBrowserRespDTOList = new ArrayList<>();
        linkBrowserStatsDOList.forEach(
                x -> {
                    double ratio = (double) x.getCnt() / browserSum;
                    double actualRatio = Math.round(ratio * 100.0) / 100.0;
                    ShortLinkStatsBrowserRespDTO shortLinkStatsBrowserRespDTO = ShortLinkStatsBrowserRespDTO.builder().browser(x.getBrowser()).cnt(x.getCnt()).ratio(actualRatio).build();
                    shortLinkStatsBrowserRespDTOList.add(shortLinkStatsBrowserRespDTO);
                }
        );
        //封装操作系统统计量
        List<LinkOsStatsDO> linkOsStatsDOList = linkOsStatsMapper.listGroupOsStatsByShortLink(shortLinkGroupStatsReqDTO);
        int osSum = linkOsStatsDOList.stream().mapToInt(LinkOsStatsDO::getCnt).sum();
        List<ShortLinkStatsOsRespDTO> shortLinkStatsOsrespDTOList = new ArrayList<>();
        linkOsStatsDOList.forEach(
                x -> {
                    double ratio = (double) x.getCnt() / osSum;
                    double actualRatio = Math.round(ratio * 100.0) / 100.0;
                    ShortLinkStatsOsRespDTO shortLinkStatsOsRespDTO = ShortLinkStatsOsRespDTO.builder().os(x.getOs()).cnt(x.getCnt()).ratio(actualRatio).build();
                    shortLinkStatsOsrespDTOList.add(shortLinkStatsOsRespDTO);
                }
        );
        //封装访客类型统计量
        List<ShortLinkStatsUvRespDTO> uvTypeStats = new ArrayList<>();
        Map<String, Object> findUvTypeByShortLink = shortLinkUvStatsService.getGroupUvTypeCntByShortLink(shortLinkGroupStatsReqDTO);
        if (findUvTypeByShortLink == null) {
            findUvTypeByShortLink = new HashMap<>();
            findUvTypeByShortLink.put("oldUserCnt", 0);
            findUvTypeByShortLink.put("newUserCnt", 0);
        }
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
        List<LinkDeviceStatsDO> linkDeviceStatsDOList = linkDeviceStatsMapper.listGroupDeviceStatsByShortLink(shortLinkGroupStatsReqDTO);
        int deviceSum = linkDeviceStatsDOList.stream().mapToInt(LinkDeviceStatsDO::getCnt).sum();
        List<ShortLinkStatsDeviceRespDTO> shortLinkStatsDeviceRespDTOList = new ArrayList<>();
        linkDeviceStatsDOList.forEach(
                x -> {
                    double ratio = (double) x.getCnt() / deviceSum;
                    double actualRatio = Math.round(ratio * 100.0) / 100.0;
                    ShortLinkStatsDeviceRespDTO shortLinkStatsDeviceRespDTO = ShortLinkStatsDeviceRespDTO.builder().device(x.getDevice()).cnt(x.getCnt()).ratio(actualRatio).build();
                    shortLinkStatsDeviceRespDTOList.add(shortLinkStatsDeviceRespDTO);
                }
        );


        //封装网络类型统计量
        List<LinkNetworkStatsDO> linkNetworkStatsDOList = linkNetworkStatsMapper.listGroupNetworkStatsByShortLink(shortLinkGroupStatsReqDTO);
        int networkSum = linkNetworkStatsDOList.stream().mapToInt(LinkNetworkStatsDO::getCnt).sum();
        List<ShortLinkStatsNetworkRespDTO> shortLinkStatsNetworkRespDTOList = new ArrayList<>();
        linkNetworkStatsDOList.forEach(
                x -> {
                    double ratio = (double) x.getCnt() / networkSum;
                    double actualRatio = Math.round(ratio * 100.0) / 100.0;
                    ShortLinkStatsNetworkRespDTO shortLinkStatsNetworkRespDTO = ShortLinkStatsNetworkRespDTO.builder().network(x.getNetwork()).cnt(x.getCnt()).ratio(actualRatio).build();
                    shortLinkStatsNetworkRespDTOList.add(shortLinkStatsNetworkRespDTO);
                }
        );
        return ShortLinkStatsRespDTO.builder()
                .daily(shortLinkGroupStatsAccessDailyRespDTOList)
                .localeCnStats(shortLinkStatsLocaleCNRespDTOSList)
                .hourStats(hourStats)
                .uvTypeStats(uvTypeStats)
                .osStats(shortLinkStatsOsrespDTOList)
                .topIpStats(topIpStats)
                .weekdayStats(weekdayStats)
                .browserStats(shortLinkStatsBrowserRespDTOList)
                .networkStats(shortLinkStatsNetworkRespDTOList)
                .deviceStats(shortLinkStatsDeviceRespDTOList)
                .pv(pv)
                .uip(uip)
                .uv(uv)
                .build();

    }

    @Override
    public IPage<ShortLinkStatsAccessRecordRespDTO> shortLinkStatsAccessRecord(ShortLinkStatsAccessRecordReqDTO requestParam) {
        // 处理时间格式，确保查询范围包含完整的一天
        String startDateTime = requestParam.getStartDate() + " 00:00:00";
        String endDateTime = requestParam.getEndDate() + " 23:59:59";
        
        // 根据includeRecycle参数决定查询逻辑
        if (requestParam.getIncludeRecycle() == null || !requestParam.getIncludeRecycle()) {
            // 如果includeRecycle为false或null，首先检查短链接是否存在且未被删除
            LambdaQueryWrapper<LinkDO> linkQueryWrapper = Wrappers.lambdaQuery(LinkDO.class)
                    .eq(LinkDO::getGid, requestParam.getGid())
                    .eq(LinkDO::getFullShortUrl, requestParam.getFullShortUrl())
                    .eq(LinkDO::getDelFlag, 0);
            LinkDO linkDO = linkMapper.selectOne(linkQueryWrapper);
            if (linkDO == null) {
                // 短链接不存在或已被删除，返回空结果
                return new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(requestParam.getCurrent(), requestParam.getSize());
            }
        }
        
        LambdaQueryWrapper<LinkAccessLogsDO> queryWrapper = Wrappers.lambdaQuery(LinkAccessLogsDO.class)
                .eq(LinkAccessLogsDO::getGid, requestParam.getGid())
                .eq(LinkAccessLogsDO::getFullShortUrl, requestParam.getFullShortUrl())
                .between(LinkAccessLogsDO::getCreateTime, startDateTime, endDateTime)
                .eq(LinkAccessLogsDO::getDelFlag, 0)
                .orderByDesc(LinkAccessLogsDO::getCreateTime);
        IPage<LinkAccessLogsDO> linkAccessLogsDOIPage = linkAccessLogsMapper.selectPage(requestParam, queryWrapper);
        IPage<ShortLinkStatsAccessRecordRespDTO> actualResult = linkAccessLogsDOIPage.convert(each -> {
            ShortLinkStatsAccessRecordRespDTO dto = BeanUtil.toBean(each, ShortLinkStatsAccessRecordRespDTO.class);
            // 设置短链接
            dto.setFullShortUrl(each.getFullShortUrl());
            // 获取短链接描述信息
            LinkDO linkDO = linkMapper.selectOne(Wrappers.lambdaQuery(LinkDO.class)
                    .eq(LinkDO::getGid, each.getGid())
                    .eq(LinkDO::getFullShortUrl, each.getFullShortUrl())
                    .eq(LinkDO::getDelFlag, 0)
                    .select(LinkDO::getDescribe, LinkDO::getOriginUrl));
            if (linkDO != null) {
                dto.setDescribe(linkDO.getDescribe());
                dto.setOriginUrl(linkDO.getOriginUrl());
            }
            return dto;
        });
        List<String> userAccessLogsList = actualResult.getRecords().stream()
                .map(ShortLinkStatsAccessRecordRespDTO::getUser)
                .toList();
        SelectUvTypeByUserReqDTO selectUvTypeByUserReqDTO = BeanUtil.toBean(requestParam, SelectUvTypeByUserReqDTO.class);
        selectUvTypeByUserReqDTO.setUserAccessLogsList(userAccessLogsList);
        //每一个map 中 key 列名 value 值
        List<Map<String, Object>> uvTypeList = shortLinkUvStatsService.getUvTypeByUser(selectUvTypeByUserReqDTO);
        for (Map<String, Object> uvType : uvTypeList) {
            LocalDateTime createTime = (LocalDateTime) uvType.get("create_time");
            String user = uvType.get("user").toString();
            for (ShortLinkStatsAccessRecordRespDTO record : actualResult.getRecords()) {
                Date date = record.getCreateTime();
                // 将Date转换为LocalDateTime
                LocalDateTime dateAsLocalDateTime = date.toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDateTime();
                if(record.getUser().equals(user) && dateAsLocalDateTime.isEqual(createTime)){
                    record.setUvType(uvType.get("uvType").toString());
                    break;
                }
            }

        }
        return actualResult;
    }

    @Override
    public IPage<ShortLinkStatsAccessRecordRespDTO> shortLinkGroupStatsAccessRecord(ShortLinkGroupStatsAccessRecordReqDTO requestParam) {
        // 分页参数验证
        if (requestParam.getCurrent() <= 0) {
            requestParam.setCurrent(1L);
        }
        if (requestParam.getSize() <= 0) {
            requestParam.setSize(10L);
        }
        if (requestParam.getSize() > 100) {
            requestParam.setSize(100L);
        }
        
        // 处理时间格式，确保查询范围包含完整的一天
        String startDateTime = requestParam.getStartDate() + " 00:00:00";
        String endDateTime = requestParam.getEndDate() + " 23:59:59";
        
        // 根据includeRecycle参数决定查询逻辑
        LambdaQueryWrapper<LinkAccessLogsDO> queryWrapper = Wrappers.lambdaQuery(LinkAccessLogsDO.class)
                .eq(LinkAccessLogsDO::getGid, requestParam.getGid())
                .between(LinkAccessLogsDO::getCreateTime, startDateTime, endDateTime)
                .orderByDesc(LinkAccessLogsDO::getCreateTime);
        
        // 如果includeRecycle为false或null，只查询有效短链接的访问日志
        if (requestParam.getIncludeRecycle() == null || !requestParam.getIncludeRecycle()) {
            // 使用EXISTS子查询优化性能，避免先查询所有短链接再IN查询
            queryWrapper.exists("SELECT 1 FROM t_link l WHERE l.gid = {0} AND l.full_short_url = {1} AND l.del_flag = 0 AND l.enable_status = 1", 
                    requestParam.getGid(), "t_link_access_logs.full_short_url");
        }
        
        // MyBatis-Plus逻辑删除会自动添加delFlag条件，无需手动添加
        IPage<LinkAccessLogsDO> linkAccessLogsDOIPage = linkAccessLogsMapper.selectPage(requestParam, queryWrapper);
        IPage<ShortLinkStatsAccessRecordRespDTO> actualResult = linkAccessLogsDOIPage.convert(each -> {
            ShortLinkStatsAccessRecordRespDTO dto = BeanUtil.toBean(each, ShortLinkStatsAccessRecordRespDTO.class);
            // 设置短链接
            dto.setFullShortUrl(each.getFullShortUrl());
            // 获取短链接描述信息
            LinkDO linkDO = linkMapper.selectOne(Wrappers.lambdaQuery(LinkDO.class)
                    .eq(LinkDO::getGid, each.getGid())
                    .eq(LinkDO::getFullShortUrl, each.getFullShortUrl())
                    .eq(LinkDO::getDelFlag, 0)
                    .select(LinkDO::getDescribe, LinkDO::getOriginUrl));
            if (linkDO != null) {
                dto.setDescribe(linkDO.getDescribe());
                dto.setOriginUrl(linkDO.getOriginUrl());
            }
            return dto;
        });
        List<String> userAccessLogsList = actualResult.getRecords().stream()
                .map(ShortLinkStatsAccessRecordRespDTO::getUser)
                .toList();
        SelectGroupUvTypeByUserReqDTO selectUvTypeByUserReqDTO = BeanUtil.toBean(requestParam, SelectGroupUvTypeByUserReqDTO.class);
        selectUvTypeByUserReqDTO.setUserAccessLogsList(userAccessLogsList);
        List<Map<String, Object>> uvTypeList = shortLinkUvStatsService.getGroupUvTypeByUser(selectUvTypeByUserReqDTO);
        actualResult.getRecords().forEach(
                eachResult -> {
                    String uvType = uvTypeList.stream().filter(eachType -> Objects.equals(eachType.get("user"), eachResult.getUser()))
                            .findFirst().map(item -> item.get("uvType").toString())
                            .orElse("旧访客");
                    eachResult.setUvType(uvType);
                }
        );

        return actualResult;
    }
}
