# 项目更新记录

## 可运行版本 12：可插拔 Agent Skill 机制

- 更新时间：2026-05-28
- 更新前备份：`project-backups/lab201-monitor-platform_source_before_agent_skill_20260528_115445.zip`
- Skill 抽象：新增 `AgentSkill`、`AgentSkillContext`、`AgentSkillSelector` 和 `AbstractAgentSkill`，将原来写死在 Planner 中的任务识别、可用工具、执行步骤和输出规范抽象成独立 Skill。
- Skill 模块：新增 `GpuDiagnosisSkill`、`ServerQuerySkill`、`RagDocumentQaSkill`、`DocumentIngestionSkill`、`MonitorTroubleshootingSkill`，分别覆盖 GPU 诊断、服务器查询、知识库问答、文档解析入库说明、监控链路故障排查。
- Planner 改造：重写 `AgentToolOrchestrator.plan()`，改为先构造 `AgentSkillContext`，再由 `AgentSkillSelector` 动态选择匹配 Skill，最后合并各 Skill 生成的工具步骤，并按 `allowedTools` 做白名单过滤。
- 执行保持兼容：`AgentToolOrchestrator.execute()` 仍然复用现有 `ServerTools`、`GpuTools`、`DocumentTools`，不改动原有工具实现、RAG 检索、SSE 流式输出和 Agent Trace 主链路。
- Prompt 增强：`ToolPlanResult.asPromptContext()` 现在会把选中 Skill 的用途、允许工具和输出规范一起传给最终 Answer Generator，让模型按 Skill 的格式组织答案。
- 可观测性增强：`formatPlan()` 序列化的计划中新增 selectedSkills 和 skillGuide，`agent_planner` 步骤可以看到本次命中了哪些 Skill，以及每个 Skill 允许使用哪些工具。
- 工程验证：`mvn -q -DskipTests compile` 通过。

## 可运行版本 11：云端 Rerank 可选增强与文档解析健壮化

- 更新时间：2026-05-16
- 更新前备份：`project-backups/lab201-monitor-platform_source_before_cloud_rag_parser_20260516_171450.zip`
- 云端 Rerank：新增 `RagRerankProvider` 扩展点，默认继续使用本地 Hybrid Search + 本地 Rerank；当 `lab201.ai.rerank.provider=dashscope` 时，可调用阿里百炼 `qwen3-rerank`，失败后按配置自动降级本地 Rerank。
- 阿里百炼配置：新增 `application-dashscope.yml`，用于切换 `qwen-plus`、`text-embedding-v4` 与 `qwen3-rerank`；保留本地 Milvus，并默认使用新的 `lab201_collection_dashscope_768` 集合名，避免不同 embedding 模型混用同一向量集合。
- 文档解析策略：新增 `DocumentTextExtractor` 策略接口，形成 Tika 主解析、LibreOffice 兜底、Tesseract OCR 可选兜底的解析链路，并通过统一超时控制避免 `.doc`、复杂 Office 文档或图片解析长时间卡住。
- 长文本入库修复：新增 `RagDocumentChunkService`，显式使用小切片参数 `400/80/5/20000/true`，并对切片结果做二次安全截断，避免 PDF/Word 中异常长段落导致 embedding 报 `input length exceeds the context length`。
- AI 会话体验：前端“新建对话”改为本地空白态，不再点击一次就创建一个数据库会话；真正发送第一条消息时才创建会话。后端会话列表同步过滤没有消息的空会话，减少历史侧边栏噪音。
- 工程验证：`mvn -q -DskipTests compile` 通过，`npm run build` 通过。

## 可运行版本 10：Hybrid Search、Rerank 与长短期记忆升级

