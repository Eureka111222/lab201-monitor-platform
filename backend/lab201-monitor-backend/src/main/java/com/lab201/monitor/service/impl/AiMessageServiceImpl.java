package com.lab201.monitor.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lab201.monitor.entity.AiMessage;
import com.lab201.monitor.mapper.AiMessageMapper;
import com.lab201.monitor.service.IAiMessageService;
import org.springframework.stereotype.Service;

@Service
public class AiMessageServiceImpl extends ServiceImpl<AiMessageMapper, AiMessage> implements IAiMessageService {
}
