package com.xduo.shortlink.project.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xduo.shortlink.project.common.convention.result.Result;
import com.xduo.shortlink.project.common.convention.result.Results;
import com.xduo.shortlink.project.dto.req.ShortLinkGroupStatsAccessRecordReqDTO;
import com.xduo.shortlink.project.dto.req.ShortLinkGroupStatsReqDTO;
import com.xduo.shortlink.project.dto.req.ShortLinkStatsAccessRecordReqDTO;
import com.xduo.shortlink.project.dto.req.ShortLinkStatsReqDTO;
import com.xduo.shortlink.project.dto.resp.ShortLinkStatsAccessRecordRespDTO;
import com.xduo.shortlink.project.dto.resp.ShortLinkStatsRespDTO;
import com.xduo.shortlink.project.service.ShortLinkStatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ShortLinkStatsController {

    private final ShortLinkStatsService shortLinkStatsService;

    /**
     * 获取一个短链接指定日期内监控统计数据
     * @param shortLinkStatsReqDTO
     * @return
     */
    @GetMapping("/api/short-link/v1/stats")
    public Result<ShortLinkStatsRespDTO> getOneShortLinkStats(@RequestBody ShortLinkStatsReqDTO shortLinkStatsReqDTO){
        return Results.success(shortLinkStatsService.getOneShortLinkStats(shortLinkStatsReqDTO));
    }
    /**
     * 获取一组短链接指定日期内监控统计数据
     * @param shortLinkGroupStatsReqDTO
     * @return
     */
    @GetMapping("/api/short-link/admin/v1/stats/group")
    public Result<ShortLinkStatsRespDTO> getGroupShortLinkStats(@RequestBody ShortLinkGroupStatsReqDTO shortLinkGroupStatsReqDTO){
        return Results.success(shortLinkStatsService.getGroupShortLinkStats(shortLinkGroupStatsReqDTO));
    }

    /**
     * 访问单个短链接指定时间内访问记录监控数据
     */
    @GetMapping("/api/short-link/v1/stats/access-record")
    public Result<IPage<ShortLinkStatsAccessRecordRespDTO>> shortLinkStatsAccessRecord(ShortLinkStatsAccessRecordReqDTO requestParam) {
        return Results.success(shortLinkStatsService.shortLinkStatsAccessRecord(requestParam));
    }
    /**
     * 访问一组短链接指定时间内访问记录监控数据
     */
    @GetMapping("/api/short-link/v1/stats/access-record/group")
    public Result<IPage<ShortLinkStatsAccessRecordRespDTO>> shortLinkGroupStatsAccessRecord(ShortLinkGroupStatsAccessRecordReqDTO requestParam) {
        return Results.success(shortLinkStatsService.shortLinkGroupStatsAccessRecord(requestParam));
    }
}
