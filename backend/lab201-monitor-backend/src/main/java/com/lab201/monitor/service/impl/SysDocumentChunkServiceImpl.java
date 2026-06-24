package com.lab201.monitor.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lab201.monitor.entity.SysDocumentChunk;
import com.lab201.monitor.mapper.SysDocumentChunkMapper;
import com.lab201.monitor.service.ISysDocumentChunkService;
import org.springframework.stereotype.Service;

@Service
public class SysDocumentChunkServiceImpl extends ServiceImpl<SysDocumentChunkMapper, SysDocumentChunk>
        implements ISysDocumentChunkService {
}
