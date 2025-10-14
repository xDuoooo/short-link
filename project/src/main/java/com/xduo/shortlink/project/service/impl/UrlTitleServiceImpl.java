package com.xduo.shortlink.project.service.impl;

import com.xduo.shortlink.project.service.UrlTitleService;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URL;

@Slf4j
@Service
public class UrlTitleServiceImpl implements UrlTitleService {
    
    @Override
    public String getTitleByUrl(String url) {
        try {
            // 使用Jsoup直接连接，设置超时和User-Agent
            Document document = Jsoup.connect(url)
                    .timeout(10000) // 10秒超时
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                    .followRedirects(true) // 跟随重定向
                    .get();
            
            String title = document.title();
            return title != null && !title.trim().isEmpty() ? title : "无标题";
            
        } catch (IOException e) {
            log.error("获取URL标题失败: {}, 错误: {}", url, e.getMessage());
            return "获取目标网站标题错误";
        } catch (Exception e) {
            log.error("获取URL标题时发生未知错误: {}, 错误: {}", url, e.getMessage());
            return "获取目标网站标题错误";
        }
    }
}

