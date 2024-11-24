package com.nageoffer.shortlink.project.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.nageoffer.shortlink.project.dao.entity.LinkDO;
import com.nageoffer.shortlink.project.dto.req.*;
import com.nageoffer.shortlink.project.dto.resp.ShortLinkPageRespDTO;

/**
 * 回收站管理接口
 */
public interface RecycleBinService extends IService<LinkDO> {
    /**
     * 保存到回收站
     * @param recycleBinSaveReqDTO
     */
    void saveRecycleBin(RecycleBinSaveReqDTO recycleBinSaveReqDTO);

    /**
     * 查询回收站里的短链接
     * @param shortLinkPageReqDTO
     * @return
     */
    IPage<ShortLinkPageRespDTO> pageShortLink(ShortLinkRecycleBinPageReqDTO shortLinkPageReqDTO);

    /**
     * 回收站恢复某一短链接
     * @param recycleBinRecoverReqDTO
     */
    void recoverRecycleBin(RecycleBinRecoverReqDTO recycleBinRecoverReqDTO);

    /**
     * 彻底删除回收站接口
     * @param recycleBinRemoveReqDTO
     */
    void removeRecycleBin(RecycleBinRemoveReqDTO recycleBinRemoveReqDTO);
}
