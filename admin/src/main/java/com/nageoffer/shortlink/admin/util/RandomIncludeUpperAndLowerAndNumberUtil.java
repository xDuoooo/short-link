package com.nageoffer.shortlink.admin.util;

import cn.hutool.core.util.RandomUtil;

public class RandomIncludeUpperAndLowerAndNumberUtil {
    // 定义大小写字母和数字的字符集合
    private static final String CHAR_SET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    /**
     * 生成指定长度的随机字符串
     *
     * @param length 随机字符串长度
     * @return 随机字符串
     */
    public static String generate(int length) {
        if (length <= 0) {
            throw new IllegalArgumentException("长度必须大于0");
        }
        return RandomUtil.randomString(CHAR_SET, length);
    }
}
