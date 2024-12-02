package com.xduo.shortlink.admin.controller;

import cn.hutool.core.bean.BeanUtil;
import com.xduo.shortlink.admin.common.convention.result.Result;
import com.xduo.shortlink.admin.common.convention.result.Results;
import com.xduo.shortlink.admin.dto.req.UserLoginReqDTO;
import com.xduo.shortlink.admin.dto.req.UserRegisterReqDTO;
import com.xduo.shortlink.admin.dto.req.UserUpdateReqDTO;
import com.xduo.shortlink.admin.dto.resp.UserActualRespDTO;
import com.xduo.shortlink.admin.dto.resp.UserLoginRespDTO;
import com.xduo.shortlink.admin.dto.resp.UserRespDTO;
import com.xduo.shortlink.admin.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 用户管理控制层
 */
@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    /**
     * 根据用户名查询用户信息
     */
    @GetMapping("/api/short-link/admin/v1/user/{username}")
    public Result<UserRespDTO> getUserByUsername(@PathVariable("username") String username){

        return Results.success(userService.getUserByUsername(username));
    }
    /**
     * 根据用户名查询真实用户信息(无脱敏)
     */
    @GetMapping("/api/short-link/admin/v1/actual/user/{username}")
    public Result<UserActualRespDTO> getActualUserByUsername(@PathVariable("username") String username){

        return Results.success(BeanUtil.toBean(userService.getUserByUsername(username), UserActualRespDTO.class));
    }
    /**
     * 根据用户名查询用户信息
     */
    @GetMapping("/api/short-link/admin/v1/user/has-username")
    public Result<Boolean> hasUsername(@RequestParam("username") String username){

        return Results.success(userService.hasUsername(username));
    }

    /**
     * 注册用户
     * @return
     */
    @PostMapping("/api/short-link/admin/v1/user")
    public Result<Void> register(@RequestBody UserRegisterReqDTO userRegisterReqDto){
        userService.register(userRegisterReqDto);
        return Results.success();
    }

    /**
     * 修改用户信息
     * @param userUpdateReqDTO
     * @return
     */
    @PutMapping("/api/short-link/admin/v1/user")
    public Result<Void> update(@RequestBody UserUpdateReqDTO userUpdateReqDTO){
        userService.update(userUpdateReqDTO);
        return Results.success();
    }

    /**
     * 用户登录
     * @param userLoginReqDTO
     * @return
     */
    @PostMapping("/api/short-link/admin/v1/user/login")
    public Result<UserLoginRespDTO> login(@RequestBody UserLoginReqDTO userLoginReqDTO){
        return Results.success(userService.login(userLoginReqDTO));
    }

    /**
     * 检查用户是否登录
     * @param username
     * @param token
     * @return
     */
    @GetMapping("/api/short-link/admin/v1/user/check-login")
    public Result<Boolean> checkLogin(@RequestParam("username") String username,@RequestParam("token") String token){
        return Results.success(userService.checkLogin(username,token));
    }

    /**
     * 用户退出登录
     * @param username
     * @param token
     * @return
     */
    @DeleteMapping("/api/short-link/admin/v1/user/check-login")
    public Result<Void> logout(@RequestParam("username") String username,@RequestParam("token") String token){
        userService.logout(username,token);
        List<Integer> list = new ArrayList<>();
        list.toArray(new Integer[0]);
        return Results.success();
    }


}
