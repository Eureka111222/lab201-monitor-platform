CREATE DATABASE IF NOT EXISTS `lab201_monitor` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `lab201_monitor`;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ========================================================
-- 1. 权限管理模块
-- ========================================================

-- [1.1] 用户表 (核心业务表：继承 BaseEntity)
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `username` VARCHAR(64) NOT NULL COMMENT '账号名称(学号)',
  `password` VARCHAR(100) NOT NULL COMMENT 'BCrypt加密密码',
  `real_name` VARCHAR(64) DEFAULT NULL COMMENT '真实姓名',
  `face_feature` LONGTEXT COMMENT '人脸特征值(Base64)',
  `avatar` VARCHAR(255) DEFAULT NULL COMMENT '头像',
  `status` TINYINT(4) DEFAULT 1 COMMENT '状态:1正常 0禁用',
  
  -- BaseEntity 字段
  `deleted` TINYINT(4) DEFAULT 0 COMMENT '逻辑删除:1已删 0未删',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=INNODB DEFAULT CHARSET=utf8mb4 COMMENT='系统用户表';

-- [1.2] 角色表 (核心业务表：继承 BaseEntity)
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `role_name` VARCHAR(64) NOT NULL COMMENT '角色名',
  `role_code` VARCHAR(64) NOT NULL COMMENT '角色编码',
  `description` VARCHAR(255) DEFAULT NULL,
  
  -- BaseEntity 字段
  `deleted` TINYINT(4) DEFAULT 0 COMMENT '逻辑删除:1已删 0未删',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_code` (`role_code`)
) ENGINE=INNODB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- [1.3] 用户-角色关联表 (关联表：不需要逻辑删除，只保留创建时间即可)
DROP TABLE IF EXISTS `sys_user_role`;
CREATE TABLE `sys_user_role` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT(20) NOT NULL,
  `role_id` BIGINT(20) NOT NULL,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_role` (`user_id`,`role_id`)
) ENGINE=INNODB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

-- 初始化数据
INSERT INTO `sys_user` ( `username`, `password`, `real_name`, `status`, `deleted`) 
VALUES ('lab201', '$2a$10$N.zmdr9k7uOCQb376ye6tup1Yc1G.j4z.w2.E5c.g.k.l.m.n', '实验室管理员', 1, 0);

-- INSERT INTO `sys_user` ( `username`, `password`, `real_name`, `status`, `deleted`) 
-- VALUES ('chw', 'chw2023', '崔瀚文', 1, 0),
-- ('xtt', 'xtt2023', '许彤彤', 1, 0),
-- ('zxt', 'zxt2023', '曾晓涛', 1, 0),
-- ('lj', 'lj2024', '梁锦', 1, 0),
-- ('lyq', 'lyq2024', '刘渔清', 1, 0),
-- ('jsm', 'jsm2024', '贾仕铭', 1, 0),
-- ('dwj', 'dwj2025', '党伟杰', 1, 0),
-- ('wsq', 'wsq2025', '王斯琪', 1, 0),
-- ('lzr', 'lzr2025', '李泽润', 1, 0),
-- ('bjh', 'bjh2026', '卞江辉', 1, 0);

INSERT INTO `sys_role` (`role_name`, `role_code`) VALUES ('管理员', 'ROLE_ADMIN'), ('普通成员', 'ROLE_USER');
INSERT INTO `sys_user_role` (`user_id`, `role_id`) VALUES (1, 1);


-- ========================================================
-- 2. 基础设施模块
-- ========================================================

