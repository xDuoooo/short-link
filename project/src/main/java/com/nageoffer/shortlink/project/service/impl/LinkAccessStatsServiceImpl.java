package com.nageoffer.shortlink.project.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nageoffer.shortlink.project.dao.entity.LinkAccessStatsDO;
import com.nageoffer.shortlink.project.dao.mapper.LinkAccessStatsMapper;
import com.nageoffer.shortlink.project.service.LinkAccessStatsService;

import org.springframework.stereotype.Service;

/**
* @author Duo
* @description 针对表【t_link_access_stats】的数据库操作Service实现
* @createDate 2024-11-25 17:28:38
*/
@Service
public class LinkAccessStatsServiceImpl extends ServiceImpl<LinkAccessStatsMapper, LinkAccessStatsDO>
    implements LinkAccessStatsService {

}




