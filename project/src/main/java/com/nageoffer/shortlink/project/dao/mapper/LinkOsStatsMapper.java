package com.nageoffer.shortlink.project.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nageoffer.shortlink.project.dao.entity.LinkOsStatsDO;
import org.springframework.web.bind.annotation.RequestParam;

/**
* @author Duo
* @description 针对表【t_link_os_stats(短链接监控操作系统访问状态)】的数据库操作Mapper
* @createDate 2024-11-26 23:16:42
* @Entity generator.dto/entity.t_link_os_stats
*/
public interface LinkOsStatsMapper extends BaseMapper<LinkOsStatsDO> {
    /**
     * 记录地区访问操作系统数据
     * @param linkOsStatsDO
     */
    void shortLinkStats(@RequestParam("linkBrowserStatsDO") LinkOsStatsDO linkOsStatsDO);

}




