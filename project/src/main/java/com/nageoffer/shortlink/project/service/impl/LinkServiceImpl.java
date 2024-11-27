package com.nageoffer.shortlink.project.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.Week;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.text.StrBuilder;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nageoffer.shortlink.project.common.convention.exception.ServiceException;
import com.nageoffer.shortlink.project.common.database.BaseDO;
import com.nageoffer.shortlink.project.common.enums.ValidateTypeEnum;
import com.nageoffer.shortlink.project.dao.entity.*;
import com.nageoffer.shortlink.project.dao.mapper.*;
import com.nageoffer.shortlink.project.dto.req.ShortLinkCreateReqDTO;
import com.nageoffer.shortlink.project.dto.req.ShortLinkPageReqDTO;
import com.nageoffer.shortlink.project.dto.req.ShortLinkUpdateReqDTO;
import com.nageoffer.shortlink.project.dto.resp.ShortLinkCreateRespDTO;
import com.nageoffer.shortlink.project.dto.resp.ShortLinkGroupCountQueryRespDTO;
import com.nageoffer.shortlink.project.dto.resp.ShortLinkPageRespDTO;
import com.nageoffer.shortlink.project.service.LinkService;
import com.nageoffer.shortlink.project.util.HashUtil;
import com.nageoffer.shortlink.project.util.LinkUtil;
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

