SA-DETR 
主要文件路径：G1/home/chr/RTdetr/RTDETR-20240307/

实验环境: 环境名称：rtdetr
    python: 3.8.16
    torch: 1.13.1+cu117
    torchvision: 0.14.1+cu117
    timm: 0.9.8
    mmcv: 2.1.0
    mmengine: 0.9.0

# 环境配置

    1. 执行pip uninstall ultralytics把安装在环境里面的ultralytics库卸载干净.<这里需要注意,如果你也在使用yolov8,最好使用anaconda创建一个虚拟环境供本代码使用,避免环境冲突导致一些奇怪的问题>
    2. 卸载完成后同样再执行一次,如果出现WARNING: Skipping ultralytics as it is not installed.证明已经卸载干净.
    3. 如果需要使用官方的CLI运行方式,需要把ultralytics库安装一下,执行命令:<python setup.py develop>,当然安装后对本代码进行修改依然有效. 注意:不需要使用官方的CLI运行方式,可以选择跳过这步
    4. 额外需要的包安装命令:
        pip install timm==0.9.8 thop efficientnet_pytorch==0.7.1 einops grad-cam==1.4.8 dill==0.3.6 albumentations==1.3.1 pytorch_wavelets==1.3.0 tidecv -i https://pypi.tuna.tsinghua.edu.cn/simple
        以下主要是使用dyhead必定需要安装的包,如果安装不成功dyhead没办法正常使用!
        pip install -U openmim
        mim install mmengine
        mim install "mmcv>=2.0.0"
    5. 运行时候如果还缺什么包就请自行安装即可.



    需要编译才能运行的一些模块:
        1. mamba
        2. dcnv3
        3. dcnv4

# 文件说明
ultralytics/cfg/models/chr_models   模型配置文件

images
    images/heat_result          各个算法的热力图结果
    images/ResultImages         各个算法的检测结果
    images/TestImage            测试图片

result                          裁切结果
runs                            训练结果和权重
dataset/visdrone.yaml           数据集配置文件



# 代码说明
1. train.py
    训练模型的脚本
2.main_profile3.py
    输出模型和模型每一层的参数,和详细信息
3. val.py
    使用训练好的模型计算指标的脚本
4. detect.py
    推理的脚本
5. cropimage.py
    在结果图片中裁切出相同大小的区域
6. heatmap.py
    生成热力图的脚本
7. get_FPS.py
    计算模型储存大小、模型推理时间、FPS的脚本
8. get_COCO_metrice.py
    计算COCO指标的脚本
9. plot_result.py
    绘制曲线对比图的脚本
10. showresult.py
    输出带目标框和不带目标框的结果
11. plotbox.py
    绘制指定图片的标注框
12. dataset/yolo2coco.py
    将VisDone数据集的标签格式转换为YOLO格式
13. analyphoto.py
    统计数据集中图片尺寸的分布情况

# 实验结果位置
    F3/home/chr/ultralytics/runs/    YOLO类算法结果


# 怎么像yolov5那样输出每一层的参数,计算量？
参考main_profile.py,选择自己的配置文件路径即可


# 如何绘制曲线对比图?
在plot_result.py中的names指定runs/train中的训练结果名字name即可.  
比如目前runs/train中有exp,exp1,exp2这三个文件夹,plot_result.py中names中的值为:['exp', 'exp1', 'exp2'],运行后会自动保存为metrice_curve.png和loss_curve.png在当前运行的目录下.

# 如何计算COCO指标?
可以看项目视频-计算COCO指标教程.  
python dataset/yolo2coco.py --image_path dataset/images/test --label_path dataset/labels/test  
python get_COCO_metrice.py --pred_json runs/val/exp/predictions.json --anno_json data.json  
新旧版的差异就在于 predictions.json的生成方式,新版就是在val.py中把save_json设置为True即可

