## Ubuntu/Debian 安装docker

2025年12月18日10:47:34

#### 通用前置：卸载旧版本（避免冲突）

若系统中已安装 Docker 旧版本（如 `docker`、`docker-engine`、`docker.io`），先卸载：

```bash
# Ubuntu/Debian
sudo apt-get remove docker docker-engine docker.io containerd runc

# CentOS/RHEL/Fedora
sudo yum remove docker docker-client docker-client-latest docker-common docker-latest docker-latest-logrotate docker-logrotate docker-engine
```

#### 步骤 1：安装依赖包

```bash
sudo apt-get update
sudo apt-get install -y ca-certificates curl gnupg lsb-release
```

#### 步骤 2：添加 Docker 官方 GPG 密钥

```bash
# 官方源密钥
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /etc/apt/trusted.gpg.d/docker.gpg

# 国内阿里云镜像源密钥（推荐，访问更快）
curl -fsSL https://mirrors.aliyun.com/docker-ce/linux/ubuntu/gpg | sudo gpg --dearmor -o /etc/apt/trusted.gpg.d/docker.gpg
```

#### 步骤 3：添加 Docker 软件源

从下面选一个配置即可，这里需要注意，如果后面docker软件下载失败，重新换其他源再下载（本次用清华的可以，其他不行）

```
# 官方源
echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/trusted.gpg.d/docker.gpg] https://download.docker.com/linux/ubuntu $(lsb_release -cs) stable" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null

# 阿里云镜像源（推荐）
echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/trusted.gpg.d/docker.gpg] https://mirrors.aliyun.com/docker-ce/linux/ubuntu $(lsb_release -cs) stable" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null

# 添加清华 Docker 镜像源
echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/trusted.gpg.d/docker.gpg] https://mirrors.tuna.tsinghua.edu.cn/docker-ce/linux/ubuntu $(lsb_release -cs) stable" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null
```

#### 步骤 4：安装 Docker 核心组件

```
sudo apt-get update
# 安装最新稳定版（包含 Docker CE、CLI、containerd、Buildx、Compose 插件）
sudo apt-get install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin
```

#### 验证安装是否成功

```
# 查看 Docker 引擎版本
docker --version

# 查看 Docker Compose 版本（新版为 docker compose 子命令）
docker compose version
```

正常出现版本号即可

#### 运行测试容器

如果不行，执行完下面配置镜像加速器再试

```
# 运行官方 hello-world 容器
docker run --rm hello-world
```

## 安装后配置

#### 启动 Docker 服务并设置开机自启

```
# 启动 Docker
sudo systemctl start docker

# 设置开机自启
sudo systemctl enable docker

# 查看服务状态
sudo systemctl status docker
```

#### 配置用户权限（避免每次用 sudo）

```bash
# 创建 docker 组（若不存在）
sudo groupadd docker

# 将当前用户加入 docker 组
sudo usermod -aG docker $USER

# 刷新组权限（无需重启，立即生效）
newgrp docker
```

执行 `docker ps`，若不报错则权限配置成功。

#### 配置国内镜像加速器（提升镜像下载速度）

很多镜像都不能用了，因为监管原因，如果不行自己多找几个试试

##### 步骤 1：创建配置文件

```
sudo mkdir -p /etc/docker
```

##### 步骤 2：编辑 `daemon.json` 文件

```
sudo tee /etc/docker/daemon.json <<-'EOF'
{
  "registry-mirrors": [
     "https://docker.m.daocloud.io",
     "https://docker.imgdb.de",
     "https://docker-0.unsee.tech",
     "https://docker.hlmirror.com",
     "https://docker.1ms.run",
     "https://func.ink",
     "https://lispy.org",
     "https://docker.xiaogenban1993.com",
     "https://docker.1panel.live",
     "https://docker.nju.edu.cn"
    ],
  "runtimes": {
      "nvidia": {
          "path": "nvidia-container-runtime",
          "runtimeArgs": []
      }
  }
}
EOF
```

##### 步骤 3：重启 Docker 服务使配置生效

```
sudo systemctl daemon-reload
sudo systemctl restart docker
```

####  配置 NVIDIA Docker（如需 GPU 支持）

```bash
# 添加 NVIDIA 源
curl -fsSL https://nvidia.github.io/libnvidia-container/gpgkey | sudo gpg --dearmor -o /usr/share/keyrings/nvidia-container-toolkit-keyring.gpg
curl -s -L https://nvidia.github.io/libnvidia-container/stable/deb/nvidia-container-toolkit.list | sed 's#deb https://#deb [signed-by=/usr/share/keyrings/nvidia-container-toolkit-keyring.gpg] https://#' | sudo tee /etc/apt/sources.list.d/nvidia-container-toolkit.list > /dev/null

# 安装 Toolkit
sudo apt-get update
sudo apt-get install -y nvidia-container-toolkit

# 配置 Docker 运行时
sudo nvidia-ctk runtime configure --runtime=docker
sudo systemctl restart docker
```

##### 验证 GPU 支持

```bash
# 运行容器并执行 nvidia-smi
docker run --rm --gpus all ubuntu nvidia-smi
```































