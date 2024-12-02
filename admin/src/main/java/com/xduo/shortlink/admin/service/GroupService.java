package com.xduo.shortlink.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xduo.shortlink.admin.dao.entity.GroupDO;
import com.xduo.shortlink.admin.dto.req.GroupOrderReqDTO;
import com.xduo.shortlink.admin.dto.req.GroupUpdateReqDTO;
import com.xduo.shortlink.admin.dto.resp.GroupRespDTO;

import java.util.List;

/**
* @author Duo
* @description 针对表【t_group】的数据库操作Service
* @createDate 2024-11-19 23:53:31
*/
public interface GroupService extends IService<GroupDO> {
    /**
     * 新增短链接分组接口
     * @param groupName
     */
    void saveGroup(String groupName);

    void saveGroup(String username, String groupName);

    /**
     * 获取当前用户的短链接分组
     * @return
     */
    List<GroupRespDTO> listGroups();

    /**
     * 短链接分组修改
     * @param groupUpdateReqDTO
     */
    void updateGroup(GroupUpdateReqDTO groupUpdateReqDTO);

    void deleteGroup(String gid);

    void orderGroup(List<GroupOrderReqDTO> groupOrderReqDTOS);
}
