package com.lab201.monitor.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lab201.monitor.entity.AiAgentTask;
import com.lab201.monitor.mapper.AiAgentTaskMapper;
import com.lab201.monitor.service.IAiAgentTaskService;
import org.springframework.stereotype.Service;

@Service
public class AiAgentTaskServiceImpl extends ServiceImpl<AiAgentTaskMapper, AiAgentTask> implements IAiAgentTaskService {
}
