package com.xduo.shortlink.project.dto.req;

import lombok.Data;

/**
 * 短链接状态更新请求对象
 */
@Data
public class ShortLinkUpdateStatusReqDTO {

    /**
     * 分组标识
     */
    private String gid;

    /**
     * 完整短链接
     */
    private String fullShortUrl;

    /**
     * 启用状态 0：未启用 1：已启用
     */
    private Integer enableStatus;
}
