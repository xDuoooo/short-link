package com.xduo.shortlink.project.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateTime;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xduo.shortlink.project.dao.entity.LinkDO;
import com.xduo.shortlink.project.dao.mapper.LinkMapper;
import com.xduo.shortlink.project.dto.req.*;
import com.xduo.shortlink.project.dto.resp.ShortLinkPageRespDTO;
import com.xduo.shortlink.project.service.RecycleBinService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import static com.xduo.shortlink.project.common.constant.RedisKeyConstant.GOTO_IS_NULL_SHORT_LINK_KEY;
import static com.xduo.shortlink.project.common.constant.RedisKeyConstant.GOTO_SHORT_LINK_HASH_KEY;

/**
 * 回收站管理接口实现层
 */
@Service
@RequiredArgsConstructor
public class RecycleBinServiceImpl extends ServiceImpl<LinkMapper, LinkDO> implements RecycleBinService {

    private final StringRedisTemplate stringRedisTemplate;

    /**
     * 保存到回收站（删除短链接）
     *
     * @param recycleBinSaveReqDTO
     */
    @Override
    public void saveRecycleBin(RecycleBinSaveReqDTO recycleBinSaveReqDTO) {
        LambdaUpdateWrapper<LinkDO> lambdaUpdateWrapper = Wrappers.lambdaUpdate(LinkDO.class)
                .eq(LinkDO::getGid, recycleBinSaveReqDTO.getGid())
                .eq(LinkDO::getFullShortUrl, recycleBinSaveReqDTO.getFullShortUrl())
                .eq(LinkDO::getEnableStatus, 1)
                .eq(LinkDO::getDelFlag, 0);
        LinkDO linkDO = LinkDO.builder().enableStatus(0).delTime(DateTime.now().getTime()).build();
        update(linkDO, lambdaUpdateWrapper);
        //删除Hash缓存，设置空值缓存
        stringRedisTemplate.delete(String.format(GOTO_SHORT_LINK_HASH_KEY, recycleBinSaveReqDTO.getFullShortUrl()));
        stringRedisTemplate.opsForValue().set(String.format(GOTO_IS_NULL_SHORT_LINK_KEY, recycleBinSaveReqDTO.getFullShortUrl()), "-", 30, java.util.concurrent.TimeUnit.SECONDS);
    }

    /**
     * 分页查询回收站短链接 - 性能优化版本
     */
    @Override
    public IPage<ShortLinkPageRespDTO> pageShortLink(ShortLinkRecycleBinPageReqDTO shortLinkPageReqDTO) {
        // 使用自定义SQL查询，避免MyBatis Plus生成的IN查询导致全表扫描
        IPage<LinkDO> resultPage = baseMapper.pageRecycleBinLinkOptimized(shortLinkPageReqDTO);
        return resultPage.convert(each -> {
            ShortLinkPageRespDTO bean = BeanUtil.toBean(each, ShortLinkPageRespDTO.class);
            bean.setDomain(bean.getDomain());
            return bean;
        });
    }

    /**
     * 从回收站中恢复
     * @param recycleBinRecoverReqDTO
     */
    @Override
    public void recoverRecycleBin(RecycleBinRecoverReqDTO recycleBinRecoverReqDTO) {
        LambdaUpdateWrapper<LinkDO> lambdaUpdateWrapper = Wrappers.lambdaUpdate(LinkDO.class)
                .eq(LinkDO::getGid, recycleBinRecoverReqDTO.getGid())
                .eq(LinkDO::getFullShortUrl, recycleBinRecoverReqDTO.getFullShortUrl())
                .eq(LinkDO::getEnableStatus, 0)
                .eq(LinkDO::getDelFlag, 0);
        LinkDO linkDO = LinkDO.builder().enableStatus(1).build();
        update(linkDO, lambdaUpdateWrapper);
        //删除空值缓存，重新构建Hash缓存
        stringRedisTemplate.delete(String.format(GOTO_IS_NULL_SHORT_LINK_KEY, recycleBinRecoverReqDTO.getFullShortUrl()));
        rebuildShortLinkCache(recycleBinRecoverReqDTO.getFullShortUrl(), recycleBinRecoverReqDTO.getGid());
    }

    /**
     * 软删除（从回收站彻底删除）
     * @param recycleBinRemoveReqDTO
     */
    @Override
    public void removeRecycleBin(RecycleBinRemoveReqDTO recycleBinRemoveReqDTO) {
        LambdaUpdateWrapper<LinkDO> updateWrapper = Wrappers.lambdaUpdate(LinkDO.class)
                .eq(LinkDO::getFullShortUrl, recycleBinRemoveReqDTO.getFullShortUrl())
                .eq(LinkDO::getGid, recycleBinRemoveReqDTO.getGid())
                .eq(LinkDO::getEnableStatus, 0)  // 回收站中的记录是禁用状态
                .eq(LinkDO::getDelFlag, 0)
                .set(LinkDO::getDelFlag, 1)
                .set(LinkDO::getDelTime, System.currentTimeMillis());
        
        int updateCount = baseMapper.update(null, updateWrapper);
        if (updateCount == 0) {
            throw new com.xduo.shortlink.project.common.convention.exception.ServiceException("短链接不存在或不在回收站中");
        }
        
        // 删除相关缓存
        stringRedisTemplate.delete(String.format(GOTO_SHORT_LINK_HASH_KEY, recycleBinRemoveReqDTO.getFullShortUrl()));
        stringRedisTemplate.delete(String.format(GOTO_IS_NULL_SHORT_LINK_KEY, recycleBinRemoveReqDTO.getFullShortUrl()));
    }
    
    /**
     * 重新构建短链接缓存
     */
    private void rebuildShortLinkCache(String fullShortUrl, String gid) {
        try {
            LambdaQueryWrapper<LinkDO> queryWrapper = Wrappers.lambdaQuery(LinkDO.class)
                    .eq(LinkDO::getFullShortUrl, fullShortUrl)
                    .eq(LinkDO::getGid, gid)
                    .eq(LinkDO::getDelFlag, 0)
                    .eq(LinkDO::getEnableStatus, 1);
            
            LinkDO linkDO = baseMapper.selectOne(queryWrapper);
            if (linkDO != null) {
                java.util.Map<String, String> linkInfo = new java.util.HashMap<>();
                linkInfo.put("originUrl", linkDO.getOriginUrl());
                linkInfo.put("gid", linkDO.getGid());
                linkInfo.put("enableStatus", String.valueOf(linkDO.getEnableStatus()));
                linkInfo.put("validDate", linkDO.getValidDate() != null ? String.valueOf(linkDO.getValidDate().getTime()) : "0");
                stringRedisTemplate.opsForHash().putAll(String.format(GOTO_SHORT_LINK_HASH_KEY, fullShortUrl), linkInfo);
                stringRedisTemplate.expire(String.format(GOTO_SHORT_LINK_HASH_KEY, fullShortUrl), 
                    com.xduo.shortlink.project.util.LinkUtil.getLinkCacheValidDate(linkDO.getValidDate()), 
                    java.util.concurrent.TimeUnit.MILLISECONDS);
            }
        } catch (Exception e) {
            // 记录日志但不抛出异常
        }
    }
}
