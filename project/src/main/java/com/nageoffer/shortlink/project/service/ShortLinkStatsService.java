package com.nageoffer.shortlink.project.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.nageoffer.shortlink.project.dto.req.ShortLinkGroupStatsReqDTO;
import com.nageoffer.shortlink.project.dto.req.ShortLinkStatsAccessRecordReqDTO;
import com.nageoffer.shortlink.project.dto.req.ShortLinkStatsReqDTO;
import com.nageoffer.shortlink.project.dto.resp.ShortLinkStatsAccessRecordRespDTO;
import com.nageoffer.shortlink.project.dto.resp.ShortLinkStatsRespDTO;

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

}
