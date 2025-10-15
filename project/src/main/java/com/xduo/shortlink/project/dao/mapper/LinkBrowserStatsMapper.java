package com.xduo.shortlink.project.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xduo.shortlink.project.dao.entity.LinkBrowserStatsDO;
import com.xduo.shortlink.project.dto.req.ShortLinkGroupStatsReqDTO;
import com.xduo.shortlink.project.dto.req.ShortLinkStatsReqDTO;
import org.apache.ibatis.annotations.Param;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
* @author Duo
* @description 针对表【t_link_browser_stats】的数据库操作Mapper
* @createDate 2024-11-26 23:49:41
* @Entity generator.dto/entity.t_link_browser_stats
*/
public interface LinkBrowserStatsMapper extends BaseMapper<LinkBrowserStatsDO> {
    /**
     * 记录基础访问监控数据
     * @param linkBrowserStatsDO
     */
    void shortLinkStats(@RequestParam("linkBrowserStatsDO") LinkBrowserStatsDO linkBrowserStatsDO);

    /**
     * 统计单个短链接的浏览器访问量
     * @param requestParam
     * @return
     */
    List<LinkBrowserStatsDO> listBrowserStatsByShortLink(@Param("requestParam") ShortLinkStatsReqDTO requestParam);

    /**
     * 统计单个短链接的浏览器访问量 - 优化版本（带username过滤）
     * @param requestParam
     * @param username
     * @return
     */
    List<LinkBrowserStatsDO> listBrowserStatsByShortLinkOptimized(@Param("requestParam") ShortLinkStatsReqDTO requestParam, @Param("username") String username);

    /**
     * 统计一组短链接的浏览器访问量
     * @param shortLinkGroupStatsReqDTO
     * @return
     */
    List<LinkBrowserStatsDO> listGroupBrowserStatsByShortLink(@Param("requestParam") ShortLinkGroupStatsReqDTO shortLinkGroupStatsReqDTO);
}




