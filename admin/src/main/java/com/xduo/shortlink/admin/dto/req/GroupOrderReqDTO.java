package com.xduo.shortlink.admin.dto.req;

import lombok.Data;

@Data
public class GroupOrderReqDTO {
    /**
     * 排序字段
     */
    private Integer sortOrder;
    /**
     * 分组id
     */
    private String gid;


}
