[TOC]

# Kotlin

[Keddit — Part 2: Kotlin Syntax, Null Safety and more in Android](https://android.jlelse.eu/learn-kotlin-while-developing-an-android-app-part-2-e53317ffcbe9)

---


文档 leakcanary, timber, retrofit, javalin, okio
timber kotlin 学习
Kotlin Coroutine 原理解析 - 编走编想 - 简书   对比 https://book.flutterchina.club/chapter1/dart.html
https://www.jianshu.com/p/06703abc56b1 
Kotlin Coroutines 执行异步加载 - 小菜鸟程序媛 - 简书
https://www.jianshu.com/p/6cbc97258952 
深入理解 Kotlin Coroutine (一) - JackChan - CSDN博客
https://blog.csdn.net/axi295309066/article/details/78066500 
Executor.asCoroutineDispatcher 的实现
Executors.kt
Dispatchers.Main 在 android 平台上的实现
见 HandlerDispatcher.kt
kotlin 学习
https://mp.weixin.qq.com/s?__biz=MzAwODY4OTk2Mg==&mid=2652045796&idx=1&sn=5c80872cd652a0ffd35e4891ea0984d9&chksm=808ca1a1b7fb28b77e1363d34461f91f5386235b75417fb5dc9fe25d8e425c63dab45182ec41&scene=21#wechat_redirect 
# Jetpack

[Android-Jetpack-Chinese-Translation/android-jetpack-chinese-translation: Android Jetpack 官方文档 中文翻译](https://github.com/Android-Jetpack-Chinese-Translation/android-jetpack-chinese-translation)

---

https://medium.com/q42-engineering/android-jetpack-compose-895b7fd04bf4
数据绑定与vue
为什么要使用 workmanager
https://stackoverflow.com/questions/50279364/android-workmanager-vs-jobscheduler  https://github.com/hypertrack/smart-scheduler-android
pagin 组件   http://km.oa.com/articles/show/392344?kmref=guess_post   android x 了解一下   https://juejin.im/entry/5b67a5746fb9a04f93301b97  https://developer.android.com/jetpack/androidx/  Data binding 之双向绑定的demo    
androidx & jetpack

# 架构

## App开发架构指南

[App开发架构指南（谷歌官方文档译文） - 泡在网上的日子](http://www.jcodecraeer.com/a/anzhuokaifa/androidkaifa/2017/0523/7963.html)

+ 基本准则
+ 架构图
+ 网络状态

![](media/15746517919054.jpg)


![](media/15746517466342.jpg)


![-w971](media/15746516982229.jpg)

# What's new

[What's New in Android (Google I/O'19)](https://youtu.be/td3Kd7fOROw)

+ [Android 5.0](https://developer.android.com/about/versions/lollipop)
    + Material Design
    + Z 轴、evalation、水波纹动画
    + ART 虚拟机
    + 新的 Camera API
    + 基于 Chrome 的 WebView
    + Job Scheduling API
    + dumpsys batterystat 电池使用统计
+ [Android 6.0](https://developer.android.com/about/versions/marshmallow/android-6.0-changes)
    + 运行时权限
    + 低电耗模式
    + 移除了 Apache HTTP 客户端
+ [Android 7.0](https://developer.android.com/about/versions/nougat/android-7.0)
    + 多窗口支持
    + **JIT/AOT 编译**
    + 删除三个常用隐式广播
        + CONNECTIVITY_ACTION
        + ACTION_NEW_PICTURE
        + ACTION_NEW_VIDEO
    + Vulkan API
    + APK signature scheme v2
+ [Android 8.0](https://developer.android.com/about/versions/oreo/android-8.0)
    + 自动填充框架
    + 可下载字体
    + 自动调整 TextView 的大小
    + 新的 StrictMode 检测程序
    + findViewById() 签名变更
    + [后台执行限制](https://developer.android.com/about/versions/oreo/background)。使用 [JobScheduler](https://developer.android.com/reference/android/app/job/JobScheduler) 替代后台 Service
+ [Android 9.0](https://developer.android.com/about/versions/pie/android-9.0)
    + 显示屏缺口支持
    + ImageDecoder
    + Neural Networks API 1.1
    + APK signature scheme v3
    + 文本优化 [PrecomputedText](https://developer.android.com/reference/android/text/PrecomputedText)
+ [Android 10.0](https://developer.android.com/about/versions/10/highlights)
    + 可折叠设备
    + 5G 网络
    + 深色主题
    + 加强对非 SDK 接口的限制
+ [Android 11](https://developer.android.com/preview/features)
    + [使用原始文件路径访问媒体文件](https://mp.weixin.qq.com/s?__biz=MzAwODY4OTk2Mg==&mid=2652052737&idx=1&sn=bcdbd8f73f95aab64b0c842b0436ef0a&chksm=808cbd44b7fb345227416ad5ec9c9ee3ddd37b26029ecb5599a3b09a681212d5f4bf9a5528df&mpshare=1&scene=1&srcid=&sharer_sharetime=1584500940185&sharer_shareid=b5535657e3516bd6d7252ce5f5ed09f4&rd2werd=1#wechat_redirect)
    + NDK 图像解码器
    + 资源加载器