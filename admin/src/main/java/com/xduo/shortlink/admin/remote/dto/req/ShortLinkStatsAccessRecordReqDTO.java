package com.xduo.shortlink.admin.remote.dto.req;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 短链接监控访问记录请求参数
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class ShortLinkStatsAccessRecordReqDTO extends Page<Object> {

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
     * 是否包含回收站短链接
     */
    private Boolean includeRecycle;

    /**
     * 用户名
     */
    private String username;
}