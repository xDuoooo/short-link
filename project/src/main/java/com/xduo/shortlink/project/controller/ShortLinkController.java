package com.xduo.shortlink.project.controller;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xduo.shortlink.project.common.convention.result.Result;
import com.xduo.shortlink.project.common.convention.result.Results;
import com.xduo.shortlink.project.dto.req.ShortLinkBatchCreateReqDTO;
import com.xduo.shortlink.project.dto.req.ShortLinkCreateReqDTO;
import com.xduo.shortlink.project.dto.req.ShortLinkPageReqDTO;
import com.xduo.shortlink.project.dto.req.ShortLinkUpdateReqDTO;
import com.xduo.shortlink.project.dto.resp.ShortLinkBatchCreateRespDTO;
import com.xduo.shortlink.project.dto.resp.ShortLinkCreateRespDTO;
import com.xduo.shortlink.project.dto.resp.ShortLinkGroupCountQueryRespDTO;
import com.xduo.shortlink.project.dto.resp.ShortLinkPageRespDTO;
import com.xduo.shortlink.project.hander.CustomBlockHandler;
import com.xduo.shortlink.project.service.LinkService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/short-link/v1")
@RequiredArgsConstructor
public class ShortLinkController {
    private final LinkService linkService;

    /**
     * 短网址跳转
     */
    @GetMapping("/{short-uri}")
    public void restoreUrl(@PathVariable("short-uri") String shortUri, HttpServletRequest request, HttpServletResponse response) {
        linkService.restoreUrl(shortUri, request, response);
    }


    /**
     * 创建短链接
     *
     * @return
     */
    @PostMapping("/create")
    @SentinelResource(
            value = "create_short-link",
            blockHandler = "createShortLinkBlockHandlerMethod",
            blockHandlerClass = CustomBlockHandler.class
    )
    public Result<ShortLinkCreateRespDTO> createShortLink(@RequestBody ShortLinkCreateReqDTO shortLinkCreateReqDTO) {
        return Results.success(linkService.createShortLink(shortLinkCreateReqDTO));

    }

    /**
     * 批量创建短链接
     */
    @PostMapping("/create/batch")
    public Result<ShortLinkBatchCreateRespDTO> batchCreateShortLink(@RequestBody ShortLinkBatchCreateReqDTO requestParam) {
        return Results.success(linkService.batchCreateShortLink(requestParam));
    }

    /**
     * 修改短链接
     *
     * @param requestParam
     * @return
     */
    @PostMapping("/update")
    public Result<Void> updateShortLink(@RequestBody ShortLinkUpdateReqDTO requestParam) {
        linkService.updateShortLink(requestParam);
        return Results.success();
    }

    /**
     * 分页查询短链接请求
     *
     * @return
     */
    @GetMapping("/page")
    public Result<IPage<ShortLinkPageRespDTO>> pageShortLink(ShortLinkPageReqDTO shortLinkPageReqDTO) {
        return Results.success(linkService.pageShortLink(shortLinkPageReqDTO));
    }

    /**
     * 查询短链接分组下短链接数量
     *
     * @return
     */
    @GetMapping("/count")
    public Result<List<ShortLinkGroupCountQueryRespDTO>> groupShortLinkCount(@RequestParam List<String> gids) {
        return Results.success(linkService.listGroupShortLinkCount(gids));

    }

}
