package com.xduo.shortlink.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xduo.shortlink.admin.dao.entity.UserDO;
import com.xduo.shortlink.admin.dto.req.UserLoginReqDTO;
import com.xduo.shortlink.admin.dto.req.UserRegisterReqDTO;
import com.xduo.shortlink.admin.dto.req.UserUpdateReqDTO;
import com.xduo.shortlink.admin.dto.req.UserChangePasswordReqDTO;
import com.xduo.shortlink.admin.dto.req.SendEmailCodeReqDTO;
import com.xduo.shortlink.admin.dto.req.SendForgotPasswordEmailReqDTO;
import com.xduo.shortlink.admin.dto.req.ForgotPasswordReqDTO;
import com.xduo.shortlink.admin.dto.resp.UserLoginRespDTO;
import com.xduo.shortlink.admin.dto.resp.UserRespDTO;

/**
 * 用户接口层
 */
public interface UserService extends IService<UserDO> {
    /**
     * 根据用户名查询用户信息
     * @param username 用户名
     * @return 用户信息返回类
     */
    UserRespDTO getUserByUsername(String username);

    /**
     * 查询用户名是否存在
     * @param username
     * @return
     */
    Boolean hasUsername(String username);

    /**
     * 注册用户
     * @param registerReqDto
     */
    void register(UserRegisterReqDTO registerReqDto);

    /**
     * 更新用户信息
     * @param userUpdateReqDTO
     */
    void update(UserUpdateReqDTO userUpdateReqDTO);

    /**
     * 用户登录
     * @param userLoginReqDTO
     * @return
     */
    UserLoginRespDTO login(UserLoginReqDTO userLoginReqDTO);

    /**
     * 检查用户是否登录
     * @param username
     * @param token
     * @return
     */
    Boolean checkLogin(String username,String token);

    /**
     * 用户退出登录
     * @param username
     * @param token
     * @return
     */
    void logout(String username, String token);

    /**
     * 修改密码
     * @param changePasswordReqDTO
     */
    void changePassword(UserChangePasswordReqDTO changePasswordReqDTO);

    /**
     * 发送邮箱验证码
     * @param sendEmailCodeReqDTO
     */
    void sendEmailCode(SendEmailCodeReqDTO sendEmailCodeReqDTO);

    /**
     * 发送找回密码邮箱验证码
     * @param sendForgotPasswordEmailReqDTO
     */
    void sendForgotPasswordEmailCode(SendForgotPasswordEmailReqDTO sendForgotPasswordEmailReqDTO);

    /**
     * 找回密码
     * @param forgotPasswordReqDTO
     */
    void forgotPassword(ForgotPasswordReqDTO forgotPasswordReqDTO);
}
