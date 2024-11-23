package com.nageoffer.shortlink.project.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.text.StrBuilder;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nageoffer.shortlink.project.common.convention.exception.ServiceException;
import com.nageoffer.shortlink.project.common.database.BaseDO;
import com.nageoffer.shortlink.project.common.enums.ValidateTypeEnum;
import com.nageoffer.shortlink.project.dao.entity.LinkDO;
import com.nageoffer.shortlink.project.dao.entity.LinkGoToDO;
import com.nageoffer.shortlink.project.dao.mapper.LinkGoToMapper;
import com.nageoffer.shortlink.project.dao.mapper.LinkMapper;
import com.nageoffer.shortlink.project.dto.req.ShortLinkCreateReqDTO;
import com.nageoffer.shortlink.project.dto.req.ShortLinkPageReqDTO;
import com.nageoffer.shortlink.project.dto.req.ShortLinkUpdateReqDTO;
import com.nageoffer.shortlink.project.dto.resp.ShortLinkCreateRespDTO;
import com.nageoffer.shortlink.project.dto.resp.ShortLinkGroupCountQueryRespDTO;
import com.nageoffer.shortlink.project.dto.resp.ShortLinkPageRespDTO;
import com.nageoffer.shortlink.project.service.LinkService;
import com.nageoffer.shortlink.project.util.HashUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.RedissonBloomFilter;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.rowset.serial.SerialException;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

import static com.nageoffer.shortlink.project.common.constant.RedisKeyConstant.GOTO_SHORT_LINK_KEY;
import static com.nageoffer.shortlink.project.common.constant.RedisKeyConstant.LOCK_GOTO_SHORT_LINK_KEY;