import static com.nageoffer.shortlink.project.common.constant.RedisKeyConstant.*;
import static com.nageoffer.shortlink.project.common.constant.ShortLinkConstant.AMOP_REMOTE_URL;

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
    @Value("${short-link.stats.locale.amap-key}")
    private String statsAmapKey;

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
                .favicon(getFavicon(shortLinkCreateReqDTO.getOriginUrl()))
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
        stringRedisTemplate.opsForValue().set(String.format(GOTO_SHORT_LINK_KEY, fullShortUrl), shortLinkCreateReqDTO.getOriginUrl(), LinkUtil.getLinkCacheValidDate(shortLinkCreateReqDTO.getValidDate()), TimeUnit.MILLISECONDS);
        shortUriCreateCachePenetrationBloomFilter.add(fullShortUrl);

        return ShortLinkCreateRespDTO.builder()
                .fullShortUrl("http://" + shortLinkDO.getFullShortUrl())
                .originUrl(shortLinkDO.getOriginUrl()).build();
    }

    /**
     * 分页查询短链接
     */
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
                shortLinkStats(fullShortUrl, null, request, response);
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
                shortLinkStats(fullShortUrl, null, request, response);
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
                shortLinkStats(fullShortUrl, linkDO.getGid(), request, response);
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

    //短链接后缀，只放在布隆过滤器中
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
            //但是如果多个相同的uri 到这个位置  有可能 都会放行!!!!!!!!!!!!!!!!!!! 存的是 域名+ 短链接
            if (!shortUriCreateCachePenetrationBloomFilter.contains(shortLinkCreateReqDTO.getDomain() + "/" + shortUri)) {
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


    private void shortLinkStats(String fullShortUrl, String gid, HttpServletRequest request, HttpServletResponse response) {
        //处理uv 利用 cookie
        //获取cookie
        //TODO 这里redis 有问题待修正
        //问题如下Cookie中键"uv"有值不就代表不是新用户吗？为啥还要操作redis？
        //2024-01-16 21:47
        //栗子ing 回复 Mirac：感觉需要设置过期时间，然后操作redis就合理了
        //2024-01-23 12:37
        //暂无爱人 回复 Mirac：Cookie中 uv有值不代表是老用户，可能是用户第一次跳转过去，这时uv要+1，所以需要在redis中判断一下如果存在，说明老用户，不存在说明新用户，uv+1。
        //2024-02-18 20:01
        //乐忧忘忧 回复 Mirac：我的理解是该用户可能访问过别的短链接存入过uv值，所以需要判断该用户是不是第一次访问这个短链接
        //2024-03-03 15:34
        //yuemo 回复 乐忧忘忧：不是的，这里设置了cookie.setPath为当前短链接，访问其他短链接的uv不会被携带
        //2024-03-15 20:02
        //。。。。。。 回复 Mirac：我也想问，cookie中有uv不就已经说明了用户已经访问过了吗？
        //2024-03-16 20:38
        //给趣多多巧克力豆 回复 Mirac：因为用户可以访问多个短链接，那么无法仅仅通过cookie是否存在，来判断是否要将当前访问的短连接uv不加1，而是要通过一个key为当前短链接的set，来判断此set是否包含当前cookie，不包含，依然要将当前短链接uv加1
        //2024-03-27 10:47
        //郎同学 回复 给趣多多巧克力豆：代码里面设置了setpath，多个短链接cookie是不共享的, 如/xx1和/xx2两个链接cookie互不相干，如果/xx1 cookie存在，用户肯定不是第一次访问，所以不需要redis set来判断吧
        //2024-04-09 16:45
        //马丁 回复 Mirac：这个我想想，感觉可以改为如果没带uv就应该+1，如果携带了忽略
        //2024-04-15 13:24
        //GP 回复 马丁：方案一：Cookie的Path作用域为/，需要引入redis的set数据结构进行判断。
        //方案二：Cookie的Path作用域为短链接的标识（shortUri），判断是否是携带了该Cookie，得知是否是第一次访问。个人认为第二种方法更好。
        //贴一个Cookie的作用域链接Cookie · 语雀 《Cookie》
        Cookie[] cookies = request.getCookies();
        String uv = UUID.fastUUID().toString();
        //有flag标识
        AtomicBoolean uvFirstFlag = new AtomicBoolean();
        Date date = new Date();
        try {
            Runnable addResponseCookieTask = () -> {
                Cookie uvCookie = new Cookie("uv", uv);
                uvCookie.setMaxAge(60 * 60 * 24 * 30);//该cookie有效期30天
                uvCookie.setPath(StringUtil.substring(fullShortUrl, fullShortUrl.indexOf("/"), fullShortUrl.length()));
                response.addCookie(uvCookie);
                uvFirstFlag.set(Boolean.TRUE);
                stringRedisTemplate.opsForSet().add("short-link:stats:uv" + fullShortUrl, uv);
            };
            if (ArrayUtil.isNotEmpty(cookies)) {
                Arrays.stream(cookies).filter(each -> Objects.equals(each.getName(), "uv"))
                        .findFirst()
                        .map(Cookie::getValue)
                        .ifPresentOrElse(each -> {
                            Long uvAdded = stringRedisTemplate.opsForSet().add("short-link:stats:uv" + fullShortUrl, each);
                            //added表示成功添加的元素的数量
                            uvFirstFlag.set(uvAdded != null && uvAdded > 0L);
                        }, addResponseCookieTask);
            } else {
                //不存在cookie
                //获取cookie并返回前端
                addResponseCookieTask.run();
            }
            String ip = LinkUtil.getIp(request);
            Long uvAdded = stringRedisTemplate.opsForSet().add("short-link:stats:uip" + fullShortUrl, ip);
            boolean uipFirstFlag = uvAdded != null && uvAdded > 0L;
            if (StrUtil.isBlank(gid)) {
                LambdaQueryWrapper<LinkGoToDO> queryWrapper = Wrappers.lambdaQuery(LinkGoToDO.class)
                        .eq(LinkGoToDO::getFullShortUrl, fullShortUrl);
                LinkGoToDO shortLinkGotoDO = linkGoToMapper.selectOne(queryWrapper);
                gid = shortLinkGotoDO.getGid();
            }
            int hour = DateUtil.hour(date, true);
            Week week = DateUtil.dayOfWeekEnum(date);
            int weekValue = week.getIso8601Value();
            LinkAccessStatsDO linkAccessStatsDO = LinkAccessStatsDO.builder()
                    .pv(1)
                    .uv(uvFirstFlag.get() ? 1 : 0)
                    .uip(uipFirstFlag ? 1 : 0)
                    .hour(hour)
                    .weekday(weekValue)
                    .fullShortUrl(fullShortUrl)
                    .gid(gid)
                    .date(date)
                    .build();
            linkAccessStatsDO.setDelFlag(0);
            linkAccessStatsDO.setCreateTime(date);
            linkAccessStatsDO.setUpdateTime(date);
            linkAccessStatsMapper.shortLinkStats(linkAccessStatsDO);
            Map<String, Object> localeGetParam = new HashMap<>();
            localeGetParam.put("ip", ip);
            localeGetParam.put("key", statsAmapKey);
            String localeResultStr = HttpUtil.get(AMOP_REMOTE_URL, localeGetParam);
            JSONObject localeResultObj = JSON.parseObject(localeResultStr);
            String infoCode = localeResultObj.getString("infocode");
            if (StrUtil.isNotBlank(infoCode) && StrUtil.equals(infoCode, "10000")) {
                String province = localeResultObj.getString("province");
                boolean unknownProvinceFlag = StrUtil.equals(province, "[]");
                LinkLocaleStatsDO linkLocaleStatsDO = LinkLocaleStatsDO.builder()
                        .province(unknownProvinceFlag ? "未知" : province)
                        .city(unknownProvinceFlag ? "未知" : localeResultObj.getString("city"))
                        .adcode(unknownProvinceFlag ? "未知" : localeResultObj.getString("adcode"))
                        .cnt(1)
                        .fullShortUrl(fullShortUrl)
                        .country("中国")
                        .gid(gid)
                        .date(new Date()).build();
                linkLocaleStatsDO.setCreateTime(date);
                linkLocaleStatsDO.setUpdateTime(date);
                linkLocaleStatsDO.setDelFlag(0);
                linkLocaleStatsMapper.shortLinkStats(linkLocaleStatsDO);
            }
            String os = LinkUtil.getOs(request);
            LinkOsStatsDO linkOsStatsDO = LinkOsStatsDO.builder()
                    .os(os)
                    .cnt(1)
                    .fullShortUrl(fullShortUrl)
                    .gid(gid)
                    .date(date)
                    .createTime(date)
                    .updateTime(date)
                    .delFlag(0)
                    .build();
            linkOsStatsMapper.shortLinkStats(linkOsStatsDO);
            String browser = LinkUtil.getBrowser(request);
            LinkBrowserStats linkBrowserStats = LinkBrowserStats.builder()
                    .browser(browser)
                    .cnt(1)
                    .fullShortUrl(fullShortUrl)
                    .gid(gid)
                    .date(date)
                    .createTime(date)
                    .updateTime(date)
                    .delFlag(0)
                    .build();
            linkBrowserStatsMapper.shortLinkStats(linkBrowserStats);


        } catch (Throwable ex) {
            log.error("短链接访问量统计异常", ex);
        }
    }
}




