package com.xduo.shortlink.project.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xduo.shortlink.project.common.convention.result.Result;
import com.xduo.shortlink.project.common.convention.result.Results;
import com.xduo.shortlink.project.dto.req.ShortLinkGroupStatsAccessRecordReqDTO;
import com.xduo.shortlink.project.dto.req.ShortLinkGroupStatsReqDTO;
import com.xduo.shortlink.project.dto.req.ShortLinkStatsAccessRecordReqDTO;
import com.xduo.shortlink.project.dto.req.ShortLinkStatsReqDTO;
import com.xduo.shortlink.project.dto.resp.ShortLinkStatsAccessRecordRespDTO;
import com.xduo.shortlink.project.dto.resp.ShortLinkStatsRespDTO;
import com.xduo.shortlink.project.service.ShortLinkStatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.xduo.shortlink.project.util.ExcelExportUtil;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class ShortLinkStatsController {

    private final ShortLinkStatsService shortLinkStatsService;

    /**
     * 获取一个短链接指定日期内监控统计数据
     * @param shortLinkStatsReqDTO
     * @return
     */
    @GetMapping("/api/short-link/v1/stats")
    public Result<ShortLinkStatsRespDTO> getOneShortLinkStats(ShortLinkStatsReqDTO shortLinkStatsReqDTO){
        return Results.success(shortLinkStatsService.getOneShortLinkStats(shortLinkStatsReqDTO));
    }
    /**
     * 获取一组短链接指定日期内监控统计数据
     * @param shortLinkGroupStatsReqDTO
     * @return
     */
    @GetMapping("/api/short-link/v1/stats/group")
    public Result<ShortLinkStatsRespDTO> getGroupShortLinkStats(ShortLinkGroupStatsReqDTO shortLinkGroupStatsReqDTO){
        return Results.success(shortLinkStatsService.getGroupShortLinkStats(shortLinkGroupStatsReqDTO));
    }

    /**
     * 访问单个短链接指定时间内访问记录监控数据
     */
    @GetMapping("/api/short-link/v1/stats/access-record")
    public Result<IPage<ShortLinkStatsAccessRecordRespDTO>> shortLinkStatsAccessRecord(ShortLinkStatsAccessRecordReqDTO requestParam) {
        return Results.success(shortLinkStatsService.shortLinkStatsAccessRecord(requestParam));
    }
    /**
     * 访问一组短链接指定时间内访问记录监控数据
     */
    @GetMapping("/api/short-link/v1/stats/access-record/group")
    public Result<IPage<ShortLinkStatsAccessRecordRespDTO>> shortLinkGroupStatsAccessRecord(ShortLinkGroupStatsAccessRecordReqDTO requestParam) {
        return Results.success(shortLinkStatsService.shortLinkGroupStatsAccessRecord(requestParam));
    }


    @GetMapping("/api/short-link/v1/stats/access-record/export")
    public ResponseEntity<byte[]> exportShortLinkStatsAccessRecord(
            @RequestParam String gid,
            @RequestParam(required = false) String fullShortUrl,
            @RequestParam String startDate,
            @RequestParam String endDate,
            @RequestParam(required = false, defaultValue = "10") Integer size
    ) throws IOException {
        // 这里只导出第一页
        IPage<ShortLinkStatsAccessRecordRespDTO> page;
        if(fullShortUrl != null){
            // 组装你的req
            ShortLinkStatsAccessRecordReqDTO req = new ShortLinkStatsAccessRecordReqDTO();
            req.setGid(gid);
            req.setFullShortUrl(fullShortUrl);
            req.setStartDate(startDate);
            req.setEndDate(endDate);
            req.setCurrent(1L);
            req.setSize(size.longValue());
            page = shortLinkStatsService.shortLinkStatsAccessRecord(req);
        }else{
            // 组装你的req
            ShortLinkGroupStatsAccessRecordReqDTO req = new ShortLinkGroupStatsAccessRecordReqDTO();
            req.setGid(gid);
            req.setStartDate(startDate);
            req.setEndDate(endDate);
            req.setCurrent(1L);
            req.setSize(size.longValue());
            page = shortLinkStatsService.shortLinkGroupStatsAccessRecord(req);
        }
        List<ShortLinkStatsAccessRecordRespDTO> records = page.getRecords();

        // 写入excel到输出流
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ExcelExportUtil.export(out, "访问记录", ShortLinkStatsAccessRecordRespDTO.class, records);
        byte[] excelBytes = out.toByteArray();

        // 处理中文文件名
        String fileName = URLEncoder.encode("访问记录.xlsx", StandardCharsets.UTF_8);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + fileName);

        return new ResponseEntity<>(excelBytes, headers, HttpStatus.OK);
    }
}
