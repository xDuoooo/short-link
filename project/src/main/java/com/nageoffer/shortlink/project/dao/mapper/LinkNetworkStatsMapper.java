package com.nageoffer.shortlink.project.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nageoffer.shortlink.project.dao.entity.LinkBrowserStatsDO;
import com.nageoffer.shortlink.project.dao.entity.LinkNetworkStatsDO;
import com.nageoffer.shortlink.project.dto.req.ShortLinkStatsReqDTO;
import org.apache.ibatis.annotations.Param;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
* @author Duo
* @description 针对表【t_link_network_stats】的数据库操作Mapper
* @createDate 2024-11-26 23:49:41
* @Entity generator.dto/entity.t_link_browser_stats
*/
public interface LinkNetworkStatsMapper extends BaseMapper<LinkNetworkStatsDO> {
    /**
     * 记录基础访问监控数据
     * @param linkNetworkStatsDO
     */
    void shortLinkStats(@RequestParam("linkNetworkStatsDO") LinkNetworkStatsDO linkNetworkStatsDO);


    /**
     * 根据短链接获取指定日期内访问网络监控数据
     */

    List<LinkNetworkStatsDO> listNetworkStatsByShortLink(@Param("requestParam") ShortLinkStatsReqDTO requestParam);
}




