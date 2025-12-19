## docker_compose

   - version: '3.8'

      services:

        \# ------------------------------------------------------------

        \# 1. MySQL (关系型数据库)

        \# 用途：存服务器列表、监控日志、用户信息

        \# ------------------------------------------------------------

        mysql:

      ​    image: mysql:8.0

      ​    container_name: lab_mysql

      ​    restart: always

      ​    environment:

      ​      MYSQL_ROOT_PASSWORD: lab201201  # ❗一定要改个强密码

      ​      MYSQL_DATABASE: lab_monitor

      ​    ports:

      ​      \- "3306:3306"

      ​    volumes:

      ​      \- ./data/mysql:/var/lib/mysql # 数据持久化

      ​      \- ./sql/init.sql:/docker-entrypoint-initdb.d/init.sql # 初始化脚本

      ​    command: --character-set-server=utf8mb4 --collation-server=utf8mb4_unicode_ci

        \# ------------------------------------------------------------

        \# 2. Redis (内存数据库)

        \# 用途：缓存热点数据(监控列表)、AI对话上下文

        \# ------------------------------------------------------------

        redis:

      ​    image: redis:7.0

      ​    container_name: lab_redis

      ​    restart: always

      ​    ports:

      ​      \- "6379:6379"

      ​    command: redis-server --requirepass "lab201201" # ❗设置密码

      ​    volumes:

      ​      \- ./data/redis:/data

        \# ------------------------------------------------------------

        \# 3. RabbitMQ (消息队列)

        \# 用途：削峰填谷，Agent上报数据先扔这里，防止后端崩掉

        \# ------------------------------------------------------------

        rabbitmq:

      ​    image: rabbitmq:3-management

      ​    container_name: lab_rabbitmq

      ​    restart: always

      ​    ports:

      ​      \- "5672:5672"   # 程序连接口

      ​      \- "15672:15672" # 网页管理后台口

      ​    environment:

      ​      RABBITMQ_DEFAULT_USER: admin

      ​      RABBITMQ_DEFAULT_PASS: admin

        \# ------------------------------------------------------------

        \# 4. Milvus (向量数据库 - 极简单机版)

        \# 用途：存RAG文档切片后的向量，用于AI检索

        \# ------------------------------------------------------------

        milvus-etcd:

      ​    image: quay.io/coreos/etcd:v3.5.5

      ​    container_name: milvus_etcd

      ​    environment:

      ​      \- ETCD_AUTO_COMPACTION_MODE=revision

      ​      \- ETCD_AUTO_COMPACTION_RETENTION=1000

      ​      \- ETCD_QUOTA_BACKEND_BYTES=4294967296

      ​    volumes:

      ​      \- ./data/etcd:/etcd

      ​    command: etcd -advertise-client-urls=http://127.0.0.1:2379 -listen-client-urls=http://0.0.0.0:2379

        milvus-minio:

      ​    image: minio/minio:RELEASE.2023-03-20T20-16-18Z

      ​    container_name: milvus_minio

      ​    environment:

      ​      MINIO_ACCESS_KEY: minioadmin

      ​      MINIO_SECRET_KEY: minioadmin

      ​    volumes:

      ​      \- ./data/minio:/minio_data

      ​    command: minio server /minio_data --console-address ":9001"

        milvus:

      ​    image: milvusdb/milvus:v2.3.4

      ​    container_name: lab_milvus

      ​    command: ["milvus", "run", "standalone"]

      ​    environment:

      ​      ETCD_ENDPOINTS: milvus-etcd:2379

      ​      MINIO_ADDRESS: milvus-minio:9000

      ​    volumes:

      ​      \- ./data/milvus:/var/lib/milvus

      ​    ports:

      ​      \- "19530:19530"

      ​    depends_on:

      ​      \- milvus-etcd

      ​      \- milvus-minio

        \# ------------------------------------------------------------

        \# 5. Ollama (本地大模型服务)

        \# 用途：跑 LLM (Llama3/Qwen)，AI的大脑

        \# ------------------------------------------------------------

        ollama:

      ​    image: ollama/ollama:latest

      ​    container_name: lab_ollama

      ​    restart: always

      ​    ports:

      ​      \- "11434:11434"

      ​    volumes:

      ​      \- ./data/ollama:/root/.ollama

      ​    deploy:

      ​      resources:

      ​        reservations:

      ​          devices:

      ​            \- driver: nvidia

      ​              count: 1

      ​              capabilities: [gpu] # ❗关键：调用宿主机的 A6000

      ***nignx.conf***

