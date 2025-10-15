package com.xduo.shortlink.project.dao.mapper;

import com.xduo.shortlink.project.dao.entity.LinkAccessLogsDO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xduo.shortlink.project.dto.req.SelectGroupUvTypeByUserReqDTO;
import com.xduo.shortlink.project.dto.req.SelectUvTypeByUserReqDTO;
import com.xduo.shortlink.project.dto.req.ShortLinkGroupStatsReqDTO;
import com.xduo.shortlink.project.dto.req.ShortLinkStatsReqDTO;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Param;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Duo
 * @description 针对表【t_link_access_logs】的数据库操作Mapper
 * @createDate 2024-11-27 22:39:35
 * @Entity generator.dto/entity.t_link_access_logs
 */
public interface LinkAccessLogsMapper extends BaseMapper<LinkAccessLogsDO> {
    /**
     * 根据短链接获取指定日期内高频访问IP数据
     */
    List<HashMap<String, Object>> listTopIpByShortLink(ShortLinkStatsReqDTO requestParam);

    /**
     * 获取指定短链接的用户访问记录（简化查询，用于应用层处理UV类型判断）
     */
    List<HashMap<String, Object>> findUserAccessLogsByShortLink(
            @Param("fullShortUrl") String fullShortUrl,
            @Param("gid") String gid,
            @Param("startDate") String startDate,
            @Param("endDate") String endDate
    );

    /**
     * 获取指定用户的访问记录（简化查询）
     */
    List<Map<String, Object>> selectUserAccessLogs(SelectUvTypeByUserReqDTO selectUvTypeByUserReqDTO);

    /**
     * 根据一组短链接获取指定日期内高频访问IP数据
     */
    List<HashMap<String, Object>> listGroupTopIpByShortLink(ShortLinkGroupStatsReqDTO shortLinkGroupStatsReqDTO);

    /**
     * 获取分组在指定时间范围内的用户访问记录（简化查询）
     */
    List<HashMap<String, Object>> findGroupUserAccessLogs(
            @Param("gid") String gid,
            @Param("startDate") String startDate,
            @Param("endDate") String endDate
    );

    /**
     * 获取分组指定用户的访问记录（简化查询）
     */
    List<Map<String, Object>> selectGroupUserAccessLogs(SelectGroupUvTypeByUserReqDTO selectGroupUvTypeByUserReqDTO);
}




