package com.xduo.shortlink.project.dto.req;

import lombok.Data;

import java.util.List;

/**
 * 批量分页查询短链接请求参数
 * @author Duo
 */
@Data
public class ShortLinkBatchPageReqDTO {

    /**
     * 分组标识列表
     */
    private List<String> gids;

    /**
     * 当前页码
     */
    private Long current = 1L;

    /**
     * 每页记录数
     */
    private Long size = 10L;

    /**
     * 排序字段
     */
    private String orderTag = "create_time";

    /**
     * 排序类型 desc/asc
     */
    private String orderType = "desc";
}
