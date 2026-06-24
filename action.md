# 项目升级路线

如果继续打磨，我建议别再“堆技术名词”，而是围绕一个清晰定位升级：

**把项目从“监控 + RAG 问答系统”升级成“面向实验室算力运维的 Agentic AI 运维平台”。**

这样简历会更像真实企业项目，而不是为了加技术栈硬拼。

**优先级路线**

| 阶段 | 目标           | 建议改造                                                    | 简历价值               |
| ---- | -------------- | ----------------------------------------------------------- | ---------------------- |
| P0   | 补企业级短板   | 密码加密、接口权限、审计日志、参数校验、异常规范            | 让项目更像真实生产系统 |
| P1   | 做可靠监控后端 | MQ 重试/DLQ、批量消费、指标归档、慢 SQL 治理、OpenTelemetry | 后端工程能力更强       |
| P2   | 做 Agentic AI  | 工具编排、任务规划、MCP、长短期记忆、AI 运维助手            | AI Agent 项目亮点      |
| P3   | 做高级 RAG     | Hybrid Search、Rerank、引用溯源、文档权限、RAG 评测         | AI 工程深度            |
| P4   | 做生产化部署   | CI/CD、灰度发布、Docker/K8s、Prometheus/Grafana             | 部署和架构能力         |

**1. 后端企业级改造**
最先建议做这几个，因为它们是面试官最容易追问的“生产级能力”。

- 密码安全：当前项目之前有明文密码痕迹，后续应改成 BCrypt 加密，登录时用 `matches()` 校验，新增用户/重置密码都存加密后的哈希值。

- 权限增强：现在主要是 JWT + 拦截器 + role 判断，可以升级为方法级权限，比如 `@RequireRole("admin")` 或 Spring Security + RBAC 权限码。

- 审计日志：对登录、上传文档、删除文档、重置密码、删除会话、Agent 调用工具等操作记录 `operatorId、action、target、ip、result、costMs`。

- 参数校验：Controller DTO 加 `@NotBlank、@NotNull、@Size`，统一由 `GlobalExceptionHandler` 返回标准错误。

- 统一响应和错误码：把业务错误、AI 模型错误、向量库错误、MQ 错误分层定义，便于前端展示和日志排查。

- 数据生命周期：GPU 原始日志可以按月归档或冷热分离，近 7 天热数据走 MySQL/Redis，历史分析走归档表或 ClickHouse。

- 幂等和事务边界再完善：RabbitMQ 消费的幂等已经有了，可以继续补 Outbox Pattern，保证“数据库落库成功”和“消息事件发布成功”的一致性。

**2. MQ 和监控链路升级**
你的监控数据上报很适合继续强化 RabbitMQ 能力。

- 手动 ack：消费成功后 ack，业务异常时 nack/requeue 或投递死信队列。

- 延迟重试：临时数据库抖动、Redis 抖动、服务超时时，不要马上失败，按 10s、30s、2min 做指数退避。

- 死信队列：超过最大重试次数的消息进入 DLQ，后台提供“死信消息查看/重放”页面。

- 批量消费和批量入库：Agent 高频上报时，消费者攒一批再 `saveBatch`，提高吞吐。

- 消息追踪：给每次 Agent 上报生成 `reportId/traceId`，日志里贯穿 HTTP 接收、MQ 投递、消费、入库、缓存刷新。

- 简历写法：设计 RabbitMQ 异步削峰链路，结合手动 ACK、死信队列、重试退避和 Redis 幂等表，保证高频监控数据可靠消费和重复消息安全处理。

**3. AI Agent 能力升级**
现在你已经有 Tool Calling，可以继续往“Agentic AI 运维助手”走。

- 工具注册中心：把 `ServerStatusTools` 扩展成工具体系，比如 `GpuTools、DocumentTools、AlertTools、UserTools、ReportTools`。

- 工具权限控制：不是所有用户都能调用所有工具，比如普通用户只能查状态，管理员可以重启采集 Agent、删除文档、生成资产报告。

- 多步任务规划：用户问“帮我分析 G1 最近一周为什么慢”，Agent 自动拆成：查服务器信息、查近 7 天 GPU 趋势、查占用用户、查知识库部署文档、生成结论。

- Agent 执行轨迹：保存每一步工具调用的输入、输出、耗时、是否失败，前端可以展示“AI 正在查询 GPU 趋势、正在检索知识库”。

- 长任务中心：复杂分析不要阻塞 SSE，可以创建 `ai_task` 表，前端订阅任务进度，支持取消、重试、查看历史报告。

- MCP 改造：把你的服务器状态查询、知识库检索、GPU 趋势查询封装成 MCP Server，让不同 AI 客户端或模型统一调用。MCP 是当前 Agent 工具生态的重要方向，Spring AI 也有 MCP Server Starter 支持。

