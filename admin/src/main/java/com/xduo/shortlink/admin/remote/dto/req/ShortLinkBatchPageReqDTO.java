package com.xduo.shortlink.admin.remote.dto.req;

import lombok.Data;

import java.util.List;

/**
 * 短链接批量分页请求参数
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
    private Long current;

    /**
     * 每页记录数
     */
    private Long size;

    /**
     * 排序标识
     */
    private String orderTag;
}
