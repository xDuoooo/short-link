package com.xduo.shortlink.project.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 短链接不存在跳转控制器
 */
@Controller
public class ShortLinkNotFoundController {
    /**
     * 短链接不存在跳转页面
     * @return
     */
    @RequestMapping("/page/notFound")
    public String notFound(){
        return "notFound";
    }
}
