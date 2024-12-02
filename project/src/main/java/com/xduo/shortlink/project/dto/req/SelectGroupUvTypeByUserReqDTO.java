package com.xduo.shortlink.project.dto.req;

import lombok.Data;

import java.util.List;

@Data
public class SelectGroupUvTypeByUserReqDTO {

    /**
     * 完整短链接
     */
    private String fullShortUrl;

    /**
     * 分组标识
     */
    private String gid;

    /**
     * 开始日期
     */
    private String startDate;

    /**
     * 结束日期
     */
    private String endDate;
    /**
     * user列表
     */
    private List<String> userAccessLogsList;
}
