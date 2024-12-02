package com.xduo.shortlink.project.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xduo.shortlink.project.dao.entity.LinkAccessLogsDO;
import com.xduo.shortlink.project.dao.mapper.LinkAccessLogsMapper;
import com.xduo.shortlink.project.service.LinkAccessLogsService;
import org.springframework.stereotype.Service;

/**
* @author Duo
* @description 针对表【t_link_access_logs】的数据库操作Service实现
* @createDate 2024-11-27 22:39:35
*/
@Service
public class LinkAccessLogsServiceImpl extends ServiceImpl<LinkAccessLogsMapper, LinkAccessLogsDO>
    implements LinkAccessLogsService {
}




