# lab201-monitor-platform
# 🔬 Lab201Monitor - 实验室分布式监控与智能运维平台

> 基于 Spring Boot 3 + Vue 3 + Spring AI 的现代化实验室服务器监控与 RAG 知识库系统。

![License](https://img.shields.io/badge/license-MIT-blue)
![Java](https://img.shields.io/badge/Java-17%2B-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.5-green)
![Vue](https://img.shields.io/badge/Vue.js-3.0-brightgreen)
![AI](https://img.shields.io/badge/AI-RAG%20%2B%20LLM-purple)

## 📖 项目简介

这是一个为了解决实验室服务器管理痛点而开发的综合平台。它不仅能实时监控 GPU 显存、温度和**当前占用用户**，还内置了基于 **RAG (检索增强生成)** 技术的 AI 助手，能够回答关于服务器配置、环境搭建等实验室内部知识。

### ✨ 核心功能

* **📊 实时监控大屏**：卡片式展示服务器状态，秒级刷新 GPU 利用率与显存。
* **🕵️ 占用者识别**：精确显示当前哪位同学正在使用显卡，拒绝“抢卡”纠纷。
* **🤖 AI 智能助手**：
    * 集成 **DeepSeek/Ollama** 大模型。
    * 基于 **Milvus** 向量数据库的 RAG 知识库，可回答实验室守则、论文写作规范等问题。
* **📈 历史回溯**：ECharts 可视化展示过去 24 小时资源使用曲线。
* **🔐 安全鉴权**：Spring Security + JWT 认证，RBAC 权限管理。

## 🛠️ 技术栈 (Tech Stack)

### 后端 (Backend)
* **框架**: Java Spring Boot 3.5.9
* **ORM**: MyBatis-Plus
* **AI**: Spring AI (对接 Ollama + Milvus)
* **数据库**: MySQL 8.0 (业务数据), Milvus (向量数据)
* **API 文档**: Knife4j (Swagger 3)

### 前端 (Frontend)
* **框架**: Vue 3 (Composition API) + Vite + TypeScript
* **UI**: Element Plus
* **图表**: Apache ECharts

### 采集端 (Agent)
* **语言**: Python 3
* **库**: pynvml (NVIDIA Management Library)

---

## 🚀 快速开始 (Quick Start)

### 1. 环境准备
确保本地已安装 [Docker](https://www.docker.com/) 和 [JDK 17](https://www.oracle.com/java/)。以下是开发流程，真正部署时都在docker内

### 2. 启动基础设施
使用 Docker Compose 一键启动 MySQL, Milvus 和 Ollama。

```bash
cd docker
docker compose up -d
```

### 3. 初始化数据库

连接 MySQL (端口 3306)，执行 `/sql/init.sql` 脚本创建表结构。

### 4. 启动后端

```bash
cd backend
mvn clean package
java -jar target/lab-monitor.jar
```

### 5. 启动前端

```bash
cd frontend
npm install
npm run dev
```

访问地址：`http://localhost:5173`

### 6. 📂 目录结构

```
lab-monitor-platform/
├── backend/       # Spring Boot 后端源码
├── frontend/      # Vue3 前端源码
├── agent/         # Python 监控采集脚本
├── docker/        # Docker Compose 部署文件
├── docs/          # 项目设计文档与架构图
└── sql/           # 数据库初始化脚本
```

### 7. 📝 开发进度

请查看[Project Kanban](https://github.com/users/Eureka111222/projects/3/views/1)

### 8. 👨‍💻 作者

- **DevTeam**: [李玉钊/lab201]
- **Email**: yuzhaolics@163.com

***

# Created with ❤️ for Lab Automation.