- 更新时间：2026-05-15
- 更新前备份：`project-backups/lab201-monitor-platform_source_before_hybrid_search_memory_20260515_224531.zip`
- Hybrid Search：新增 `RagHybridSearchService`，将 Milvus 向量召回与 MySQL 文档切片关键词召回合并，解决服务器名、IP、端口号、CUDA 版本、GPU 型号等精确词召回不稳定的问题。
- Rerank：召回后进行本地重排，综合向量分、BM25 关键词分和精确词命中加权，最终取 Top 5 作为模型上下文。
- Query Rewrite / Multi Query：检索前生成多个 query 变体，例如将 `G1` 扩展为 `G1 serverG1`，并针对“装机、端口”等问题补充部署、CUDA、驱动、访问地址等关键词。
- Parent-Child Chunk：新增 `sys_document_chunk` 表，小切片用于关键词召回，父级上下文使用相邻切片拼接后交给模型回答，减少上下文碎片化。
- 文档解析增强：RAG 文档解析时同步写入 `sys_document_chunk`，并给向量元数据补充 `chunk_index`，后续向量命中可回查父级上下文。
- 长期记忆：新增 `ai_memory` 表，用于沉淀用户偏好、实体别名和历史任务，例如偏好中文回答、`G1` 对应 `serverG1`、曾分析某服务器异常。
- 摘要记忆：新增 `ai_conversation_summary` 表，会话超过最近消息窗口后，将较早消息压缩为摘要上下文，配合最近 20 条短期记忆控制 Prompt 长度。
- RAG Prompt 升级：最终回答同时使用短期消息、会话摘要、长期记忆、Hybrid Search 上下文和 Agent 工具结果。
- SQL 脚本：新增 `sql/upgrade_20260515_hybrid_search_memory.sql`。
- 工程验证：`mvn -q -DskipTests compile` 通过，`npm run build` 通过。

## 可运行版本 9：RAG 检索权限与文件可见权限解耦

- 更新时间：2026-05-15
- 更新前备份：`project-backups/lab201-monitor-platform_source_before_rag_scope_permission_20260515_220812.zip`
- 权限模型升级：新增 `rag_scope`，将文件可见权限、是否进入向量库、AI 检索权限拆成三个独立维度。
- 后端上传链路：`/api/v1/sys-document/upload` 新增可选 `ragScope` 参数；未加入 RAG 时固定为私有无效值，加入 RAG 时默认由前端传入 AI 检索范围。
- RAG 过滤逻辑：AI 检索时先判断 `is_rag=1` 且 `parse_status=2`，管理员可检索全部 RAG 文档，普通用户只能检索 `rag_scope=1` 或自己上传的 RAG 文档。
- 文档工具权限：`search_lab_documents` 同步改为按 `rag_scope` 控制 AI 文档检索，不再复用 `view_scope`。
- 前端上传体验：不加入 RAG 时隐藏 AI 检索范围；开启 RAG 后显示“仅自己/管理员”和“全员可问”，默认跟随文件可见范围，用户可主动调整。
- 前端列表展示：文档列表新增“AI检索”列，直观看到不加入、仅本人/管理员、全员可问三种状态。
- SQL 脚本：新增 `sql/upgrade_20260515_rag_scope_permission.sql`，用于给 `sys_document` 增加 `rag_scope` 字段并迁移历史数据。
- 工程验证：`mvn -q -DskipTests compile` 通过，`npm run build` 通过。

## 可运行版本 8：Agent 任务规划与确定性执行链路

- 更新时间：2026-05-15
- 更新前备份：`project-backups/lab201-monitor-platform_source_before_agent_planner_20260515_174259.zip`
- 任务规划：新增 `AgentToolOrchestrator`，根据用户问题生成结构化执行计划，包含目标、工具名称、执行原因和工具参数。
- 确定性执行：持久化 AI 会话不再完全依赖模型主动 Tool Calling，而是由后端 Planner 先规划，Executor 再执行工具，最后由模型负责总结答案。
- 执行轨迹修复：Planner、RAG 检索、服务器查询、GPU 实时状态、GPU 趋势和文档检索都会通过 `AgentTraceService.traceOperation/traceTool` 写入 `ai_agent_step`，前端可以实时展示“AI 执行过程”。
- 任务状态兜底：流式问答如果被浏览器刷新、关闭窗口或网络中断，会把任务标记为失败并写入“客户端中断流式响应”，避免 `ai_agent_task` 长期停留在 RUNNING。
- RAG 溯源增强：RAG 检索本身也作为步骤记录命中文档数量、docId、title、score 和摘要，最终仍通过 citations 展示参考来源。
- 权限策略确认：继续采用企业级安全策略，`is_rag=1` 表示允许向量化，`view_scope` 决定 AI 检索可见范围；管理员可检索全部 RAG 文档，普通用户只能检索公开文档和自己上传的私有 RAG 文档。
- 前端提示：上传文档弹窗补充说明，公开 RAG 文档可被全员 AI 引用，私有 RAG 文档仅自己和管理员可被 AI 引用。
- 工程验证：`mvn -q -DskipTests compile` 通过，`npm run build` 通过。