# 常见错误和解决方案(如果是跑自带的一些配置文件报错可以先看看第十大点对应的配置文件是否有提示需要修改内容)
1. RuntimeError: xxxxxxxxxxx does not have a deterministic implementation, but you set 'torch.use_deterministic_algorithms(True)'.....

    解决方案：在ultralytics/utils/torch_utils.py中init_seeds函数中把torch.use_deterministic_algorithms里面的True改为False

2. ModuleNotFoundError：No module named xxx

    解决方案：缺少对应的包，先把YOLOV8环境配置的安装命令进行安装一下，如果还是缺少显示缺少包，安装对应的包即可(xxx就是对应的包).

3. OMP: Error #15: Initializing libiomp5md.dll, but found libiomp5md.dll already initialized.  

    解决方案：https://zhuanlan.zhihu.com/p/599835290

<a id="a"></a>

4. 多卡训练问题.[参考链接](https://docs.ultralytics.com/yolov5/tutorials/multi_gpu_training/#multi-gpu-dataparallel-mode-not-recommended:~:text=just%201%20GPU.-,Multi%2DGPU%20DistributedDataParallel%20Mode%20(%E2%9C%85%20recommended),-You%20will%20have)

    python -m torch.distributed.run --nproc_per_node 2 train.py

5. 指定显卡训练.

    1. 使用device参数进行指定.  
    2. 参考链接:https://blog.csdn.net/m0_55097528/article/details/130323125, 简单来说就是用这个来代替device参数.  

6. ValueError: Expected more than 1 value per channel when training, got input size torch.Size...

    如果是在训练情况下的验证阶段出现的话,大概率就是最后一个验证的batch为1,这种情况只需要把验证集多一张或者少一张即可,或者变更batch参数.

7. AttributeError: Can't pickle local object 'EMASlideLoss.__init__.<locals>.<lambda>'

    可以在ultralytics/utils/loss.py中添加import dill as pickle,然后装一下dill这个包.  
    pip install dill -i https://pypi.tuna.tsinghua.edu.cn/simple

8. RuntimeError: Dataset 'xxxxx' error ❌

    将data.yaml中的路径都改为绝对路径.

# 常见疑问
1. After Fuse指的是什么？

    Fuse是指模型的一些模块进行融合,最常见的就是conv和bn层进行融合,在训练的时候模型是存在conv和bn的,但在推理的过程中,模型在初始化的时候会进行模型fuse,把其中的conv和bn进行融合,通过一些数学转换把bn层融合到conv里面,还有一些例如DBB,RepVGG等等模块支持融合的,这些在fuse阶段都会进行融合,融合后可以一般都可以得到比融合前更快的推理速度,而且基本不影响精度.

2. FPS如何计算？

    在运行val.py后最后会出来Speed: 0.1ms preprocess, 5.4ms inference, 0.0ms loss, 0.4ms postprocess per image这行输出,这行输出就代表了每张图的前处理,推理,loss,后处理的时间,当然在val.py过程中是不需要计算loss的,所以为0,FPS最严谨来说就是1000(1s)/(preprocess+inference+postprocess),没那么严谨的话就是只除以inference的时间,还有一个问题就是batchsize应该设置为多少,其实这行输出就已经是每张图的时间了,但是batchsize还是会对这个时间有所影响,主要是关于并行处理的问题,GPU中可以一次处理多个batch的数据,也可以只处理一个数据,但是处理多batch的数据比处理一个数据的时候整体速度要快,举个例子,比如我有1000张图,我分别设置batchsize为32和batchsize为1,整体运行的时间百分之99都是batchsize为32的快,因此这就导致不同batch输出的时间不同,至于该设置多少来计算FPS,貌似众说纷纭,所以这里我也不好给意见.  
    附上yolov5作者对于FPS和Batch的一个实验链接: https://github.com/ultralytics/yolov5/discussions/6649

3. 训练的时候出现两次结构打印是什么情况?

    第一次打印的和第二次打印的主要不同地方就是类别数,第一次打印的是yaml配置文件中的nc参数的类别数的结构,第二次打印的是你实际数据集类别数的结构,其差异就在类别数,实际使用的是第二次打印的结构.
