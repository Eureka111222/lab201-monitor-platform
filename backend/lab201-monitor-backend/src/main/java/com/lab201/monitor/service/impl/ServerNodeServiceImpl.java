package com.lab201.monitor.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.lab201.monitor.entity.ServerNode;
import com.lab201.monitor.mapper.ServerNodeMapper;
import com.lab201.monitor.service.IServerNodeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.grpc.Server;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * <p>
 * 服务器资产配置表 服务实现类
 * </p>
 *
 * @author lab201
 * @since 2025-12-22
 */
@Service
public class ServerNodeServiceImpl extends ServiceImpl<ServerNodeMapper, ServerNode> implements IServerNodeService {

    @Override
    public void updateHeartbeat(String serverName) {
        LambdaUpdateWrapper<ServerNode> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(ServerNode::getServerName,serverName)
                .set(ServerNode::getLastHeartbeat, LocalDateTime.now()) //更新心跳
                .set(ServerNode::getStatus,1); // 设置服务器在线

        this.update(wrapper);
    }
}
