package com.nageoffer.shortlink.admin.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nageoffer.shortlink.admin.common.biz.user.UserContext;
import com.nageoffer.shortlink.admin.common.convention.exception.ClientException;
import com.nageoffer.shortlink.admin.common.convention.result.Result;
import com.nageoffer.shortlink.admin.common.database.BaseDO;
import com.nageoffer.shortlink.admin.dao.entity.GroupDO;
import com.nageoffer.shortlink.admin.dao.mapper.GroupMapper;
import com.nageoffer.shortlink.admin.dto.req.GroupOrderReqDTO;
import com.nageoffer.shortlink.admin.dto.req.GroupUpdateReqDTO;
import com.nageoffer.shortlink.admin.dto.resp.GroupRespDTO;
import com.nageoffer.shortlink.admin.remote.dto.ShortLinkRemoteService;
import com.nageoffer.shortlink.admin.remote.dto.resp.ShortLinkGroupCountQueryRespDTO;
import com.nageoffer.shortlink.admin.service.GroupService;
import com.nageoffer.shortlink.admin.util.RandomIncludeUpperAndLowerAndNumberUtil;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.nageoffer.shortlink.admin.common.constant.RedisCacheConstants.LOCK_GROUP_CREATE_KEY;

/**
* @author Duo
* @description 针对表【t_group】的数据库操作Service实现
* @createDate 2024-11-19 23:53:31
*/

@Slf4j
@Service
@RequiredArgsConstructor
public class GroupServiceImpl extends ServiceImpl<GroupMapper, GroupDO>
    implements GroupService {

    private final RedissonClient redissonClient;

    @Value("${short-link.group.max-num}")
    private Integer groupMaxNum;


    /**
     * TODO 后续重构为 SpringCloud Feign 调用
     */
    ShortLinkRemoteService shortLinkRemoteService = new ShortLinkRemoteService() {
    };


    @Override
    public void saveGroup(String username, String groupName) {
        RLock lock = redissonClient.getLock(String.format(LOCK_GROUP_CREATE_KEY, username));
        lock.lock();
        try {
            LambdaQueryWrapper<GroupDO> queryWrapper = Wrappers.lambdaQuery(GroupDO.class)
                    .eq(GroupDO::getUsername, username)
                    .eq(GroupDO::getDelFlag, 0);
            List<GroupDO> groupDOList = baseMapper.selectList(queryWrapper);
            if (CollUtil.isNotEmpty(groupDOList) && groupDOList.size() == groupMaxNum) {
                throw new ClientException(String.format("已超出最大分组数：%d", groupMaxNum));
            }
            String gid;
            do {
                gid = RandomIncludeUpperAndLowerAndNumberUtil.generate(6);
            } while (hasGid(username, gid));
            GroupDO groupDO = GroupDO.builder()
                    .gid(gid)
                    .sortOrder(0)
                    .username(username)
                    .name(groupName)
                    .build();
            baseMapper.insert(groupDO);
        } finally {
            lock.unlock();
        }
    }
    @Override
    public void saveGroup(String groupName) {
        this.saveGroup(UserContext.getUsername(),groupName);
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
    //可以有多个相同gid对应不同username
    private boolean hasGid(String username,String gid){
        LambdaQueryWrapper<GroupDO> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(GroupDO::getGid,gid).eq(GroupDO::getUsername, Optional.ofNullable(username).orElse(UserContext.getUsername()));
        GroupDO groupDO = baseMapper.selectOne(lambdaQueryWrapper);

        if (groupDO!=null){
            return true;
        }
        return false;

    }
}




