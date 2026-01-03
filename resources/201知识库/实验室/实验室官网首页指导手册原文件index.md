🍕科研煲粥与食用手册
====================

![扇贝单词](http://yrqzxh.com/gsdoc/shanbei.php)

> 🚩**快捷导航：**
>
> * 🔗[GPU使用情况](http://www.yrqzxh.com/server/)
> * 🔗[服务器列表](https://docs.qq.com/sheet/DSVVVem1QaGVBV2dm)

> 🙌**写这个文档的目的**
>
> * 快速入门，掌握各种技巧，提高效率
> * 节省自己和他人的宝贵时间

## 📐常用煲粥工具

* [谷歌学术镜像_Google镜像站 (scqylaw.com)](http://scholar.scqylaw.com/) 网页，可在不具备翻墙条件下，使用谷歌学术，有广告；
* [Science and Engineering Journal Abbreviations](https://woodward.library.ubc.ca/woodward/research-help/journal-abbreviations/) 网页，查询期刊缩写，写论文加参考文献用；
* [DeepL翻译：全世界最准确的翻译](https://www.deepl.com/zh/translator) 网页，如标题，可以和google翻译结合使用；
* [Saladict 沙拉查词 (crimx.com)](https://saladict.crimx.com/) 浏览器插件，用于网页取词查词、翻译；
* [有道写作 (youdao.com)](https://write.youdao.com/) 网页，英文写作语法检查，相似句来源，用词调整；
* [Paraphrasing Tool | QuillBot AI](https://quillbot.com/) 网页，英文写作，句式变换，用词调整；
* [Overleaf, 在线LaTeX编辑器](https://cn.overleaf.com/) 网页，在线LaTex编辑器，功能完整；
* [iThenticate论文查重检测](http://crosscheck.ithenticatecn.com/) 网页，论文查重，20.00元 / 千字；
* [PyTorch  documentation](https://pytorch.org/docs/stable/index.html) PyTorch官方文档；
* [Diagram Software and Flowchart Maker](https://www.diagrams.net/) 绘制矢量图网站，可在线绘图，也有客户端；
* [Veusz – a scientific plotting package](https://veusz.github.io/) 论文数据图表绘制应用，不用写代码，比matplotlib好用；
* [iconfont-阿里巴巴矢量图标库](https://www.iconfont.cn/) 免费矢量图库，画框图用；

## 🔓SSH连接

ssh是我们常用的连接服务器的方式，在使用ssh连接服务器时一般需要指定用户名 `user`、主机IP `host`和端口号 `port`，通用的ssh命令如下：

```bash
ssh user@host -p port
```

Note: 在提示输入密码时，键入的任何字符都不会显示在终端中，输入密码回车即可，每次登录有三次输入密码的机会。

很多地方都提供了ssh工具，如Windows 10自带的cmd和powershell、Windows 10中的wsl、Putty、Xshell和Xterm等等，这里主要介绍两款常用的连接服务器的工具。

**实验室服务器的IP和端口号具体可参见：**[腾讯文档 服务器列表](https://docs.qq.com/sheet/DSVVVem1QaGVBV2dm)（请向文档拥有者申请查看权限）

Warning: 请勿将服务器IP与端口号泄露给他人，防止他人爆破或攻击服务器。

Note: 列表中同一地址的服务器属于同一局域网，路由器支持NAT环回，可以通过局域网快速传输文件(1Gbps)。

### 🔸MobaXterm

MobaXterm是一款强大的远程连接软件，提供了SSH、RDP、VNC等远程连接工具和FTP、SFTP等文件传输工具，内置X11 Forwarding和SSH隧道，免费使用。

![](image/base/1628845323118.png)

在使用时，只需要新建Session，选择SSH，填入指定的参数即可，连接后，左边会显示服务器的文件列表，传输协议为SFTP，可以直接**拖动文件**进行传输。

![](image/base/1628845531819.png)

MobaXterm可以记住密码，不必再每次连接都输入密码。相关设置可参考：[Mobaxterm SSH连接设置保存密码](https://blog.csdn.net/feikillyou/article/details/109130260)。另外在右侧终端中，选中即复制，右键即粘贴，也是可以设置的：[如何设置MobaXterm内鼠标右键粘贴的快捷键](https://www.jianshu.com/p/c9feb28b7975)。如果连接服务器无操作时经常自动断开，请勾选设置中 `SSH keepalive`选项：

![](image/base/1628846763100.png)

[MobaXterm下载链接](https://mobaxterm.mobatek.net/download-home-edition.html)

### 🔸X11 Forwarding

虽然服务器没有提供远程桌面连接的服务，但并不代表不能使用服务器上GUI程序，X11 Forwarding提供了一种在本地运行远程服务器图像化程序的途径。

Linux下执行一个GUI程序通常需要两个部分来完成，X server与X client。X server是专门负责显示用户界面的，它管理显示器，键盘以及鼠标，通常看到的桌面系统即是由它驱动的，X client则负责程序的逻辑，如果需要使用用户界面，则通过给X server发送请求来完成。通常情况下，X server与X client都运行在同一台机器上。但因为X系统当初设计成是通过socket在X server与X client之间通信的，所以它们也可以运行在不同的机器上。

X11 Forwarding就提供了一个方法，在远程机器上执行X client程序（如PyCharm），但是在本地机器上显示（即运行X server），MobaXterm自带了X11服务器，自动启用，不需要在额外配置。

![](image/base/1628919166596.png)

这样就可以使用命令打开任何服务器上的GUI程序，如查看图片 `xdp-open`，编辑文件 `gedit`，打开PyCharm，打开文件管理器 `nautilus`等等。

## 🔞Linux入门

### 🔸目录结构

Linux的文件系统与Windows的虚拟磁盘不同，不过只需要了解这几个就可以了：

`/`：根目录，所有的目录都是由根目录衍生出来的。

`/home`：家目录，每个用户的文件夹都在这里面存放，每个用户单独存放，登录时，默认进入的就是自己的用户目录 `/home/user`。**`~`也是代表自己的用户目录**。

`/mnt`：这个目录一般用来挂载一些硬盘，用来存放数据集。

**`.`表示当前目录，`..`表示上一级目录**。

### 🔸基本命令

`ls`: 列出目录及文件名，添加参数 `-all`可以查看文件或文件夹权限，软链接等等

`pwd`：显示目前的目录

`mkdir`：创建一个新的目录

`cp`: 复制文件或目录，`-r`： 递归持续复制，用于目录的复制

`rm`: 删除文件或目录，`-r `：递归删除，用于目录的删除

`mv`: 移动文件与目录，或修改文件与目录的名称

`cd`：切换目录

---

`cat`：由第一行开始显示文件内容

`tail`：查看文件末尾内容，`-f`：实时监测文件末尾内容，`Ctrl+C`退出

---

`ps`：（英文全拼：process status）命令用于显示当前进程的状态，类似于 Windows 的任务管理器。

查看系统进程，包含用户、PID和命令等信息：

```bash
ps -ef
```

查找指定进程：

```bash
ps -ef | grep 进程关键字/PID
```

例如显示python的进程：

```bash
ps -ef | grep python
```

```
root      1027     1  0 8月10 ?       00:00:00 /usr/bin/python3 /usr/bin/networkd-dispatcher --run-startup-triggers
root      1089     1  0 8月10 ?       00:00:46 /usr/bin/python3 /usr/bin/fail2ban-server -xf start
root      1130     1  0 8月10 ?       00:00:00 /usr/bin/python3 /usr/share/unattended-upgrades/unattended-upgrade-shutdown --wait-for-signal
```

结束进程：

```bash
kill xxx
```

例如：

```bash
kill 1130
```

Note: 普通用户只能kill自己的进程，管理员用户可以使用sudo强制停止其他人的进程。

---

查看端口占用情况：

```bash
netstat -nltp
```

---

更多请参考：

[Linux 文件与目录管理](https://www.runoob.com/linux/linux-file-content-manage.html)

[Linux ps 命令](https://www.runoob.com/linux/linux-comm-ps.html)

[Linux kill命令](https://www.runoob.com/linux/linux-comm-kill.html)

### 🔸Vim编辑文件

Vim是从 vi 发展出来的一个文本编辑器。代码补完、编译及错误跳转等方便编程的功能特别丰富，在程序员中被广泛使用。

**总结一下就是：**`i`进入编辑模式，左下角变 `Insert`，按 `Esc`退出编辑模式，输入英文标点 `:`进入命令模式，输入 `wq`然后回车，保存退出就OK了。

可以在看看[Linux vi/vim](https://www.runoob.com/linux/linux-vim.html)，写的很详细，也有演示。vim是在命令行最常用的代码编辑工具，**这个一定要会**。

### 🔸Linux的环境变量

环境变量是具有特殊名字的一个特定对象，包含了一个或多个应用程序运行所需的信息，例如：

> * `PATH`可执行程序的搜索路径，当要求系统运行一个程序，而没告诉系统它的具体路径时，系统就要在 `PTAH`值的路径中寻找此程序，找到去执行
> * `LIBRARY_PATH`静态库搜索路经
> * `LD_LIBRARY_PATH`动态库搜索路径
> * `HOME`指向用户目录的路径

在Linux系统中，环境变量按照其作用范围不同大致可以分为**系统环境变量**和**用户环境变量**。用户环境变量是每一个登录到系统的用户只能够读取到的属于自己的用户级环境变量，只对当前登录用户起作用。而系统环境变量对所有用户都起作用，且普通用户无权修改。

对于用户来说，环境变量的设置分为两种方式：**临时**和**永久**

* 临时设置使用 `export`，例如 `export PATH=/home/xyz/Tesseract/bin:$PATH`，这里需要注意的是要把原来的 `$PATH`放在后面，并用 `:`连接起来，因为系统查找可执行文件，是按照从前往后的顺序查找的，一旦找到就不会继续查找了，所以放在最前面让系统优先找这个目录。查看是否已经设好，可用 `export`（查看所有设置的环境变量）或者 `echo`查看，例如 `echo $PATH`。
* 永久生效，修改 `~/.bashrc`文件，每个用户目录下都有这个文件，对单独用户有效，在文件末尾加入 `export PATH=/home/xyz/Tesseract/bin:$PATH`，重新登录即可。

当用户需要**不同于系统默认的软件版本**或者**想快速执行某个命令**时，就需要自己设置环境变量，比如代码要求不同版本的cuda环境，不同版本的gcc，快速启动位于某个目录下的脚本等等。

### 🔸文件下载

Linux系统中的wget是一个下载文件的工具，它用在命令行下。wget支持HTTP，HTTPS和FTP协议，可以使用HTTP代理。常见的几个用法如下：

使用wget下载单个文件：

```bash
wget http://www.minjieren.com/wordpress-3.1-zh_CN.zip
```

指定下载文件名称：

```bash
wget -O wordpress.zip http://www.minjieren.com/download.aspx?id=1080
```

后台下载：

```bash
$ wget -b http://www.minjieren.com/wordpress-3.1-zh_CN.zip
Continuing in background, pid 1840.
Output will be written to 'wget-log'.
```

你可以使用以下命令来查看下载进度:

```bash
tail -f wget-log
```

断点续传：

```bash
wget -c http://www.minjieren.com/wordpress-3.1-zh_CN.zip
```

### 🔸文件解压

Linux常用的解压和压缩命令如下：

**.tar**

```bash
tar xvf FileName.tar
```

**.gz**

```bash
gunzip FileName.gz
gzip -d FileName.gz
```

**.tar.gz 和 .tgz**

```bash
tar zxvf FileName.tar.gz
```

**.zip**

```bash
unzip FileName.zip
```

**.rar**

```bash
rar x FileName.rar
```

[Linux命令搜索引擎 命令详解：最专业的Linux命令大全，内容包含Linux命令手册、详解、学习，值得收藏的Linux命令速查手册。 ](https://wangchujiang.com/linux-command/)

## 📝服务器使用规范

服务器是大家的公有资源，良好的使用习惯可以降低服务器安全风险，减少维护人员的工作负担。

### 🔸用户权限

服务器上每个人都有单独的普通权限账户，无权进行的操作包含但不止于：安装卸载系统软件，修改系统文件，修改系统服务等，如有相关需求请联系管理员，这是为了服务器安全和稳定运行考虑的，请理解。

### 🔸用户密码

请勿设置过于简单的密码，比如连续数字，以防服务器被攻击，占用显卡资源，可使用 `passwd`命令更改自己密码。**任何用户在登录时连续错误输入密码三次，IP会被加入服务器黑名单10mins。**

### 🔸数据存放

基本每一个服务器都有一个或多个单独的数据硬盘，用于存放用户数据集和用户大文件，一般在 `/mnt`目录下挂载，所以较大的数据（如数据集和权重文件）请放在此路径下，不要随意存放在自己 `home`目录下，不然挂载 `home`目录的硬盘会随着时间推移，可用空间逐渐减少，影响其他用户使用和系统稳定。

以E1为例：

```bash
$ df -h
/dev/nvme0n1p2  916G   38G  832G   5% /
/dev/sda1       1.8T  1.3T  410G  77% /home
/dev/sdb1       3.6T  541G  2.9T  16% /mnt/sdb
```

1T的固态硬盘挂载在根目录用于系统，2T的机械硬盘挂载在 `home`目录，用于存放系统用户数据，另外还有4T的机械硬盘挂载在 `/mnt/sdb`，用于存放自定义用户数据。

在其下，用户拥有一个拥有者是自身的文件夹（由管理员创建），用户可自行读写。

```bash
$ ls -all
total 28K
drwxr-xr-x 3 hz   hz   4.0K 7月   6 20:26 hz
drwx------ 2 root root  16K 6月  30 17:26 lost+found
drwxr-xr-x 3 xyl  xyl  4.0K 7月   6 22:03 xyl
drwxr-xr-x 8 zx   zx   4.0K 8月  15 10:21 zx
```

**用户可以通过创建软链接的方式（类似于Windows的快捷方式），将此处的文件夹链接到指定目录。**

Linux ln（英文全拼：link files）命令是一个非常重要命令，它的功能是为某一个文件在另外一个位置建立一个同步的链接。当我们需要在不同的目录，用到相同的文件时，我们不需要在每一个需要的目录下都放一个必须相同的文件，我们只要在某个固定的目录，放上该文件，然后在其它的目录下用ln命令链接（link）它就可以，不必重复的占用磁盘空间。

```bash
ln -s [源文件或目录][目标文件或目录]
```

创建软链接：

```bash
ln -s /mnt/sdb/zx/coco /home/zx/project/data/coco
```

删除软链接：

```bash
rm /home/zx/project/data/coco
```

Note: `ls`命令查看目录列表时，目录，文件和软链接的颜色是不同的，可以加上 `-all`参数查看软链接的目标路径。

Note: 账号申请，注销，密码重置等问题请联系张宇浩或者张熙。

## ☢️深度学习环境

### 🔸Anaconda

因为服务器都是大家共用的，每个人所需要的python环境各不相同，环境中的包也不相同，所以在服务器上都会使用anaconda创建不同的python环境。为了节省服务器空间，**A2，B，C，D，F1，E1，E2，F2，F3, G1 的anaconda均安装在公共目录下，用户不需要再单独安装**，其他服务器上若使用anaconda则需要**自行安装**。

在**初次使用anconda**时，应执行 `conda init`来初始化anaconda的用户环境变量。（如果shell使用的zsh，请执行 `conda init zsh`），同时为避免公共anaconda的 `pkg`目录权限问题，请执行  `conda config --add pkgs_dirs ~/.conda/pkgs/`，否则在创建环境时可能会出现一些问题。

Note: 如果不想在登录系统时进入base环境，请执行 `conda config --set auto_activate_base False`

Warning: 为方便conda环境管理，通过conda创建的环境请统一命名为：用户名_环境名，如：zx_mml

---

**若需自行安装Anaconda，步骤如下：**

使用wget从网站下载Linux版的anaconda安装包，如 Anaconda3-2021.05-Linux-x86_64.sh（来自 [Anaconda | Anaconda Distribution](https://www.anaconda.com/products/distribution)）
执行 `sh Anaconda3-2021.05-Linux-x86_64.sh`
默认安装目录为 `/home/<user>/anaconda<2 or 3>`

`Do you wish the installer to initialize Anaconda3 by running conda init?` 输入 `yes`
初始化shell环境变量（默认shell为bash的用户请输入yes）

Note: 默认shell不是bash的用户请输入 `no`，然后输入 `source <path to conda>/bin/activate`，执行 `conda init <shell-name>`，如 `conda init zsh`，进行其他shell的环境变量初始化。

---

在anaconda中，使用conda同时进行**包的管理**（和python的pip包管理器类似）和**虚拟环境**的管理，值得注意的是，在虚拟环境中**仍然可以使用pip**进行包的管理。

conda 和 pip 的命令参考和对比：

| 任务                 | conda 命令                                                                             | pip 命令     |
| :------------------- | :------------------------------------------------------------------------------------- | :----------- |
| 创建环境             | `conda create --name $ENVIRONMENT_NAME python=3.7`                                   | ——         |
| 激活环境             | `conda activate $ENVIRONMENT_NAME`                                                   | ——         |
| 退出环境             | `conda deactivate`                                                                   | ——         |
| 删除环境             | `conda env remove --name $ENVIRONMENT_NAME --all`                                    | ——         |
| 查看已创建的虚拟环境 | `conda env list`                                                                     | ——         |
| 安装包               | `conda install $PACKAGE_NAME`                      | `pip install $PACKAGE_NAME`   |              |
| 卸载包               | `conda uninstall $PACKAGE_NAME`                    | `pip uninstall $PACKAGE_NAME` |              |
| 查看已安装包         | `conda list`                                                                         | `pip list` |
| 搜索可用包           | `conda search $SEARCH_TERM`                        | `pip search $SEARCH_TERM`     |              |

为了加速conda和pip的下载速度，建议使用国内开源镜像，设置方法如下：

**conda**

修改用户目录下的 `.condarc` 文件为：

```
channels:
  - defaults
show_channel_urls: true
default_channels:
  - https://mirrors.bfsu.edu.cn/anaconda/pkgs/main
  - https://mirrors.bfsu.edu.cn/anaconda/pkgs/r
  - https://mirrors.bfsu.edu.cn/anaconda/pkgs/msys2
custom_channels:
  conda-forge: https://mirrors.bfsu.edu.cn/anaconda/cloud
  msys2: https://mirrors.bfsu.edu.cn/anaconda/cloud
  bioconda: https://mirrors.bfsu.edu.cn/anaconda/cloud
  menpo: https://mirrors.bfsu.edu.cn/anaconda/cloud
  pytorch: https://mirrors.bfsu.edu.cn/anaconda/cloud
  simpleitk: https://mirrors.bfsu.edu.cn/anaconda/cloud
```

即可添加 Anaconda Python 免费仓库。运行 `conda clean -i` 清除索引缓存，保证用的是镜像站提供的索引。

用户目录下的 `.condarc` 可能不存在，可使用vim或者touch命令创建。

**pip**

进入虚拟环境：

```bash
pip config set global.index-url https://mirrors.bfsu.edu.cn/pypi/web/simple
pip config set install.trusted-host mirrors.bfsu.edu.cn
```

相关链接：

[🔗北京外国语大学开源软件镜像站](https://mirrors.bfsu.edu.cn/)

[🔗阿里巴巴开源镜像站](https://developer.aliyun.com/mirror/)

### 🔸PyTorch的安装

借助GPU的深度学习环境基本都由nvidia驱动，cuda工具包，cudnn工具包，深度学习框架（PyTorch，TensorFlow等）几部分组成，四个部分环环相扣。nvidia驱动，cuda，cudnn都是安装在系统中的，所有人共用。而深度学习框架是用户使用的，安装在自己的虚拟环境中。

根据服务器使用的显卡型号，每台服务器都预置了兼容了nvidia驱动，cuda和cudnn，不同cuda版本所需的深度学习框架版本是不同的，安装时要确定自己所使用的cuda版本。

通过查看/usr/local目录：

```bash
$ ls /usr/local -all
total 44K
drwxr-xr-x  2 root root 4.0K 6月  24 22:36 bin
lrwxrwxrwx  1 root root   21 6月  23 14:38 cuda -> /usr/local/cuda-11.1/
drwxr-xr-x 15 root root 4.0K 8月  16 21:58 cuda-10.1
drwxr-xr-x 14 root root 4.0K 6月  23 14:39 cuda-11.1
drwxr-xr-x 18 root root 4.0K 8月  16 15:20 cuda-9.0
```

或者通过查看nvcc编译器版本：

```bash
$ nvcc -V
nvcc: NVIDIA (R) Cuda compiler driver
Copyright (c) 2005-2020 NVIDIA Corporation
Built on Mon_Oct_12_20:09:46_PDT_2020
Cuda compilation tools, release 11.1, V11.1.105
Build cuda_11.1.TC455_06.29190527_0
```

Warning: 有些服务器可能安装了多个cuda版本，/usr/local/cuda所指向的为系统默认cuda。

若要更换自己使用的CUDA版本，请更改用户环境变量，在用户目录的 `.bashrc`文件（`zsh`用户请添加到 `.zshrc`）中添加以下内容（请将xx.x替换为自己需要的版本，通过 `ls /usr/local -all`查看服务器已安装的CUDA版本）：

```bash
export CUDA_HOME=/usr/local/cuda-xx.x 
export PATH=$CUDA_HOME/bin:$PATH 
export LD_LIBRARY_PATH=/usr/local/cuda-xx.x/lib64${LD_LIBRARY_PATH:+:${LD_LIBRARY_PATH}}
```

在确定了cuda版本之后，就可以在虚拟环境中安装对应的深度学习框架了，以pytorch为例，可使用conda和pip（wheel）两种方式的安装方法，**不同版本的命令列表详见**[Previous PyTorch Versions | PyTorch](https://pytorch.org/get-started/previous-versions/)

**推荐从国内镜像自行下载wheel安装包，并通过pip安装，因为pytorch官方通道大部分情况下下载都比较慢**

由于官方pip源下载缓慢，可以从国内镜像手动下载wheel安装包进行下载，[mirror.sjtu.edu.cn/pytorch-wheels/torch_stable.html](https://mirror.sjtu.edu.cn/pytorch-wheels/torch_stable.html)

由于cuda版本众多，可能需要手动搜索对应的cuda，pytorch，torchvision版本，如pytorch官方安装命令为

```
pip install torch==1.13.1+cu116 torchvision==0.14.1+cu116 torchaudio==0.13.1 --extra-index-url https://download.pytorch.org/whl/cu116
```

那就需要在镜像中搜索cu116的版本：

![1702791523693](image/index/1702791523693.png)

使用 `wget`下载到服务器后，使用pip进行安装，安装前请切换 `pip`为国内源，否则一些依赖包下载速度会很慢。

```
pip install torch-xxx-xxx.whl
pip install torchvision-xxx-xxx.whl
```

下载完成后使用 `pip install xxx.whl`直接安装，先安装 `torch`，再安装 `torchvision`，**安装前请切换 `pip`为国内源，否则一些依赖包下载速度会很慢。**

Warning: 不建议使用pytorch<1.0的版本，**显卡为3090的机器，必须使用 CUDA 11.1+ pytorch 1.8+**，其他版本均不支持，如有比较老旧的项目可能需要修改才能运行。

### 🔸GPU的状态

可以使用 `nvidia-smi`或者 `gpustat`查看当前GPU的状态

```bash
$ nvidia-smi
```

```
Wed Aug 18 14:42:21 2021
+-----------------------------------------------------------------------------+
| NVIDIA-SMI 470.57.02    Driver Version: 470.57.02    CUDA Version: 11.4     |
|-------------------------------+----------------------+----------------------+
| GPU  Name        Persistence-M| Bus-Id        Disp.A | Volatile Uncorr. ECC |
| Fan  Temp  Perf  Pwr:Usage/Cap|         Memory-Usage | GPU-Util  Compute M. |
|                               |                      |               MIG M. |
|===============================+======================+======================|
|   0  NVIDIA GeForce ...  Off  | 00000000:2D:00.0 Off |                  N/A |
| 43%   52C    P0   120W / 390W |      0MiB / 24268MiB |      0%      Default |
|                               |                      |                  N/A |
+-------------------------------+----------------------+----------------------+
|   1  NVIDIA GeForce ...  Off  | 00000000:2E:00.0 Off |                  N/A |
| 86%   66C    P2   345W / 350W |  22539MiB / 24268MiB |     99%      Default |
|                               |                      |                  N/A |
+-------------------------------+----------------------+----------------------+

+-----------------------------------------------------------------------------+
| Processes:                                                                  |
|  GPU   GI   CI        PID   Type   Process name                  GPU Memory |
|        ID   ID                                                   Usage      |
|=============================================================================|
|    1   N/A  N/A      8793      C   ...nvs/open-mmlab/bin/python    22537MiB |
+-----------------------------------------------------------------------------+
```

```bash
$ gpustat
```

```
ServerE1                    Wed Aug 18 14:45:01 2021  470.57.02
[0] NVIDIA GeForce RTX 3090 | 51'C,   0 % |     0 / 24268 MB |
[1] NVIDIA GeForce RTX 3090 | 66'C,  93 % | 22539 / 24268 MB | fsr(22537M)
```

若要连续查看可使用 `watch`命令，如 `watch -n 1 nvidia-smi` 每1s输出刷新一次，`Ctrl+C`退出。

若要指定运行的GPU，可以通过**设置环境变量**的方式：

在程序一开始设置：

```python
import os
os.environ["CUDA_DEVICE_ORDER"] = "PCI_BUS_ID"
os.environ['CUDA_VISIBLE_DEVICES'] = "0,1" #单卡os.environ['CUDA_VISIBLE_DEVICES'] = "0"
```

或者在运行代码前执行：

```bash
export CUDA_DEVICE_ORDER=PCI_BUS_ID
export CUDA_VISIBLE_DEVICES=0,1
```

Note: 环境变量 `CUDA_DEVICE_ORDER`指明了显卡的排列顺序，如果不指定为 `PCI_BUS_ID`，实际使用的显卡编号可能与 `nvidia-smi`命令中的编号不一致。

Note: pytorch代码中的 `cuda:id`其中的 `id`是环境变量 `CUDA_VISIBLE_DEVICES`中的顺序编号，与物理显卡设备顺序无关。如CUDA_VISIBLE_DEVICES=1，那么 `cuda:0`即指1号显卡。

## 🛩️文件传输

除了可以在使用MobaXterm登录服务器的同时，在左侧进行文件的拖动传输之外，也有一些专门的工具进行文件传输。

### 🔸WinSCP

> WinSCP is an open source free **SFTP client**, FTP client, WebDAV client, S3 client and **SCP client** for **Windows**. Its main function is file transfer between a local and a remote computer.

WinSCP只能用在Windows上用于与服务器进行文件传输，可以使用sftp和scp两种安全协议。使用时，我们只需要将用于ssh登录的 `host`，`user`，`port`填入即可，sftp，scp和ssh连接的端口都是一致的。[下载页面](https://winscp.net/eng/download.php)

![img](image/base/1629302722756.png)

### 🔸Filezilla

> The FileZilla Client not only supports FTP, but also FTP over TLS (FTPS) and SFTP.

Filezilla是一款支持SFTP的跨平台文件传输工具，它不仅可以在Windows上使用，也可以在Linux上使用，这样就可以在服务器之间传输文件。[下载页面](https://filezilla-project.org/download.php?type=client)

**在Windows上使用：**

![](image/base/1629304117080.png)

协议选择SFTP，同样主机名，端口号，用户密码均与ssh登录的相同。

**在服务器上使用：**

打开mobaxterm连接服务器，在命令行输入 `filezilla`， 随后filezilla的X11窗口就会打开

![](image/base/1629305656435.png)

点击左上角站点管理器

![](image/base/1629305733103.png)

在Protocal处滚动滚轮选择SFTP协议，Logon Type同样的方式选择Normal。分别输入另一台服务器的 `host`，`user`，`port`和密码，连接即可直接在服务器之间传输文件。

Filezilla在传输文件时，最大同时传输的文件数为2，如果用来传输大量小文件大量图片，可将该值设置为10（最大值为10）🚀

![1646841810782.png](image/index/1646841810782.png)

Warning: WinSCP和Filezilla在连接服务器之后，并不会在使用过程中自动刷新远程目录，请手动进行刷新。

### 🔸FTP服务器

除了上述两种传输策略外，在**校园网内**还有一个共用的FTP文件服务器🗂️，可提供公共数据集下载，文件中转，**常用软件下载**（texlive，Adobe，MobaXterm，Anaconda，PyTorch等，公共安装包在 `public/software`目录下）的功能。服务器地址为 `ftp.laocheng.tech`，端口均为默认的21端口。

**在Linux服务器上使用：**

在服务器上的命令行使用时，要借助lftp工具，它是一个交互式的ftp文件传输工具。

```bash
$ lftp ftp.laocheng.tech
lftp ftp1.laocheng.tech:~> ls
drwxr-xr-x 1 ftp ftp              0 May 11  2021 Bert pretrained
drwxr-xr-x 1 ftp ftp              0 Aug 12 12:14 caption_datasets
drwxr-xr-x 1 ftp ftp              0 Jan 24  2021 CelebA
drwxr-xr-x 1 ftp ftp              0 Jan 24  2021 cifar10
drwxr-xr-x 1 ftp ftp              0 Jan 24  2021 Cityscapes
drwxr-xr-x 1 ftp ftp              0 Jan 24  2021 coco_yolo_labels
drwxr-xr-x 1 ftp ftp              0 Jul 15  2021 COVERAGE
drwxr-xr-x 1 ftp ftp              0 May 21  2021 DAVIS
drwxr-xr-x 1 ftp ftp              0 Jan 26  2021 DIV2K
drwxr-xr-x 1 ftp ftp              0 Jan 24  2021 DOTA
drwxr-xr-x 1 ftp ftp              0 Mar 29  2021 DUT-OMRON
drwxr-xr-x 1 ftp ftp              0 Mar 27  2021 ECSSD
drwxr-xr-x 1 ftp ftp              0 Aug 12 16:35 Flickr30k
drwxr-xr-x 1 ftp ftp              0 Mar 27  2021 FSS-1000
drwxr-xr-x 1 ftp ftp              0 May 21  2021 FVI
drwxr-xr-x 1 ftp ftp              0 Mar 18  2021 HashDataset
drwxr-xr-x 1 ftp ftp              0 Jan 24  2021 HRSC2016
```

常用命令如下：

连接服务器：`lftp ftp.laocheng.tech`

列出服务器目录：`ls`

切换服务器目录：`cd`

列出本地目录：`!ls`

切换本地目录：`lcd`

上传文件：`put xxx`

上传多个文件：`mput xxx xxx`

下载文件：`get xxx`

下载多个文件：`mget xxx xxx`

多线程下载：`pget xxx`

下载目录：`mirror xxx`

上传目录：`mirror -R xxx`

文件操作支持通配符 `*`

退出lftp交互命令行：`Ctrl+D`

**在Windows上使用：**

![](image/base/1629612678857.png)

选择添加网络位置，填入服务器地址，选择匿名登录即可。

![](image/base/1629612805298.png)

Warning: ftp文件服务器上的根目录和数据集目录权限均为只读，`/public`的部分目录为公有目录可读可写。

## 🕐后台运行

与使用桌面不同，我们在ssh终端中运行的命令，都会随着ssh连接的断开而终止，而深度学习的训练一般都是非常耗时的，在训练过程中，ssh连接可能会因为网络不稳定等因素而断开，所以需要一种会话的机制，让我们能够在会话中运行一些指令，在会话断开后，命令不会停止。screen和tmux就提供了这种功能，两者用法类似，可依据自身喜好选择。这里就以screen为例介绍，tmux的使用可以参考：[Tmux使用手册](http://louiszhai.github.io/2017/09/30/tmux/)

创建会话：`screen -S test`

从会话中detach（不会影响代码运行）：`Ctrl+A, Ctrl+D`

列出当前所有会话：`screen -ls`

```
There is a screen on:
29978.test      (2021年08月22日 15时15分08秒)   (Detached)
1 Socket in /run/screen/S-zx.
```

重新连接会话：`screen -r test` 或者 ` screen -r 29978`

在会话内，结束会话：`Ctrl+D`

在会话外，结束会话：`screen -X -S test quit`

手动断开已连接的会话（某些情况下的非正常断开，会导致会话状态为attached，而无法恢复连接，需要手动detach）：`screen -d test `或者 ` screen -d 29978`

在进入screen模式下之后，无法在终端使用鼠标滚轮。要先按 `Ctrl+A`键，然后释放，再按 `[`键即可进入**复制模式**，PgUp（或Ctrl+F）和PgDn（或Ctrl+B）、鼠标滚轮进行滚动和翻页，退出复制模式 `Ctrl+C`或者 `Esc`。

Warning: 要在进入到会话之后再激活环境，否则可能无法进入环境，虽然可能提示已经在对应的虚拟环境中。

## 🎢远程调试

仅仅依靠ssh连接的命令行来进行代码的编写和调试是远远不能满足需求的，我们可以在本地搭建开发环境（PyCharm，VSCode等），将修改的代码同步到服务器运行。

### 🔸VSCode with Remote-SSH

本地的VSCode可以直接连接远程服务器进行远程调试，体验要比x11 forwarding要好很多。

![img](image/base/1629792502968.png)

图为VSCode的远程调试的设计框架，下面是具体的使用步骤：

![](image/base/1629793075334.png)

安装Remote-SSH扩展包。

![](image/base/1629793219934.png)

点击左下角图标，新建连接。

![](image/base/1629793338364.png)

![img](image/base/1629793375559.png)

按照 `ssh user@host -p port -A`的格式输入要连接的远程主机。

![](image/base/1629793396818.png)

保存连接配置到用户ssh文件夹的config文件中。

![](image/base/1629793416181.png)

选择目标主机（即服务器）的操作系统平台，Linux。

![](image/base/1629793466207.png)

输入用户密码（**后续操作可能会多次要求输入密码，使用密钥登录可以避免重复输入密码**），等待VSCode的服务端安装完毕。

![](image/base/1629793483805.png)

在连接成功之后，左下角会变为已连接的主机名称，在左侧资源管理器可以选择要打开的远程文件夹。

![](image/base/1630324614891.png)

![](image/base/1630324912739.png)

选择要打开的文件夹，并点击确定，在左侧可以进行文件的上传和下载。（右键进行文件的下载，上传直接从本地拖入即可）

![](image/base/1630325059143.png)

和在本机上使用VSCode类似，也要在扩展中为远程VSCode安装所需的扩展包，如python扩展包，提供代码语法检查，代码提示和调试等功能（部分扩展包可能需要重启VSCode才会生效）。

![](image/base/1630324848190.png)

在安装了Python的扩展包之后，打开文件夹中的Python文件，会自动激活python扩展，然后选择服务器上自己创建的虚拟环境即可。**（最新VSCode的虚拟环境选择在窗口视图的右下角）**

![1648522268482.png](image/index/1648522268482.png)

![](image/base/1630325936207.png)

可以在标题栏点击终端，即可打开远程服务器的Terminal。

Warning: 使用远程调试时，服务器上修改的代码不会保存到本地，请定时备份服务器上的文件，以防止文件丢失。

### 🔸PyCharm with X11 Forwarding

PyCharm不仅有Windows的版本，也有服务器上的版本，借助x11 forwarding，我们可以在本地借助PyCharm调试远程服务器的代码。

首先打开MobaXterm，下载并解压（[Download PyCharm: Python IDE for Professional Developers by JetBrains](https://www.jetbrains.com/PyCharm/download/#section=linux)，选择Linux社区版获取最新版下载链接）：

```bash
wget https://download.jetbrains.com/python/pycharm-community-2021.2.tar.gz
```

```bash
tar zxvf pycharm-community-2021.2.tar.gz
```

运行PyCharm：

```bash
cd pycharm-community-2021.2/bin
```

```bash
./pycharm.sh
```

等待片刻PyCharm的窗口会在本地打开：

![](image/base/1629633631793.png)

打开项目文件后，要配置使用的conda环境：

![](image/base/1629634028948.png)

在使用PyCharm时，MobaXterm不能退出，否则远程服务器的PyCharm本地窗口会随之关闭。

要想直接通过点击PyCharm的**运行按钮**运行代码，需要添加运行时的必要参数如：

![](image/base/1629636049738.png)

### 🔸VSCode with SFTP

VSCode作为一个轻量化代码编辑器，在本地调试Python代码时，需要安装Python（内置Pylance）这个扩展包才能进行Python代码的调试和运行。Pylance提供了VSCode中Python代码的自动补全，代码索引等功能。SFTP是一种文件传输协议，VSCode的相关扩展可以自动帮助我们同步修改的文件。

![](image/index/1635064452317.png)

首先在扩展中搜索SFTP，并安装（注意安装的版本来源，liximono的版本原作者已经不再维护了，而且目前处于不可用状态。）

![](image/index/1635064601974.png)

安装完成之后，Ctrl+Shift+P 打开VSCode的命令界面，选中 `SFTP: Config`

![](image/index/1635064717594.png)

之后，扩展会自动在打开的项目文件夹目录下创建.vscode文件夹和sftp.json的配置文件。我们需要指定所连接服务器的 `host`、`port`和 `remotePath`以建立sftp文件传输连接并同步文件。

![](image/index/1635065196303.png)

如果不想每次同步文件时都输入密码，可以添加windows的ssh私钥文件路径到配置文件中（`privateKeyPath`），密钥生成方法参考文档之后的内容。

![](image/index/1635065269478.png)

随后就可以在侧边栏列出服务器 `remotePath`中的文件列表，当编辑本地的文件并保存时，会自动将文件同步到服务器并在左下方显示文件上传状态。

![](image/index/1635065427849.png)

在资源管理器右键文件夹或者文件时，还可以手动选择同步选项。

这种在本地编辑代码然后同步到服务器上的方式，只能在连接服务器的终端中执行命令，不能在本地调试和运行。所以通过以下两种方式，我们还可以**直接修改和调试远程服务器上的代码。**

## ✨密钥登录

除了使用用户名和密码登录登录服务器之外，还可以使用密钥登录，只需要将自己的公钥拷贝到服务器上即可，再次登录时便不需要输入密码。

**首先在Windows的cmd或者powershell中生成密钥对（一路回车就行）：**

```bash
ssh-keygen
```

```
Generating public/private rsa key pair.
Enter file in which to save the key (C:\Users\HAPPY/.ssh/id_rsa):
Enter passphrase (empty for no passphrase):
Enter same passphrase again:
Your identification has been saved in C:\Users\HAPPY/.ssh/id_rsa.
Your public key has been saved in C:\Users\HAPPY/.ssh/id_rsa.pub.
The key fingerprint is:
SHA256:mkz4fLvfdvfd8Ejn8hlv5mjhkpyRWuemhgmhDjB+h4dcfg happy@DESKTOP-A49DO1F
The key's randomart image is:
+---[RSA 3072]----+
|             /.o.|
|              ..E|
|               ..|
|     . 3   . ...o|
|    . . S   = B+=|
|     = 2     O-%=|
|      * . o./oBBB|
|       . . *+ ++=|
|          . o..+o|
+----[SHA256]-----+
```

Warning: 重复运行此命令会覆盖之前生成的密钥，导致免密登录失效。

**然后将公钥拷贝至服务器端：**

公钥文件储存放在Windows用户文件夹的 `.ssh`文件夹中， `C:/Users/<user name>/.ssh/id_rsa.pub`，用记事本打开 `id_rsa.pub`，复制全部内容到服务器家目录(`~`)中的  `.ssh/authorized_keys `文件中，若 `authorized_keys `文件不存在，可使用 `vim `或者 `touch `创建。在 `authorized_keys`文件中，每个公钥占一行。

```bash
$ ll
total 8.0K
-rw-rw-r-- 1 zx zx 810 8月  17 17:15 authorized_keys
-rw-r--r-- 1 zx zx 666 8月  17 15:09 known_hosts
```

```bash
$ cat authorized_keys
ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABAQDkFUFwpNI8ia9T6dHLHAO+by5aO//ilOjald15Ax9WJ+DfzsX+cXkltYtPLrGfsGz3ayTgPOqvwRrfnZs7rPSnTgJxm5+GSKBT9IA1dgtF3wNKvNGJJxvC0ccE6orytUdueOx2fFWExu47vC3MEIGjMYP0aycU9x0ByeUnHx6ZQCx8lP+D9eYvZB+TTWS2siu0rtjO3jJnr6mMbsstfgrgesgesrgewgegsrgNb62px1DeKgRU3FOSwqenG+bmxMR4LHvtSgWImDix zhangxi@DESKTOP-A5A3VVN
ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABAQDhKH3IzVUosRhHmi/CMQotx7yxhrx52ntkguVoCIm/vooA5Oy3ezbUAl3dO7gLYDkMJtVVXDvpCkRks/x7edrEMm6LVjtEamf7Axj38Hnhwokxu5XGhuiilNoOeHU0BX52J2APs2U3YiisN+/Idg2e/ezljLM7kmn2zjB+v7ghG/mo/R/F8r+7h9i6ZBNjmH/2yhR14EWxPhgAuXmUXPFD1/uzRfrQvjeAATFEmyEMj3O3oNnQ9it+3n535gdgewtergdgergreageage5pppBVqNxaXV4H1+jxgypPfC2tdb8OyEvguPhs9RBiv happy@DESKTOP-4O4VA9I
```

保存文件，重新登录时，就不用再输入密码了。

Warning: 修改用户密码不影响使用密钥登录服务器，若有人破解了服务器之后植入了自己的公钥，那需要通过删除 `authorized_keys`文件来防止其再次登录。

## 📈训练可视化

对于训练周期较长的代码，了解训练过程的各种指标（损失，评价指标，图形等等）是很重要的。TensorBoard是最常用的训练可视化工具，目前已经集成在PyTorch中，VisualDL是百度PaddlePaddle的可视化工具。两者都是独立的Python包，不依赖于特定的深度学习框架，且用法类似。本章节只介绍最基本的使用，更多内容请分别参考以下文档：[torch.utils.tensorboard — PyTorch 1.11.0 documentation](https://pytorch.org/docs/stable/tensorboard.html)和[VisualDL 工具简介-使用文档-PaddlePaddle深度学习平台](https://www.paddlepaddle.org.cn/documentation/docs/zh/1.5/user_guides/howto/evaluation_and_debugging/debug/visualdl.html)，TensorBoard的示例较少，国内文档不多，而VisualDL为百度开发，文档示例齐全，两者用法几乎相同。

### 🔸基本使用

所有的可视化工具几乎都是使用Python实现的一个Web服务器，通过命令启动之后，就可以在浏览器看到Web服务器中展示的可视化内容。以tensorboard为例:

```bash
$ tensorboard --logdir ./tb_logs --port 8080
```

Note: 可以创建一个screen会话，运行tensorboard

然后打开MobaXterm，借助X11 Forwarding打开服务器上的火狐浏览器：

```bash
firefox http://localhost:8080
```

但是这种方式比较卡顿，依赖于X11 Forwarding，建议使用下面两种方式。

### 🔸ssh隧道

除了使用X11打开服务器上的浏览器之外，还可以借助ssh连接建立ssh隧道的端口转发，将远程服务器的某个端口映射到本地来，MobaXterm提供了这个功能的可视化设置：

![](image/base/1630681404972.png)

![](image/base/1630681614492.png)

![](image/base/1630681749725.png)

此时在在自己的浏览器打开**http://localhost:10000**，就能访问服务器上的可视化网页。

Note: 本地的端口应该是空闲的，才能正常使用。设置中**要绑定的本地端口**，**ssh登录所用端口**和**远程服务器上的服务端口**三者没有必然联系。

在VSCode的远程调试中也提供了通过ssh隧道端口转发的功能，另外在ssh登录时也可以添加相关参数实现端口转发，他们的原理都是相同的，这里不再详细说明。

### 🔸端口转发

除了通过以上两种方式打开训练可视化的页面外，服务器所连接的路由器上还做了部分端口的转发，这样可以直接通过访问IP和端口号打开远程服务器上的特定端口上的Web服务。例如E1的12001-13000范围内的端口，均转发到了校园网内，要访问这些端口的Web服务，只需要使用连接E1时所用的主机名加上特定端口号即可。

此时在运行可视化工具时，需要修改端口号到指定范围内，并添加 `--bind_all`选项，如：

```bash
$ tensorboard --logdir ./tb_logs --port 12345 --bind_all
```

这样在校园网内任意浏览器打开**http://lab.laocheng.tech:12345**即可访问服务器上的Web内容了。服务器具体转发的端口范围请查看[腾讯文档 服务器列表](https://docs.qq.com/sheet/DSVVVem1QaGVBV2dm)。

Warning: 这种方式无法在通过公网登录的情况下使用，因为公网只穿透了ssh所用端口。

## 🖲️多机多卡分布式训练

现有的大型模型开源代码基本上都是基于 DDP 的分布式训练模型，属于单节点分布式训练，但是我们的目前的服务器最多只有2张显卡，在这种情况下为了更快的训练模型，就需要使用多节点分布式训练，这本质上也是基于 DDP 的分布式训练，而 NCCL 是 PyTorch 的 DDP 训练中常用的后端支持框架，支持 TCP/InfiniBand 连接的 GPU 分布式训练。但是要想在多个节点上实现比较理想的分布式训练，对节点之间的数据交换速度有很高的要求，大概需要 40~100Gbps 的节点带宽（目前消费级主机最快配备 2.5Gbps 的网口速率）。

目前在F2与F3，E2与F1之间实现了 40Gbps 的 InfiniBand 的连接。

### 🔸使用前准备：

`ibstat`确认两个节点的服务器已经通过InfiniBand连接：

![1662381380017](image/index/1662381380017.png)

若 `State` 显示 `Initializing`，请联系管理员执行 `sudo /etc/init.d/opensmd restart`

管理员执行 `sudo ibnodes` 确认当前已经接入 `子网管理器`的节点（一般端口状态为 `Active` 时，`ibnodes` 不用再核验）：

![1662382718332](image/index/1662382718332.png)

Note: 以下代码以E2和F1为例，E2为node1(`node_rank`: `0`, `IP`: `192.168.31.56`)，F1为node2(`node_rank`: `1`, `IP`:`192.168.31.5`)。

![1662433181997](image/index/1662433181997.png)

### 🔸几个概念

`world_size`：全局进程数量，即所有显卡数量(`4`)；

`node_rank`：节点编号(`0`, `1`)；

`local_rank`：每个节点的GPU编号（`0`, `1`），用于控制数据和模型的位置；

`global_rank`：全局节点编号(`0(0-0), 1(0-1), 2(1-0), 3(1-1)`) (`global_rank(node_rank-local_rank)`)，可用于控制 checkpoint 和log的保存；

`master_addr`：`node_rank` 为 `0` 的节点 `IP`，本机为 `127.0.0.1`，其他节点填写 node1 的 `内网IP`，可通过 `ifconfig` 查看；

`master_port`：`node_rank` 为 `0` 的节点任意一个可用端口；

`per_gpu_batchsize`：每个GPU上的 `batch size` 即 `Dataloader`中设置的 `batch_size`；

### 🔸通过 spawn 启动

这种方式需要在 `init_process_group` 中传入 `master_addr`，`master_port`，`world_size `和计算得到的 `global_rank (local_rank + node_rank * 2)。`

```python
import torch
import torch.nn as nn
from torch.utils.data import DataLoader
from torch.utils.data.distributed import DistributedSampler
import torch.optim as optim
from torch.nn.parallel import DistributedDataParallel as DDP
import torch.multiprocessing as mp
import torch.distributed as dist
import argparse
parser = argparse.ArgumentParser()
parser.add_argument('--per_gpu_batchsize', default=4, type=int)
parser.add_argument("--learning_rate", type=float, default=1e-4)
parser.add_argument('--max_epochs', default=10, type=int)
parser.add_argument("--world_size", type=int, default=1)
parser.add_argument("--node_rank", type=int, default=0)
parser.add_argument("--master_addr", default="127.0.0.1", type=str)
parser.add_argument("--master_port", default="2345", type=str)
args = parser.parse_args()

def train(local_rank, node_rank, world_size):
    node_device_count = torch.cuda.device_count()
    global_rank = local_rank + node_rank * node_device_count
    dist.init_process_group(
        backend='nccl',
        init_method=f'tcp://{args.master_addr}:{args.master_port}',
        world_size=world_size,
        rank=global_rank
    )
    print(
        dist.get_world_size(),
        dist.get_rank(),
        local_rank,
        node_rank,
    )
    device = torch.device(f"cuda:{local_rank}")
    _model = Model(args).to(device)
    model = DDP(_model, device_ids=[local_rank], output_device=local_rank, find_unused_parameters=True)
    if dist.get_rank() == 0:
        print(args)

    train_dataset = Dataset()
    train_sampler = DistributedSampler(train_dataset, shuffle=True, drop_last=False)
    train_dataloader = DataLoader(train_dataset, batch_size=args.per_gpu_batchsize, sampler=train_sampler, num_workers=8)

    loss_fn = nn.CrossEntropyLoss(reduction='mean')
    grouped_parameters = [
        {"params": model.parameters(), "lr": 1e-6,}
    ]
    optimizer = optim.AdamW(grouped_parameters)
    for epoch in range(args.max_epochs):
        train_dataloader.sampler.set_epoch(epoch)
        for x, y in train_dataloader:
            optimizer.zero_grad()
            label = label.to(device)
            _y = model(x)
            loss = loss_fn(y, _y)
            loss.backward() 
            optimizer.step()

if __name__=='__main__':
    mp.spawn(
        train,
        args=(args.node_rank, args.world_size, ),
        nprocs=torch.cuda.device_count(),
        join=True
    )

```

`node1`上运行：

```bash
python train_ddp.py --world_size 4 --node_rank 0 --master_addr 127.0.0.1 --master_port 2345
```

`node2`上运行：

```bash
python train_ddp.py --world_size 4 --node_rank 1 --master_addr 192.168.31.56 --master_port 2345
```

### 🔸通过 launch 启动

```python
import torch
import torch.nn as nn
from torch.utils.data import DataLoader
from torch.utils.data.distributed import DistributedSampler
import torch.optim as optim
from torch.nn.parallel import DistributedDataParallel as DDP
import torch.distributed as dist


import argparse
parser = argparse.ArgumentParser()
parser.add_argument('--per_gpu_batchsize', default=4, type=int)
parser.add_argument("--learning_rate", type=float, default=1e-4)
parser.add_argument('--max_epochs', default=10, type=int)
parser.add_argument("--local_rank", type=int, default=0)

args = parser.parse_args()

def train(local_rank):
    dist.init_process_group(
        backend='nccl',
        init_method='env://'
    )
    print(
        dist.get_world_size(),
        dist.get_rank(),
        local_rank,
    )
    device = torch.device(f"cuda:{local_rank}")
    _model = Model(vars(args)).to(device)
    model = DDP(_model, device_ids=[local_rank], output_device=local_rank, find_unused_parameters=True)
    if dist.get_rank() == 0:
        print(args)
        summary(model)

    train_dataset = Dataset()
    train_sampler = DistributedSampler(train_dataset, shuffle=True, drop_last=False)
    train_dataloader = DataLoader(train_dataset, batch_size=args.per_gpu_batchsize, sampler=train_sampler, num_workers=8)

    loss_fn = nn.CrossEntropyLoss(reduction='mean')
    grouped_parameters = [
        {"params": model.parameters(), "lr": 1e-6,}
    ]
    optimizer = optim.AdamW(grouped_parameters)
    for epoch in range(args.max_epochs):
        train_dataloader.sampler.set_epoch(epoch)
        for x, y in train_dataloader:
            optimizer.zero_grad()
            y = y.to(device)
            _y = model(x)
            loss = loss_fn(y, _y)
            loss.backward() 
            optimizer.step()

if __name__=='__main__':
    train(args.local_rank)

```

`node1`上运行：

```bash
python -m torch.distributed.launch --nproc_per_node=2 --nnodes=2 --node_rank=0 --master_addr="192.168.31.56" --master_port=12355 train.py
```

`node2`上运行：

```bash
python -m torch.distributed.launch --nproc_per_node=2 --nnodes=2 --node_rank=1 --master_addr="192.168.31.56" --master_port=12355 train.py
```

Warning: 此处 `args.local_rank` 参数必须为 `int` 类型。若使用 `torch.distributed.launch` 或者 `torch.distributed.run`（默认 `--use_env`）运行时，添加了 `--use_env` 参数，进程组启动工具将不再向训练代码传入 `local_rank` 参数，此时需要使用 `train(int(os.environ['LOCAL_RANK']))` 替换 `train(args.local_rank)`，并删除 `argparse` 中的 `--local_rank` 参数。

### 🔸python_lightning 启动

`python_lightning` 通过环境变量识别不同训练节点，而运行代码和参数是相同的

**代码和运行方式**

```python
trainer = Trainer(accelerator="gpu", devices=8, num_nodes=4, strategy="ddp")
```

`node1`上运行：

```bash
export MASTER_ADDR=127.0.0.1
export MASTER_PORT=2345
export WORLD_SIZE=4
export NODE_RANK=0
python run.py
```

`node2`上运行：

```bash
export MASTER_ADDR=192.168.31.56
export MASTER_PORT=2345
export WORLD_SIZE=4
export NODE_RANK=1
python run.py
```

### 🔸其他

两个节点的代码执行间隔不易过长，进程组会等待所有进程都准备好之后开始训练，训练期间要保证内网和InfiniBand设备连通。在运行之前建议设置环境变量 `export NCCL_DEBUG=INFO` 可以在训练开始时观察到不同进程的连接方式（P2P或者IB），以确定代码顺利运行。每个节点都要一份数据集，路径可以不同，训练的模型要完全一致。在每张卡 `batch_size`不变的情况下，总的 `batch_size`会增加一倍，总的迭代次数会变为原来的一半，训练时间会缩短近一半。

![1662433665580](image/index/1662433665580.png)

启动方式详情参见：[Distributed communication package - torch.distributed(launch-utility) — PyTorch 1.12 documentation](https://pytorch.org/docs/stable/distributed.html#launch-utility)

进程组初始化方式详情参见：[Distributed communication package - torch.distributed(initialization) — PyTorch 1.12 documentation](https://pytorch.org/docs/stable/distributed.html#initialization)

pytorch_lightning 有关集群训练详情参见：[Run on an on-prem cluster (intermediate) — PyTorch Lightning 1.7.4 documentation ](https://pytorch-lightning.readthedocs.io/en/stable/clouds/cluster_intermediate_1.html)

## 👻科学上网

有时需要在服务器上使用git clone，或者使用脚本下载github或者谷歌网盘上的内容，然而这些资源在国内的连通性并不稳定。此时可以通过设置服务器上的代理，将连接请求发送到稳定的代理服务器（`proxy.laocheng.tech`，端口为 `23333`），进行外网的访问。只需要设置用户的**环境变量**（`https_proxy`和 `http_proxy`）即可：

```bash
export https_proxy="http://proxy.laocheng.tech:23333"
export http_proxy="http://proxy.laocheng.tech:23333"
```

环境变量设置后立即生效，可通过 `curl -v www.google.com`验证：

```
Uses proxy env variable http_proxy == 'http://proxy1.laocheng.tech:8888'Trying 10.170.33.135:8888...Connected to proxy1.laocheng.tech (10.170.33.135) port 8888 (#0)GET http://www.google.com/ HTTP/1.1
Host: www.google.com
User-Agent: curl/7.71.1
Accept: /
Proxy-Connection: Keep-AliveMark bundle as not supporting multiuseHTTP 1.0, assume close after body
< HTTP/1.0 200 OK
< Via: 1.1 tinyproxy (tinyproxy/1.10.0)
< Set-Cookie: 1P_JAR=2021-09-03-12; expires=Sun, 03-Oct-2021 12:52:44 GMT; path=/; domain=.google.com; Secure
< Set-Cookie: NID=222=HplnHUTlqm77DaC_Dz6t4Aldmv9kKBfqwsXPJws7eYLj8zpznAba71io9qOM3Ryzhn6SW9yTBRN5k5ZWy9ztY8pZNFgx8e7Ef8Um0vgjBbvzIFy76nzDGXPshcqeQlPwXZelIh0nvuiOhdTjo-46rV3K_iGl6CpzFAnL5I4qxNQ; expires=Sat, 05-Mar-2022 12:52:44 GMT; path=/; domain=.google.com; HttpOnly
< Cache-Control: private, max-age=0
< X-XSS-Protection: 0
< Date: Fri, 03 Sep 2021 12:52:44 GMT
< Server: gws
< Accept-Ranges: none
< Content-Type: text/html; charset=ISO-8859-1
< Vary: Accept-Encoding
< P3P: CP="This is not a P3P policy! See g.co/p3phelp for more info."
< Expires: -1
< X-Frame-Options: SAMEORIGIN
<
```

用户环境变量只在当前登录的ssh会话中有效，退出后自动失效，也可以手动取消代理：

```bash
unset https_proxy
unset http_proxy
```

Warning: 搭建的代理服务器对允许连接的主机和允许访问的网站做了访问控制，**只能用于服务器访问特定内容**。

## 💤在校外使用服务器

### 🔸直接使用ssh连接

在之前的腾讯文档中，还列出了用于公网访问服务器的主机名和端口号，这是因为在学校中，我们可以通过IP地址互相访问（10.17x.x.x，A类局域网地址），但是这些都是属于学校的内网环境，一旦离开学校，便无法接入学校内网，学校的提供的VPN也只能访问学校的网站服务，不能用于连接服务器。这时想要直接访问学校服务器，就需要用到这些主机名和端口号。

Note: 在公网连接服务器时，峰值带宽为5Mbps（校园网内网带宽水平目前在50Mbps左右），在与服务器传输文件时速度会比较慢。在网络攻击的高峰期，可能会关闭这种方式的连接途径。

### 🔸通过向日葵连接

如果在学校（实验室或者宿舍）有电脑的话，还可以在自己的电脑上安装向日葵，通过远程控制连接到自己在学校的电脑，然后再在学校的电脑上连接服务器。

实验室电脑的BIOS里面一般都设置了来电自启动，以应对在假期时实验室意断电导致电脑关机的情况。另外417的电脑都是通过交换机直接连接在校园网下的，需要自己**创建拨号连接**或者通过**网页认证**上网，网页认证在电脑不睡眠的情况下，可以保持较长时间的连接。而Win10的校园网拨号连接比较不稳定，建议配置开机自动拨号和掉线自动重播，方法比较多，可参考[win10宽带断线如何自动重拨？](https://www.zhihu.com/question/59449991)。**注意网页认证和拨号连接不能同时使用**。如果有路由器的话，就不用考虑这些了。

## 👁️‍🗨️远程桌面连接

如果实验室的电脑或者自己的电脑使用的是**win10企业版**或者**专业版**的话，在校园网内可以直接使用win10自带的**远程桌面连接**功能，以较高的质量进行win10电脑之间的远程。

首先需要在受控电脑上打开**远程桌面**连接功能，允许被远程控制。

![](image/base/1630661766396.png)

在另一台电脑上打开“远程桌面连接”，在“计算机”中填入目标主机的IP地址，自己电脑的IP可以在网页查询：[流量查询](https://zfw.xidian.edu.cn/)。

![](image/base/1630665044482.png)

点击**更多选项--使用其他账户**，输入远程主机的用户名和密码，连接即可。

![](image/base/1630665183748.png)

Note: 如果目标主机使用的微软账户，用户名要输入账户的邮箱。注意密码不是电脑的PIN。如果目标主机连接在路由器下，需要将Windows远程桌面连接的3389端口转发到路由器的某一端口（在路由器端口转发中设置），并在连接时的“计算机”中加入端口号信息，如 `ip:port`。手机上也有微软提供的远程桌面连接工具，名字为RDP Client。

> 🛠️**修改日志**
>
> * 2021年9月24日，增加了Anaconda的安装教程；🍔
> * 2021年9月29日，纠正了部分拼写错误，添加和纠正部分描述；🍟
> * 2021年10月10日，添加部分描述；🌭
> * 2021年10月24日，添加VScode使用SFTP扩展的教程；🍿
> * 2021年11月6日，更新服务器资源，更改部分说明；🍞
> * 2021年12月20日，添加部分说明；🥯
> * 2022年3月10日，添加部分说明；🎂
> * 2022年3月24日，修改VSCode选择环境的说明；🍩
> * 2022年3月29日，修改密钥生成的部分说明；🍥
> * 2022年3月30日，添加screen内使用鼠标滚轮的部分说明；🍗
> * 2022年4月6日，添加pytorch的快速下载安装说明；🍭
> * 2022年4月7日，修改pip源的配置说明；🍨
> * 2022年4月18日，修改tensorboard远程查看方法，更新pytorch文件列表，修改部分错别字；🍫
> * 2022年5月5日，（[GPU使用情况](http://www.yrqzxh.com/server/)）为统计图的每个数据点提示框添加用户信息；🍵
> * 2022年儿童节，修改在screen内滚动终端缓存区的说明；🍭
> * 2022年6月7日，提供因 multi-user anaconda 带来的权限问题的解决方案；🍨
> * 2022年6月8日，新增服务器资源，修改部分说明；🐼
> * 2022年6月30日，修改 [GPU使用情况](http://www.yrqzxh.com/server/) 网页，列表形式，简介明了；🍧
> * 2022年9月5日，添加多机多卡分布式训练的使用说明文档；🥠
> * 2022年9月24日，修改部分说明；🥟

[🔝回到顶部](#📐常用煲粥工具)

![IP签名](https://tool.lu/netcard/)
