package com.nageoffer.shortlink.project.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nageoffer.shortlink.project.dao.entity.LinkStatsTodayDO;
import org.apache.ibatis.annotations.Param;

/**
* @author Duo
* @description 针对表【t_link_stats_today】的数据库操作Mapper
* @createDate 2024-11-29 13:34:38
* @Entity generator.dto/entity.t_link_stats_today
*/
public interface LinkStatsTodayMapper extends BaseMapper<LinkStatsTodayDO> {

    void shortLinkTodayState(@Param("param") LinkStatsTodayDO linkStatsTodayDO);
}