/**
 * @author Duo
 * @description 针对表【t_link】的数据库操作Service实现
 * @createDate 2024-11-21 09:54:27
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LinkServiceImpl extends ServiceImpl<LinkMapper, LinkDO> implements LinkService {

    private final RBloomFilter<String> shortUriCreateCachePenetrationBloomFilter;

    private final LinkMapper linkMapper;

    private final LinkGoToMapper linkGoToMapper;

    private final StringRedisTemplate stringRedisTemplate;

    private final RedissonClient redissonClient;


    @Override
    public ShortLinkCreateRespDTO createShortLink(ShortLinkCreateReqDTO shortLinkCreateReqDTO) {
        String shortLinkSuffix = generateSuffix(shortLinkCreateReqDTO);
        String fullShortUrl = StrBuilder.create(shortLinkCreateReqDTO.getDomain())
                .append("/")
                .append(shortLinkSuffix)
                .toString();
        LinkDO shortLinkDO = LinkDO.builder()
                .domain(shortLinkCreateReqDTO.getDomain())
                .originUrl(shortLinkCreateReqDTO.getOriginUrl())
                .gid(shortLinkCreateReqDTO.getGid())
                .createdType(shortLinkCreateReqDTO.getCreatedType())
                .validDateType(shortLinkCreateReqDTO.getValidDateType())
                .validDate(shortLinkCreateReqDTO.getValidDate())
                .describe(shortLinkCreateReqDTO.getDescribe())
                .shortUri(shortLinkSuffix)
                .enableStatus(1)
                .fullShortUrl(fullShortUrl)
                .build();
        LinkGoToDO linkGoToDO = LinkGoToDO.builder()
                .fullShortUrl(fullShortUrl)
                .gid(shortLinkDO.getGid())
                .build();
        try {
            //此时多个相同的并发请求可能到达这里
            baseMapper.insert(shortLinkDO);
            linkGoToMapper.insert(linkGoToDO);

        } catch (DuplicateKeyException ex) {

            LambdaQueryWrapper<LinkDO> queryWrapper = Wrappers.lambdaQuery(LinkDO.class)
                    .eq(LinkDO::getFullShortUrl, fullShortUrl);
            LinkDO hasShortLinkDO = baseMapper.selectOne(queryWrapper);
            if (hasShortLinkDO != null) {
                log.warn("短链接：{} 重复入库", fullShortUrl);
                throw new ServiceException("短链接生成重复");
            }
        }
        shortUriCreateCachePenetrationBloomFilter.add(fullShortUrl);
        return ShortLinkCreateRespDTO.builder()
                .fullShortUrl("http://" + shortLinkDO.getFullShortUrl())
                .originUrl(shortLinkDO.getOriginUrl()).build();
    }

    @Override
    public IPage<ShortLinkPageRespDTO> pageShortLink(ShortLinkPageReqDTO shortLinkPageReqDTO) {

        LambdaQueryWrapper<LinkDO> lambdaQueryWrapper = Wrappers.lambdaQuery(LinkDO.class)
                .eq(LinkDO::getGid, shortLinkPageReqDTO.getGid())
                .eq(LinkDO::getEnableStatus, 1)
                .eq(BaseDO::getDelFlag, 0);
        IPage<LinkDO> resultPage = baseMapper.selectPage(shortLinkPageReqDTO, lambdaQueryWrapper);
        return resultPage.convert(each -> {
            ShortLinkPageRespDTO bean = BeanUtil.toBean(each, ShortLinkPageRespDTO.class);
            bean.setDomain("http://" + bean.getDomain());
            return bean;
        });
    }

    @Override
    public List<ShortLinkGroupCountQueryRespDTO> listGroupShortLinkCount(List<String> gids) {
        return linkMapper.listGroupShortLinkCount(gids);

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateShortLink(ShortLinkUpdateReqDTO requestParam) {

        LambdaQueryWrapper<LinkDO> lambdaQueryWrapper = Wrappers.lambdaQuery(LinkDO.class)
                .eq(LinkDO::getFullShortUrl, requestParam.getFullShortUrl())
                .eq(BaseDO::getDelFlag, 0)
                .eq(LinkDO::getEnableStatus, 1);
        LinkDO selectedOne = baseMapper.selectOne(lambdaQueryWrapper);
        if (selectedOne == null) {
            throw new ServiceException("短链接记录不存在");
        }
        LinkDO linkDO = LinkDO.builder()
                .gid(requestParam.getGid())
                .originUrl(requestParam.getOriginUrl())
                .describe(requestParam.getDescribe())
                .validDate(requestParam.getValidDate())
                .validDateType(requestParam.getValidDateType())
                .domain(selectedOne.getDomain())
                .shortUri(selectedOne.getShortUri())
                .clickNum(selectedOne.getClickNum())
                .favicon(selectedOne.getFavicon())
                .createdType(selectedOne.getCreatedType())
                .enableStatus(1)
                .fullShortUrl(requestParam.getFullShortUrl())
                .build();

        if (Objects.equals(selectedOne.getGid(), requestParam.getGid())) {
            LambdaUpdateWrapper<LinkDO> wrapper = Wrappers.lambdaUpdate(LinkDO.class)
                    .eq(LinkDO::getFullShortUrl, requestParam.getFullShortUrl())
                    .eq(LinkDO::getGid, requestParam.getGid())
                    .eq(LinkDO::getDelFlag, 0)
                    .eq(LinkDO::getEnableStatus, 1).set(Objects.equals(requestParam.getValidDateType(), ValidateTypeEnum.PERMANENT.getType()), LinkDO::getValidDate, null);

            baseMapper.update(linkDO, wrapper);
        } else {
            LambdaUpdateWrapper<LinkDO> wrapper = Wrappers.lambdaUpdate(LinkDO.class)
                    .eq(LinkDO::getFullShortUrl, requestParam.getFullShortUrl())
                    .eq(LinkDO::getDelFlag, 0)
                    .eq(LinkDO::getEnableStatus, 1);
            baseMapper.delete(wrapper);
            linkDO.setGid(requestParam.getGid());
            baseMapper.insert(linkDO);


        }


    }

    @Override
    public void restoreUrl(String shortUri, HttpServletRequest request, HttpServletResponse response) {
        String serverName = request.getServerName();
        String fullShortUrl = serverName + "/" + shortUri;
        String originalLink = stringRedisTemplate.opsForValue().get(String.format(GOTO_SHORT_LINK_KEY, fullShortUrl));
        if (StrUtil.isNotBlank(originalLink)) {
            try {
                response.sendRedirect(originalLink);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return;
        }

        //缓存击穿 使用分布式锁解决
        RLock lock = redissonClient.getLock(String.format(LOCK_GOTO_SHORT_LINK_KEY, fullShortUrl));
        lock.lock();
        try {
            //双检 防止 大量请求在拿到锁前停滞
            originalLink = stringRedisTemplate.opsForValue().get(String.format(GOTO_SHORT_LINK_KEY, fullShortUrl));
            if (StrUtil.isNotBlank(originalLink)) {
                response.sendRedirect(originalLink);
                return;
            }
            LambdaQueryWrapper<LinkGoToDO> linkGoToDOLambdaQueryWrapper = Wrappers.lambdaQuery(LinkGoToDO.class)
                    .eq(LinkGoToDO::getFullShortUrl, fullShortUrl);
            LinkGoToDO linkGoToDO = linkGoToMapper.selectOne(linkGoToDOLambdaQueryWrapper);
            if (linkGoToDO == null) {
                //严禁来说，此处需要进行风控
                return;
            }
            LambdaQueryWrapper<LinkDO> linkDOLambdaQueryWrapper = Wrappers.lambdaQuery(LinkDO.class)
                    .eq(LinkDO::getGid, linkGoToDO.getGid())
                    .eq(LinkDO::getFullShortUrl, fullShortUrl)
                    .eq(BaseDO::getDelFlag, 0)
                    .eq(LinkDO::getEnableStatus, 1);
            LinkDO linkDO = baseMapper.selectOne(linkDOLambdaQueryWrapper);
            if (linkDO != null) {
                try {
                    stringRedisTemplate.opsForValue().set(String.format(GOTO_SHORT_LINK_KEY, fullShortUrl), linkDO.getOriginUrl());
                    response.sendRedirect(linkDO.getOriginUrl());

                } catch (Exception e) {
                    throw new ServiceException("服务端异常:跳转失败");
                }
            }


        } catch (Exception e) {
            throw new ServiceException("服务端异常:跳转失败");
        } finally {
            lock.unlock();
        }

    }

    private String generateSuffix(ShortLinkCreateReqDTO shortLinkCreateReqDTO) {
        int customGenerateCount = 0;
        String shortUri;
        while (true) {
            if (customGenerateCount > 10) {
                throw new ServiceException("短链接频繁生成，请稍后重试。");
            }
            String originUrl = shortLinkCreateReqDTO.getOriginUrl();
            originUrl += System.currentTimeMillis();
            //注意 这一步可能会存在冲突
            shortUri = HashUtil.hashToBase62(originUrl);
            //在下一步 redis 中判断是否 存在 冲突
            //这里 如果不存在 就 一定   ！！在布隆过滤器中！！  不存在
            //但是如果多个相同的uri 到这个位置  有可能 都会放行!!!!!!!!!!!!!!!!!!!
            if (!shortUriCreateCachePenetrationBloomFilter.contains(shortLinkCreateReqDTO.getDomain() + "/" + shortUri)) {
                break;
            }
            customGenerateCount++;

        }
        return shortUri;
    }
}




