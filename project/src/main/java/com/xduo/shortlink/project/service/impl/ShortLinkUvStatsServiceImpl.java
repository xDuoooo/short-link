package com.xduo.shortlink.project.service.impl;

import com.xduo.shortlink.project.dao.mapper.LinkAccessLogsMapper;
import com.xduo.shortlink.project.dto.req.SelectGroupUvTypeByUserReqDTO;
import com.xduo.shortlink.project.dto.req.SelectUvTypeByUserReqDTO;
import com.xduo.shortlink.project.dto.req.ShortLinkGroupStatsReqDTO;
import com.xduo.shortlink.project.dto.req.ShortLinkStatsReqDTO;
import com.xduo.shortlink.project.service.ShortLinkUvStatsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 短链接UV统计服务实现类
 * 将复杂的SQL逻辑移到应用层处理，避免ShardingSphere分表路由问题
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ShortLinkUvStatsServiceImpl implements ShortLinkUvStatsService {

    private final LinkAccessLogsMapper linkAccessLogsMapper;

    @Override
    public Map<String, Object> getUvTypeCntByShortLink(ShortLinkStatsReqDTO requestParam) {
        // 获取用户访问记录
        List<HashMap<String, Object>> userAccessLogs = linkAccessLogsMapper.findUserAccessLogsByShortLink(
                requestParam.getFullShortUrl(),
                requestParam.getGid(),
                requestParam.getStartDate(),
                requestParam.getEndDate()
        );

        // 在应用层计算新老访客数量
        Map<String, Object> result = new HashMap<>();
        int newUserCnt = 0;
        int oldUserCnt = 0;

        for (HashMap<String, Object> logEntry : userAccessLogs) {
            Object firstAccessTimeObj = logEntry.get("first_access_time");
            String firstAccessTime;
            
            // 处理不同的时间类型
            if (firstAccessTimeObj instanceof LocalDateTime) {
                firstAccessTime = ((LocalDateTime) firstAccessTimeObj).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            } else if (firstAccessTimeObj instanceof String) {
                firstAccessTime = (String) firstAccessTimeObj;
            } else {
                log.warn("未知的时间类型: {}", firstAccessTimeObj.getClass());
                continue;
            }
            
            // 判断是否为新访客：首次访问时间在查询时间范围内
            if (isNewUser(firstAccessTime, requestParam.getStartDate(), requestParam.getEndDate())) {
                newUserCnt++;
            } else {
                oldUserCnt++;
            }
        }

        result.put("newUserCnt", newUserCnt);
        result.put("oldUserCnt", oldUserCnt);
        return result;
    }

    @Override
    public List<Map<String, Object>> getUvTypeByUser(SelectUvTypeByUserReqDTO requestParam) {
        // 获取用户访问记录
        List<Map<String, Object>> userAccessLogs = linkAccessLogsMapper.selectUserAccessLogs(requestParam);

        // 在应用层处理UV类型判断
        return userAccessLogs.stream()
                .map(logEntry -> {
                    Map<String, Object> result = new HashMap<>(logEntry);
                    String user = (String) logEntry.get("user");
                    
                    // 处理create_time的类型转换
                    Object createTimeObj = logEntry.get("create_time");
                    String createTime;
                    if (createTimeObj instanceof LocalDateTime) {
                        createTime = ((LocalDateTime) createTimeObj).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                    } else if (createTimeObj instanceof String) {
                        createTime = (String) createTimeObj;
                    } else {
                        log.warn("未知的create_time类型: {}", createTimeObj.getClass());
                        createTime = "";
                    }
                    
                    // 判断UV类型
                    String uvType = determineUvType(user, createTime, requestParam);
                    result.put("uvType", uvType);
                    return result;
                })
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> getGroupUvTypeCntByShortLink(ShortLinkGroupStatsReqDTO requestParam) {
        // 获取分组用户访问记录
        List<HashMap<String, Object>> userAccessLogs = linkAccessLogsMapper.findGroupUserAccessLogs(
                requestParam.getGid(),
                requestParam.getStartDate(),
                requestParam.getEndDate()
        );

        // 在应用层计算新老访客数量
        Map<String, Object> result = new HashMap<>();
        int newUserCnt = 0;
        int oldUserCnt = 0;

        for (HashMap<String, Object> logEntry : userAccessLogs) {
            Object firstAccessTimeObj = logEntry.get("first_access_time");
            String firstAccessTime;
            
            // 处理不同的时间类型
            if (firstAccessTimeObj instanceof LocalDateTime) {
                firstAccessTime = ((LocalDateTime) firstAccessTimeObj).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            } else if (firstAccessTimeObj instanceof String) {
                firstAccessTime = (String) firstAccessTimeObj;
            } else {
                log.warn("未知的时间类型: {}", firstAccessTimeObj.getClass());
                continue;
            }
            
            // 判断是否为新访客
            if (isNewUser(firstAccessTime, requestParam.getStartDate(), requestParam.getEndDate())) {
                newUserCnt++;
            } else {
                oldUserCnt++;
            }
        }

        result.put("newUserCnt", newUserCnt);
        result.put("oldUserCnt", oldUserCnt);
        return result;
    }

    @Override
    public List<Map<String, Object>> getGroupUvTypeByUser(SelectGroupUvTypeByUserReqDTO requestParam) {
        // 获取分组用户访问记录
        List<Map<String, Object>> userAccessLogs = linkAccessLogsMapper.selectGroupUserAccessLogs(requestParam);

        // 在应用层处理UV类型判断
        return userAccessLogs.stream()
                .map(logEntry -> {
                    Map<String, Object> result = new HashMap<>(logEntry);
                    String user = (String) logEntry.get("user");
                    
                    // 处理create_time的类型转换
                    Object createTimeObj = logEntry.get("create_time");
                    String createTime;
                    if (createTimeObj instanceof LocalDateTime) {
                        createTime = ((LocalDateTime) createTimeObj).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                    } else if (createTimeObj instanceof String) {
                        createTime = (String) createTimeObj;
                    } else {
                        log.warn("未知的create_time类型: {}", createTimeObj.getClass());
                        createTime = "";
                    }
                    
                    // 判断UV类型
                    String uvType = determineGroupUvType(user, createTime, requestParam);
                    result.put("uvType", uvType);
                    return result;
                })
                .collect(Collectors.toList());
    }

    /**
     * 判断用户是否为新访客
     */
    private boolean isNewUser(String firstAccessTime, String startDate, String endDate) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            LocalDateTime firstAccess = LocalDateTime.parse(firstAccessTime, formatter);
            LocalDateTime start = LocalDateTime.parse(startDate + " 00:00:00", formatter);
            LocalDateTime end = LocalDateTime.parse(endDate + " 23:59:59", formatter);
            
            return firstAccess.isAfter(start) && firstAccess.isBefore(end);
        } catch (Exception e) {
            log.error("解析时间失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 判断单个用户的UV类型
     */
    private String determineUvType(String user, String createTime, SelectUvTypeByUserReqDTO requestParam) {
        // 这里需要查询该用户的历史首次访问时间
        // 简化处理：如果当前访问时间等于首次访问时间，则为新访客
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            LocalDateTime currentAccess = LocalDateTime.parse(createTime, formatter);
            LocalDateTime start = LocalDateTime.parse(requestParam.getStartDate() + " 00:00:00", formatter);
            
            // 简化逻辑：如果访问时间在查询开始时间之后，认为是新访客
            return currentAccess.isAfter(start) ? "新访客" : "老访客";
        } catch (Exception e) {
            log.error("解析时间失败: {}", e.getMessage());
            return "老访客";
        }
    }

    /**
     * 判断分组用户的UV类型
     */
    private String determineGroupUvType(String user, String createTime, SelectGroupUvTypeByUserReqDTO requestParam) {
        // 类似逻辑，但针对分组
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            LocalDateTime currentAccess = LocalDateTime.parse(createTime, formatter);
            LocalDateTime start = LocalDateTime.parse(requestParam.getStartDate() + " 00:00:00", formatter);
            
            return currentAccess.isAfter(start) ? "新访客" : "老访客";
        } catch (Exception e) {
            log.error("解析时间失败: {}", e.getMessage());
            return "老访客";
        }
    }
}
