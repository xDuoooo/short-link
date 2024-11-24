package com.nageoffer.shortlink.admin.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.nageoffer.shortlink.admin.common.convention.result.Result;
import com.nageoffer.shortlink.admin.remote.dto.req.ShortLinkRecycleBinPageReqDTO;
import com.nageoffer.shortlink.admin.remote.dto.resp.ShortLinkPageRespDTO;
import org.springframework.stereotype.Service;

public interface RecycleBinService {
    Result<IPage<ShortLinkPageRespDTO>> recycleBinPageShortLink(ShortLinkRecycleBinPageReqDTO requestParam);
}
