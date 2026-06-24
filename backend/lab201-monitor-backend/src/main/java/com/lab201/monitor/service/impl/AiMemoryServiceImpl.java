package com.lab201.monitor.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lab201.monitor.entity.AiMemory;
import com.lab201.monitor.mapper.AiMemoryMapper;
import com.lab201.monitor.service.IAiMemoryService;
import org.springframework.stereotype.Service;

@Service
public class AiMemoryServiceImpl extends ServiceImpl<AiMemoryMapper, AiMemory> implements IAiMemoryService {
}
