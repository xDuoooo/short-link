package com.xduo.shortlink.project.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xduo.shortlink.project.dao.entity.LinkStatsTodayDO;
import com.xduo.shortlink.project.dao.mapper.LinkStatsTodayMapper;
import com.xduo.shortlink.project.service.LinkStatsTodayService;
import org.springframework.stereotype.Service;


@Service
public class LinkStatsTodayServiceImpl extends ServiceImpl<LinkStatsTodayMapper, LinkStatsTodayDO> implements LinkStatsTodayService {
}