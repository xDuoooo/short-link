package com.nageoffer.shortlink.admin.controller;

import com.nageoffer.shortlink.admin.common.convention.result.Result;
import com.nageoffer.shortlink.admin.common.convention.result.Results;
import com.nageoffer.shortlink.admin.dto.req.GroupSaveReqDTO;
import com.nageoffer.shortlink.admin.dto.req.GroupUpdateReqDTO;
import com.nageoffer.shortlink.admin.dto.resp.GroupRespDTO;
import com.nageoffer.shortlink.admin.service.GroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 短链接分组控制层
 */
@RestController
@RequestMapping("/api/short-link/v1/group")
@RequiredArgsConstructor
public class GroupController {
    private final GroupService groupService;

    /**
     * 新增短链接分组
     * @param groupSaveReqDTO
     * @return
     */
    @PostMapping
    public Result<Void> save(@RequestBody GroupSaveReqDTO groupSaveReqDTO) {
        groupService.saveGroup(groupSaveReqDTO.getName());
        return Results.success();
    }

    /**
     * 获取短链接分组DTO
     * @return
     */
    @GetMapping
    public Result<List<GroupRespDTO>> listGroups(){
        return Results.success(groupService.listGroups());
    }

    /**
     * 短链接分组修改
     * @param groupUpdateReqDTO
     * @return
     */
    @PutMapping
    public Result<Void> updateGroup(@RequestBody GroupUpdateReqDTO groupUpdateReqDTO){
        groupService.updateGroup(groupUpdateReqDTO);
        return Results.success();
    }
}
