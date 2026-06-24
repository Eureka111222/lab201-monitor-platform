package com.lab201.monitor.ai.tool;

import org.springframework.stereotype.Component;

@Component
public class AgentToolRegistry {

    private final ServerTools serverTools;

    private final GpuTools gpuTools;

    private final DocumentTools documentTools;

    public AgentToolRegistry(ServerTools serverTools, GpuTools gpuTools, DocumentTools documentTools) {
        this.serverTools = serverTools;
        this.gpuTools = gpuTools;
        this.documentTools = documentTools;
    }

    public Object[] resolveTools(String role) {
        // 当前工具均为只读工具，普通用户和管理员都可以使用。
        // 后续如果加入重启服务、删除文档等写操作，可以在这里按 RBAC 返回不同工具集合。
        return new Object[]{serverTools, gpuTools, documentTools};
    }
}
