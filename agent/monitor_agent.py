import pynvml
import requests
import time
import os
import pwd
import socket
import logging

# ================= 配置区域 =================
# 1. 后端服务器地址 (你的ServerH1 IP)
MASTER_IP = "192.168.31.129" 
# 2. 接口地址 (必须与 Java Controller 的 @RequestMapping 一致)
REPORT_URL = f"http://{MASTER_IP}:8080/api/v1/gpu/report"

# 3. 本机名字 (必须与数据库 server_node 表的 server_name 一致)
# 如果 Linux hostname 就是 'titanxp'，则不用改；否则请手动写死，例如: SERVER_NAME = "titanxp"
SERVER_NAME = socket.gethostname() 
# ===========================================

# 配置日志打印
logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(levelname)s - %(message)s')

def get_gpu_user(handle):
    """获取显存占用最高的用户名"""
    try:
        procs = pynvml.nvmlDeviceGetComputeRunningProcesses(handle)
        if not procs: return "" # 对应Java的空字符串
        
        # 找到显存占用最大的进程
        max_proc = max(procs, key=lambda p: p.usedGpuMemory)
        
        # 读取 /proc/{pid} 获取用户信息
        try:
            uid = os.stat(f"/proc/{max_proc.pid}").st_uid
            return pwd.getpwuid(uid).pw_name
        except (FileNotFoundError, ProcessLookupError):
            return "" # 进程可能刚结束
    except Exception as e:
        logging.error(f"获取用户失败: {e}")
        return ""

def collect_and_report():
    try:
        pynvml.nvmlInit()
        device_count = pynvml.nvmlDeviceGetCount()
        logs = [] # 对应 Java 的 List<GpuMetricLog>

        for i in range(device_count):
            handle = pynvml.nvmlDeviceGetHandleByIndex(i)
            mem_info = pynvml.nvmlDeviceGetMemoryInfo(handle)
            util_rates = pynvml.nvmlDeviceGetUtilizationRates(handle)
            
            # 获取显卡名 (处理 bytes/str 兼容性)
            name_raw = pynvml.nvmlDeviceGetName(handle)
            gpu_name = name_raw.decode('utf-8') if isinstance(name_raw, bytes) else name_raw

            # 构造单条显卡数据 (字段名必须与 Java GpuMetricLog 实体类完全一致!)
            logs.append({
                "gpuIndex": i,
                "gpuName": gpu_name,
                "utilization": util_rates.gpu,
                "memoryUsed": int(mem_info.used / 1024 / 1024),
                "memoryTotal": int(mem_info.total / 1024 / 1024),
                "temperature": pynvml.nvmlDeviceGetTemperature(handle, pynvml.NVML_TEMPERATURE_GPU),
                "fanSpeed": pynvml.nvmlDeviceGetFanSpeed(handle),
                "powerUsage": pynvml.nvmlDeviceGetPowerUsage(handle) // 1000,
                "userName": get_gpu_user(handle)
            })

        pynvml.nvmlShutdown()

        # 组装发送给 Java 的包 (对应 Java 的 GpuReportDto)
        payload = {
            "serverName": SERVER_NAME,
            "logs": logs
        }

        # 发送 POST 请求
        logging.info(f"正在上报 [{SERVER_NAME}] 的数据到 {REPORT_URL}...")
        resp = requests.post(REPORT_URL, json=payload, timeout=3)
        
        if resp.status_code == 200:
            logging.info(f"✅ 上报成功! ({len(logs)} 张显卡)")
        else:
            logging.error(f"❌ 上报失败: 状态码 {resp.status_code} - 返回: {resp.text}")

    except requests.exceptions.ConnectionError:
        logging.error(f"❌ 无法连接到服务器 {MASTER_IP}，请检查防火墙或网络。")
    except Exception as e:
        logging.error(f"❌ 发生未知错误: {e}")

if __name__ == "__main__":
    logging.info(f"🚀 监控 Agent 已启动 | 目标服务器: {MASTER_IP}")
    while True:
        collect_and_report()
        time.sleep(5) # 每5秒上报一次