package com.nageoffer.shortlink.project.util;

import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import com.nageoffer.shortlink.project.common.constant.ShortLinkConstant;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Date;
import java.util.Optional;

/**
 * 短链接工具类
 */
public class LinkUtil {
    /**
     * 获取短链接缓存有效期时间
     * @param validDate 有效期时间
     * @return 有效期时间戳 单位 毫秒
     */
    public static long getLinkCacheValidDate(Date validDate){
        return Optional.ofNullable(validDate).map(each -> DateUtil.between(new Date(),each, DateUnit.MS)).orElse(ShortLinkConstant.DEFAULT_CACHE_TIMEOUT);
    }
    /**
     * 获取请求的 IP 地址
     * @param request
     * @return
     */
    public static String getIp(HttpServletRequest request) {
        String ipAddress = request.getHeader("X-Forwarded-For");

        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getRemoteAddr();
        }

        return ipAddress;
    }
}
