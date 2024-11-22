package com.nageoffer.shortlink.admin.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nageoffer.shortlink.admin.common.biz.user.UserContext;
import com.nageoffer.shortlink.admin.common.convention.result.Result;
import com.nageoffer.shortlink.admin.common.database.BaseDO;
import com.nageoffer.shortlink.admin.dao.entity.GroupDO;
import com.nageoffer.shortlink.admin.dao.mapper.GroupMapper;
import com.nageoffer.shortlink.admin.dto.req.GroupOrderReqDTO;
import com.nageoffer.shortlink.admin.dto.req.GroupUpdateReqDTO;
import com.nageoffer.shortlink.admin.dto.resp.GroupRespDTO;
import com.nageoffer.shortlink.admin.remote.dto.ShortLinkRemoteService;
import com.nageoffer.shortlink.admin.remote.dto.resp.ShortLinkGroupCountQueryRespDTO;
import com.nageoffer.shortlink.admin.remote.dto.resp.ShortLinkPageRespDTO;
import com.nageoffer.shortlink.admin.service.GroupService;
import com.nageoffer.shortlink.admin.util.RandomIncludeUpperAndLowerAndNumberUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
* @author Duo
* @description 针对表【t_group】的数据库操作Service实现
* @createDate 2024-11-19 23:53:31
*/

@Slf4j
@Service
public class GroupServiceImpl extends ServiceImpl<GroupMapper, GroupDO>
    implements GroupService {

    /**
     * TODO 后续重构为 SpringCloud Feign 调用
     */
    ShortLinkRemoteService shortLinkRemoteService = new ShortLinkRemoteService() {
    };


    @Override
    public void saveGroup(String groupName) {
        String gid;
        //Gid存在
        do {
            gid = RandomIncludeUpperAndLowerAndNumberUtil.generate(6);
        } while (hasGid(gid));
        String username = (UserContext.getUsername());
        GroupDO buildDO = GroupDO.builder().gid(gid).sortOrder(0).name(groupName).username(UserContext.getUsername()).build();
        baseMapper.insert(buildDO);

    }

    @Override
    public List<GroupRespDTO> listGroups() {
        LambdaQueryWrapper<GroupDO> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(GroupDO::getUsername,UserContext.getUsername())
                .eq(BaseDO::getDelFlag,0)
                .orderByDesc(GroupDO::getSortOrder,BaseDO::getUpdateTime);
        List<GroupDO> groupDOs = baseMapper.selectList(lambdaQueryWrapper);
        Result<List<ShortLinkGroupCountQueryRespDTO>> gidsGroups = shortLinkRemoteService.listGroupShortLinkCount(groupDOs.stream().map(GroupDO::getGid).toList());
        List<ShortLinkGroupCountQueryRespDTO> data = gidsGroups.getData();
        //key: gid  value: 数量
        Map<String, Integer> countGroupMap = data.stream()
                .collect(Collectors.toMap(ShortLinkGroupCountQueryRespDTO::getGid, ShortLinkGroupCountQueryRespDTO::getShortLinkCount));
        List<GroupRespDTO> groupRespDTOS = BeanUtil.copyToList(groupDOs, GroupRespDTO.class);
        for (GroupRespDTO groupRespDTO : groupRespDTOS) {
            groupRespDTO.setShortLinkCount(countGroupMap.get(groupRespDTO.getGid()));
        }
        return groupRespDTOS;
    }

    @Override
    public void updateGroup(GroupUpdateReqDTO groupUpdateReqDTO) {
        LambdaUpdateWrapper<GroupDO> eq = Wrappers.lambdaUpdate(GroupDO.class)
                .eq(GroupDO::getUsername, UserContext.getUsername())
                .eq(GroupDO::getGid, groupUpdateReqDTO.getGid())
                .eq(GroupDO::getDelFlag, 0);
        GroupDO groupDO = new GroupDO();
        groupDO.setName(groupUpdateReqDTO.getName());
        baseMapper.update(groupDO,eq);
    }

    @Override
    public void deleteGroup(String gid) {
        LambdaUpdateWrapper<GroupDO> eq = Wrappers.lambdaUpdate(GroupDO.class)
                .eq(GroupDO::getUsername, UserContext.getUsername())
                .eq(GroupDO::getGid, gid)
                .eq(GroupDO::getDelFlag, 0);
        GroupDO groupDO = new GroupDO();
        groupDO.setDelFlag(1);
        baseMapper.update(groupDO,eq);
    }

    @Override
    public void orderGroup(List<GroupOrderReqDTO> groupOrderReqDTOS) {
        for (GroupOrderReqDTO groupOrderReqDTO : groupOrderReqDTOS) {
            LambdaUpdateWrapper<GroupDO> lambdaUpdateWrapper = new LambdaUpdateWrapper<>();
            lambdaUpdateWrapper.eq(GroupDO::getGid,groupOrderReqDTO.getGid()).eq(GroupDO::getUsername,UserContext.getUsername()).eq(BaseDO::getDelFlag,0);
            GroupDO groupDO = GroupDO.builder().sortOrder(groupOrderReqDTO.getSortOrder()).build();
            update(groupDO,lambdaUpdateWrapper);
        }

    }

    private boolean hasGid(String gid){
        LambdaQueryWrapper<GroupDO> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        // TODO 网关
        lambdaQueryWrapper.eq(GroupDO::getGid,gid);
        GroupDO groupDO = baseMapper.selectOne(lambdaQueryWrapper);

        if (groupDO!=null){
            return true;
        }
        return false;

    }
}




