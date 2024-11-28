package com.nageoffer.shortlink.project.service;

import com.nageoffer.shortlink.project.dto.req.ShortLinkStatsReqDTO;
import com.nageoffer.shortlink.project.dto.resp.ShortLinkStatsRespDTO;

public interface ShortLinkStatsService {
    /**
     * 查询单个短链接数据统计接口
     * @param shortLinkStatsReqDTO
     * @return
     */
    ShortLinkStatsRespDTO getOneShortLinkStats(ShortLinkStatsReqDTO shortLinkStatsReqDTO);
}