```
server {
    listen 80;
    server_name localhost;

    # 开启 gzip 压缩，加快加载
    gzip on;
    gzip_min_length 1k;
    gzip_types text/plain application/javascript text/css;

    # ==================================================
    # 1. 默认首页：实验室知识手册 (MDwiki)
    # 访问 http://192.168.31.130 显示此页面
    # ==================================================
    location / {
        root /usr/share/nginx/html/wiki; # 对应 docker-compose 的 ./html/wiki
        index index.html;
    }

    # ==================================================
    # 2. 新监控系统 (Vue)
    # 访问 http://192.168.31.130/monitor
    # ==================================================
    location /monitor {
        alias /usr/share/nginx/html/dist; # 对应 ./html/dist
        index index.html;
        try_files $uri $uri/ /monitor/index.html; # Vue History 模式支持
    }

    # ==================================================
    # 3. 后端 API 反向代理
    # ==================================================
    # 开发阶段：指向你本地电脑的 IDEA (需配合 SSH 隧道或公网IP，暂不配置)
    # 部署阶段：指向 Docker 里的 backend 容器
    location /api/ {
        # 如果 backend 容器没启动，这里会报错 502，这是正常的
        proxy_pass http://lab_backend:8080/; 
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}

```





![1766030751173](C:\Users\yuzhao li\AppData\Roaming\Typora\typora-user-images\1766030751173.png)

## 开发架构

本地电脑
 ├─ IDEA / VSCode / SQLyog
 │    ↓（直连）
H1（192.168.31.130）
 ├─ Docker Compose（核心服务）
 │    ├─ MySQL
 │    ├─ Redis
 │    ├─ RabbitMQ
 │    ├─ Milvus（etcd + minio + milvus）
 │    ├─ Ollama（A6000 GPU）
 │    ├─ Spring Boot Backend
 │    └─ Nginx + 前端
 │
 └─ gpu_agent.py（仅采集 H1 自己的 GPU）
      ↑
其他服务器（A1/A2/E1/E2…）
 └─ gpu_agent.py（python 直跑，POST 到 H1）

## 项目目录

/home/lab201/lab-monitor-platform/docker/
├── docker-compose.yml       # [核心] 启动配置文件
├── nginx.conf               # [核心] 前端网关配置
├── sql/
│   └── init.sql             # [必须] 之前我给你的完整建表 SQL
├── data/                    # [自动生成] 数据库数据（空文件夹即可）

│     ├── mysql/
│     ├── redis/
│     ├── milvus/
│     ├── minio/
│     ├── etcd/
│     └── ollama/

└── html/
    ├── dist/                # [未来] 放 Vue 打包文件 (现在可以为空)
    └── wiki/                # [现在] 放 MDwiki 的 index.html, index.md 等

## 启动成功

启动前放行端口，使得可以访问

```
sudo ufw allow 80
sudo ufw allow 3306
sudo ufw allow 6379
sudo ufw allow 5672
sudo ufw allow 15672
sudo ufw allow 19530
sudo ufw allow 11434
```

```
启动所有服务
docker compose up -d
关闭所有服务
docker compose down
查看某个应用log
docker logs milvus_etcd
sudo rm -rf ./data/minio
sudo rm -rf ./data/milvus
sudo rm -rf ./data/etcd
```



![1766064212490](C:\Users\yuzhao li\AppData\Roaming\Typora\typora-user-images\1766064212490.png)

## 测试连接

1.**MySQL**:  账号：root  密码：lab201201 之后尽量不用root登录，会创建一个数据库专门管理的用户

用 SQLyog 连接服务器 IP:3306

![1766064538534](C:\Users\yuzhao li\AppData\Roaming\Typora\typora-user-images\1766064538534.png)

2.**Ollama**:

```bash
docker exec -it lab201_ollama bash
ollama run qwen2.5:7b  # 下载并测试模型
```

![1766065413132](C:\Users\yuzhao li\AppData\Roaming\Typora\typora-user-images\1766065413132.png)

3.**前端**:

- 访问 `http://服务器IP` -> 应该显示 Vue 的页面 (如果你还没放 Vue 打包文件，会报错 403/404，正常)。
- 访问 `http://服务器IP/wiki/index.html` -> **应该能完美显示你之前的实验室手册页面**。

4.**rabbitmq**  账号：lab201admin 密码：lab201201

访问http://192.168.31.130:15672

![1766065245108](C:\Users\yuzhao li\AppData\Roaming\Typora\typora-user-images\1766065245108.png)

![1766065285511](C:\Users\yuzhao li\AppData\Roaming\Typora\typora-user-images\1766065285511.png)

5.**redis**

```
# 进入 Redis 容器
docker exec -it lab201_redis redis-cli

# 验证密码
auth lab201201
# 返回 OK

# 写入测试
set mykey "Hello Lab"
# 返回 OK

# 读取测试
get mykey
# 返回 "Hello Lab"

# 退出
exit
```

使用redis软件，Redis Insight 

redis://:lab201201@192.168.31.130:6379