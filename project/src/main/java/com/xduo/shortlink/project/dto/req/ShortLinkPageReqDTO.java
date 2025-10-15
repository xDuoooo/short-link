package com.xduo.shortlink.project.dto.req;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xduo.shortlink.project.dao.entity.LinkDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 短链接分页请求参数
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class ShortLinkPageReqDTO extends Page<LinkDO> {
    /**
     * 分组标识
     */
    private String gid;
    /**
     * 排序标识
     */
    private String orderTag;
    /**
     * 是否包含回收站短链接
     */
    private Boolean includeRecycle;
}
