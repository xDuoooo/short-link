package com.xduo.shortlink.admin.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xduo.shortlink.admin.common.convention.result.Result;
import com.xduo.shortlink.admin.common.convention.result.Results;
import com.xduo.shortlink.admin.remote.dto.ShortLinkRemoteService;
import com.xduo.shortlink.admin.remote.dto.req.RecycleBinRecoverReqDTO;
import com.xduo.shortlink.admin.remote.dto.req.RecycleBinRemoveReqDTO;
import com.xduo.shortlink.admin.remote.dto.req.RecycleBinSaveReqDTO;
import com.xduo.shortlink.admin.remote.dto.req.ShortLinkRecycleBinPageReqDTO;
import com.xduo.shortlink.admin.remote.dto.resp.ShortLinkPageRespDTO;
import com.xduo.shortlink.admin.service.RecycleBinService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
@RequiredArgsConstructor
@RestController
public class RecycleBinController {

    private final RecycleBinService recycleBinService;

    ShortLinkRemoteService shortLinkRemoteService = new ShortLinkRemoteService() {
    };

    /**
     * 短链接移至回收站
     */
    @PostMapping("/api/short-lin/admin/v1/recycle-bin/save")
    public Result<Void> saveRecycleBin(@RequestBody RecycleBinSaveReqDTO recycleBinSaveReqDTO){
        shortLinkRemoteService.saveRecycleBin(recycleBinSaveReqDTO);
        return Results.success();
    }

    /**
     * 分页查询回收站里的短链接(当前用户)
     */
    @GetMapping("/api/short-link/admin/v1/recycle-bin/page")
    public Result<IPage<ShortLinkPageRespDTO>> pageBinShortLink(ShortLinkRecycleBinPageReqDTO requestParam) {
        return recycleBinService.recycleBinPageShortLink(requestParam);
    }

    /**
     * 短链接移出回收站
     */
    @PostMapping("/api/short-lin/admin/v1/recycle-bin/recover")
    public Result<Void> recoverRecycleBin(@RequestBody RecycleBinRecoverReqDTO recycleBinRecoverReqDTO){
        shortLinkRemoteService.recoverRecycleBin(recycleBinRecoverReqDTO);
        return Results.success();
    }

    /**
     * 短链接彻底删除
     */
    @PostMapping("/api/short-lin/admin/v1/recycle-bin/remove")
    public Result<Void> removeRecycleBin(@RequestBody RecycleBinRemoveReqDTO recycleBinRemoveReqDTO) {
        shortLinkRemoteService.removeRecycleBin(recycleBinRemoveReqDTO);
        return Results.success();
    }
}
