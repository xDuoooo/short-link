package com.xduo.shortlink.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xduo.shortlink.admin.common.biz.user.UserContext;
import com.xduo.shortlink.admin.common.convention.result.Result;
import com.xduo.shortlink.admin.remote.dto.ShortLinkActualRemoteService;
import com.xduo.shortlink.admin.remote.dto.req.ShortLinkGroupStatsAccessRecordReqDTO;
import com.xduo.shortlink.admin.remote.dto.req.ShortLinkGroupStatsReqDTO;
import com.xduo.shortlink.admin.remote.dto.req.ShortLinkStatsAccessRecordReqDTO;
import com.xduo.shortlink.admin.remote.dto.req.ShortLinkStatsReqDTO;
import com.xduo.shortlink.admin.remote.dto.resp.ShortLinkStatsAccessRecordRespDTO;
import com.xduo.shortlink.admin.remote.dto.resp.ShortLinkStatsRespDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController(value = "shortLinkStatsControllerByAdmin")
@RequiredArgsConstructor
public class ShortLinkStatsController {

    private final ShortLinkActualRemoteService shortLinkActualRemoteService;

    /**
     * 访问单个短链接指定时间内监控数据
     */
    @GetMapping("/api/short-link/admin/v1/stats")
    public Result<ShortLinkStatsRespDTO> shortLinkStats(ShortLinkStatsReqDTO requestParam) {
        // 从UserContext获取username并设置到DTO中
        String username = UserContext.getUsername();
        if (username != null) {
            requestParam.setUsername(username);
        }
        return shortLinkActualRemoteService.oneShortLinkStats(requestParam);
    }

    /**
     * 访问分组短链接指定时间内监控数据
     */
    @GetMapping("/api/short-link/admin/v1/stats/group")
    public Result<ShortLinkStatsRespDTO> groupShortLinkStats(ShortLinkGroupStatsReqDTO requestParam) {
        // 从UserContext获取username并设置到DTO中
        String username = UserContext.getUsername();
        if (username != null) {
            requestParam.setUsername(username);
        }
        return shortLinkActualRemoteService.groupShortLinkStats(requestParam);
    }

    /**
     * 访问单个短链接指定时间内访问记录监控数据
     */
    @GetMapping("/api/short-link/admin/v1/stats/access-record")
    public Result<Page<ShortLinkStatsAccessRecordRespDTO>> shortLinkStatsAccessRecord(ShortLinkStatsAccessRecordReqDTO requestParam) {
        // 从UserContext获取username并设置到DTO中
        String username = UserContext.getUsername();
        if (username != null) {
            requestParam.setUsername(username);
        }
        return shortLinkActualRemoteService.shortLinkStatsAccessRecord(requestParam);
    }

    /**
     * 访问分组短链接指定时间内访问记录监控数据
     */
    @GetMapping("/api/short-link/admin/v1/stats/access-record/group")
    public Result<Page<ShortLinkStatsAccessRecordRespDTO>> groupShortLinkStatsAccessRecord(ShortLinkGroupStatsAccessRecordReqDTO requestParam) {
        // 从UserContext获取username并设置到DTO中
        String username = UserContext.getUsername();
        if (username != null) {
            requestParam.setUsername(username);
        }
        return shortLinkActualRemoteService.groupShortLinkStatsAccessRecord(requestParam);
    }
}