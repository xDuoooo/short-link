package com.xduo.shortlink.admin.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xduo.shortlink.admin.common.convention.result.Result;
import com.xduo.shortlink.admin.remote.dto.req.ShortLinkRecycleBinPageReqDTO;
import com.xduo.shortlink.admin.remote.dto.resp.ShortLinkPageRespDTO;

public interface RecycleBinService {
    Result<Page<ShortLinkPageRespDTO>> recycleBinPageShortLink(ShortLinkRecycleBinPageReqDTO requestParam);
}