**4. RAG 继续升级**
目前是基础向量检索，可以升级成更接近企业知识库。

- Hybrid Search：向量检索 + 关键词 BM25 混合，解决服务器名、命令、端口号、报错码这类精确词不好召回的问题。

- Rerank：先召回 top 30，再用 reranker 重排 top 5，提高答案准确率。

- Query Rewrite：用户问“G1 怎么装机”，先改写成“serverG1 装机流程 Ubuntu CUDA 驱动 环境部署”。

- Multi Query：同一个问题生成多个检索 query，扩大召回覆盖。

- Parent-Child Chunk：小块用于召回，大块用于回答，避免答案上下文碎片化。

- 引用溯源：回答下面展示“参考文档、页码、分块、score”，面试时非常加分。

- 文档权限：Milvus metadata 里加入 `viewScope/uploaderId/role`，检索时按用户权限过滤，防止私有文档泄露。

- RAG 评测集：整理 30-50 个标准问答，自动评估命中率、引用准确率、幻觉率，每次改 Prompt 或模型都跑回归。

**5. 模型网关和多模型调度**
你现在已经有 Ollama/Qwen/DeepSeek 配置，可以做成“模型路由层”。

- Provider 抽象：统一封装 `Ollama、DashScope Qwen、DeepSeek、OpenAI compatible`。

- 模型降级：Qwen 超时就切 DeepSeek，本地 Ollama 不可用就切云模型。

- 成本控制：简单问题走便宜模型，复杂分析走强模型。

- 熔断限流：某个 provider 频繁 429 或超时，短时间内自动熔断。

- 模型调用日志：保存 provider、model、prompt tokens、completion tokens、latency、errorType。

- 简历写法：设计多模型路由网关，支持本地 Ollama 与云厂商模型热切换、失败降级、调用耗时与错误分类观测。

**6. AI 记忆系统升级**
你现在有会话级短期记忆，可以继续做“长期记忆”。

- 短期记忆：最近 20 条消息直接拼 Prompt。

- 摘要记忆：会话很长后，把早期消息压缩成 summary，避免 token 太长。

- 用户偏好记忆：记录用户常问服务器、常用环境、身份角色。

- 项目知识记忆：把“服务器别名 G1 = serverG1”这类映射保存为结构化 memory。

- 记忆权限：用户可查看、删除自己的长期记忆，避免隐私问题。

- Spring AI 方向：可以研究 Chat Memory、Advisors，把手写 memory 流程逐步迁移成 Advisor 方式，更标准。

**7. 可观测性和运维体系**
这部分非常符合后端企业级。

- OpenTelemetry：接入 trace，把一次请求串起来：前端请求、RAG 检索、模型调用、Tool Calling、数据库查询、RabbitMQ 消费。

- Prometheus 指标：监控 HTTP QPS、接口耗时、RabbitMQ 积压、Redis 命中率、模型耗时、RAG 检索耗时。

- Grafana 大盘：做“系统服务大盘”和“AI 调用大盘”。

- 日志规范：统一 `traceId、userId、conversationId、reportId、toolName、costMs`。

- 告警机制：RabbitMQ 堆积、Agent 超时未上报、GPU 温度过高、模型 429 过多时自动告警。

- 简历写法：基于 OpenTelemetry 构建端到端链路追踪，覆盖 HTTP、RabbitMQ、MySQL、Redis、Milvus 和 LLM 调用，提升问题定位效率。

**8. 安全和 AI 风控**
AI 项目非常容易被问安全问题。

- Prompt Injection 防护：知识库文档里如果包含“忽略上面指令”这类恶意内容，模型不能盲从。

- Tool Calling 权限：模型只能“申请调用工具”，真正执行前后端要做权限判断。

- 工具白名单：只暴露安全工具，不允许模型执行任意命令。

- 敏感信息脱敏：回答里隐藏密码、token、私钥、服务器敏感公网信息。

- 文档上传安全：限制文件类型、大小、病毒扫描、解析超时。

- OWASP LLM Top 10：可以按这个框架给项目补安全设计，面试时很有说服力。

**9. 监控业务功能升级**
项目本身是实验室服务器平台，业务能力也可以继续加。

- GPU 异常检测：温度过高、显存长期占满、GPU 利用率异常低但显存占用高。

- 资源预约：用户预约服务器/GPU，避免多人抢同一台机器。

- 自动报告：每天生成服务器利用率日报，每周生成 GPU 使用率周报。

- 自然语言报表：用户问“上周哪台服务器最忙”，AI 自动查询聚合数据并生成图表解释。

- Agent 自升级：Python Agent 支持版本号、远程配置、心跳、采集项开关。

- 资产拓扑：服务器、GPU、用户、任务、文档之间形成关系图谱。

**10. 最推荐的实施顺序**
1. 先做密码 BCrypt、审计日志、接口权限细化，这是企业级底座。