## 可运行版本 7：Agent 执行轨迹、工具编排与 RAG 引用溯源

- 更新时间：2026-05-15
- 更新前备份：`project-backups/lab201-monitor-platform_before_agent_trace_tool_citation_20260515_105639.zip`
- Agent 执行轨迹：新增 `ai_agent_task` 和 `ai_agent_step` 表，记录每次 AI 问答任务、工具调用步骤、工具入参、工具结果、耗时、状态和错误信息。
- 工具编排：拆分 `ServerTools`、`GpuTools`、`DocumentTools`，新增 `AgentToolRegistry` 统一按角色返回可用工具集合，保留后续扩展管理员写操作工具的入口。
- 工具审计：工具方法统一通过 `AgentTraceService.traceTool` 包装，自动记录 RUNNING/SUCCESS/FAILED 状态，并通过 SSE 向前端推送“正在查询/已完成”的执行过程。
- RAG 权限过滤：检索后按现有文档权限逻辑二次过滤，只允许使用已加入 RAG 且解析成功的文档；管理员可用所有 RAG 文档，普通用户仅可用公开文档和自己上传的文档。
- 引用溯源：RAG 返回引用来源事件，包含 docId、title、score、snippet，前端在 AI 回答下方展示“参考来源”。
- 前端体验：AI 助手支持展示 Agent 执行过程和参考来源，流式回答过程中可实时看到工具调用状态。
- SQL 脚本：新增 `sql/upgrade_20260515_agent_trace_tool_citation.sql`。
- 工程验证：`mvn -q -DskipTests compile` 通过，`npm run build` 通过。

## 可运行版本 6：AI 助手滚动体验与弹层规范修复

- 更新时间：2026-05-14
- 更新前备份：`project-backups/lab201-monitor-platform_before_chat_ui_scroll_modal_fix_20260514_220550.zip`
- AI 助手体验：限制聊天窗口始终停留在浏览器可视区域内，打开、拖动、缩放、浏览器尺寸变化时都会自动校正位置和尺寸，避免底部输入框跑出屏幕。
- AI 助手体验：修复长回答把消息区撑开导致输入区不可见的问题，为聊天主区和消息列表补充 `min-height: 0`，让消息列表自己滚动，输入区稳定保留在底部。
- 流式滚动优化：流式输出时只有用户本来位于底部附近才自动跟随到底部；用户主动向上查看历史时，不再被新 token 强制拉回底部。
- 弹层规范修复：降低 AI 助手窗口 z-index，让 Element Plus 的确认删除弹框显示在助手之上，并将删除确认框挂载到 `document.body`。
- 工程验证：`npm run build` 通过。

## 可运行版本 5：AI 助手体验优化与管理员重置密码

- 更新时间：2026-05-14
- 更新前备份：`project-backups/lab201-monitor-platform_before_chat_ui_password_reset_20260514_205539.zip`
- AI 助手体验：将顶部下拉历史会话改为类 ChatGPT 的左侧历史会话栏，支持滚动查看、切换会话、删除会话、新建会话。
- AI 助手体验：新增历史栏拖拽调整宽度与收起/展开能力，保留原有聊天窗口整体拖拽和八方向缩放。
- 流式渲染修复：重写前端 SSE 解析逻辑，按标准 `data:` 事件恢复多行内容，避免 Markdown 表格、列表、换行在流式阶段被拼成一行。
- 会话刷新修复：AI 回答完成后刷新会话列表，使第一句问题生成的标题能及时展示在历史栏中。
- 用户管理修复：新增后端 `/api/v1/sys-user/{id}/reset-password` 接口，仅管理员可调用，真正把用户密码重置为 `123456`。
- 用户管理修复：前端“重置密码”按钮改为调用后端接口，避免只弹出成功提示但数据库未更新。
- 工程验证：`mvn -q -DskipTests compile` 通过，`npm run build` 通过。

## 可运行版本 4：AI 聊天持久化与上下文记忆

