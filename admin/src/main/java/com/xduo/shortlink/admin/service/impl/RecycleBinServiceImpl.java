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

@Service(value = "recycleBinServiceImplByAdmin")
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
        
        // 如果前端没有传递gid参数或者传递了所有分组的标识，则查询所有分组
        if (CollUtil.isEmpty(requestParam.getGidList())) {
            requestParam.setGidList(groupDOS.stream().map(GroupDO::getGid).toList());
        } else {
            // 验证前端传递的gid是否属于当前用户
            List<String> userGidList = groupDOS.stream().map(GroupDO::getGid).toList();
            List<String> validGidList = requestParam.getGidList().stream()
                    .filter(userGidList::contains)
                    .toList();
            if (CollUtil.isEmpty(validGidList)) {
                throw new ClientException("没有权限访问指定的分组");
            }
            requestParam.setGidList(validGidList);
        }
        
        return shortLinkActualRemoteService.pageRecycleBinShortLink(requestParam);
    }
}