2. 再做 AI Agent 执行轨迹表和多工具体系，让项目从“聊天”变成“能执行任务的运维 Agent”。

3. 再做 RAG Hybrid Search + Rerank + 引用溯源，提升 AI 回答质量。

4. 再做 OpenTelemetry + Prometheus/Grafana，把后端和 AI 链路可观测化。

5. 最后做 MCP Server 和多模型路由，作为高级亮点放进简历。

**简历可以升级成这样**
- 基于 Spring Boot 3 + Spring AI 构建实验室算力运维 Agent，支持 RAG 知识问答、Tool Calling、多轮记忆、SSE 流式输出和服务器实时状态查询。
- 设计 AI Agent 工具体系，将 GPU 实时状态、服务器资产、历史趋势、知识库检索封装为安全可控工具，支持多步任务规划和执行轨迹记录。
- 基于 Milvus 构建企业级 RAG 检索链路，引入 Hybrid Search、Rerank、引用溯源和文档权限过滤，降低知识幻觉并保障私有文档隔离。
- 设计高频监控数据处理链路，使用 RabbitMQ 削峰解耦、Redis 缓存实时状态、MySQL 存储历史日志，并通过幂等机制、死信队列和重试退避保障消息可靠消费。
- 接入 OpenTelemetry/Prometheus/Grafana，对 HTTP、MQ、Redis、MySQL、Milvus 和 LLM 调用进行端到端可观测，支持模型错误分类、耗时统计和链路追踪。

**我最建议你下一步做**
最有性价比的是这三个：

1. `RAG 质量升级`：Hybrid Search + Rerank + 引用来源展示。

2. `Agent 执行轨迹`：保存每次工具调用步骤，让它真正像 AI Agent。

3. `OpenTelemetry 可观测性`：后端面试非常容易讲清楚，也最像企业真实项目。

