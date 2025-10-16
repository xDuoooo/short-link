package com.xduo.shortlink.admin.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xduo.shortlink.admin.common.convention.result.Result;
import com.xduo.shortlink.admin.common.convention.result.Results;
import com.xduo.shortlink.admin.remote.dto.ShortLinkActualRemoteService;
import com.xduo.shortlink.admin.remote.dto.req.*;
import com.xduo.shortlink.admin.remote.dto.resp.*;
import com.xduo.shortlink.admin.util.EasyExcelWebUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 短链接后管控制层
 */
@RestController(value = "shortLinkControllerByAdmin")
@RequiredArgsConstructor
public class ShortLinkController {

    private final ShortLinkActualRemoteService shortLinkActualRemoteService;

    /**
     * 创建短链接
     */
    @PostMapping("/api/short-link/admin/v1/create")
    public Result<ShortLinkCreateRespDTO> createShortLink(@RequestBody ShortLinkCreateReqDTO requestParam) {
        return shortLinkActualRemoteService.createShortLink(requestParam);
    }

    /**
     * 批量创建短链接
     */
    @SneakyThrows
    @PostMapping("/api/short-link/admin/v1/create/batch")
    public void batchCreateShortLink(@RequestBody ShortLinkBatchCreateReqDTO requestParam, HttpServletResponse response) {
        Result<ShortLinkBatchCreateRespDTO> shortLinkBatchCreateRespDTOResult = shortLinkActualRemoteService.batchCreateShortLink(requestParam);
        if (shortLinkBatchCreateRespDTOResult.isSuccess()) {
            List<ShortLinkBaseInfoRespDTO> baseLinkInfos = shortLinkBatchCreateRespDTOResult.getData().getBaseLinkInfos();
            EasyExcelWebUtil.write(response, "批量创建短链接-SaaS短链接系统", ShortLinkBaseInfoRespDTO.class, baseLinkInfos);
        }
    }

    /**
     * 修改短链接
     */
    @PostMapping("/api/short-link/admin/v1/update")
    public Result<Void> updateShortLink(@RequestBody ShortLinkUpdateReqDTO requestParam) {
        shortLinkActualRemoteService.updateShortLink(requestParam);
        return Results.success();
    }

    /**
     * 分页查询短链接
     */
    @GetMapping("/api/short-link/admin/v1/page")
    public Result<IPage<ShortLinkPageRespDTO>> pageShortLink(ShortLinkPageReqDTO requestParam) {
        return Results.success(shortLinkActualRemoteService.pageShortLink(requestParam).getData());
    }

    /**
     * 批量分页查询短链接
     */
    @PostMapping("/api/short-link/admin/v1/page/batch")
    public Result<ShortLinkBatchPageRespDTO> batchPageShortLink(@RequestBody ShortLinkBatchPageReqDTO requestParam) {
        return shortLinkActualRemoteService.batchPageShortLink(requestParam);
    }

    /**
     * 查询短链接分组下短链接数量
     */
    @GetMapping("/api/short-link/admin/v1/count")
    public Result<List<ShortLinkGroupCountQueryRespDTO>> groupShortLinkCount(@RequestParam("gids") List<String> gids) {
        return shortLinkActualRemoteService.listGroupShortLinkCount(gids);
    }


}
