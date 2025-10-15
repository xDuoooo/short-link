package com.xduo.shortlink.admin.controller;

import cn.hutool.core.bean.BeanUtil;
import com.xduo.shortlink.admin.common.convention.result.Result;
import com.xduo.shortlink.admin.common.convention.result.Results;
import com.xduo.shortlink.admin.dto.req.UserLoginReqDTO;
import com.xduo.shortlink.admin.dto.req.UserRegisterReqDTO;
import com.xduo.shortlink.admin.dto.req.UserUpdateReqDTO;
import com.xduo.shortlink.admin.dto.req.UserChangePasswordReqDTO;
import com.xduo.shortlink.admin.dto.req.SendEmailCodeReqDTO;
import com.xduo.shortlink.admin.dto.req.SendForgotPasswordEmailReqDTO;
import com.xduo.shortlink.admin.dto.req.ForgotPasswordReqDTO;
import com.xduo.shortlink.admin.dto.resp.UserActualRespDTO;
import com.xduo.shortlink.admin.dto.resp.UserLoginRespDTO;
import com.xduo.shortlink.admin.dto.resp.UserRespDTO;
import com.xduo.shortlink.admin.service.MinIOService;
import com.xduo.shortlink.admin.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

/**
 * 用户管理控制层
 */
@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final MinIOService minIOService;
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
     * 修改密码
     * @param changePasswordReqDTO
     * @return
     */
    @PostMapping("/api/short-link/admin/v1/user/change-password")
    public Result<Void> changePassword(@RequestBody UserChangePasswordReqDTO changePasswordReqDTO){
        userService.changePassword(changePasswordReqDTO);
        return Results.success();
    }

    /**
     * 发送邮箱验证码
     * @param sendEmailCodeReqDTO
     * @return
     */
    @PostMapping("/api/short-link/admin/v1/user/send-email-code")
    public Result<Void> sendEmailCode(@RequestBody SendEmailCodeReqDTO sendEmailCodeReqDTO){
        userService.sendEmailCode(sendEmailCodeReqDTO);
        return Results.success();
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

    /**
     * 上传头像
     * @param file 头像文件
     * @param username 用户名
     * @return 头像上传结果
     */
    @PostMapping("/api/short-link/admin/v1/avatar/upload")
    public Result<String> uploadAvatar(@RequestParam("file") MultipartFile file,
                                      @RequestParam("username") String username) {
        try {
            // 生成唯一的文件名
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null && originalFilename.contains(".") 
                ? originalFilename.substring(originalFilename.lastIndexOf(".")) 
                : "";
            String objectName = "avatars/" + username + "_" + System.currentTimeMillis() + extension;
            
            // 直接使用admin模块的MinIO服务
            String avatarUrl = minIOService.uploadFile(file, objectName);
            
            // 上传成功后，更新用户信息中的头像字段
            UserUpdateReqDTO userUpdateReqDTO = new UserUpdateReqDTO();
            userUpdateReqDTO.setUsername(username);
            userUpdateReqDTO.setAvatar(avatarUrl);
            userService.update(userUpdateReqDTO);
            
            return Results.success(avatarUrl);
        } catch (Exception e) {
            return Results.failure("AVATAR_UPLOAD_FAILED", "头像上传失败: " + e.getMessage(), String.class);
        }
    }

    /**
     * 发送找回密码邮箱验证码
     */
    @PostMapping("/api/short-link/admin/v1/user/send-forgot-password-email-code")
    public Result<Void> sendForgotPasswordEmailCode(@RequestBody SendForgotPasswordEmailReqDTO sendForgotPasswordEmailReqDTO) {
        userService.sendForgotPasswordEmailCode(sendForgotPasswordEmailReqDTO);
        return Results.success();
    }

    /**
     * 找回密码
     */
    @PostMapping("/api/short-link/admin/v1/user/forgot-password")
    public Result<Void> forgotPassword(@RequestBody ForgotPasswordReqDTO forgotPasswordReqDTO) {
        userService.forgotPassword(forgotPasswordReqDTO);
        return Results.success();
    }

}
