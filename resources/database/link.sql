-- =============================================
-- 短链接系统数据库表结构
-- 使用分片表设计，支持水平扩展
-- =============================================

-- =============================================
-- 1. 分组表 (t_group) - 16个分表
-- =============================================
CREATE TABLE `t_group_0`
(
    `id`          bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `gid`         varchar(32)  DEFAULT NULL COMMENT '分组标识',
    `name`        varchar(64)  DEFAULT NULL COMMENT '分组名称',
    `username`    varchar(256) DEFAULT NULL COMMENT '创建分组用户名',
    `sort_order`  int(3) DEFAULT NULL COMMENT '分组排序',
    `create_time` datetime     DEFAULT NULL COMMENT '创建时间',
    `update_time` datetime     DEFAULT NULL COMMENT '修改时间',
    `del_flag`    tinyint(1) DEFAULT NULL COMMENT '删除标识 0：未删除 1：已删除',
    PRIMARY KEY (`id`),
    KEY           `idx_username` (`username`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 使用LIKE创建其他分组表
CREATE TABLE `t_group_1` LIKE `t_group_0`;
CREATE TABLE `t_group_2` LIKE `t_group_0`;
CREATE TABLE `t_group_3` LIKE `t_group_0`;
CREATE TABLE `t_group_4` LIKE `t_group_0`;
CREATE TABLE `t_group_5` LIKE `t_group_0`;
CREATE TABLE `t_group_6` LIKE `t_group_0`;
CREATE TABLE `t_group_7` LIKE `t_group_0`;
CREATE TABLE `t_group_8` LIKE `t_group_0`;
CREATE TABLE `t_group_9` LIKE `t_group_0`;
CREATE TABLE `t_group_10` LIKE `t_group_0`;
CREATE TABLE `t_group_11` LIKE `t_group_0`;
CREATE TABLE `t_group_12` LIKE `t_group_0`;
CREATE TABLE `t_group_13` LIKE `t_group_0`;
CREATE TABLE `t_group_14` LIKE `t_group_0`;
CREATE TABLE `t_group_15` LIKE `t_group_0`;

-- 分组唯一标识表
CREATE TABLE `t_group_unique`
(
    `id`  bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `gid` varchar(32) DEFAULT NULL COMMENT '分组标识',
    PRIMARY KEY (`id`),
    UNIQUE KEY `idx_unique_gid` (`gid`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =============================================
-- 2. 短链接表 (t_link) - 16个分表
-- =============================================
CREATE TABLE `t_link_0`
(
    `id`              bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `domain`          varchar(128)                                   DEFAULT NULL COMMENT '域名',
    `short_uri`       varchar(8) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL COMMENT '短链接',
    `full_short_url`  varchar(128)                                   DEFAULT NULL COMMENT '完整短链接',
    `origin_url`      varchar(1024)                                  DEFAULT NULL COMMENT '原始链接',
    `click_num`       int(11) DEFAULT '0' COMMENT '点击量',
    `gid`             varchar(32)                                    DEFAULT 'default' COMMENT '分组标识',
    `favicon`         varchar(256)                                   DEFAULT NULL COMMENT '网站图标',
    `enable_status`   tinyint(1) DEFAULT NULL COMMENT '启用标识 0：启用 1：未启用',
    `created_type`    tinyint(1) DEFAULT NULL COMMENT '创建类型 0：接口创建 1：控制台创建',
    `valid_date_type` tinyint(1) DEFAULT NULL COMMENT '有效期类型 0：永久有效 1：自定义',
    `valid_date`      datetime                                       DEFAULT NULL COMMENT '有效期',
    `describe`        varchar(1024)                                  DEFAULT NULL COMMENT '描述',
    `total_pv`        int(11) DEFAULT NULL COMMENT '历史PV',
    `total_uv`        int(11) DEFAULT NULL COMMENT '历史UV',
    `total_uip`       int(11) DEFAULT NULL COMMENT '历史UIP',
    `create_time`     datetime                                       DEFAULT NULL COMMENT '创建时间',
    `update_time`     datetime                                       DEFAULT NULL COMMENT '修改时间',
    `del_time`        bigint(20) DEFAULT '0' COMMENT '删除时间戳',
    `del_flag`        tinyint(1) DEFAULT NULL COMMENT '删除标识 0：未删除 1：已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `idx_unique_full-short-url` (`full_short_url`,`del_time`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 使用LIKE创建其他短链接表
CREATE TABLE `t_link_1` LIKE `t_link_0`;
CREATE TABLE `t_link_2` LIKE `t_link_0`;
CREATE TABLE `t_link_3` LIKE `t_link_0`;
CREATE TABLE `t_link_4` LIKE `t_link_0`;
CREATE TABLE `t_link_5` LIKE `t_link_0`;
CREATE TABLE `t_link_6` LIKE `t_link_0`;
CREATE TABLE `t_link_7` LIKE `t_link_0`;
CREATE TABLE `t_link_8` LIKE `t_link_0`;
CREATE TABLE `t_link_9` LIKE `t_link_0`;
CREATE TABLE `t_link_10` LIKE `t_link_0`;
CREATE TABLE `t_link_11` LIKE `t_link_0`;
CREATE TABLE `t_link_12` LIKE `t_link_0`;
CREATE TABLE `t_link_13` LIKE `t_link_0`;
CREATE TABLE `t_link_14` LIKE `t_link_0`;
CREATE TABLE `t_link_15` LIKE `t_link_0`;

-- =============================================
-- 3. 访问日志表 (t_link_access_logs) - 64个分表
-- =============================================
CREATE TABLE `t_link_access_logs_0`
(
    `id`             bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `full_short_url` varchar(128) DEFAULT NULL COMMENT '完整短链接',
    `gid`            varchar(32)  DEFAULT NULL COMMENT '分组标识',
    `user`           varchar(64)  DEFAULT NULL COMMENT '用户信息',
    `ip`             varchar(64)  DEFAULT NULL COMMENT 'IP',
    `browser`        varchar(64)  DEFAULT NULL COMMENT '浏览器',
    `os`             varchar(64)  DEFAULT NULL COMMENT '操作系统',
    `network`        varchar(64)  DEFAULT NULL COMMENT '访问网络',
    `device`         varchar(64)  DEFAULT NULL COMMENT '访问设备',
    `locale`         varchar(256) DEFAULT NULL COMMENT '地区',
    `create_time`    datetime     DEFAULT NULL COMMENT '创建时间',
    `update_time`    datetime     DEFAULT NULL COMMENT '修改时间',
    `del_flag`       tinyint(1) DEFAULT NULL COMMENT '删除标识 0：未删除 1：已删除',
    PRIMARY KEY (`id`),
    KEY              `idx_full_short_url` (`full_short_url`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 使用LIKE创建其他访问日志表 (1-63)
CREATE TABLE `t_link_access_logs_1` LIKE `t_link_access_logs_0`;
CREATE TABLE `t_link_access_logs_2` LIKE `t_link_access_logs_0`;
CREATE TABLE `t_link_access_logs_3` LIKE `t_link_access_logs_0`;
CREATE TABLE `t_link_access_logs_4` LIKE `t_link_access_logs_0`;
CREATE TABLE `t_link_access_logs_5` LIKE `t_link_access_logs_0`;
CREATE TABLE `t_link_access_logs_6` LIKE `t_link_access_logs_0`;
CREATE TABLE `t_link_access_logs_7` LIKE `t_link_access_logs_0`;
CREATE TABLE `t_link_access_logs_8` LIKE `t_link_access_logs_0`;
CREATE TABLE `t_link_access_logs_9` LIKE `t_link_access_logs_0`;
CREATE TABLE `t_link_access_logs_10` LIKE `t_link_access_logs_0`;
CREATE TABLE `t_link_access_logs_11` LIKE `t_link_access_logs_0`;
CREATE TABLE `t_link_access_logs_12` LIKE `t_link_access_logs_0`;
CREATE TABLE `t_link_access_logs_13` LIKE `t_link_access_logs_0`;
CREATE TABLE `t_link_access_logs_14` LIKE `t_link_access_logs_0`;
CREATE TABLE `t_link_access_logs_15` LIKE `t_link_access_logs_0`;
CREATE TABLE `t_link_access_logs_16` LIKE `t_link_access_logs_0`;
CREATE TABLE `t_link_access_logs_17` LIKE `t_link_access_logs_0`;
CREATE TABLE `t_link_access_logs_18` LIKE `t_link_access_logs_0`;
CREATE TABLE `t_link_access_logs_19` LIKE `t_link_access_logs_0`;
CREATE TABLE `t_link_access_logs_20` LIKE `t_link_access_logs_0`;
CREATE TABLE `t_link_access_logs_21` LIKE `t_link_access_logs_0`;
CREATE TABLE `t_link_access_logs_22` LIKE `t_link_access_logs_0`;
CREATE TABLE `t_link_access_logs_23` LIKE `t_link_access_logs_0`;
CREATE TABLE `t_link_access_logs_24` LIKE `t_link_access_logs_0`;
CREATE TABLE `t_link_access_logs_25` LIKE `t_link_access_logs_0`;
CREATE TABLE `t_link_access_logs_26` LIKE `t_link_access_logs_0`;
CREATE TABLE `t_link_access_logs_27` LIKE `t_link_access_logs_0`;
CREATE TABLE `t_link_access_logs_28` LIKE `t_link_access_logs_0`;
CREATE TABLE `t_link_access_logs_29` LIKE `t_link_access_logs_0`;
CREATE TABLE `t_link_access_logs_30` LIKE `t_link_access_logs_0`;
CREATE TABLE `t_link_access_logs_31` LIKE `t_link_access_logs_0`;
CREATE TABLE `t_link_access_logs_32` LIKE `t_link_access_logs_0`;
CREATE TABLE `t_link_access_logs_33` LIKE `t_link_access_logs_0`;
CREATE TABLE `t_link_access_logs_34` LIKE `t_link_access_logs_0`;
CREATE TABLE `t_link_access_logs_35` LIKE `t_link_access_logs_0`;
CREATE TABLE `t_link_access_logs_36` LIKE `t_link_access_logs_0`;
CREATE TABLE `t_link_access_logs_37` LIKE `t_link_access_logs_0`;
CREATE TABLE `t_link_access_logs_38` LIKE `t_link_access_logs_0`;
CREATE TABLE `t_link_access_logs_39` LIKE `t_link_access_logs_0`;
CREATE TABLE `t_link_access_logs_40` LIKE `t_link_access_logs_0`;
CREATE TABLE `t_link_access_logs_41` LIKE `t_link_access_logs_0`;
CREATE TABLE `t_link_access_logs_42` LIKE `t_link_access_logs_0`;
CREATE TABLE `t_link_access_logs_43` LIKE `t_link_access_logs_0`;
CREATE TABLE `t_link_access_logs_44` LIKE `t_link_access_logs_0`;
CREATE TABLE `t_link_access_logs_45` LIKE `t_link_access_logs_0`;
CREATE TABLE `t_link_access_logs_46` LIKE `t_link_access_logs_0`;
CREATE TABLE `t_link_access_logs_47` LIKE `t_link_access_logs_0`;
CREATE TABLE `t_link_access_logs_48` LIKE `t_link_access_logs_0`;
CREATE TABLE `t_link_access_logs_49` LIKE `t_link_access_logs_0`;
CREATE TABLE `t_link_access_logs_50` LIKE `t_link_access_logs_0`;
CREATE TABLE `t_link_access_logs_51` LIKE `t_link_access_logs_0`;
CREATE TABLE `t_link_access_logs_52` LIKE `t_link_access_logs_0`;
CREATE TABLE `t_link_access_logs_53` LIKE `t_link_access_logs_0`;
CREATE TABLE `t_link_access_logs_54` LIKE `t_link_access_logs_0`;
CREATE TABLE `t_link_access_logs_55` LIKE `t_link_access_logs_0`;
CREATE TABLE `t_link_access_logs_56` LIKE `t_link_access_logs_0`;
CREATE TABLE `t_link_access_logs_57` LIKE `t_link_access_logs_0`;
CREATE TABLE `t_link_access_logs_58` LIKE `t_link_access_logs_0`;
CREATE TABLE `t_link_access_logs_59` LIKE `t_link_access_logs_0`;
CREATE TABLE `t_link_access_logs_60` LIKE `t_link_access_logs_0`;
CREATE TABLE `t_link_access_logs_61` LIKE `t_link_access_logs_0`;
CREATE TABLE `t_link_access_logs_62` LIKE `t_link_access_logs_0`;
CREATE TABLE `t_link_access_logs_63` LIKE `t_link_access_logs_0`;

-- =============================================
-- 4. 访问统计表 (t_link_access_stats) - 32个分表
-- =============================================
CREATE TABLE `t_link_access_stats_0`
(
    `id`             bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `full_short_url` varchar(128) DEFAULT NULL COMMENT '完整短链接',
    `gid`            varchar(32)  DEFAULT NULL COMMENT '分组标识',
    `date`           date         DEFAULT NULL COMMENT '日期',
    `pv`             int(11) DEFAULT NULL COMMENT '访问量',
    `uv`             int(11) DEFAULT NULL COMMENT '独立访客数',
    `uip`            int(11) DEFAULT NULL COMMENT '独立IP数',
    `hour`           int(3) DEFAULT NULL COMMENT '小时',
    `weekday`        int(3) DEFAULT NULL COMMENT '星期',
    `create_time`    datetime     DEFAULT NULL COMMENT '创建时间',
    `update_time`    datetime     DEFAULT NULL COMMENT '修改时间',
    `del_flag`       tinyint(1) DEFAULT NULL COMMENT '删除标识 0：未删除 1：已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `idx_unique_access_stats` (`gid`,`full_short_url`,`date`,`hour`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 使用LIKE创建其他访问统计表 (1-31)
CREATE TABLE `t_link_access_stats_1` LIKE `t_link_access_stats_0`;
CREATE TABLE `t_link_access_stats_2` LIKE `t_link_access_stats_0`;
CREATE TABLE `t_link_access_stats_3` LIKE `t_link_access_stats_0`;
CREATE TABLE `t_link_access_stats_4` LIKE `t_link_access_stats_0`;
CREATE TABLE `t_link_access_stats_5` LIKE `t_link_access_stats_0`;
CREATE TABLE `t_link_access_stats_6` LIKE `t_link_access_stats_0`;
CREATE TABLE `t_link_access_stats_7` LIKE `t_link_access_stats_0`;
CREATE TABLE `t_link_access_stats_8` LIKE `t_link_access_stats_0`;
CREATE TABLE `t_link_access_stats_9` LIKE `t_link_access_stats_0`;
CREATE TABLE `t_link_access_stats_10` LIKE `t_link_access_stats_0`;
CREATE TABLE `t_link_access_stats_11` LIKE `t_link_access_stats_0`;
CREATE TABLE `t_link_access_stats_12` LIKE `t_link_access_stats_0`;
CREATE TABLE `t_link_access_stats_13` LIKE `t_link_access_stats_0`;
CREATE TABLE `t_link_access_stats_14` LIKE `t_link_access_stats_0`;
CREATE TABLE `t_link_access_stats_15` LIKE `t_link_access_stats_0`;
CREATE TABLE `t_link_access_stats_16` LIKE `t_link_access_stats_0`;
CREATE TABLE `t_link_access_stats_17` LIKE `t_link_access_stats_0`;
CREATE TABLE `t_link_access_stats_18` LIKE `t_link_access_stats_0`;
CREATE TABLE `t_link_access_stats_19` LIKE `t_link_access_stats_0`;
CREATE TABLE `t_link_access_stats_20` LIKE `t_link_access_stats_0`;
CREATE TABLE `t_link_access_stats_21` LIKE `t_link_access_stats_0`;
CREATE TABLE `t_link_access_stats_22` LIKE `t_link_access_stats_0`;
CREATE TABLE `t_link_access_stats_23` LIKE `t_link_access_stats_0`;
CREATE TABLE `t_link_access_stats_24` LIKE `t_link_access_stats_0`;
CREATE TABLE `t_link_access_stats_25` LIKE `t_link_access_stats_0`;
CREATE TABLE `t_link_access_stats_26` LIKE `t_link_access_stats_0`;
CREATE TABLE `t_link_access_stats_27` LIKE `t_link_access_stats_0`;
CREATE TABLE `t_link_access_stats_28` LIKE `t_link_access_stats_0`;
CREATE TABLE `t_link_access_stats_29` LIKE `t_link_access_stats_0`;
CREATE TABLE `t_link_access_stats_30` LIKE `t_link_access_stats_0`;
CREATE TABLE `t_link_access_stats_31` LIKE `t_link_access_stats_0`;

-- =============================================
-- 5. 浏览器统计表 (t_link_browser_stats) - 16个分表
-- =============================================
CREATE TABLE `t_link_browser_stats_0`
(
    `id`             bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `full_short_url` varchar(128) DEFAULT NULL COMMENT '完整短链接',
    `gid`            varchar(32)  DEFAULT NULL COMMENT '分组标识',
    `date`           date         DEFAULT NULL COMMENT '日期',
    `cnt`            int(11) DEFAULT NULL COMMENT '访问量',
    `browser`        varchar(64)  DEFAULT NULL COMMENT '浏览器',
    `create_time`    datetime     DEFAULT NULL COMMENT '创建时间',
    `update_time`    datetime     DEFAULT NULL COMMENT '修改时间',
    `del_flag`       tinyint(1) DEFAULT NULL COMMENT '删除标识 0：未删除 1：已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `idx_unique_browser_stats` (`gid`,`full_short_url`,`date`,`browser`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 使用LIKE创建其他浏览器统计表 (1-15)
CREATE TABLE `t_link_browser_stats_1` LIKE `t_link_browser_stats_0`;
CREATE TABLE `t_link_browser_stats_2` LIKE `t_link_browser_stats_0`;
CREATE TABLE `t_link_browser_stats_3` LIKE `t_link_browser_stats_0`;
CREATE TABLE `t_link_browser_stats_4` LIKE `t_link_browser_stats_0`;
CREATE TABLE `t_link_browser_stats_5` LIKE `t_link_browser_stats_0`;
CREATE TABLE `t_link_browser_stats_6` LIKE `t_link_browser_stats_0`;
CREATE TABLE `t_link_browser_stats_7` LIKE `t_link_browser_stats_0`;
CREATE TABLE `t_link_browser_stats_8` LIKE `t_link_browser_stats_0`;
CREATE TABLE `t_link_browser_stats_9` LIKE `t_link_browser_stats_0`;
CREATE TABLE `t_link_browser_stats_10` LIKE `t_link_browser_stats_0`;
CREATE TABLE `t_link_browser_stats_11` LIKE `t_link_browser_stats_0`;
CREATE TABLE `t_link_browser_stats_12` LIKE `t_link_browser_stats_0`;
CREATE TABLE `t_link_browser_stats_13` LIKE `t_link_browser_stats_0`;
CREATE TABLE `t_link_browser_stats_14` LIKE `t_link_browser_stats_0`;
CREATE TABLE `t_link_browser_stats_15` LIKE `t_link_browser_stats_0`;

-- =============================================
-- 6. 设备统计表 (t_link_device_stats) - 16个分表
-- =============================================
CREATE TABLE `t_link_device_stats_0`
(
    `id`             bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `full_short_url` varchar(128) DEFAULT NULL COMMENT '完整短链接',
    `gid`            varchar(32)  DEFAULT NULL COMMENT '分组标识',
    `date`           date         DEFAULT NULL COMMENT '日期',
    `cnt`            int(11) DEFAULT NULL COMMENT '访问量',
    `device`         varchar(64)  DEFAULT NULL COMMENT '访问设备',
    `create_time`    datetime     DEFAULT NULL COMMENT '创建时间',
    `update_time`    datetime     DEFAULT NULL COMMENT '修改时间',
    `del_flag`       tinyint(1) DEFAULT NULL COMMENT '删除标识 0：未删除 1：已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `idx_unique_device_stats` (`gid`,`full_short_url`,`date`,`device`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 使用LIKE创建其他设备统计表 (1-15)
CREATE TABLE `t_link_device_stats_1` LIKE `t_link_device_stats_0`;
CREATE TABLE `t_link_device_stats_2` LIKE `t_link_device_stats_0`;
CREATE TABLE `t_link_device_stats_3` LIKE `t_link_device_stats_0`;
CREATE TABLE `t_link_device_stats_4` LIKE `t_link_device_stats_0`;
CREATE TABLE `t_link_device_stats_5` LIKE `t_link_device_stats_0`;
CREATE TABLE `t_link_device_stats_6` LIKE `t_link_device_stats_0`;
CREATE TABLE `t_link_device_stats_7` LIKE `t_link_device_stats_0`;
CREATE TABLE `t_link_device_stats_8` LIKE `t_link_device_stats_0`;
CREATE TABLE `t_link_device_stats_9` LIKE `t_link_device_stats_0`;
CREATE TABLE `t_link_device_stats_10` LIKE `t_link_device_stats_0`;
CREATE TABLE `t_link_device_stats_11` LIKE `t_link_device_stats_0`;
CREATE TABLE `t_link_device_stats_12` LIKE `t_link_device_stats_0`;
CREATE TABLE `t_link_device_stats_13` LIKE `t_link_device_stats_0`;
CREATE TABLE `t_link_device_stats_14` LIKE `t_link_device_stats_0`;
CREATE TABLE `t_link_device_stats_15` LIKE `t_link_device_stats_0`;

-- =============================================
-- 7. 地区统计表 (t_link_locale_stats) - 16个分表
-- =============================================
CREATE TABLE `t_link_locale_stats_0`
(
    `id`             bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `full_short_url` varchar(128) DEFAULT NULL COMMENT '完整短链接',
    `gid`            varchar(32)  DEFAULT NULL COMMENT '分组标识',
    `date`           date         DEFAULT NULL COMMENT '日期',
    `cnt`            int(11) DEFAULT NULL COMMENT '访问量',
    `province`       varchar(64)  DEFAULT NULL COMMENT '省份名称',
    `city`           varchar(64)  DEFAULT NULL COMMENT '市名称',
    `adcode`         varchar(64)  DEFAULT NULL COMMENT '城市编码',
    `country`        varchar(64)  DEFAULT NULL COMMENT '国家标识',
    `create_time`    datetime     DEFAULT NULL COMMENT '创建时间',
    `update_time`    datetime     DEFAULT NULL COMMENT '修改时间',
    `del_flag`       tinyint(1) DEFAULT NULL COMMENT '删除标识 0：未删除 1：已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `idx_unique_locale_stats` (`gid`,`full_short_url`,`date`,`adcode`,`province`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 使用LIKE创建其他地区统计表 (1-15)
CREATE TABLE `t_link_locale_stats_1` LIKE `t_link_locale_stats_0`;
CREATE TABLE `t_link_locale_stats_2` LIKE `t_link_locale_stats_0`;
CREATE TABLE `t_link_locale_stats_3` LIKE `t_link_locale_stats_0`;
CREATE TABLE `t_link_locale_stats_4` LIKE `t_link_locale_stats_0`;
CREATE TABLE `t_link_locale_stats_5` LIKE `t_link_locale_stats_0`;
CREATE TABLE `t_link_locale_stats_6` LIKE `t_link_locale_stats_0`;
CREATE TABLE `t_link_locale_stats_7` LIKE `t_link_locale_stats_0`;
CREATE TABLE `t_link_locale_stats_8` LIKE `t_link_locale_stats_0`;
CREATE TABLE `t_link_locale_stats_9` LIKE `t_link_locale_stats_0`;
CREATE TABLE `t_link_locale_stats_10` LIKE `t_link_locale_stats_0`;
CREATE TABLE `t_link_locale_stats_11` LIKE `t_link_locale_stats_0`;
CREATE TABLE `t_link_locale_stats_12` LIKE `t_link_locale_stats_0`;
CREATE TABLE `t_link_locale_stats_13` LIKE `t_link_locale_stats_0`;
CREATE TABLE `t_link_locale_stats_14` LIKE `t_link_locale_stats_0`;
CREATE TABLE `t_link_locale_stats_15` LIKE `t_link_locale_stats_0`;

-- =============================================
-- 8. 网络统计表 (t_link_network_stats) - 16个分表
-- =============================================
CREATE TABLE `t_link_network_stats_0`
(
    `id`             bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `full_short_url` varchar(128) DEFAULT NULL COMMENT '完整短链接',
    `gid`            varchar(32)  DEFAULT NULL COMMENT '分组标识',
    `date`           date         DEFAULT NULL COMMENT '日期',
    `cnt`            int(11) DEFAULT NULL COMMENT '访问量',
    `network`        varchar(64)  DEFAULT NULL COMMENT '访问网络',
    `create_time`    datetime     DEFAULT NULL COMMENT '创建时间',
    `update_time`    datetime     DEFAULT NULL COMMENT '修改时间',
    `del_flag`       tinyint(1) DEFAULT NULL COMMENT '删除标识 0：未删除 1：已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `idx_unique_network_stats` (`gid`,`full_short_url`,`date`,`network`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 使用LIKE创建其他网络统计表 (1-15)
CREATE TABLE `t_link_network_stats_1` LIKE `t_link_network_stats_0`;
CREATE TABLE `t_link_network_stats_2` LIKE `t_link_network_stats_0`;
CREATE TABLE `t_link_network_stats_3` LIKE `t_link_network_stats_0`;
CREATE TABLE `t_link_network_stats_4` LIKE `t_link_network_stats_0`;
CREATE TABLE `t_link_network_stats_5` LIKE `t_link_network_stats_0`;
CREATE TABLE `t_link_network_stats_6` LIKE `t_link_network_stats_0`;
CREATE TABLE `t_link_network_stats_7` LIKE `t_link_network_stats_0`;
CREATE TABLE `t_link_network_stats_8` LIKE `t_link_network_stats_0`;
CREATE TABLE `t_link_network_stats_9` LIKE `t_link_network_stats_0`;
CREATE TABLE `t_link_network_stats_10` LIKE `t_link_network_stats_0`;
CREATE TABLE `t_link_network_stats_11` LIKE `t_link_network_stats_0`;
CREATE TABLE `t_link_network_stats_12` LIKE `t_link_network_stats_0`;
CREATE TABLE `t_link_network_stats_13` LIKE `t_link_network_stats_0`;
CREATE TABLE `t_link_network_stats_14` LIKE `t_link_network_stats_0`;
CREATE TABLE `t_link_network_stats_15` LIKE `t_link_network_stats_0`;

-- =============================================
-- 9. 操作系统统计表 (t_link_os_stats) - 16个分表
-- =============================================
CREATE TABLE `t_link_os_stats_0`
(
    `id`             bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `full_short_url` varchar(128) DEFAULT NULL COMMENT '完整短链接',
    `gid`            varchar(32)  DEFAULT NULL COMMENT '分组标识',
    `date`           date         DEFAULT NULL COMMENT '日期',
    `cnt`            int(11) DEFAULT NULL COMMENT '访问量',
    `os`             varchar(64)  DEFAULT NULL COMMENT '操作系统',
    `create_time`    datetime     DEFAULT NULL COMMENT '创建时间',
    `update_time`    datetime     DEFAULT NULL COMMENT '修改时间',
    `del_flag`       tinyint(1) DEFAULT NULL COMMENT '删除标识 0：未删除 1：已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `idx_unique_os_stats` (`gid`,`full_short_url`,`date`,`os`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 使用LIKE创建其他操作系统统计表 (1-15)
CREATE TABLE `t_link_os_stats_1` LIKE `t_link_os_stats_0`;
CREATE TABLE `t_link_os_stats_2` LIKE `t_link_os_stats_0`;
CREATE TABLE `t_link_os_stats_3` LIKE `t_link_os_stats_0`;
CREATE TABLE `t_link_os_stats_4` LIKE `t_link_os_stats_0`;
CREATE TABLE `t_link_os_stats_5` LIKE `t_link_os_stats_0`;
CREATE TABLE `t_link_os_stats_6` LIKE `t_link_os_stats_0`;
CREATE TABLE `t_link_os_stats_7` LIKE `t_link_os_stats_0`;
CREATE TABLE `t_link_os_stats_8` LIKE `t_link_os_stats_0`;
CREATE TABLE `t_link_os_stats_9` LIKE `t_link_os_stats_0`;
CREATE TABLE `t_link_os_stats_10` LIKE `t_link_os_stats_0`;
CREATE TABLE `t_link_os_stats_11` LIKE `t_link_os_stats_0`;
CREATE TABLE `t_link_os_stats_12` LIKE `t_link_os_stats_0`;
CREATE TABLE `t_link_os_stats_13` LIKE `t_link_os_stats_0`;
CREATE TABLE `t_link_os_stats_14` LIKE `t_link_os_stats_0`;
CREATE TABLE `t_link_os_stats_15` LIKE `t_link_os_stats_0`;

-- =============================================
-- 10. 短链接跳转表 (t_link_goto) - 16个分表
-- =============================================
CREATE TABLE `t_link_goto_0`
(
    `id`             bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `gid`            varchar(32)  DEFAULT 'default' COMMENT '分组标识',
    `full_short_url` varchar(128) DEFAULT NULL COMMENT '完整短链接',
    PRIMARY KEY (`id`),
    UNIQUE KEY `idx_full_short_url` (`full_short_url`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 使用LIKE创建其他短链接跳转表 (1-15)
CREATE TABLE `t_link_goto_1` LIKE `t_link_goto_0`;
CREATE TABLE `t_link_goto_2` LIKE `t_link_goto_0`;
CREATE TABLE `t_link_goto_3` LIKE `t_link_goto_0`;
CREATE TABLE `t_link_goto_4` LIKE `t_link_goto_0`;
CREATE TABLE `t_link_goto_5` LIKE `t_link_goto_0`;
CREATE TABLE `t_link_goto_6` LIKE `t_link_goto_0`;
CREATE TABLE `t_link_goto_7` LIKE `t_link_goto_0`;
CREATE TABLE `t_link_goto_8` LIKE `t_link_goto_0`;
CREATE TABLE `t_link_goto_9` LIKE `t_link_goto_0`;
CREATE TABLE `t_link_goto_10` LIKE `t_link_goto_0`;
CREATE TABLE `t_link_goto_11` LIKE `t_link_goto_0`;
CREATE TABLE `t_link_goto_12` LIKE `t_link_goto_0`;
CREATE TABLE `t_link_goto_13` LIKE `t_link_goto_0`;
CREATE TABLE `t_link_goto_14` LIKE `t_link_goto_0`;
CREATE TABLE `t_link_goto_15` LIKE `t_link_goto_0`;

-- =============================================
-- 11. 今日统计表 (t_link_stats_today) - 16个分表
-- =============================================
CREATE TABLE `t_link_stats_today_0`
(
    `id`             bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `gid`            varchar(32)  DEFAULT NULL COMMENT '分组标识',
    `full_short_url` varchar(128) DEFAULT NULL COMMENT '短链接',
    `date`           date         DEFAULT NULL COMMENT '日期',
    `today_pv`       int(11) DEFAULT '0' COMMENT '今日PV',
    `today_uv`       int(11) DEFAULT '0' COMMENT '今日UV',
    `today_uip`      int(11) DEFAULT '0' COMMENT '今日IP数',
    `create_time`    datetime     DEFAULT NULL COMMENT '创建时间',
    `update_time`    datetime     DEFAULT NULL COMMENT '修改时间',
    `del_flag`       tinyint(1) DEFAULT NULL COMMENT '删除标识 0：未删除 1：已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `idx_unique_today_stats` (`gid`,`full_short_url`,`date`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 使用LIKE创建其他今日统计表 (1-15)
CREATE TABLE `t_link_stats_today_1` LIKE `t_link_stats_today_0`;
CREATE TABLE `t_link_stats_today_2` LIKE `t_link_stats_today_0`;
CREATE TABLE `t_link_stats_today_3` LIKE `t_link_stats_today_0`;
CREATE TABLE `t_link_stats_today_4` LIKE `t_link_stats_today_0`;
CREATE TABLE `t_link_stats_today_5` LIKE `t_link_stats_today_0`;
CREATE TABLE `t_link_stats_today_6` LIKE `t_link_stats_today_0`;
CREATE TABLE `t_link_stats_today_7` LIKE `t_link_stats_today_0`;
CREATE TABLE `t_link_stats_today_8` LIKE `t_link_stats_today_0`;
CREATE TABLE `t_link_stats_today_9` LIKE `t_link_stats_today_0`;
CREATE TABLE `t_link_stats_today_10` LIKE `t_link_stats_today_0`;
CREATE TABLE `t_link_stats_today_11` LIKE `t_link_stats_today_0`;
CREATE TABLE `t_link_stats_today_12` LIKE `t_link_stats_today_0`;
CREATE TABLE `t_link_stats_today_13` LIKE `t_link_stats_today_0`;
CREATE TABLE `t_link_stats_today_14` LIKE `t_link_stats_today_0`;
CREATE TABLE `t_link_stats_today_15` LIKE `t_link_stats_today_0`;

-- =============================================
-- 12. 用户表 (t_user) - 16个分表
-- =============================================
CREATE TABLE `t_user_0`
(
    `id`            bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `gid`           varchar(32)  DEFAULT NULL COMMENT '分组标识',
    `username`      varchar(256) DEFAULT NULL COMMENT '用户名',
    `password`      varchar(512) DEFAULT NULL COMMENT '密码',
    `real_name`     varchar(256) DEFAULT NULL COMMENT '真实姓名',
    `phone`         varchar(128) DEFAULT NULL COMMENT '手机号',
    `mail`          varchar(512) DEFAULT NULL COMMENT '邮箱',
    `avatar`        varchar(512) DEFAULT NULL COMMENT '头像URL',
    `deletion_time` bigint(20) DEFAULT NULL COMMENT '注销时间戳',
    `create_time`   datetime     DEFAULT NULL COMMENT '创建时间',
    `update_time`   datetime     DEFAULT NULL COMMENT '修改时间',
    `del_flag`      tinyint(1) DEFAULT NULL COMMENT '删除标识 0：未删除 1：已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `idx_unique_username` (`username`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1716344307570487299 DEFAULT CHARSET=utf8mb4;

-- 使用LIKE创建其他用户表 (1-15)
CREATE TABLE `t_user_1` LIKE `t_user_0`;
CREATE TABLE `t_user_2` LIKE `t_user_0`;
CREATE TABLE `t_user_3` LIKE `t_user_0`;
CREATE TABLE `t_user_4` LIKE `t_user_0`;
CREATE TABLE `t_user_5` LIKE `t_user_0`;
CREATE TABLE `t_user_6` LIKE `t_user_0`;
CREATE TABLE `t_user_7` LIKE `t_user_0`;
CREATE TABLE `t_user_8` LIKE `t_user_0`;
CREATE TABLE `t_user_9` LIKE `t_user_0`;
CREATE TABLE `t_user_10` LIKE `t_user_0`;
CREATE TABLE `t_user_11` LIKE `t_user_0`;
CREATE TABLE `t_user_12` LIKE `t_user_0`;
CREATE TABLE `t_user_13` LIKE `t_user_0`;
CREATE TABLE `t_user_14` LIKE `t_user_0`;
CREATE TABLE `t_user_15` LIKE `t_user_0`;