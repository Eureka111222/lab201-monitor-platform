package com.lab201.monitor.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lab201.monitor.entity.ServerNode;

/**
 * <p>
 * 服务器资产配置表 服务类
 * </p>
 *
 * @author lab201
 * @since 2025-12-22
 */
public interface IServerNodeService extends IService<ServerNode> {

    void updateHeartbeat(String serverName);
}
