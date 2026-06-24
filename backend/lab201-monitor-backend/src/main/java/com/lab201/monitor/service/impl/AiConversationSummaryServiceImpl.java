package com.lab201.monitor.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lab201.monitor.entity.AiConversationSummary;
import com.lab201.monitor.mapper.AiConversationSummaryMapper;
import com.lab201.monitor.service.IAiConversationSummaryService;
import org.springframework.stereotype.Service;

@Service
public class AiConversationSummaryServiceImpl
        extends ServiceImpl<AiConversationSummaryMapper, AiConversationSummary>
        implements IAiConversationSummaryService {
}
