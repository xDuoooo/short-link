package com.xduo.shortlink.project.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xduo.shortlink.project.dao.entity.LinkDeviceStatsDO;
import com.xduo.shortlink.project.dto.req.ShortLinkGroupStatsReqDTO;
import com.xduo.shortlink.project.dto.req.ShortLinkStatsReqDTO;
import org.apache.ibatis.annotations.Param;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
* @author Duo
* @description 针对表【t_link_network_stats】的数据库操作Mapper
* @createDate 2024-11-26 23:49:41
* @Entity generator.dto/entity.t_link_browser_stats
*/
public interface LinkDeviceStatsMapper extends BaseMapper<LinkDeviceStatsDO> {
    /**
     * 记录基础访问监控数据
     * @param linkDeviceStatsDO
     */
    void shortLinkStats(@RequestParam("linkDeviceStatsDO") LinkDeviceStatsDO linkDeviceStatsDO);

    List<LinkDeviceStatsDO> listDeviceStatsByShortLink(@Param("requestParam") ShortLinkStatsReqDTO shortLinkStatsReqDTO);

    List<LinkDeviceStatsDO> listDeviceStatsByShortLinkOptimized(@Param("requestParam") ShortLinkStatsReqDTO shortLinkStatsReqDTO, @Param("username") String username);

    List<LinkDeviceStatsDO> listGroupDeviceStatsByShortLink(@Param("requestParam") ShortLinkGroupStatsReqDTO shortLinkGroupStatsReqDTO);
}




