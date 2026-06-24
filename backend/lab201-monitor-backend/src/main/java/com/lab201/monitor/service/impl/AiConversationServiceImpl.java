package com.lab201.monitor.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lab201.monitor.entity.AiConversation;
import com.lab201.monitor.mapper.AiConversationMapper;
import com.lab201.monitor.service.IAiConversationService;
import org.springframework.stereotype.Service;

@Service
public class AiConversationServiceImpl extends ServiceImpl<AiConversationMapper, AiConversation> implements IAiConversationService {
}
