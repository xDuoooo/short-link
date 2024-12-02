package com.xduo.shortlink.admin.dto.req;

import lombok.Data;

/**
 * 短链接分组修改参数
 */
@Data
public class GroupUpdateReqDTO {
    /**
     * 分组gid
     */
    private String gid;
    /**
     * 分组名
     */
    private String name;
}