-- [2.1] 服务器节点表 (核心业务表：继承 BaseEntity)
DROP TABLE IF EXISTS `server_node`;
CREATE TABLE `server_node` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
  
  -- 基础信息
  `server_name` VARCHAR(64) NOT NULL COMMENT '主机名(唯一标识)',
  `server_ip` VARCHAR(64) DEFAULT NULL COMMENT '内网IP',
  `public_ip` VARCHAR(64) DEFAULT NULL COMMENT '公网IP',
  `ssh_port` INT(11) DEFAULT 22 COMMENT 'SSH端口',
  `location` VARCHAR(100) DEFAULT NULL COMMENT '物理位置',

  -- 硬件配置
  `gpu_model` VARCHAR(128) DEFAULT NULL COMMENT '显卡型号',
  `port_range` VARCHAR(50) DEFAULT NULL COMMENT '端口范围',
  `has_anaconda` TINYINT(4) DEFAULT 0 COMMENT '公共Anaconda: 1是 0否',
  `nccl_info` VARCHAR(255) DEFAULT NULL COMMENT 'NCCL信息',
  `cuda_info` json DEFAULT NULL COMMENT 'CUDA版本(JSON)',

  -- 状态
  `status` TINYINT(4) DEFAULT 0 COMMENT '状态: 1在线 0离线',
  `last_heartbeat` DATETIME DEFAULT NULL COMMENT '最后心跳',
  
  -- BaseEntity 字段
  `deleted` TINYINT(4) DEFAULT 0 COMMENT '逻辑删除:1已删 0未删',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_server_name` (`server_name`)
) ENGINE=INNODB DEFAULT CHARSET=utf8mb4 COMMENT='服务器资产配置表';

-- 初始化服务器数据 (12台)
TRUNCATE TABLE `server_node`;

INSERT INTO `server_node` (server_name, server_ip, public_ip, ssh_port, gpu_model, port_range, has_anaconda, cuda_info, STATUS, location) VALUES 
-- ('titanxp',  '', '201.xiumei.tech', 10000, 'TITAN X (Pascal) / TITAN XP', '13001-14000', 0, '[{"version": "9.0", "isDefault": false}, {"version": "11.0", "isDefault": true}]', 1, '新科技楼lab201'),
-- ('serverA2', '', '201.xiumei.tech', 10086, 'GTX 1080Ti / TITAN Xp',       '14001-15000', 1, '[{"version": "9.0", "isDefault": false}, {"version": "10.2", "isDefault": true}]', 1, '新科技楼lab201'),
-- ('serverB',  '', '201.xiumei.tech', 10010, 'GTX 1080Ti / TITAN Xp',       '15001-16000', 1, '[{"version": "9.0", "isDefault": false}, {"version": "10.1", "isDefault": true}, {"version": "10.2", "isDefault": false}]', 1, '新科技楼lab201'),
('serverC',  '192.168.31.114', '201.xiumei.tech', 10085, 'RTX 2080 Ti', '11001-12000', 1, '[{"version": "10.0", "isDefault": false}, {"version": "10.1", "isDefault": false}, {"version": "11.1", "isDefault": true}]', 1, '新科技楼lab201'),
('serverD',  '192.168.31.27',  '201.xiumei.tech', 10089, 'RTX 2080 Ti', '12001-13000', 1, '[{"version": "10.2", "isDefault": true}]', 1, '新科技楼lab201'),
('ServerE1', '192.168.31.134', '201.xiumei.tech', 10000, 'RTX 3090',      '10000-10999', 1, '[{"version": "11.1", "isDefault": false}, {"version": "11.6", "isDefault": true}]', 1, '新科技楼lab201'),
('ServerE2', '192.168.31.56',  '201.xiumei.tech', 11000, 'RTX 3090',      '11000-11999', 1, '[{"version": "11.1", "isDefault": false}, {"version": "11.2", "isDefault": false}, {"version": "11.6", "isDefault": true}]', 1, '新科技楼lab201'),
('ServerF1', '192.168.31.243', '201.xiumei.tech', 12000, 'RTX 3090',      '12000-12999', 1, '[{"version": "12.2", "isDefault": true}]', 1, '新科技楼lab201'),
('serverF2', '192.168.31.230', '201.xiumei.tech', 22000, 'RTX 3090',      '22001-22999', 1, '[{"version": "11.1", "isDefault": false}, {"version": "11.2", "isDefault": true}]', 1, '新科技楼lab201'),
('serverF3', '192.168.31.166', '201.xiumei.tech', 20000, 'RTX 3090',      '20001-20999', 1, '[{"version": "11.1", "isDefault": true}]', 1, '新科技楼lab201'),
('serverG1', '192.168.31.151', '201.xiumei.tech', 30000, 'RTX 4090',      '30001-30999', 1, '[{"version": "12.0", "isDefault": true}]', 1, '新科技楼lab201'),
('ServerH1', '192.168.31.130', '201.xiumei.tech', 31000, 'RTX A6000',     '31000-31999', 1, '[{"version": "12.4", "isDefault": true}]', 1, '新科技楼lab201');

-- 补充 NCCL 信息
UPDATE `server_node` SET nccl_info = '2.8.4-1+cuda11.1' WHERE server_name IN ('ServerE2', 'serverF2', 'serverF3');
UPDATE `server_node` SET nccl_info = '2.8.4-1+cuda11.1\n2.8.4-1+cuda11.2' WHERE server_name = 'ServerF1';


-- ========================================================
-- 3. 监控数据模块
-- ========================================================

-- [3.1] GPU 监控流水表 (日志表：继承 BaseLogEntity)
-- 不包含 update_time 和 deleted
DROP TABLE IF EXISTS `gpu_metric_log`;
CREATE TABLE `gpu_metric_log` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `server_name` VARCHAR(64) NOT NULL COMMENT '主机名',
  `gpu_index` INT(11) NOT NULL COMMENT '显卡索引',
  `gpu_name` VARCHAR(128) DEFAULT NULL COMMENT '显卡型号',
  `utilization` INT(11) DEFAULT 0 COMMENT '利用率(%)',
  `memory_used` INT(11) DEFAULT 0 COMMENT '已用显存(MB)',
  `memory_total` INT(11) DEFAULT 0 COMMENT '总显存(MB)',
  `temperature` INT(11) DEFAULT 0 COMMENT '温度(℃)',
  `fan_speed` INT(11) DEFAULT 0 COMMENT '风扇转速(%)',
  `power_usage` INT(11) DEFAULT 0 COMMENT '功率(W)',
  `user_name` VARCHAR(64) DEFAULT NULL COMMENT '占用者',
  
  -- BaseLogEntity 字段
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '上报时间',
  
  PRIMARY KEY (`id`),
  KEY `idx_server_time` (`server_name`, `create_time`)
) ENGINE=INNODB DEFAULT CHARSET=utf8mb4 COMMENT='GPU监控日志表';


-- ========================================================
-- 4. 知识库模块
-- ========================================================

-- [4.1] 知识库文档表 (核心业务表：继承 BaseEntity)
DROP TABLE IF EXISTS `sys_document`;
CREATE TABLE `sys_document` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `file_name` VARCHAR(255) NOT NULL,
  `file_path` VARCHAR(500) NOT NULL COMMENT 'MinIO路径',
  `milvus_id` VARCHAR(128) DEFAULT NULL COMMENT '向量ID',
  `status` TINYINT(4) DEFAULT 0 COMMENT '0:解析中 1:成功 2:失败',
  `summary` TEXT COMMENT '文档摘要',
  
  -- BaseEntity 字段
  `deleted` TINYINT(4) DEFAULT 0 COMMENT '逻辑删除:1已删 0未删',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  
  PRIMARY KEY (`id`)
) ENGINE=INNODB DEFAULT CHARSET=utf8mb4 COMMENT='知识库文档';

SET FOREIGN_KEY_CHECKS = 1;