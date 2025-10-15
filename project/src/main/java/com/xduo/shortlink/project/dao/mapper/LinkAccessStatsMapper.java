package com.xduo.shortlink.project.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xduo.shortlink.project.dao.entity.LinkAccessStatsDO;
import com.xduo.shortlink.project.dto.req.ShortLinkGroupStatsReqDTO;
import com.xduo.shortlink.project.dto.req.ShortLinkStatsReqDTO;
import org.apache.ibatis.annotations.Param;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
* @author Duo
* @description 针对表【t_link_access_stats】的数据库操作Mapper
* @createDate 2024-11-25 17:28:38
* @Entity generator.dto/entity.t_link_access_stats
*/
public interface LinkAccessStatsMapper extends BaseMapper<LinkAccessStatsDO> {

    /**
     * 记录基础访问监控数据
     * @param linkAccessStatsDO
     */
    void shortLinkStats(@RequestParam("linkBrowserStatsDO") LinkAccessStatsDO linkAccessStatsDO);

    /**
     * 获取指定日期的基础数据
     * @param shortLinkStatsReqDTO
     * @return
     */
    List<LinkAccessStatsDO> getOneLinkBaseDateBetweenDate(ShortLinkStatsReqDTO shortLinkStatsReqDTO);

    /**
     * 获取指定日期的基础数据（优化版本，支持username路由）
     * @param shortLinkStatsReqDTO
     * @param username
     * @return
     */
    List<LinkAccessStatsDO> getOneLinkBaseDateBetweenDateOptimized(ShortLinkStatsReqDTO shortLinkStatsReqDTO, @Param("username") String username);
    /**
     * 获取一组短链接指定日期的基础数据
     * @param shortLinkGroupStatsReqDTO
     * @return
     */
    List<LinkAccessStatsDO> getGroupLinkBaseDateBetweenDate(ShortLinkGroupStatsReqDTO shortLinkGroupStatsReqDTO);

    /**
     * 根据短链接获取指定日期内小时基础监控数据
     */
    List<LinkAccessStatsDO> listHourStatsByShortLink(ShortLinkStatsReqDTO shortLinkStatsReqDTO);

    /**
     * 根据短链接获取指定日期内小时基础监控数据（优化版本）
     */
    List<LinkAccessStatsDO> listHourStatsByShortLinkOptimized(ShortLinkStatsReqDTO shortLinkStatsReqDTO, @Param("username") String username);

    /**
     * 根据短链接获取指定日期weekday基础监控数据
     */
    List<LinkAccessStatsDO> listWeekdayStatsByShortLink(ShortLinkStatsReqDTO requestParam);

    /**
     * 根据短链接获取指定日期weekday基础监控数据（优化版本）
     */
    List<LinkAccessStatsDO> listWeekdayStatsByShortLinkOptimized(ShortLinkStatsReqDTO requestParam, @Param("username") String username);
    /**
     * 根据一组短链接获取指定日期内小时基础监控数据
     */
    List<LinkAccessStatsDO> listGroupHourStatsByShortLink(ShortLinkGroupStatsReqDTO shortLinkGroupStatsReqDTO);
    /**
     * 根据一组短链接获取指定日期weekday基础监控数据
     */
    List<LinkAccessStatsDO> listGroupWeekdayStatsByShortLink(ShortLinkGroupStatsReqDTO shortLinkGroupStatsReqDTO);
}




