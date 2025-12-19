import pynvml
import requests
import time
import os
import pwd
import socket

# ================= 配置区域 =================
# 1. 这里填 H1 服务器的 IP 地址
# 如果使用了 Nginx 反向代理，端口通常是 80；如果是直连 Java，通常是 8080
MASTER_IP = "192.168.1.100" 
REPORT_URL = f"http://{MASTER_IP}:80/api/v1/agent/report"

# 2. 自动获取本机的主机名 (如 "serverA1")，也可以手动写死
# SERVER_NAME = "serverA1" 
SERVER_NAME = socket.gethostname() 

# 3. 安全 Token (与 Java 后端配置一致)
API_TOKEN = "lab_secret_123456"
# ===========================================

def get_gpu_user(handle):
    try:
        procs = pynvml.nvmlDeviceGetComputeRunningProcesses(handle)
        if not procs: return "空闲"
        # 获取显存占用最大的进程 PID
        pid = sorted(procs, key=lambda x: x.usedGpuMemory)[-1].pid
        # 获取该进程的用户名
        uid = os.stat(f"/proc/{pid}").st_uid
        return pwd.getpwuid(uid).pw_name
    except:
        return "未知"

def collect_and_report():
    pynvml.nvmlInit()
    try:
        device_count = pynvml.nvmlDeviceGetCount()
        gpus = []
        for i in range(device_count):
            handle = pynvml.nvmlDeviceGetHandleByIndex(i)
            mem_info = pynvml.nvmlDeviceGetMemoryInfo(handle)
            
            gpus.append({
                "index": i,
                "name": pynvml.nvmlDeviceGetName(handle).decode('utf-8'),
                "utilization": pynvml.nvmlDeviceGetUtilizationRates(handle).gpu,
                "memory_used": mem_info.used // 1024 // 1024, # MB
                "memory_total": mem_info.total // 1024 // 1024,
                "temperature": pynvml.nvmlDeviceGetTemperature(handle, 0),
                "fan_speed": pynvml.nvmlDeviceGetFanSpeed(handle),
                "power_usage": pynvml.nvmlDeviceGetPowerUsage(handle) // 1000, # W
                "username": get_gpu_user(handle)
            })
        
        # 组装数据
        payload = {
            "serverName": SERVER_NAME,
            "ip": get_local_ip(), # 下面定义的辅助函数
            "gpus": gpus
        }
        
        # 发送请求
        print(f"[{time.strftime('%H:%M:%S')}] Reporting to {REPORT_URL}...")
        resp = requests.post(REPORT_URL, json=payload, headers={"X-Agent-Token": API_TOKEN}, timeout=5)
        if resp.status_code == 200:
            print("Success!")
        else:
            print(f"Failed: {resp.status_code} - {resp.text}")

    except Exception as e:
        print(f"Error: {e}")
    finally:
        pynvml.nvmlShutdown()

def get_local_ip():
    """获取本机在局域网的IP，用于展示"""
    try:
        s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
        # 不需要真的连通，只是为了探测出口IP
        s.connect((MASTER_IP, 80))
        ip = s.getsockname()[0]
        s.close()
        return ip
    except:
        return "127.0.0.1"

if __name__ == "__main__":
    print(f"Agent started for {SERVER_NAME}. Master: {MASTER_IP}")
    while True:
        collect_and_report()
        time.sleep(5) # 每5秒上报一次