- 更新时间：2026-05-14
- 更新前备份：`project-backups/lab201-monitor-platform_before_ai_memory_persistence_20260514_121646.zip`
- 新增数据库脚本：`sql/upgrade_20260514_ai_chat_memory.sql`，新增 `ai_conversation` 会话表和 `ai_message` 消息表，用于保存每个用户的 AI 会话、问答内容、模型 provider/model、耗时和失败信息。
- 新增后端能力：新增 `AiConversationController`，提供创建会话、查询会话列表、查询会话消息、删除会话接口。
- 新增后端能力：新增 `AiChatMemoryService`，统一封装会话归属校验、消息落库、最近上下文读取、AI 回答成功/失败状态更新，避免把持久化逻辑散落在 Controller 里。
- RAG 链路升级：`/api/v1/rag/chat` 和 `/api/v1/rag/stream` 增加可选 `conversationId` 参数；传入后会把最近 20 条成功消息拼入 Prompt，实现刷新后的历史恢复和模型上下文记忆。
- RAG 链路保持兼容：不传 `conversationId` 时仍保持原有一次性 RAG/Tool Calling 行为，尽量不破坏已有接口调用。
- 前端升级：AI 悬浮助手新增历史会话加载、会话切换、新会话创建；发送消息时自动携带 `conversationId`，刷新页面后再次打开可以从数据库恢复聊天记录。
- 工程验证：`mvn -q -DskipTests compile` 通过，`npm run build` 通过。

## 可运行版本 3：GPU 历史趋势查询优化

- 更新时间：2026-05-12
- 更新前备份：`project-backups/lab201-monitor-platform_before_gpu_query_optimization_20260512_224051.zip`
- 新增功能：增加 `/api/v1/gpu/trend/{serverName}` 接口，默认查询某台服务器近 7 天 GPU 数据，并按 5 分钟时间桶聚合后返回趋势点。
- 新增功能：增加 `/api/v1/gpu/overview` 大屏聚合接口，一次返回服务器列表与每台服务器最新 GPU 状态，减少前端每 5 秒刷新时的 N+1 HTTP 请求。
- 查询优化：历史趋势不再返回所有原始日志，后端聚合 `avgUtilization`、`avgMemoryPercent`、`avgTemperature`、`avgFanSpeed`、`avgPowerUsage` 和 `sampleCount`，降低网络传输与 ECharts 渲染压力。
- 查询优化：新增用户占比聚合结果 `userUsages`，前端饼图直接使用后端聚合结果，不再基于原始日志逐条统计。
- 工程增强：补充 MyBatis-Plus `PaginationInnerInterceptor`，确保普通分页查询生成真实分页 SQL。
- 前端优化：大屏页改用 `fetchGpuOverview`，替代原先“先查服务器列表，再逐台查询最新 GPU 状态”的 N+1 请求模式。
- 前端优化：服务器历史详情页改用 `fetchGpuTrend`，默认 `days=7`、`bucketMinutes=5`，替代原先 `size=2000` 的原始日志分页请求。
- SQL 增强：初始化脚本补充 `idx_create_time`，并新增 `sql/upgrade_20260512_gpu_query_optimization.sql`，用于优化全局按时间倒序查询的兜底场景。
- 验证结果：`mvn -q -DskipTests compile` 通过，`npm run build` 通过。

## 可运行版本 2：AI Tool Calling 与可观测日志增强

- 更新时间：2026-05-12
- 更新前备份：`project-backups/lab201-monitor-platform_before_tool_calling_observability_20260512_211618.zip`
- 新增功能：增加 `ServerStatusTools`，通过 Spring AI `@Tool` 暴露 `list_lab_servers` 和 `get_gpu_status` 两个函数工具，让模型可以按需调用 Java 后端方法查询服务器与 GPU 实时状态。
- 新增功能：RAG 对话和 SSE 流式对话均通过 `ChatClient.tools(serverStatusTools)` 注册工具，实时状态类问题可以走工具调用，文档知识类问题继续走 Milvus RAG。
- 日志增强：新增 RAG 检索日志，记录每次查询命中的 Document 排名、id、score、doc_id、title 和文本摘要。
- 日志增强：新增模型调用日志，记录 provider、model、是否流式、chunk 数量、耗时和问题摘要。
- 错误日志：补充向量库不可用、429 限流、超时、API Key/模型不可用等错误分类日志。
- 工程增强：Maven 编译打开 `parameters`，让 Tool Calling 能获取 Java 方法参数名，提升工具入参 schema 可读性。
- 验证结果：`mvn -q -DskipTests compile` 通过。

## 可运行版本 1

- 范围：本次更新前的项目状态。
- 能力：已具备监控数据 HTTP 接入、RabbitMQ 异步消费、Redis 缓存实时状态、MySQL 落库、Milvus + Ollama RAG 问答，以及 qwen/deepseek profile 配置文件。
