## Android录音应用
一个简洁高效的Android录音工具，支持音频录制、播放、管理和可视化功能。学习使用。
### 功能特点
高质量录音：支持多种音频格式录制
音频可视化：实时显示录音波形图
录音管理：查看、播放、删除录音文件
简洁界面：直观易用的用户界面
录音计时：精确显示录音时长


### 项目结构
```
app/
├── src/
│   ├── main/
│   │   ├── java/com/explore/androidaudioplayer/
│   │   │   ├── adapter/        # 适配器类
│   │   │   ├── audio/          # 音频录制和播放相关类
│   │   │   ├── model/          # 数据模型
│   │   │   ├── utils/          # 工具类
│   │   │   ├── views/          # 自定义视图
│   │   │   └── MainActivity.kt # 主活动
│   │   ├── res/                # 资源文件
│   │   │   ├── layout/         # 布局文件
│   │   │   ├── drawable/       # 图像资源
│   │   │   ├── values/         # 字符串、颜色等资源
│   │   │   └── menu/           # 菜单资源
│   │   └── AndroidManifest.xml # 应用配置文件
│   └── test/                   # 测试代码
└── build.gradle                # 项目构建配置
```

