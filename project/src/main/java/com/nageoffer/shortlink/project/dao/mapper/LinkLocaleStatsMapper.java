package com.nageoffer.shortlink.project.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nageoffer.shortlink.project.dao.entity.LinkLocaleStatsDO;
import com.nageoffer.shortlink.project.dto.req.ShortLinkStatsReqDTO;
import com.nageoffer.shortlink.project.dto.resp.ShortLinkStatsLocaleCNRespDTO;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
* @author Duo
* @description 针对表【t_link_locale_stats】的数据库操作Mapper
* @createDate 2024-11-26 20:12:59
* @Entity generator.dto/entity.t_link_locale_stats
*/
public interface LinkLocaleStatsMapper extends BaseMapper<LinkLocaleStatsDO> {

    /**
     * 记录地区访问监控数据
     * @param linkLocaleStatsDO
     */
    void shortLinkStats(@RequestParam("linkBrowserStatsDO") LinkLocaleStatsDO linkLocaleStatsDO);


    List<ShortLinkStatsLocaleCNRespDTO> getOneLinkLocaleCntBetweenDateGroupByProvince(ShortLinkStatsReqDTO shortLinkStatsReqDTO);
}




