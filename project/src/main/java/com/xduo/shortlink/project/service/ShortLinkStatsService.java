package com.xduo.shortlink.project.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xduo.shortlink.project.dto.req.ShortLinkGroupStatsAccessRecordReqDTO;
import com.xduo.shortlink.project.dto.req.ShortLinkGroupStatsReqDTO;
import com.xduo.shortlink.project.dto.req.ShortLinkStatsAccessRecordReqDTO;
import com.xduo.shortlink.project.dto.req.ShortLinkStatsReqDTO;
import com.xduo.shortlink.project.dto.resp.ShortLinkStatsAccessRecordRespDTO;
import com.xduo.shortlink.project.dto.resp.ShortLinkStatsRespDTO;

public interface ShortLinkStatsService {
    /**
     * 查询单个短链接数据统计接口
     * @param shortLinkStatsReqDTO
     * @return
     */
    ShortLinkStatsRespDTO getOneShortLinkStats(ShortLinkStatsReqDTO shortLinkStatsReqDTO);

    /**
     * 查询一组短链接数据统计接口
     * @param shortLinkGroupStatsReqDTO
     * @return
     */
    ShortLinkStatsRespDTO getGroupShortLinkStats(ShortLinkGroupStatsReqDTO shortLinkGroupStatsReqDTO);

    /**
     * 访问单个短链接指定时间内访问记录监控数据
     *
     * @param requestParam 获取短链接监控访问记录数据入参
     * @return 访问记录监控数据
     */
    IPage<ShortLinkStatsAccessRecordRespDTO> shortLinkStatsAccessRecord(ShortLinkStatsAccessRecordReqDTO requestParam);
    /**
     * 访问一组短链接指定时间内访问记录监控数据
     *
     * @param requestParam 获取短链接监控访问记录数据入参
     * @return 访问记录监控数据
     */
    IPage<ShortLinkStatsAccessRecordRespDTO> shortLinkGroupStatsAccessRecord(ShortLinkGroupStatsAccessRecordReqDTO requestParam);
}
