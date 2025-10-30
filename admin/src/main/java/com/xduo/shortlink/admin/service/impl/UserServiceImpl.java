package com.xduo.shortlink.admin.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.UUID;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xduo.shortlink.admin.common.constant.RedisCacheConstants;
import com.xduo.shortlink.admin.common.convention.exception.ClientException;
import com.xduo.shortlink.admin.common.enums.UserErrorCodeEnum;
import com.xduo.shortlink.admin.dao.entity.UserDO;
import com.xduo.shortlink.admin.dao.mapper.UserMapper;
import com.xduo.shortlink.admin.dto.req.UserLoginReqDTO;
import com.xduo.shortlink.admin.dto.req.UserRegisterReqDTO;
import com.xduo.shortlink.admin.dto.req.UserUpdateReqDTO;
import com.xduo.shortlink.admin.dto.req.UserChangePasswordReqDTO;
import com.xduo.shortlink.admin.dto.req.SendEmailCodeReqDTO;
import com.xduo.shortlink.admin.dto.req.SendForgotPasswordEmailReqDTO;
import com.xduo.shortlink.admin.dto.req.ForgotPasswordReqDTO;
import com.xduo.shortlink.admin.dto.req.UserChangeEmailReqDTO;
import com.xduo.shortlink.admin.dto.resp.UserLoginRespDTO;
import com.xduo.shortlink.admin.dto.resp.UserRespDTO;
import com.xduo.shortlink.admin.service.GroupService;
import com.xduo.shortlink.admin.service.UserService;
import com.xduo.shortlink.admin.service.EmailCodeService;
import com.xduo.shortlink.admin.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.xduo.shortlink.admin.common.constant.RedisCacheConstants.USER_LOGIN_KEY;

