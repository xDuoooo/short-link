package com.nageoffer.shortlink.project.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nageoffer.shortlink.project.dao.entity.LinkAccessStatsDO;
import com.nageoffer.shortlink.project.dao.entity.LinkBrowserStats;
import org.springframework.web.bind.annotation.RequestParam;

/**
* @author Duo
* @description 针对表【t_link_browser_stats】的数据库操作Mapper
* @createDate 2024-11-26 23:49:41
* @Entity generator.dto/entity.t_link_browser_stats
*/
public interface LinkBrowserStatsMapper extends BaseMapper<LinkBrowserStats> {
    /**
     * 记录基础访问监控数据
     * @param linkBrowserStats
     */
    void shortLinkStats(@RequestParam("linkAccessStats") LinkBrowserStats linkBrowserStats);
}




