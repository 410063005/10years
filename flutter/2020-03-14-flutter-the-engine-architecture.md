# Flutter 引擎

简单来说，Flutter 引擎包括两部分：

+ [a Dart framework](https://github.com/flutter/flutter)
+ [engine](https://github.com/flutter/engine)

简单来说，Flutter 引擎实现了以下功能：

+ Flutter's core libraries
    + animation and graphics
    + file and network I/O
    + accessibility support
    + plugin architecture
+ Dart runtime
+ toolchain
    + developing
    + compiling
    + running

简单来说，Flutter 引擎架构如下：

+ Framework 框架
+ Engine 引擎
    + Skia - 2D graphics rendering library
    + Dart - VM for GC OO 编程语言
    + <font color="red">不创建或管理线程</font>
+ shell/embedder
    + 不同平台有不同的 shell，实现诸如跟 IMEs 或应用程序生命周期回调等功能
    + <font color="red">创建和管理线程</font>

# Task Runner Configuration

The main task runners are:

+ Platform Task Runner - 从 embedder 角度，这个是主线程
    + 类似 [Android Main Thread](https://developer.android.com/guide/components/processes-and-threads.html)
    + 不应有耗时操作，不应被阻塞
+ UI Task Runner - 这个线程负责执行 Dart 代码，渲染 UI
    + 不应有耗时操作，否则引起 UI 卡顿
    + 耗时操作通过 [compute](https://docs.flutter.io/flutter/foundation/compute.html) 分享到其他 [Dart isolate](https://docs.flutter.io/flutter/dart-isolate/dart-isolate-library.html)
+ GPU Task Runner - 这个线程负责执行需要访问 GPU 的任务
    + layer tree 是这个 Runner 的输入，GPU 是它的输出
    + 这个 Runner 太忙会影响 UI 线程的帧调度，导致 UI 卡顿
    + 用户代码不能访问这个线程
    + 建议每个引擎有一个专门的线程用于这个 Runner
+ IO Task Runner - 这个线程的主要功能是从 assets 中读取图片，准备用于 GPU 渲染
    + 用户代码(无论 Dart 代码还是 Native 代码)不能访问这个线程
    + 建议每个引擎有一个专门的线程用于这个 Runner

当前各平台的线程 config 如下：

+ Android - UI, GPU and IO task runner 在每个引擎实例中有一个专用的线程，所有引擎实例共享同一个 Platform Task Runner 主线程

在 Android Studio Profiler 中确实可以看到上述几个线程

![-w525](/images/15841744097254.jpg)

# 参考

https://github.com/flutter/flutter/wiki/The-Engine-architecture