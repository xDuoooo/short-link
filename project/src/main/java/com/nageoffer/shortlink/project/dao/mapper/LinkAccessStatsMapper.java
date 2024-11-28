package com.nageoffer.shortlink.project.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nageoffer.shortlink.project.dao.entity.LinkAccessStatsDO;
import com.nageoffer.shortlink.project.dto.req.ShortLinkStatsReqDTO;
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
     * 根据短链接获取指定日期内小时基础监控数据
     */
    List<LinkAccessStatsDO> listHourStatsByShortLink(ShortLinkStatsReqDTO shortLinkStatsReqDTO);

    /**
     * 根据短链接获取指定日期weekday基础监控数据
     */
    List<LinkAccessStatsDO> listWeekdayStatsByShortLink(ShortLinkStatsReqDTO requestParam);
}




