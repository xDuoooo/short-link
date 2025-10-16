package com.xduo.shortlink.project.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xduo.shortlink.project.dao.entity.LinkDO;
import com.xduo.shortlink.project.dto.req.ShortLinkBatchPageReqDTO;
import com.xduo.shortlink.project.dto.req.ShortLinkPageReqDTO;
import com.xduo.shortlink.project.dto.resp.ShortLinkGroupCountQueryRespDTO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
* @author Duo
* @description 针对表【t_link】的数据库操作Mapper
* @createDate 2024-11-21 09:54:27
* @Entity generator.dto/entity.TLink
*/

public interface LinkMapper extends BaseMapper<LinkDO> {

    List<ShortLinkGroupCountQueryRespDTO> listGroupShortLinkCount(List<String> gids);

    /**
     * 短链接访问统计自增
     */
    void incrementStats(
            @Param("gid") String gid,
            @Param("fullShortUrl") String fullShortUrl,
            @Param("totalPv") Integer totalPv,
            @Param("totalUv") Integer totalUv,
            @Param("totalUip") Integer totalUip
    );

    IPage<LinkDO> pageLink(ShortLinkPageReqDTO shortLinkPageReqDTO);

    /**
     * 优化的分页查询短链接 - 当有gid过滤时使用
     */
    IPage<LinkDO> pageLinkOptimized(ShortLinkPageReqDTO shortLinkPageReqDTO);

    /**
     * 批量分页查询短链接 - 支持多个分组批量查询
     */
    List<LinkDO> batchPageLinkOptimized(ShortLinkBatchPageReqDTO shortLinkBatchPageReqDTO);
}




