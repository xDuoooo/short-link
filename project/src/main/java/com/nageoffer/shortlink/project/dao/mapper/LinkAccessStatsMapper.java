package com.nageoffer.shortlink.project.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nageoffer.shortlink.project.dao.entity.LinkAccessStatsDO;
import org.springframework.web.bind.annotation.RequestParam;

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
    void shortLinkStats(@RequestParam("linkAccessStats") LinkAccessStatsDO linkAccessStatsDO);
}




