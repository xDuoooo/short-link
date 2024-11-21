package com.nageoffer.shortlink.project.controller;

import com.nageoffer.shortlink.project.common.convention.result.Result;
import com.nageoffer.shortlink.project.common.convention.result.Results;
import com.nageoffer.shortlink.project.dto.req.ShortLinkCreateReqDTO;
import com.nageoffer.shortlink.project.dto.resp.ShortLinkCreateRespDTO;
import com.nageoffer.shortlink.project.service.LinkService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/short-link")
@RequiredArgsConstructor
public class ShortLinkController {
    private final LinkService linkService;

    /**
     * 创建短链接
     * @return
     */
    @PostMapping
    public Result<ShortLinkCreateRespDTO> createShortLink(@RequestBody ShortLinkCreateReqDTO shortLinkCreateReqDTO){
        return Results.success(linkService.createShortLink(shortLinkCreateReqDTO));
    }

}