/**
 * 用户接口实现层
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, UserDO> implements UserService {
    private final RBloomFilter<String> userRegisterCachePenetrationBloomFilter;
    private final RedissonClient redissonClient;
    private final StringRedisTemplate stringRedisTemplate;
    private final GroupService groupService;
    private final EmailCodeService emailCodeService;
    private final EmailService emailService;

    @Override
    public UserRespDTO getUserByUsername(String username) {
        LambdaQueryWrapper<UserDO> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(UserDO::getUsername, username);
        UserDO userDO = baseMapper.selectOne(lambdaQueryWrapper);
        UserRespDTO userRespDTO = new UserRespDTO();
        if (userDO != null) {
            BeanUtils.copyProperties(userDO, userRespDTO);
            return userRespDTO;
        }
        throw new ClientException(UserErrorCodeEnum.USER_NULL);
    }

    @Override
    public Boolean hasUsername(String username) {
        return userRegisterCachePenetrationBloomFilter.contains(username);
    }

    @Override
    public void register(UserRegisterReqDTO registerReqDto) {
        // 1. 验证用户名格式
        validateUsername(registerReqDto.getUsername());
        
        // 2. 验证密码格式
        validatePassword(registerReqDto.getPassword());
        
        // 3. 检查用户名是否存在
        if (hasUsername(registerReqDto.getUsername())) {
            throw new ClientException(UserErrorCodeEnum.USER_EXIST);
        }
        
        // 4. 验证邮箱验证码
        if (registerReqDto.getEmailCode() == null || registerReqDto.getEmailCode().trim().isEmpty()) {
            throw new ClientException("邮箱验证码不能为空");
        }
        if (registerReqDto.getMail() == null || registerReqDto.getMail().trim().isEmpty()) {
            throw new ClientException("邮箱不能为空");
        }
        if (!emailCodeService.verifyEmailCode(registerReqDto.getMail(), registerReqDto.getEmailCode())) {
            throw new ClientException("邮箱验证码错误或已过期");
        }
        
        // 5. 注册用户
        RLock lock = redissonClient.getLock(RedisCacheConstants.LOCK_USER_REGISTER_KEY + registerReqDto.getUsername());
        try {
            if (lock.tryLock()) {
                try {
                    int insert = baseMapper.insert(BeanUtil.toBean(registerReqDto, UserDO.class));
                    if (insert < 1) {
                        throw new ClientException(UserErrorCodeEnum.USER_SAVE_ERROR);
                    }
                } catch (DuplicateKeyException exception) {
                    throw new ClientException(UserErrorCodeEnum.USER_EXIST);

                }
                userRegisterCachePenetrationBloomFilter.add(registerReqDto.getUsername());
                groupService.saveGroup(registerReqDto.getUsername(), "默认分组");
                
                // 删除验证码
                emailCodeService.deleteEmailCode(registerReqDto.getMail());
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void update(UserUpdateReqDTO userUpdateReqDTO) {
        //TODO 验证当前登录用户是否为当前用户


        LambdaUpdateWrapper<UserDO> lambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        lambdaUpdateWrapper.eq(UserDO::getUsername, userUpdateReqDTO.getUsername());
        int update = baseMapper.update(BeanUtil.toBean(userUpdateReqDTO, UserDO.class), lambdaUpdateWrapper);
        if (update < 1) {
            throw new ClientException("不存在该用户");
        }
    }

    @Override
    public UserLoginRespDTO login(UserLoginReqDTO userLoginReqDTO) {


        LambdaQueryWrapper<UserDO> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(UserDO::getUsername, userLoginReqDTO.getUsername()).eq(UserDO::getDelFlag, 0);
        UserDO userDO = baseMapper.selectOne(lambdaQueryWrapper);
        if (userDO == null) {
            throw new ClientException("用户不存在");
        }
        if (!userDO.getPassword().equals(userLoginReqDTO.getPassword())) {
            throw new ClientException("密码错误");
        }
        //已经校验过了身份正确性
        stringRedisTemplate.expire(USER_LOGIN_KEY + userLoginReqDTO.getUsername(), 30L, TimeUnit.MINUTES);
        //如果存在登录信息则取第一个返回
        Map<Object, Object> hasLoginMap = stringRedisTemplate.opsForHash().entries(USER_LOGIN_KEY + userLoginReqDTO.getUsername());
        if (CollUtil.isNotEmpty(hasLoginMap)) {
            String token = hasLoginMap.keySet().stream()
                    .findFirst()
                    .map(Object::toString)
                    .orElseThrow(() -> new ClientException("用户登录错误"));
            return new UserLoginRespDTO(token);
        }
        /**
         * Hash
         * Key：login_用户名
         * Value：
         *  Key：token标识
         *  Val：JSON 字符串（用户信息）
         */

        //如果不存在登录信息就创造新的token返回
        String uuid = UUID.randomUUID().toString();
        stringRedisTemplate.opsForHash().put(USER_LOGIN_KEY + userLoginReqDTO.getUsername(), uuid, JSON.toJSONString(userDO));
        stringRedisTemplate.expire(USER_LOGIN_KEY + userLoginReqDTO.getUsername(), 30L, TimeUnit.MINUTES);
        return new UserLoginRespDTO(uuid);
    }

    @Override
    public Boolean checkLogin(String username, String token) {
        Object redisToken = stringRedisTemplate.opsForHash().get(USER_LOGIN_KEY + username, token);
        return redisToken != null;
    }

    @Override
    public void logout(String username, String token) {
        if (checkLogin(username, token)) {
            stringRedisTemplate.opsForHash().delete(USER_LOGIN_KEY + username, token);
            return;
        }
        throw new ClientException("用户Token不存在或用户未登录");
    }

    @Override
    public void changePassword(UserChangePasswordReqDTO changePasswordReqDTO) {
        // 验证参数
        if (changePasswordReqDTO.getUsername() == null || changePasswordReqDTO.getUsername().trim().isEmpty()) {
            throw new ClientException("用户名不能为空");
        }
        
        // 查询用户信息
        LambdaQueryWrapper<UserDO> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(UserDO::getUsername, changePasswordReqDTO.getUsername()).eq(UserDO::getDelFlag, 0);
        UserDO userDO = baseMapper.selectOne(lambdaQueryWrapper);
        if (userDO == null) {
            throw new ClientException("用户不存在");
        }

        // 根据修改方式验证
        if ("PASSWORD".equals(changePasswordReqDTO.getChangeType())) {
            // 使用当前密码修改
            if (changePasswordReqDTO.getCurrentPassword() == null || changePasswordReqDTO.getCurrentPassword().isEmpty()) {
                throw new ClientException("当前密码不能为空");
            }
            if (!userDO.getPassword().equals(changePasswordReqDTO.getCurrentPassword())) {
                throw new ClientException("当前密码错误");
            }
        } else if ("EMAIL".equals(changePasswordReqDTO.getChangeType())) {
            // 使用邮箱验证码修改
            if (changePasswordReqDTO.getEmailCode() == null || changePasswordReqDTO.getEmailCode().isEmpty()) {
                throw new ClientException("邮箱验证码不能为空");
            }
            if (userDO.getMail() == null || userDO.getMail().isEmpty()) {
                throw new ClientException("用户未绑定邮箱");
            }
            if (!emailCodeService.verifyEmailCode(userDO.getMail(), changePasswordReqDTO.getEmailCode())) {
                throw new ClientException("邮箱验证码错误或已过期");
            }
        } else {
            throw new ClientException("修改方式不支持");
        }

        // 验证新密码
        if (changePasswordReqDTO.getNewPassword() == null || changePasswordReqDTO.getNewPassword().isEmpty()) {
            throw new ClientException("新密码不能为空");
        }
        if (changePasswordReqDTO.getNewPassword().length() < 6) {
            throw new ClientException("新密码长度不能少于6位");
        }

        // 更新密码
        LambdaUpdateWrapper<UserDO> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(UserDO::getUsername, changePasswordReqDTO.getUsername())
                .set(UserDO::getPassword, changePasswordReqDTO.getNewPassword());
        
        int update = baseMapper.update(null, updateWrapper);
        if (update < 1) {
            throw new ClientException("密码修改失败");
        }

        // 如果是邮箱验证码方式，删除验证码
        if ("EMAIL".equals(changePasswordReqDTO.getChangeType())) {
            emailCodeService.deleteEmailCode(userDO.getMail());
        }
    }

    @Override
    public void sendEmailCode(SendEmailCodeReqDTO sendEmailCodeReqDTO) {
        // 查询用户信息
        LambdaQueryWrapper<UserDO> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(UserDO::getUsername, sendEmailCodeReqDTO.getUsername()).eq(UserDO::getDelFlag, 0);
        UserDO userDO = baseMapper.selectOne(lambdaQueryWrapper);
        if (userDO == null) {
            throw new ClientException("用户不存在");
        }

        // 验证邮箱是否匹配
        if (userDO.getMail() == null || userDO.getMail().isEmpty()) {
            throw new ClientException("用户未绑定邮箱");
        }
        if (!userDO.getMail().equals(sendEmailCodeReqDTO.getEmail())) {
            throw new ClientException("邮箱地址不匹配");
        }

        // 生成并发送验证码
        String code = emailCodeService.sendEmailCode(userDO.getMail(), sendEmailCodeReqDTO.getUsername());
        emailService.sendEmailCode(userDO.getMail(), code, sendEmailCodeReqDTO.getUsername());
    }

    @Override
    public void sendForgotPasswordEmailCode(SendForgotPasswordEmailReqDTO sendForgotPasswordEmailReqDTO) {
        // 查询用户信息
        LambdaQueryWrapper<UserDO> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(UserDO::getUsername, sendForgotPasswordEmailReqDTO.getUsername()).eq(UserDO::getDelFlag, 0);
        UserDO userDO = baseMapper.selectOne(lambdaQueryWrapper);
        if (userDO == null) {
            throw new ClientException("用户不存在");
        }

        // 验证邮箱是否匹配
        if (userDO.getMail() == null || userDO.getMail().isEmpty()) {
            throw new ClientException("用户未绑定邮箱");
        }
        if (!userDO.getMail().equals(sendForgotPasswordEmailReqDTO.getEmail())) {
            throw new ClientException("邮箱地址不匹配");
        }

        // 生成并发送找回密码验证码
        String code = emailCodeService.sendEmailCode(userDO.getMail(), sendForgotPasswordEmailReqDTO.getUsername());
        emailService.sendForgotPasswordEmailCode(userDO.getMail(), code, sendForgotPasswordEmailReqDTO.getUsername());
    }

    @Override
    public void forgotPassword(ForgotPasswordReqDTO forgotPasswordReqDTO) {
        // 查询用户信息
        LambdaQueryWrapper<UserDO> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(UserDO::getUsername, forgotPasswordReqDTO.getUsername()).eq(UserDO::getDelFlag, 0);
        UserDO userDO = baseMapper.selectOne(lambdaQueryWrapper);
        if (userDO == null) {
            throw new ClientException("用户不存在");
        }

        // 验证邮箱是否匹配
        if (userDO.getMail() == null || userDO.getMail().isEmpty()) {
            throw new ClientException("用户未绑定邮箱");
        }
        if (!userDO.getMail().equals(forgotPasswordReqDTO.getEmail())) {
            throw new ClientException("邮箱地址不匹配");
        }

        // 验证邮箱验证码
        boolean isCodeValid = emailCodeService.verifyEmailCode(forgotPasswordReqDTO.getEmail(), forgotPasswordReqDTO.getEmailCode());
        if (!isCodeValid) {
            throw new ClientException("验证码错误或已过期");
        }

        // 更新密码
        LambdaUpdateWrapper<UserDO> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(UserDO::getUsername, forgotPasswordReqDTO.getUsername())
                .set(UserDO::getPassword, forgotPasswordReqDTO.getNewPassword());
        int updateResult = baseMapper.update(null, updateWrapper);
        if (updateResult < 1) {
            throw new ClientException("密码更新失败");
        }

        // 删除验证码
        emailCodeService.deleteEmailCode(userDO.getMail());
    }
    
    @Override
    public void sendRegisterEmailCode(String email) {
        // 验证邮箱格式
        if (email == null || email.trim().isEmpty()) {
            throw new ClientException("邮箱不能为空");
        }
        
        // 检查邮箱是否已经被注册
        LambdaQueryWrapper<UserDO> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(UserDO::getMail, email).eq(UserDO::getDelFlag, 0);
        UserDO existingUser = baseMapper.selectOne(lambdaQueryWrapper);
        if (existingUser != null) {
            throw new ClientException("该邮箱已被注册");
        }
        
        // 生成并发送验证码
        String code = emailCodeService.sendEmailCode(email, "");
        emailService.sendRegisterEmailCode(email, code);
    }
    
    @Override
    public void changeEmail(UserChangeEmailReqDTO reqDTO) {
        if (reqDTO.getUsername() == null || reqDTO.getUsername().trim().isEmpty()) {
            throw new ClientException("用户名不能为空");
        }
        if (reqDTO.getNewEmail() == null || reqDTO.getNewEmail().trim().isEmpty()) {
            throw new ClientException("新邮箱不能为空");
        }
        if (reqDTO.getEmailCode() == null || reqDTO.getEmailCode().trim().isEmpty()) {
            throw new ClientException("邮箱验证码不能为空");
        }
        // 校验新邮箱是否已被注册
        LambdaQueryWrapper<UserDO> emailUniqueCheck = new LambdaQueryWrapper<>();
        emailUniqueCheck.eq(UserDO::getMail, reqDTO.getNewEmail()).eq(UserDO::getDelFlag, 0);
        UserDO existedUser = baseMapper.selectOne(emailUniqueCheck);
        if (existedUser != null) {
            throw new ClientException("该邮箱已被注册");
        }
        // 校验验证码
        if (!emailCodeService.verifyEmailCode(reqDTO.getNewEmail(), reqDTO.getEmailCode())) {
            throw new ClientException("邮箱验证码错误或已过期");
        }
        // 变更邮箱
        LambdaUpdateWrapper<UserDO> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(UserDO::getUsername, reqDTO.getUsername())
                     .set(UserDO::getMail, reqDTO.getNewEmail());
        int update = baseMapper.update(null, updateWrapper);
        if (update < 1) {
            throw new ClientException("邮箱变更失败");
        }
        // 删除验证码
        emailCodeService.deleteEmailCode(reqDTO.getNewEmail());
    }
    
    @Override
    public void sendChangeEmailCode(SendEmailCodeReqDTO reqDTO) {
        if (reqDTO.getUsername() == null || reqDTO.getUsername().trim().isEmpty()) {
            throw new ClientException("用户名不能为空");
        }
        if (reqDTO.getEmail() == null || reqDTO.getEmail().trim().isEmpty()) {
            throw new ClientException("邮箱不能为空");
        }
        // 生成验证码并缓存（新邮箱）
        String code = emailCodeService.sendEmailCode(reqDTO.getEmail(), reqDTO.getUsername());
        // 发送变更邮箱邮件
        emailService.sendChangeEmailCode(reqDTO.getEmail(), code, reqDTO.getUsername());
    }
    
    /**
     * 验证用户名格式
     */
    private void validateUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new ClientException("用户名不能为空");
        }
        
        // 用户名长度至少6个字符
        if (username.length() < 6) {
            throw new ClientException("用户名至少需要6个字符");
        }
        
        // 用户名最多20个字符
        if (username.length() > 20) {
            throw new ClientException("用户名最多20个字符");
        }
        
        // 用户名只能包含字母、数字、下划线和连字符
        if (!username.matches("^[a-zA-Z0-9_-]+$")) {
            throw new ClientException("用户名只能包含字母、数字、下划线和连字符");
        }
    }
    
    /**
     * 验证密码格式
     */
    private void validatePassword(String password) {
        if (password == null || password.trim().isEmpty()) {
            throw new ClientException("密码不能为空");
        }
        
        // 密码长度至少6个字符
        if (password.length() < 6) {
            throw new ClientException("密码至少需要6个字符");
        }
        
        // 密码最多20个字符
        if (password.length() > 20) {
            throw new ClientException("密码最多20个字符");
        }
        
        // 密码必须至少包含两种类型的字符（小写字母、大写字母、数字、特殊字符）
        boolean hasLowercase = password.matches(".*[a-z].*");
        boolean hasUppercase = password.matches(".*[A-Z].*");
        boolean hasDigit = password.matches(".*\\d.*");
        boolean hasSpecial = password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*");
        
        int typeCount = 0;
        if (hasLowercase) typeCount++;
        if (hasUppercase) typeCount++;
        if (hasDigit) typeCount++;
        if (hasSpecial) typeCount++;
        
        if (typeCount < 2) {
            throw new ClientException("密码必须至少包含两种类型的字符（小写字母、大写字母、数字、特殊字符）");
        }
    }

}
