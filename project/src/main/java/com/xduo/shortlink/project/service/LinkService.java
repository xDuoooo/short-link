package com.xduo.shortlink.project.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xduo.shortlink.project.dao.entity.LinkDO;
import com.xduo.shortlink.project.dto.req.*;
import com.xduo.shortlink.project.dto.resp.ShortLinkBatchCreateRespDTO;
import com.xduo.shortlink.project.dto.resp.ShortLinkCreateRespDTO;
import com.xduo.shortlink.project.dto.resp.ShortLinkGroupCountQueryRespDTO;
import com.xduo.shortlink.project.dto.resp.ShortLinkPageRespDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.List;

/**
* @author Duo
* @description 针对表【t_link】的数据库操作Service
* @createDate 2024-11-21 09:54:27
*/
public interface LinkService extends IService<LinkDO> {


    /**
     * 串讲短链接接口
     *
     * @return 短链接创建纤细
     */
    ShortLinkCreateRespDTO createShortLink(ShortLinkCreateReqDTO shortLinkCreateReqDTO);

    /**
     * 分页查询短链接
     *
     * @param shortLinkPageReqDTO
     * @return
     */
    IPage<ShortLinkPageRespDTO> pageShortLink(ShortLinkPageReqDTO shortLinkPageReqDTO);

    /**
     * 获取分组下短链接数量接口
     *
     * @param gids
     * @return
     */
    List<ShortLinkGroupCountQueryRespDTO> listGroupShortLinkCount(List<String> gids);

    /**
     * 修改对岸链接接口
     *
     * @param requestParam
     */
    void updateShortLink(ShortLinkUpdateReqDTO requestParam);

    /**
     * 短链接跳转
     *
     * @param shortUri
     * @param request
     * @param response
     */
    void restoreUrl(String shortUri, HttpServletRequest request, HttpServletResponse response);

    /**
     * 批量创建短链接
     * @param requestParam
     * @return
     */
    ShortLinkBatchCreateRespDTO batchCreateShortLink(ShortLinkBatchCreateReqDTO requestParam);

    /**
     * 短链接统计
     *
     * @param fullShortUrl         完整短链接
     * @param gid                  分组标识
     * @param shortLinkStatsRecord 短链接统计实体参数
     */
    void shortLinkStats(String fullShortUrl, String gid, ShortLinkStatsRecordDTO shortLinkStatsRecord);

}