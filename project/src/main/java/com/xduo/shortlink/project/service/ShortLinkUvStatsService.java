package com.xduo.shortlink.project.service;

import com.xduo.shortlink.project.dto.req.SelectGroupUvTypeByUserReqDTO;
import com.xduo.shortlink.project.dto.req.SelectUvTypeByUserReqDTO;
import com.xduo.shortlink.project.dto.req.ShortLinkGroupStatsReqDTO;
import com.xduo.shortlink.project.dto.req.ShortLinkStatsReqDTO;

import java.util.List;
import java.util.Map;

/**
 * 短链接UV统计服务接口
 * 将复杂的SQL逻辑移到应用层处理，避免ShardingSphere分表路由问题
 */
public interface ShortLinkUvStatsService {

    /**
     * 获取短链接的新老访客数量统计
     */
    Map<String, Object> getUvTypeCntByShortLink(ShortLinkStatsReqDTO requestParam);

    /**
     * 获取短链接的用户UV类型详情
     */
    List<Map<String, Object>> getUvTypeByUser(SelectUvTypeByUserReqDTO requestParam);

    /**
     * 获取分组的新老访客数量统计
     */
    Map<String, Object> getGroupUvTypeCntByShortLink(ShortLinkGroupStatsReqDTO requestParam);

    /**
     * 获取分组的用户UV类型详情
     */
    List<Map<String, Object>> getGroupUvTypeByUser(SelectGroupUvTypeByUserReqDTO requestParam);
}
