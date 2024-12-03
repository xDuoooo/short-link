package com.xduo.shortlink.admin.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xduo.shortlink.admin.common.biz.user.UserContext;
import com.xduo.shortlink.admin.common.convention.exception.ClientException;
import com.xduo.shortlink.admin.common.convention.result.Result;
import com.xduo.shortlink.admin.dao.entity.GroupDO;
import com.xduo.shortlink.admin.dao.mapper.GroupMapper;
import com.xduo.shortlink.admin.remote.dto.ShortLinkActualRemoteService;
import com.xduo.shortlink.admin.remote.dto.req.ShortLinkRecycleBinPageReqDTO;
import com.xduo.shortlink.admin.remote.dto.resp.ShortLinkPageRespDTO;
import com.xduo.shortlink.admin.service.RecycleBinService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RecycleBinServiceImpl implements RecycleBinService {

    private final GroupMapper groupMapper;

    private final ShortLinkActualRemoteService shortLinkActualRemoteService;


    @Override
    public Result<Page<ShortLinkPageRespDTO>> recycleBinPageShortLink(ShortLinkRecycleBinPageReqDTO requestParam) {
        LambdaQueryWrapper<GroupDO> lambdaQueryWrapper = Wrappers.lambdaQuery(GroupDO.class).eq(GroupDO::getUsername, UserContext.getUsername());
        List<GroupDO> groupDOS = groupMapper.selectList(lambdaQueryWrapper);
        if(CollUtil.isEmpty(groupDOS)){
            throw new ClientException("当前用户没有分组");
        }
        requestParam.setGidList(groupDOS.stream().map(GroupDO::getGid).toList());
        return shortLinkActualRemoteService.pageRecycleBinShortLink(requestParam);
    }
}
