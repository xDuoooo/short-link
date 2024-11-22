package com.nageoffer.shortlink.project.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nageoffer.shortlink.project.dao.entity.LinkDO;
import com.nageoffer.shortlink.project.dto.resp.ShortLinkGroupCountQueryRespDTO;

import java.util.List;

/**
* @author Duo
* @description 针对表【t_link】的数据库操作Mapper
* @createDate 2024-11-21 09:54:27
* @Entity generator.dto/entity.TLink
*/

public interface LinkMapper extends BaseMapper<LinkDO> {

    List<ShortLinkGroupCountQueryRespDTO> listGroupShortLinkCount(List<String> gids);

}




