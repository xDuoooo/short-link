package com.nageoffer.shortlink.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.nageoffer.shortlink.admin.dao.entity.GroupDO;
import com.nageoffer.shortlink.admin.dto.req.GroupUpdateReqDTO;
import com.nageoffer.shortlink.admin.dto.resp.GroupRespDTO;

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
}
