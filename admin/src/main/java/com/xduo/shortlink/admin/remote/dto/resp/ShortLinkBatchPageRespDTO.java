package com.xduo.shortlink.admin.remote.dto.resp;

import lombok.Data;

import java.util.List;

/**
 * 短链接批量分页响应参数
 */
@Data
public class ShortLinkBatchPageRespDTO {

    /**
     * 短链接分页数据
     */
    private List<ShortLinkPageRespDTO> records;

    /**
     * 当前页码
     */
    private Long current;

    /**
     * 每页记录数
     */
    private Long size;

    /**
     * 总记录数
     */
    private Long total;

    /**
     * 总页数
     */
    private Long pages;
}