参考资料：Spring AI 的 [ChatClient](https://docs.spring.io/spring-ai/reference/api/chatclient.html)、[Advisors](https://docs.spring.io/spring-ai/reference/api/advisors.html)、[Tool Calling](https://docs.spring.io/spring-ai/reference/api/tools.html)、[Chat Memory](https://docs.spring.io/spring-ai/reference/api/chat-memory.html)、[RAG](https://docs.spring.io/spring-ai/reference/api/retrieval-augmented-generation.html)，MCP 官方 [Specification](https://modelcontextprotocol.io/specification/2024-11-05/index) 和 Spring AI [MCP Server](https://docs.spring.io/spring-ai/reference/api/mcp/mcp-server-boot-starter-docs.html)，Milvus [Hybrid Search/Rerank](https://milvus.io/docs/de/v2.5.x/reranking.md)，OpenTelemetry Java [Instrumentation](https://opentelemetry.io/docs/languages/java/instrumentation/)，OWASP [LLM Top 10](https://owasp.org/www-project-top-10-for-large-language-model-applications)。

# Agent学习

下面这些词其实可以分成两大类：

```text
Agent 能力：工具编排、任务规划、MCP、长短期记忆、AI 运维助手
RAG 能力：Hybrid Search、Rerank、引用溯源、文档权限、RAG 评测
```

你的项目现在已经有基础版：`RAG + Tool Calling + 会话记忆`。继续升级，就是让它从“能问答”变成“能规划、能调用工具、能追踪结果、能评估质量”的 AI Agent 系统。

**1. 工具编排**
工具编排就是：AI 不只是调用一个 Java 方法，而是根据用户问题选择多个工具，并按顺序执行。

比如用户问：

```text
帮我分析 G1 最近为什么跑得慢
```

AI 可能需要：

```text
1. 查询 G1 对应的服务器信息
2. 查询最近 7 天 GPU 使用趋势
3. 查询当前 GPU 占用用户
4. 检索知识库里的 G1 使用说明
5. 综合生成结论
```

现在你项目里只有：

```java
.tools(serverStatusTools)
```

更企业级可以改成工具注册中心：

```java
@Component
public class AgentToolRegistry {

    private final ServerStatusTools serverStatusTools;
    private final GpuTrendTools gpuTrendTools;
    private final DocumentSearchTools documentSearchTools;

    public Object[] getToolsForRole(String role) {
        if ("admin".equals(role)) {
            return new Object[]{serverStatusTools, gpuTrendTools, documentSearchTools};
        }
        return new Object[]{serverStatusTools, documentSearchTools};
    }
}
```

调用时：

```java
Object[] tools = agentToolRegistry.getToolsForRole(loginUser.getRole());

String answer = chatClient.prompt()
        .user(query)
        .tools(tools)
        .call()
        .content();
```

学习重点：先学 Spring AI `@Tool`，再学工具权限、工具日志、工具失败兜底。

**2. 任务规划**
任务规划就是让模型先生成“执行计划”，再由后端按计划执行工具，而不是让模型随意调用。

典型流程：

```text
用户问题
  ↓
模型生成计划 JSON
  ↓
后端解析计划
  ↓
逐步执行工具
  ↓
保存执行轨迹
  ↓
模型总结最终答案
```

计划对象可以这样设计：

```java
@Data
public class AgentPlan {
    private String goal;
    private List<AgentStep> steps;
}

@Data
public class AgentStep {
    private String toolName;
    private Map<String, Object> arguments;
    private String reason;
}
```

让模型输出计划：

```java
AgentPlan plan = chatClient.prompt()
        .system("""
            你是 Lab201 运维 Agent。
            请把用户问题拆成工具执行计划。
            只能输出 JSON，不要输出解释。
            """)
        .user(query)
        .call()
        .entity(AgentPlan.class);
```

执行计划：

```java
for (AgentStep step : plan.getSteps()) {
    Object result = toolExecutor.execute(step.getToolName(), step.getArguments());

    agentTraceService.saveStep(
            taskId,
            step.getToolName(),
            step.getArguments(),
            result
    );
}
```

这比普通 Tool Calling 更好讲，因为你可以说：我实现了 Agent 的“规划-执行-观测”闭环。

**3. MCP**
MCP，全称 Model Context Protocol，是一种让 AI 应用连接外部工具、数据库、文件、系统的标准协议。你可以把它理解成：

```text
普通 Tool Calling：工具只给当前项目用
MCP：工具变成标准服务，Claude、ChatGPT、Cursor、其他 Agent 都能接
```

在你的项目里，未来可以把这些能力做成 MCP Server：

```text
get_gpu_status
list_lab_servers
query_gpu_trend
search_lab_document
create_weekly_report
```

概念上类似：

```java
@Component
public class Lab201McpTools {

    @Tool(description = "查询服务器实时 GPU 状态")
    public String getGpuStatus(String serverName) {
        return monitorCacheService.getLatestGpuLogs(serverName).toString();
    }

    @Tool(description = "检索实验室知识库文档")
    public String searchDocuments(String query) {
        return ragRetrievalService.search(query).toString();
    }
}
```

然后通过 MCP Server Starter 暴露出去。这样你的项目就不只是“自己能聊天”，而是“把 Lab201 的运维能力标准化暴露给外部 AI 客户端”。

学习重点：先理解 MCP 的 `Tools、Resources、Prompts`。`Tools` 是函数调用，`Resources` 是可读取的数据，`Prompts` 是可复用提示词模板。

**4. 长短期记忆**
短期记忆就是最近几轮聊天，通常直接拼进 Prompt。

你现在已经实现了类似逻辑：

```java
List<AiMessage> recentContext = aiChatMemoryService.listRecentContext(userId, conversationId, 20);
String history = buildConversationHistory(recentContext);
```

长期记忆是跨会话保存的重要事实，比如：

```text
用户经常查 serverG1
G1 实际对应 serverG1
用户偏好中文回答
某台服务器常用于深度学习训练
```

可以设计一张表：

```sql
CREATE TABLE ai_memory (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  memory_type VARCHAR(64),
  memory_key VARCHAR(128),
  memory_value TEXT,
  confidence DOUBLE,
  create_time DATETIME,
  update_time DATETIME
);
```

保存长期记忆：

```java
public void saveMemory(Long userId, String key, String value) {
    AiMemory memory = new AiMemory();
    memory.setUserId(userId);
    memory.setMemoryType("USER_PREFERENCE");
    memory.setMemoryKey(key);
    memory.setMemoryValue(value);
    memory.setConfidence(0.9);
    aiMemoryService.save(memory);
}
```

使用长期记忆：

```java
String longTermMemory = aiMemoryService.findUserMemory(userId);

String answer = chatClient.prompt()
        .system("用户长期记忆如下：\n" + longTermMemory)
        .user(query)
        .call()
        .content();
```

学习重点：短期记忆解决“刚才说的它是谁”，长期记忆解决“这个用户长期偏好和常用上下文是什么”。

**5. AI 运维助手**
AI 运维助手就是把 Agent 放到具体业务里。你的项目场景非常合适，因为它天然有服务器、GPU、日志、知识库、用户权限。

它可以回答：

```text
哪台服务器现在空闲？
G1 为什么离线？
最近 7 天谁占用 GPU 最多？
帮我生成本周服务器使用报告。
serverG1 怎么配置 CUDA 环境？
```

后端可以设计统一入口：

```java
@PostMapping("/api/v1/agent/chat")
public Flux<String> chat(@RequestBody AgentChatRequest request) {
    return agentService.run(request.getConversationId(), request.getQuery());
}
```

核心服务：

```java
public Flux<String> run(String conversationId, String query) {
    AgentPlan plan = planner.plan(query);
    AgentExecutionResult result = executor.execute(plan);
    return answerGenerator.streamAnswer(query, result);
}
```

学习重点：不要只说“接了大模型”，要说“围绕运维场景设计了工具、权限、执行轨迹和可观测性”。

**6. Hybrid Search**
普通向量检索适合语义相似，比如：

```text
“怎么配置显卡驱动”
```

但它不擅长精确匹配，比如：

```text
serverG1
192.168.31.151
CUDA 12.4
端口 8899
```

Hybrid Search 就是混合检索：

```text
向量检索：理解语义
关键词检索：匹配精确词
最终融合结果
```

伪代码：

```java
List<Document> vectorHits = vectorSearch(query, 20);
List<Document> keywordHits = keywordSearch(query, 20);

List<Document> merged = hybridRank(vectorHits, keywordHits);
```

简单融合算法：

```java
Map<String, Double> scoreMap = new HashMap<>();

for (Document doc : vectorHits) {
    scoreMap.merge(doc.getId(), doc.getScore() * 0.7, Double::sum);
}

for (Document doc : keywordHits) {
    scoreMap.merge(doc.getId(), doc.getScore() * 0.3, Double::sum);
}
```

在你项目里很适合解决“服务器名、IP、命令、端口号、报错码”检索不准的问题。

学习重点：先学向量检索，再学 BM25/关键词检索，最后学结果融合。

**7. Rerank**
Rerank 是“重排序”。第一阶段先多召回一点，第二阶段用更强的模型重新排序。

流程：

```text
Milvus 初召回 top 30
  ↓
Reranker 判断 query 和每个 chunk 的相关性
  ↓
选 top 5 给大模型回答
```

伪代码：

```java
List<Document> candidates = vectorStore.similaritySearch(
        SearchRequest.builder()
                .query(query)
                .topK(30)
                .build()
);

List<Document> reranked = rerankService.rerank(query, candidates).stream()
        .limit(5)
        .toList();
```

Rerank 服务：

```java
public List<Document> rerank(String query, List<Document> docs) {
    return docs.stream()
            .sorted((a, b) -> Double.compare(
                    score(query, b.getText()),
                    score(query, a.getText())
            ))
            .toList();
}
```

真实项目里 `score()` 可以换成：

```text
bge-reranker
Qwen rerank
Cohere rerank
Jina reranker
```

学习重点：召回解决“有没有找出来”，Rerank 解决“顺序准不准”。

**8. 引用溯源**
引用溯源就是回答时告诉用户：这个答案来自哪篇文档、哪个片段、相似度多少。

你的 RAG 检索结果里已经有 metadata：

```java
metadata.get("doc_id")
metadata.get("title")
document.getScore()
```

可以返回给前端：

```java
@Data
public class RagAnswer {
    private String answer;
    private List<RagCitation> citations;
}

@Data
public class RagCitation {
    private Long docId;
    private String title;
    private String snippet;
    private Double score;
}
```

构造引用：

```java
List<RagCitation> citations = documents.stream()
        .map(doc -> {
            RagCitation c = new RagCitation();
            c.setDocId((Long) doc.getMetadata().get("doc_id"));
            c.setTitle((String) doc.getMetadata().get("title"));
            c.setSnippet(doc.getText().substring(0, Math.min(120, doc.getText().length())));
            c.setScore(doc.getScore());
            return c;
        })
        .toList();
```

前端展示：

```text
参考来源：
1. CUDA环境部署.docx，score=0.82
2. serverG1使用说明.pdf，score=0.79
```

学习重点：引用溯源是降低幻觉的重要手段，企业知识库基本都需要。

**9. 文档权限**
文档权限就是：不是所有用户都能检索所有知识库文档。

比如：

```text
公开文档：所有人可查
私有文档：上传者可查
管理员文档：管理员可查
```

你上传文档时可以把权限写入 metadata：

```java
splitDoc.getMetadata().put("doc_id", docId);
splitDoc.getMetadata().put("view_scope", doc.getViewScope());
splitDoc.getMetadata().put("uploader_id", doc.getUploaderId());
```

检索时加过滤条件：

```java
String filterExpression;

if ("admin".equals(role)) {
    filterExpression = "view_scope >= 0";
} else {
    filterExpression = "view_scope == 1 || uploader_id == " + userId;
}

SearchRequest request = SearchRequest.builder()
        .query(query)
        .topK(6)
        .filterExpression(filterExpression)
        .build();

List<Document> docs = vectorStore.similaritySearch(request);
```

这点非常重要，否则用户可能通过 AI 问答看到不该看的私有文档。

学习重点：RAG 不是只做检索，还要做“权限过滤后的检索”。

**10. RAG 评测**
RAG 评测就是给知识库问答做自动化测试。否则你改了 Prompt、换了模型、改了切分策略，不知道效果是变好还是变差。

你可以准备一个评测集：

```json
[
  {
    "question": "serverG1 的内网 IP 是多少？",
    "expectedAnswer": "192.168.31.151",
    "expectedDoc": "服务器资产表"
  },
  {
    "question": "Ubuntu 怎么换源？",
    "expectedDoc": "Ubuntu换源指南"
  }
]
```

评测流程：

```java
for (EvalCase item : evalCases) {
    RagAnswer answer = ragService.answer(item.getQuestion());

    evalService.saveResult(
            item.getQuestion(),
            answer.getAnswer(),
            answer.getCitations(),
            judge(answer, item)
    );
}
```

常见指标：

```text
Context Precision：检索出来的上下文有多少是有用的
Context Recall：应该检索到的内容有没有被检索到
Faithfulness：回答是否忠于上下文，有没有胡编
Response Relevancy：回答是否真正回应问题
Tool Call Accuracy：Agent 工具调用是否正确
```

简单判断代码：

```java
public boolean containsExpectedDoc(RagAnswer answer, String expectedDoc) {
    return answer.getCitations().stream()
            .anyMatch(c -> c.getTitle().contains(expectedDoc));
}
```

更高级可以用 LLM-as-a-Judge：

```java
String verdict = chatClient.prompt()
        .system("你是 RAG 评测器，只输出 PASS 或 FAIL")
        .user("""
            问题：{question}
            标准答案：{expected}
            模型回答：{actual}
            请判断模型回答是否正确。
            """)
        .call()
        .content();
```

学习重点：先做 30 条人工标准问题，再自动跑评测，最后把结果接入 CI 或管理页面。

**建议学习顺序**
1. 先学 Tool Calling：理解模型如何选择工具，后端如何真正执行工具。

2. 再学 Agent 任务规划：让模型先输出 JSON 计划，再由后端执行。

3. 再学短期记忆和长期记忆：理解会话上下文、摘要记忆、用户偏好记忆。

4. 再学 RAG 三件套：Hybrid Search、Rerank、引用溯源。

5. 再学文档权限：把 RAG 从 demo 变成企业知识库。

6. 最后学 RAG 评测：用数据证明你的优化真的有效。

**对你这个项目最合适的落地顺序**
```text
第一步：引用溯源 + 文档权限
第二步：Hybrid Search + Rerank
第三步：Agent 执行轨迹 + 任务规划
第四步：长期记忆
第五步：MCP Server
第六步：RAG 自动评测
```

这样简历可以写得很漂亮：

```text
将原有 RAG 问答升级为 Agentic AI 运维助手，支持任务规划、工具编排、长短期记忆、Hybrid Search、Rerank、引用溯源、文档权限隔离和 RAG 自动评测，实现从“知识问答”到“可观测、可追溯、可评估”的企业级 AI Agent 平台演进。
```

参考资料：Spring AI [Tool Calling](https://docs.spring.io/spring-ai/reference/api/tools.html)、[ChatClient](https://docs.spring.io/spring-ai/reference/api/chatclient.html)、[Advisors](https://docs.spring.io/spring-ai/reference/api/advisors.html)、[RAG](https://docs.spring.io/spring-ai/reference/api/retrieval-augmented-generation.html)，MCP 官方 [Introduction](https://modelcontextprotocol.io/docs/getting-started/intro) 和 [Specification](https://modelcontextprotocol.io/specification/2025-11-25)，Milvus [Reranking](https://milvus.io/docs/reranking.md)，Ragas [Evaluation Metrics](https://docs.ragas.io/en/latest/concepts/metrics/available_metrics/)。

# Agent升级

**我的建议**
下一步最有含金量的方向不是先做 MCP，而是先把项目升级成：

```text
可追踪、可审计、可评估的 AI 运维 Agent
```

也就是让 AI 不只是回答，而是能：

```text
理解问题 → 制定计划 → 调用工具 → 记录执行轨迹 → 引用知识库来源 → 输出分析结论
```

这样最像真实企业 AI Agent 项目。

**优先级排序**
| 优先级 | 功能                   |       含金量 | 建议                         |
| ------ | ---------------------- | -----------: | ---------------------------- |
| P0     | Agent 执行轨迹         |         很高 | 最推荐先做                   |
| P0     | 工具编排 + 工具权限    |         很高 | 和你现有 Tool Calling 最契合 |
| P1     | 引用溯源 + 文档权限    |         很高 | RAG 企业级必备               |
| P1     | Agent 任务规划         |         很高 | 面试非常好讲                 |
| P2     | Hybrid Search + Rerank |           高 | 提升 RAG 准确率              |
| P2     | 长短期记忆升级         |           高 | 让 AI 更像助手               |
| P3     | MCP Server             | 高但风险更高 | 放后面做，先内网只读         |
| P3     | RAG 自动评测           |           高 | 用数据证明优化有效           |

**第一阶段：Agent 执行轨迹**
这是我最建议你马上做的。原因很简单：你现在已经有 Tool Calling，但用户看不到 AI 做了什么，后端也不好追踪。

目标是让一次 AI 回答可以保存成：

```text
用户问题：帮我分析 G1 最近为什么慢

执行步骤：
1. list_lab_servers 查询服务器列表
2. get_gpu_status 查询 G1 当前 GPU 状态
3. query_gpu_trend 查询近 7 天趋势
4. search_documents 检索知识库
5. generate_answer 生成最终结论
```

建议新增表：

```text
ai_agent_task
ai_agent_step
```

`ai_agent_task` 保存一次任务：

```text
task_id
conversation_id
user_id
query
status
start_time
end_time
cost_ms
final_answer
```

`ai_agent_step` 保存每一步：

```text
task_id
step_order
tool_name
tool_args
tool_result
status
cost_ms
error_message
```

前端可以展示：

```text
AI 正在分析...
✓ 查询服务器状态
✓ 查询 GPU 趋势
✓ 检索知识库
✓ 生成结论
```

简历写法：

```text
设计 Agent 执行轨迹系统，记录模型规划、工具调用、入参出参、耗时和失败原因，实现 AI 运维过程可观测、可审计、可回放。
```

**第二阶段：工具编排 + 工具权限**
你现在只有 `ServerStatusTools`，下一步应该扩展成工具体系。

建议工具分组：

```text
ServerTools：查服务器列表、查服务器详情
GpuTools：查实时 GPU、查近 7 天趋势、查用户占用
DocumentTools：检索知识库、获取引用文档
AlertTools：查询异常、生成告警建议
ReportTools：生成日报、周报
```

权限规则：

```text
普通用户：只能查状态、问知识库
管理员：可以管理文档、查看所有服务器、重置密码、查看审计日志
```

不要让模型直接决定能不能执行工具。正确做法是：

```text
模型申请调用工具
后端校验用户权限
后端执行工具
结果返回模型
```

简历写法：

```text
基于 RBAC 设计 AI 工具权限控制，模型仅负责提出工具调用意图，后端统一完成权限校验、参数校验和执行审计。
```

**第三阶段：引用溯源 + 文档权限**
这也是企业级 RAG 的关键点。

现在 AI 回答后，用户不知道答案来自哪里。下一步应该让回答底部显示：

```text
参考来源：
1. serverG1使用说明.pdf，score=0.82
2. CUDA环境配置.docx，score=0.77
```

同时要做文档权限过滤：

```text
公开文档：所有用户可检索
私有文档：上传者和管理员可检索
管理员文档：仅管理员可检索
```

实现后，你可以说：

```text
RAG 检索不是全库裸搜，而是带用户权限过滤的安全检索。
```

这句话面试很加分。

简历写法：

```text
实现 RAG 引用溯源与文档级权限过滤，在 Milvus metadata 中维护 docId、title、viewScope、uploaderId，检索阶段按用户角色动态过滤，避免私有知识泄露。
```

**第四阶段：Agent 任务规划**
这个是让项目真正像 Agent 的核心。

现在是用户问什么，模型直接回答。升级后可以变成：

```text
用户问题
  ↓
Planner 生成执行计划
  ↓
Executor 执行工具
  ↓
Observer 记录轨迹
  ↓
Answer Generator 总结答案
```

比如用户问：

```text
帮我分析 serverG1 最近为什么慢
```

Planner 输出：

```json
{
  "goal": "分析 serverG1 性能变慢原因",
  "steps": [
    {
      "toolName": "get_gpu_status",
      "reason": "先查看当前 GPU 是否被占满"
    },
    {
      "toolName": "query_gpu_trend",
      "reason": "分析近 7 天趋势"
    },
    {
      "toolName": "search_documents",
      "reason": "检索 serverG1 使用说明"
    }
  ]
}
```

这比单纯 Tool Calling 更高级，因为后端可以控制每一步，记录每一步。

简历写法：

```text
实现 Planner-Executor-Observer 模式的 AI Agent 执行框架，支持多步任务规划、工具编排、执行轨迹记录和失败兜底。
```

**第五阶段：Hybrid Search + Rerank**
这个是提升 RAG 准确率的。

你的项目里有很多精确词：

```text
serverG1
192.168.31.151
CUDA 12.4
端口 8899
RTX4090
```

纯向量检索对这种词不一定准。所以要做 Hybrid Search：

```text
向量检索：负责语义理解
关键词检索：负责精确匹配
Rerank：负责重排序
```

流程：

```text
向量召回 top 20
关键词召回 top 20
合并去重
Rerank 重排
取 top 5 给模型
```

简历写法：

```text
优化 RAG 检索链路，引入 Hybrid Search 与 Rerank，将语义召回和关键词精确匹配结合，提升服务器名、IP、命令、端口号等场景的检索准确率。
```

**第六阶段：长短期记忆升级**
你现在已经有短期记忆，也就是最近 20 条消息。

下一步可以做长期记忆：

```text
用户偏好：喜欢中文、常查 serverG1
实体记忆：G1 对应 serverG1
任务记忆：用户上次分析过 GPU 利用率异常
```

建议新增：

```text
ai_memory
ai_conversation_summary
```

短期记忆：

```text
最近 20 条消息直接拼 Prompt
```

长期记忆：

```text
跨会话保存用户偏好和重要事实
```

摘要记忆：

```text
会话很长时，把早期对话压缩成 summary
```

简历写法：

```text
设计 AI 长短期记忆体系，短期记忆用于多轮上下文理解，长期记忆用于用户偏好和实体别名沉淀，并通过摘要机制控制 Prompt Token 成本。
```

**第七阶段：MCP Server**
MCP 有含金量，但我建议放后面，不要一上来做。

原因是：MCP 适合把你的工具标准化暴露出去，但如果你的内部工具体系还没稳定，先做 MCP 会有点空。

适合暴露成 MCP 的工具：

```text
list_lab_servers
get_gpu_status
query_gpu_trend
search_lab_documents
get_document_citation
```

建议只做内网、只读工具，不要一开始暴露危险操作，比如重启服务器、删除数据。

简历写法：

```text
将实验室服务器状态查询、GPU 趋势分析和知识库检索封装为 MCP Server，只读工具通过统一协议暴露给外部 AI 客户端，实现 Agent 工具能力标准化复用。
```

**第八阶段：RAG 自动评测**
这个非常适合体现“工程化 AI”。

你可以整理 30-50 个标准问题：

```text
serverG1 的内网 IP 是多少？
Ubuntu 如何换源？
怎么查询 GPU 占用用户？
CUDA 环境如何配置？
```

每次改模型、改 Prompt、改切分策略，都跑评测：

```text
检索是否命中正确文档
回答是否包含正确答案
是否有引用来源
是否出现幻觉
```

简历写法：

```text
构建 RAG 自动评测集，覆盖知识库问答、服务器资产查询和 GPU 运维场景，通过命中率、引用准确率、答案忠实度等指标量化优化效果。
```

**我建议你的下一步具体做法**
第一步先做这个组合：

```text
Agent 执行轨迹 + 工具编排 + 引用溯源
```

这是最划算的，因为：

```text
和现有代码衔接最自然
不用大改架构
面试最好讲
简历含金量最高
能马上看出效果
```

具体拆成 3 个小版本：

**版本 7：Agent 执行轨迹**

```text
新增 ai_agent_task / ai_agent_step 表
记录每次 AI 问答的工具调用过程
前端展示“AI 正在查询xxx”
后端记录 toolName、args、result、costMs、status
```

**版本 8：工具编排与权限**

```text
拆分 ServerTools、GpuTools、DocumentTools
新增 AgentToolRegistry
根据用户角色决定可用工具
工具调用统一记录审计日志
```

**版本 9：RAG 引用溯源**

```text
RAG 返回 answer + citations
前端展示参考来源
检索时按 viewScope/uploaderId 过滤
管理员和普通用户看到不同文档范围
```

**最推荐的简历升级方向**
你最后可以把项目从：

```text
高性能计算集群监控与智能知识库平台
```

升级成：

```text
高性能计算集群监控与 Agentic AI 运维平台
```

核心描述可以改成：

```text
基于 Spring Boot 3、Spring AI、RabbitMQ、Redis、Milvus 构建实验室算力运维 Agent 平台，支持服务器实时监控、RAG 知识问答、Tool Calling、多轮记忆、Agent 任务规划、工具编排、执行轨迹追踪和引用溯源。
```

**我的最终建议**
不要下一步直接做 MCP。  
先做：

```text
1. Agent 执行轨迹
2. 工具编排和权限
3. 引用溯源和文档权限
```

这三个做完，你这个项目就已经明显从“AI 问答 Demo”变成“企业级 AI Agent 运维系统”了。然后再做 Hybrid Search、Rerank、MCP 和 RAG 评测，会非常顺。

参考方向：Spring AI 官方文档里已经有 [Tool Calling](https://docs.spring.io/spring-ai/reference/api/tools.html)、[Advisors](https://docs.spring.io/spring-ai/reference/api/advisors.html)、[Chat Memory](https://docs.spring.io/spring-ai/reference/api/chat-memory.html)；MCP 官方文档强调核心能力是 Tools、Resources、Prompts，可以参考 [MCP Server Concepts](https://modelcontextprotocol.io/docs/learn/server-concepts) 和 [MCP Tools](https://modelcontextprotocol.io/specification/2025-06-18/server/tools)；Milvus 的 Hybrid Search 文档提到多向量和 RRF/Weighted Scoring 等融合策略，可以参考 [Milvus Hybrid Search](https://blog.milvus.io/docs/v2.4.x/multi-vector-search.md)。