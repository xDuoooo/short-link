package com.nageoffer.shortlink.project.dao.mapper;

import com.nageoffer.shortlink.project.dao.entity.LinkAccessLogsDO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nageoffer.shortlink.project.dto.req.ShortLinkStatsReqDTO;
import org.apache.ibatis.annotations.Param;

import java.util.HashMap;
import java.util.List;

/**
* @author Duo
* @description 针对表【t_link_access_logs】的数据库操作Mapper
* @createDate 2024-11-27 22:39:35
* @Entity generator.dto/entity.t_link_access_logs
*/
public interface LinkAccessLogsMapper extends BaseMapper<LinkAccessLogsDO> {
    /**
     * 根据短链接获取指定日期内高频访问IP数据
     */
    List<HashMap<String, Object>> listTopIpByShortLink(ShortLinkStatsReqDTO requestParam);

    /**
     * 根据短链接获取指定日期内新旧访客数据
     */

    HashMap<String, Object> findUvTypeCntByShortLink(@Param("param") ShortLinkStatsReqDTO param);

}




