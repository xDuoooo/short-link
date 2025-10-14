package com.xduo.shortlink.project.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.text.StrBuilder;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xduo.shortlink.project.common.convention.exception.ClientException;
import com.xduo.shortlink.project.common.convention.exception.ServiceException;
import com.xduo.shortlink.project.common.database.BaseDO;
import com.xduo.shortlink.project.common.enums.ValidateTypeEnum;
import com.xduo.shortlink.project.config.GotoDomainWhiteListConfiguration;
import com.xduo.shortlink.project.dao.entity.*;
import com.xduo.shortlink.project.dao.mapper.*;
import com.xduo.shortlink.project.dto.req.*;
import com.xduo.shortlink.project.dto.resp.*;
import com.xduo.shortlink.project.mq.producer.ShortLinkStatsSaveProducer;
import com.xduo.shortlink.project.service.LinkService;
import com.xduo.shortlink.project.service.LinkStatsTodayService;
import com.xduo.shortlink.project.util.HashUtil;
import com.xduo.shortlink.project.util.LinkUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jodd.util.StringUtil;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static com.xduo.shortlink.project.common.constant.RedisKeyConstant.*;
import static com.xduo.shortlink.project.common.constant.ShortLinkConstant.UV;

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

    private final LinkAccessStatsMapper linkAccessStatsMapper;

    private final LinkLocaleStatsMapper linkLocaleStatsMapper;

    private final LinkOsStatsMapper linkOsStatsMapper;

    private final LinkBrowserStatsMapper linkBrowserStatsMapper;

    private final LinkAccessLogsMapper linkAccessLogsMapper;

    private final LinkDeviceStatsMapper linkDeviceStatsMapper;

    private final LinkNetworkStatsMapper linkNetworkStatsMapper;

    private final LinkStatsTodayMapper linkStatsTodayMapper;

    private final GotoDomainWhiteListConfiguration gotoDomainWhiteListConfiguration;

    private final ShortLinkStatsSaveProducer shortLinkStatsSaveProducer;

    private final LinkStatsTodayService linkStatsTodayService;

    @Value("${short-link.domain.default}")
    private String defaultDomain;

    @Override
    @Transactional
    public ShortLinkCreateRespDTO createShortLink(ShortLinkCreateReqDTO shortLinkCreateReqDTO) {
        // 验证 gid 字段不能为空
        if (shortLinkCreateReqDTO.getGid() == null || StrUtil.isBlank(shortLinkCreateReqDTO.getGid())) {
            throw new ClientException("分组标识不能为空");
        }
        verificationWhitelist(shortLinkCreateReqDTO.getOriginUrl());
        String shortLinkSuffix = generateSuffix(shortLinkCreateReqDTO);
        // 使用前端传递的domain参数，如果为空则使用默认域名
        String domain = StrUtil.isNotBlank(shortLinkCreateReqDTO.getDomain()) 
                ? shortLinkCreateReqDTO.getDomain() 
                : defaultDomain;
        String fullShortUrl = StrBuilder.create(domain)
                .append("/")
                .append(shortLinkSuffix)
                .toString();
        LinkDO shortLinkDO = LinkDO.builder()
                .domain(domain)
                .originUrl(shortLinkCreateReqDTO.getOriginUrl())
                .gid(shortLinkCreateReqDTO.getGid())
                .createdType(shortLinkCreateReqDTO.getCreatedType())
                .validDateType(shortLinkCreateReqDTO.getValidDateType())
                .validDate(shortLinkCreateReqDTO.getValidDate())
                .describe(shortLinkCreateReqDTO.getDescribe())
                .shortUri(shortLinkSuffix)
                .favicon(getFavicon(shortLinkCreateReqDTO.getOriginUrl()))
                .enableStatus(1)
                .totalPv(0)
                .totalUip(0)
                .totalUv(0)
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

                log.warn("短链接：{} 重复入库", fullShortUrl);
                throw new ServiceException("短链接生成重复:" + fullShortUrl);
        }
        stringRedisTemplate.opsForValue().set(String.format(GOTO_SHORT_LINK_KEY, fullShortUrl), shortLinkCreateReqDTO.getOriginUrl(), LinkUtil.getLinkCacheValidDate(shortLinkCreateReqDTO.getValidDate()), TimeUnit.MILLISECONDS);
        shortUriCreateCachePenetrationBloomFilter.add(fullShortUrl);

        return ShortLinkCreateRespDTO.builder()
                .fullShortUrl(shortLinkDO.getFullShortUrl())
                .originUrl(shortLinkDO.getOriginUrl()).build();
    }

    /**
     * 分页查询短链接
     */
    @Override
    public IPage<ShortLinkPageRespDTO> pageShortLink(ShortLinkPageReqDTO shortLinkPageReqDTO) {


        IPage<LinkDO> resultPage = baseMapper.pageLink(shortLinkPageReqDTO);
        return resultPage.convert(each -> {
            ShortLinkPageRespDTO bean = BeanUtil.toBean(each, ShortLinkPageRespDTO.class);
            bean.setDomain(bean.getDomain());
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
        //写锁

        verificationWhitelist(requestParam.getOriginUrl());
        LambdaQueryWrapper<LinkDO> lambdaQueryWrapper = Wrappers.lambdaQuery(LinkDO.class)
                .eq(LinkDO::getFullShortUrl, requestParam.getFullShortUrl())
                .eq(LinkDO::getGid, requestParam.getOriginGid())
                .eq(BaseDO::getDelFlag, 0)
                .eq(LinkDO::getEnableStatus, 1);
        LinkDO selectedOne = baseMapper.selectOne(lambdaQueryWrapper);
        if (selectedOne == null) {
            throw new ServiceException("短链接记录不存在");
        }

        if (Objects.equals(selectedOne.getGid(), requestParam.getGid())) {
            LambdaUpdateWrapper<LinkDO> wrapper = Wrappers.lambdaUpdate(LinkDO.class)
                    .eq(LinkDO::getFullShortUrl, requestParam.getFullShortUrl())
                    .eq(LinkDO::getGid, requestParam.getGid())
                    .eq(LinkDO::getDelFlag, 0)
                    .eq(LinkDO::getEnableStatus, 1).set(Objects.equals(requestParam.getValidDateType(), ValidateTypeEnum.PERMANENT.getType()), LinkDO::getValidDate, null);
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

            baseMapper.update(linkDO, wrapper);
        } else {
            RReadWriteLock readWriteLock = redissonClient.getReadWriteLock(String.format(LOCK_GID_UPDATE_KEY, requestParam.getFullShortUrl()));
            RLock rLock = readWriteLock.writeLock();
            if (!rLock.tryLock()) {
                throw new ServiceException("短链接正在被访问，请稍后再试...");
            }
            try {
                LambdaUpdateWrapper<LinkDO> linkUpdateWrapper = Wrappers.lambdaUpdate(LinkDO.class)
                        .eq(LinkDO::getFullShortUrl, requestParam.getFullShortUrl())
                        .eq(LinkDO::getGid, selectedOne.getGid())
                        .eq(LinkDO::getDelFlag, 0)
                        .eq(LinkDO::getDelTime, 0L)
                        .set(LinkDO::getDelFlag, 1)
                        .eq(LinkDO::getEnableStatus, 1);
                LinkDO delShortLinkDO = LinkDO.builder()
                        .delTime(System.currentTimeMillis())
                        .delFlag(1)
                        .build();
                baseMapper.update(delShortLinkDO, linkUpdateWrapper);


                LinkDO shortLinkDO = LinkDO.builder()
                        .domain(defaultDomain)
                        .originUrl(requestParam.getOriginUrl())
                        .gid(requestParam.getGid())
                        .createdType(selectedOne.getCreatedType())
                        .validDateType(requestParam.getValidDateType())
                        .validDate(requestParam.getValidDate())
                        .describe(requestParam.getDescribe())
                        .shortUri(selectedOne.getShortUri())
                        .enableStatus(selectedOne.getEnableStatus())
                        .totalPv(selectedOne.getTotalPv())
                        .totalUv(selectedOne.getTotalUv())
                        .totalUip(selectedOne.getTotalUip())
                        .fullShortUrl(selectedOne.getFullShortUrl())
                        .favicon(getFavicon(requestParam.getOriginUrl()))
                        .delTime(0L)
                        .build();
                baseMapper.insert(shortLinkDO);
                LambdaQueryWrapper<LinkStatsTodayDO> statsTodayQueryWrapper = Wrappers.lambdaQuery(LinkStatsTodayDO.class)
                        .eq(LinkStatsTodayDO::getFullShortUrl, requestParam.getFullShortUrl())
                        .eq(LinkStatsTodayDO::getGid, selectedOne.getGid())
                        .eq(LinkStatsTodayDO::getDelFlag, 0);
                List<LinkStatsTodayDO> linkStatsTodayDOList = linkStatsTodayMapper.selectList(statsTodayQueryWrapper);
                if (CollUtil.isNotEmpty(linkStatsTodayDOList)) {
                    linkStatsTodayMapper.deleteBatchIds(linkStatsTodayDOList.stream()
                            .map(LinkStatsTodayDO::getId)
                            .toList()
                    );
                    linkStatsTodayDOList.forEach(each -> each.setGid(requestParam.getGid()));
                    linkStatsTodayService.saveBatch(linkStatsTodayDOList);
                }
                LambdaQueryWrapper<LinkGoToDO> linkGotoQueryWrapper = Wrappers.lambdaQuery(LinkGoToDO.class)
                        .eq(LinkGoToDO::getFullShortUrl, requestParam.getFullShortUrl())
                        .eq(LinkGoToDO::getGid, selectedOne.getGid());
                LinkGoToDO shortLinkGotoDO = linkGoToMapper.selectOne(linkGotoQueryWrapper);
                linkGoToMapper.deleteById(shortLinkGotoDO.getId());
                shortLinkGotoDO.setGid(requestParam.getGid());
                linkGoToMapper.insert(shortLinkGotoDO);
                LambdaUpdateWrapper<LinkAccessStatsDO> linkAccessStatsUpdateWrapper = Wrappers.lambdaUpdate(LinkAccessStatsDO.class)
                        .eq(LinkAccessStatsDO::getFullShortUrl, requestParam.getFullShortUrl())
                        .eq(LinkAccessStatsDO::getGid, selectedOne.getGid())
                        .eq(LinkAccessStatsDO::getDelFlag, 0);
                LinkAccessStatsDO linkAccessStatsDO = LinkAccessStatsDO.builder()
                        .gid(requestParam.getGid())
                        .build();
                linkAccessStatsMapper.update(linkAccessStatsDO, linkAccessStatsUpdateWrapper);
                LambdaUpdateWrapper<LinkLocaleStatsDO> linkLocaleStatsUpdateWrapper = Wrappers.lambdaUpdate(LinkLocaleStatsDO.class)
                        .eq(LinkLocaleStatsDO::getFullShortUrl, requestParam.getFullShortUrl())
                        .eq(LinkLocaleStatsDO::getGid, selectedOne.getGid())
                        .eq(LinkLocaleStatsDO::getDelFlag, 0);
                LinkLocaleStatsDO linkLocaleStatsDO = LinkLocaleStatsDO.builder()
                        .gid(requestParam.getGid())
                        .build();
                linkLocaleStatsMapper.update(linkLocaleStatsDO, linkLocaleStatsUpdateWrapper);
                LambdaUpdateWrapper<LinkOsStatsDO> linkOsStatsUpdateWrapper = Wrappers.lambdaUpdate(LinkOsStatsDO.class)
                        .eq(LinkOsStatsDO::getFullShortUrl, requestParam.getFullShortUrl())
                        .eq(LinkOsStatsDO::getGid, selectedOne.getGid())
                        .eq(LinkOsStatsDO::getDelFlag, 0);
                LinkOsStatsDO linkOsStatsDO = LinkOsStatsDO.builder()
                        .gid(requestParam.getGid())
                        .build();
                linkOsStatsMapper.update(linkOsStatsDO, linkOsStatsUpdateWrapper);
                LambdaUpdateWrapper<LinkBrowserStatsDO> linkBrowserStatsUpdateWrapper = Wrappers.lambdaUpdate(LinkBrowserStatsDO.class)
                        .eq(LinkBrowserStatsDO::getFullShortUrl, requestParam.getFullShortUrl())
                        .eq(LinkBrowserStatsDO::getGid, selectedOne.getGid())
                        .eq(LinkBrowserStatsDO::getDelFlag, 0);
                LinkBrowserStatsDO linkBrowserStatsDO = LinkBrowserStatsDO.builder()
                        .gid(requestParam.getGid())
                        .build();
                linkBrowserStatsMapper.update(linkBrowserStatsDO, linkBrowserStatsUpdateWrapper);
                LambdaUpdateWrapper<LinkDeviceStatsDO> linkDeviceStatsUpdateWrapper = Wrappers.lambdaUpdate(LinkDeviceStatsDO.class)
                        .eq(LinkDeviceStatsDO::getFullShortUrl, requestParam.getFullShortUrl())
                        .eq(LinkDeviceStatsDO::getGid, selectedOne.getGid())
                        .eq(LinkDeviceStatsDO::getDelFlag, 0);
                LinkDeviceStatsDO linkDeviceStatsDO = LinkDeviceStatsDO.builder()
                        .gid(requestParam.getGid())
                        .build();
                linkDeviceStatsMapper.update(linkDeviceStatsDO, linkDeviceStatsUpdateWrapper);
                LambdaUpdateWrapper<LinkNetworkStatsDO> linkNetworkStatsUpdateWrapper = Wrappers.lambdaUpdate(LinkNetworkStatsDO.class)
                        .eq(LinkNetworkStatsDO::getFullShortUrl, requestParam.getFullShortUrl())
                        .eq(LinkNetworkStatsDO::getGid, selectedOne.getGid())
                        .eq(LinkNetworkStatsDO::getDelFlag, 0);
                LinkNetworkStatsDO linkNetworkStatsDO = LinkNetworkStatsDO.builder()
                        .gid(requestParam.getGid())
                        .build();
                linkNetworkStatsMapper.update(linkNetworkStatsDO, linkNetworkStatsUpdateWrapper);
                LambdaUpdateWrapper<LinkAccessLogsDO> linkAccessLogsUpdateWrapper = Wrappers.lambdaUpdate(LinkAccessLogsDO.class)
                        .eq(LinkAccessLogsDO::getFullShortUrl, requestParam.getFullShortUrl())
                        .eq(LinkAccessLogsDO::getGid, selectedOne.getGid())
                        .eq(LinkAccessLogsDO::getDelFlag, 0);
                LinkAccessLogsDO linkAccessLogsDO = LinkAccessLogsDO.builder()
                        .gid(requestParam.getGid())
                        .build();
                linkAccessLogsMapper.update(linkAccessLogsDO, linkAccessLogsUpdateWrapper);
            } finally {
                rLock.unlock();
            }
        }

        // 0 永久有效 1自定义
        //过期 -> 不过期 删缓存

        //不过期 —>不过期 缓存不动

        //过期 -> 过期 缓存不动

        //不过期 ->过期 删缓存
        //这里是 0 永久有效  -> 自定义有效期  且 自定义的有效期已经过期
        if (!Objects.equals(requestParam.getValidDateType(), selectedOne.getValidDateType()) && (requestParam.getValidDateType() == 1 && requestParam.getValidDate().before(new Date()))) {
            stringRedisTemplate.delete(String.format(GOTO_IS_NULL_SHORT_LINK_KEY, selectedOne.getFullShortUrl()));
            stringRedisTemplate.delete(String.format(GOTO_SHORT_LINK_KEY, selectedOne.getFullShortUrl()));
            return;
        }
        //自定义有效期已经过期的 -> 到 永久有效
        if (selectedOne.getValidDateType() == 1 && (selectedOne.getValidDate().before(new Date())) && requestParam.getValidDateType() == 0) {
            stringRedisTemplate.delete(String.format(GOTO_IS_NULL_SHORT_LINK_KEY, selectedOne.getFullShortUrl()));
            stringRedisTemplate.delete(String.format(GOTO_SHORT_LINK_KEY, selectedOne.getFullShortUrl()));
            return;
        }
        //自定义有效期 没过期 -> 过期
        if(selectedOne.getValidDateType() == 1 && requestParam.getValidDateType() == 1 && requestParam.getValidDate().before(new Date())&& selectedOne.getValidDate().after(new Date())){
            stringRedisTemplate.delete(String.format(GOTO_IS_NULL_SHORT_LINK_KEY, selectedOne.getFullShortUrl()));
            stringRedisTemplate.delete(String.format(GOTO_SHORT_LINK_KEY, selectedOne.getFullShortUrl()));
            return;
        }
        //自定义有效期 过期 -> 没过期
        if(selectedOne.getValidDateType() == 1 && requestParam.getValidDateType() == 1 && requestParam.getValidDate().after(new Date())&& selectedOne.getValidDate().before(new Date())){
            stringRedisTemplate.delete(String.format(GOTO_IS_NULL_SHORT_LINK_KEY, selectedOne.getFullShortUrl()));
            stringRedisTemplate.delete(String.format(GOTO_SHORT_LINK_KEY, selectedOne.getFullShortUrl()));
        }

    }

    @Override
    public void restoreUrl(String shortUri, HttpServletRequest request, HttpServletResponse response) {

        String serverName = request.getServerName();
        String fullShortUrl = serverName + "/" + shortUri;
        String originalLink = stringRedisTemplate.opsForValue().get(String.format(GOTO_SHORT_LINK_KEY, fullShortUrl));
        if (StrUtil.isNotBlank(originalLink)) {
            try {
                ShortLinkStatsRecordDTO statsRecord = buildLinkStatsRecordAndSetUser(fullShortUrl, request, response);
                shortLinkStats(fullShortUrl, null, statsRecord);
                response.sendRedirect(originalLink);
            } catch (Exception e) {
                throw new ServiceException("服务端异常:跳转失败");
            }
            return;
        }
        //查的是域名+短链接
        boolean contains = shortUriCreateCachePenetrationBloomFilter.contains(fullShortUrl);
        //如果不存在 那么就一定不存在 直接返回
        if (!contains) {
            try {
                response.sendRedirect("/page/notFound");
            } catch (Exception e) {
                throw new ServiceException("服务端异常:跳转失败");
            }
            return;
        }
        //缓存中存在，那么也有可能不存在，此时需要判断
        String gotoIsNullShortLink = stringRedisTemplate.opsForValue().get(String.format(GOTO_IS_NULL_SHORT_LINK_KEY, fullShortUrl));
        if (StrUtil.isNotBlank(gotoIsNullShortLink)) {
            //IS NULL
            try {
                response.sendRedirect("/page/notFound");
            } catch (Exception e) {
                throw new ServiceException("服务端异常:跳转失败");
            }
            return;
        }

        //记录不是NULL

        //缓存击穿 使用分布式锁解决
        RLock lock = redissonClient.getLock(String.format(LOCK_GOTO_SHORT_LINK_KEY, fullShortUrl));
        lock.lock();
        try {
            //双检 防止 大量请求在拿到锁前停滞
            originalLink = stringRedisTemplate.opsForValue().get(String.format(GOTO_SHORT_LINK_KEY, fullShortUrl));
            if (StrUtil.isNotBlank(originalLink)) {
                ShortLinkStatsRecordDTO statsRecord = buildLinkStatsRecordAndSetUser(fullShortUrl, request, response);
                shortLinkStats(fullShortUrl, null, statsRecord);
                response.sendRedirect(originalLink);
                return;
            }


            LambdaQueryWrapper<LinkGoToDO> linkGoToDOLambdaQueryWrapper = Wrappers.lambdaQuery(LinkGoToDO.class)
                    .eq(LinkGoToDO::getFullShortUrl, fullShortUrl);
            LinkGoToDO linkGoToDO = linkGoToMapper.selectOne(linkGoToDOLambdaQueryWrapper);
            if (linkGoToDO == null) {
                stringRedisTemplate.opsForValue().set(String.format(GOTO_IS_NULL_SHORT_LINK_KEY, fullShortUrl), "-", 30, TimeUnit.SECONDS);
                //严禁来说，此处需要进行风控
                try {
                    response.sendRedirect("/page/notFound");
                } catch (Exception e) {
                    throw new ServiceException("服务端异常:跳转失败");
                }
                return;
            }
            LambdaQueryWrapper<LinkDO> linkDOLambdaQueryWrapper = Wrappers.lambdaQuery(LinkDO.class)
                    .eq(LinkDO::getGid, linkGoToDO.getGid())
                    .eq(LinkDO::getFullShortUrl, fullShortUrl)
                    .eq(BaseDO::getDelFlag, 0)
                    .eq(LinkDO::getEnableStatus, 1);
            LinkDO linkDO = baseMapper.selectOne(linkDOLambdaQueryWrapper);
            if (linkDO == null || linkDO.getValidDate() != null && linkDO.getValidDate().before(new Date())) {
                //有效期判断
                //过了有效期
                stringRedisTemplate.opsForValue().set(String.format(GOTO_IS_NULL_SHORT_LINK_KEY, fullShortUrl), "-", 30, TimeUnit.SECONDS);
                try {
                    response.sendRedirect("/page/notFound");
                } catch (Exception e) {
                    throw new ServiceException("服务端异常:跳转失败");
                }
                return;
            }
            try {
                stringRedisTemplate.opsForValue().set(String.format(GOTO_SHORT_LINK_KEY, fullShortUrl), linkDO.getOriginUrl(), LinkUtil.getLinkCacheValidDate(linkDO.getValidDate()), TimeUnit.MILLISECONDS);
                ShortLinkStatsRecordDTO statsRecord = buildLinkStatsRecordAndSetUser(fullShortUrl, request, response);
                shortLinkStats(fullShortUrl, linkDO.getGid(), statsRecord);
                response.sendRedirect(linkDO.getOriginUrl());

            } catch (Exception e) {
                throw new ServiceException("服务端异常:跳转失败");
            }
        } catch (Exception e) {
            throw new ServiceException("服务端异常:跳转失败");
        } finally {
            lock.unlock();
        }

    }

    @Override
    public ShortLinkBatchCreateRespDTO batchCreateShortLink(ShortLinkBatchCreateReqDTO requestParam) {
        // 验证 gid 字段不能为空
        if (StrUtil.isBlank(requestParam.getGid())) {
            throw new ClientException("分组标识不能为空");
        }
        List<String> originUrls = requestParam.getOriginUrls();
        List<String> describes = requestParam.getDescribes();
        List<ShortLinkBaseInfoRespDTO> result = new ArrayList<>();
        for (int i = 0; i < originUrls.size(); i++) {
            ShortLinkCreateReqDTO shortLinkCreateReqDTO = BeanUtil.toBean(requestParam, ShortLinkCreateReqDTO.class);
            shortLinkCreateReqDTO.setOriginUrl(originUrls.get(i));
            shortLinkCreateReqDTO.setDescribe(describes.get(i));
            try {
                ShortLinkCreateRespDTO shortLink = createShortLink(shortLinkCreateReqDTO);
                ShortLinkBaseInfoRespDTO linkBaseInfoRespDTO = ShortLinkBaseInfoRespDTO.builder()
                        .fullShortUrl(shortLink.getFullShortUrl())
                        .originUrl(shortLink.getOriginUrl())
                        .describe(describes.get(i))
                        .build();
                result.add(linkBaseInfoRespDTO);
            } catch (Throwable ex) {
                log.error("批量创建短链接失败，原始参数：{}", originUrls.get(i));
            }
        }
        return ShortLinkBatchCreateRespDTO.builder()
                .total(result.size())
                .baseLinkInfos(result)
                .build();


    }

    //短链接后缀，只放在布隆过滤器中
    private String generateSuffix(ShortLinkCreateReqDTO shortLinkCreateReqDTO) {
        int customGenerateCount = 0;
        String shortUri;
        while (true) {
            if (customGenerateCount > 10) {
                throw new ServiceException("短链接频繁生成，请稍后重试。");
            }
            String originUrl = shortLinkCreateReqDTO.getOriginUrl();
            originUrl += UUID.randomUUID().toString();
            //注意 这一步可能会存在冲突
            shortUri = HashUtil.hashToBase62(originUrl);
            //在下一步 redis 中判断是否 存在 冲突
            //这里 如果不存在 就 一定   ！！在布隆过滤器中！！  不存在
            //但是如果多个相同的uri 到这个位置  有可能 都会放行!!!!!!!!!!!!!!!!!!! 存的是 域名+ 短链接
            if (!shortUriCreateCachePenetrationBloomFilter.contains(defaultDomain + "/" + shortUri)) {
                break;
            }
            customGenerateCount++;

        }
        return shortUri;
    }

    @SneakyThrows
    private String getFavicon(String url) {
        try {
            // 使用 Jsoup 直接连接网站（自动处理重定向）
            Document document = Jsoup.connect(url).followRedirects(true).get();

            // 尝试找到 favicon 的 <link> 标签
            Element faviconElement = document.selectFirst("link[rel~=(?i)^(shortcut|icon|apple-touch-icon)]");
            if (faviconElement != null) {
                return faviconElement.attr("abs:href"); // 返回完整的 favicon URL
            }
        } catch (IOException e) {
            // 打印错误信息或进行日志记录
            System.err.println("Error fetching favicon: " + e.getMessage());
        }
        return null; // 未找到 favicon
    }


    private ShortLinkStatsRecordDTO buildLinkStatsRecordAndSetUser(String fullShortUrl, HttpServletRequest request, HttpServletResponse response) {
        Cookie[] cookies = request.getCookies();
        //有flag标识
        AtomicBoolean uvFirstFlag = new AtomicBoolean();
        AtomicReference<String> uv = new AtomicReference<>(UUID.fastUUID().toString());
        Runnable addResponseCookieTask = () -> {
            Cookie uvCookie = new Cookie(UV, uv.get());
            uvCookie.setMaxAge(60 * 60 * 24 * 30);
            uvCookie.setPath(StringUtil.substring(fullShortUrl, fullShortUrl.indexOf("/"), fullShortUrl.length()));
            response.addCookie(uvCookie);
            uvFirstFlag.set(Boolean.TRUE);
            //TODO 后续可以考虑是否采用bitmap
            stringRedisTemplate.opsForSet().add(String.format(STATS_UV_KEY_PREFIX, fullShortUrl), uv.get());
        };
        if (ArrayUtil.isNotEmpty(cookies)) {
            Arrays.stream(cookies).filter(each -> Objects.equals(each.getName(), UV))
                    .findFirst()
                    .map(Cookie::getValue)
                    .ifPresentOrElse(each -> {
                        uv.set(each);
                        Long uvAdded = stringRedisTemplate.opsForSet().add(String.format(STATS_UV_KEY_PREFIX, fullShortUrl), each);
                        uvFirstFlag.set(uvAdded != null && uvAdded > 0L);
                    }, addResponseCookieTask);
        } else {
            //不存在cookie
            //获取cookie并返回前端
            addResponseCookieTask.run();
        }
        String remoteAddr = LinkUtil.getIp(request);
        Long uvAdded = stringRedisTemplate.opsForSet().add(STATS_UV_KEY_PREFIX + fullShortUrl, remoteAddr);
        boolean uipFirstFlag = uvAdded != null && uvAdded > 0L;
        String os = LinkUtil.getOs(request);
        String browser = LinkUtil.getBrowser(request);
        String device = LinkUtil.getDevice(request);
        String network = LinkUtil.getNetwork(request);
        return ShortLinkStatsRecordDTO.builder()
                .os(os)
                .uv(uv.get())
                .remoteAddr(remoteAddr)
                .network(network)
                .uipFirstFlag(uipFirstFlag)
                .uvFirstFlag(uvFirstFlag.get())
                .device(device)
                .fullShortUrl(fullShortUrl)
                .browser(browser)
                .build();


    }

    public void shortLinkStats(String fullShortUrl, String gid, ShortLinkStatsRecordDTO statsRecord) {
        //TODO 后续采用消息队列

        Map<String, String> producerMap = new HashMap<>();
        producerMap.put("fullShortUrl", fullShortUrl);
        producerMap.put("gid", gid);
        producerMap.put("statsRecord", JSON.toJSONString(statsRecord));
        shortLinkStatsSaveProducer.send(producerMap);
    }

    private void verificationWhitelist(String originUrl) {
        Boolean enable = gotoDomainWhiteListConfiguration.getEnable();
        if (enable == null || !enable) {
            return;
        }
        String domain = LinkUtil.extractDomain(originUrl);
        if (StrUtil.isBlank(domain)) {
            throw new ClientException("跳转链接填写错误");
        }
        List<String> details = gotoDomainWhiteListConfiguration.getDetails();
        if (!details.contains(domain)) {
            throw new ClientException("演示环境为避免恶意攻击，请生成以下网站跳转链接：" + gotoDomainWhiteListConfiguration.getNames());
        }
    }
}




