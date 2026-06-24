---
name: arxiv-rt-det-dinov3-parser
description: Parse and summarize real-time object detection papers that combine large pretrained vision backbones (e.g., DINOv3/ViT) with DETR-style detectors (RT-DETR/DEIM/DEIMv2). Use when the user asks to automatically analyze a paper like "Real-Time Object Detection Meets DINOv3", provides an arXiv title/id, or wants structured Chinese research notes with key metrics, method breakdown, and extracted figures.
---

# Real-time OD × DINOv3 论文自动解析（项目级 Skill）

## 适用范围
- 用户给 **arXiv 标题或 ID**，希望“自动解析/自动笔记/结构化总结”
- 论文主题包含：**实时目标检测**、**DETR/RT-DETR**、**DINOv3/ViT 预训练特征**、**Adapter/多尺度特征**、**轻量化/端侧部署**

## 强约束（目录与命名）
在当前工作目录（本项目 `frontend/`）下操作，并保持以下结构：
- `arxiv_paper_source/{paper_id}/`：论文源码解压目录
- `arxiv_paper_pdf/`：论文 PDF
- `arxiv_paper_notes/`：输出笔记目录
- 笔记文件命名：`arxiv_paper_notes/{paper_id}_{method}.md`
- 图片目录：`arxiv_paper_notes/{paper_id}_figures/`

## 工作流（默认流程）

### 0) 准备目录
确保存在：
- `arxiv_paper_source/`
- `arxiv_paper_pdf/`
- `arxiv_paper_notes/`

### 1) 确定 paper_id
优先用标题定位最匹配的 arXiv 条目；如果用户直接给了 arXiv id，直接用。

### 2) 下载 PDF 与源码并解压
- PDF：保存到 `arxiv_paper_pdf/`
- 源码：下载 `https://arxiv.org/src/{paper_id}` 到 `arxiv_paper_source/{paper_id}/source.tar.gz` 并解压到同目录

### 3) 定位主 TeX
优先选择包含：
- `\documentclass`
- `\begin{document}`
并且 `\input{...}`/`\include{...}` 组织完整章节的主文件（常见：`main.tex`、`paper.tex`）。

### 4) 结构化阅读（只抓“可复述的要点”）
按顺序抽取：
- 摘要：一句话结论 + 关键数字
- 引言：研究问题、动机、挑战、贡献列表
- 方法：整体架构、关键模块（adapter、多尺度、训练技巧、decoder 改造）、损失函数、训练/推理流程
- 实验：主表（AP/Params/FLOPs/Latency）、与强基线差距、消融（若有）、失败点/讨论
- 局限与未来：尤其关注小目标、延迟优化、部署可行性

### 5) 提取重要图片并嵌入笔记
优先抽取三类图：
1) 方法整体框架图（pipeline/framework）
2) 关键模块示意图（adapter、multi-scale、fusion）
3) 性能-成本总览图（AP vs Params/FLOPs/Latency）

#### 图片转换说明（Windows 常见 Poppler 版本）
如果 `pdftoppm` **不支持 `-jpeg`**，使用 PNG：

```bash
pdftoppm -png -cropbox -r 300 -f 1 -l 1 -singlefile "in.pdf" "out_prefix"
```

输出文件会是 `out_prefix.png`，存放到 `arxiv_paper_notes/{paper_id}_figures/`。

### 6) 输出笔记（必须是中文、可当演示稿）
笔记必须包含以下板块（标题可微调，但信息必须齐全）：
- 基本信息（标题、arXiv、作者、链接、任务/数据集/指标）
- 一句话总结
- 研究问题
- 主要贡献（列表）
- 方法细节（分模块讲清楚：输入输出、关键公式/损失、训练与推理）
- 实验效果（关键数字、对比、趋势解释）
- 局限性与未来工作

## 输出模板（推荐）
直接使用以下骨架写入 `arxiv_paper_notes/{paper_id}_{method}.md`：

```markdown
# [论文标题]（[method]）论文自动解析笔记

## 基本信息
- 论文标题：
- arXiv：
- 作者/机构：
- 代码/项目页：
- 数据集/任务：
- 指标：

## 一句话总结
[用 1-2 句说清“做了什么 + 为什么有效 + 关键数字”]

## 研究问题
- 背景：
- 痛点/挑战：
- 本文定位：

## 主要贡献
- ...

## 方法细节
### 整体架构
### 关键模块 1（例如 STA/Adapter）
### 关键模块 2（例如 decoder 简化/训练策略）
### 损失函数与训练设置

## 实验效果
- 主结果表结论：
- 关键对比：
- 消融与分析：

## 局限性与未来工作
- ...
```

## 快速示例
- 输入：`Real-Time Object Detection Meets DINOv3`
- 输出：
  - `arxiv_paper_pdf/2509.20787_*.pdf`
  - `arxiv_paper_source/2509.20787/`（含 `main.tex` 与章节 TeX）
  - `arxiv_paper_notes/2509.20787_DEIMv2.md`
  - `arxiv_paper_notes/2509.20787_figures/*.png`

