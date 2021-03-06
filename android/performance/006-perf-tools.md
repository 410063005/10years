- [systrace](#systrace)
  + [简介](#简介)
  + [基本用法](#基本用法)
  + [demo](#demo)
  + [高级话题](#高级话题)
  + [参考](#参考)


# MAT
## 常见错误
Mac 上启动 MAT 可能报错。一个[解决方法](https://blog.csdn.net/qq1263292336/article/details/78174219)如下，进入其所在的目录，指定 `-data` 参数运行：

```
> ./mat.app/Contents/MacOS
> ./MemoryAnalyzer -data dump
```

MAT 无法直接打开从 Android 设备上导出的 hprof 文件：

```
Error opening heap dump 'memory-20190120T134854.hprof'. Check the error log for further details.
Error opening heap dump 'memory-20190120T134854.hprof'. Check the error log for further details.
Unknown HPROF Version (JAVA PROFILE 1.0.3) (java.io.IOException)
Unknown HPROF Version (JAVA PROFILE 1.0.3)
```

使用 `Android/sdk/platform-tools/hprof-conv` 进行转换

```
hprof-conv original.hprof 
```

常用术语

+ GC Root
+ 可达性
+ 引用类型，Reference queque
+ Shallow Heap，Retained Heap
+ Dominator Tree
+ 正则式查找
+ OQL
+ 内存快照对比



# systrace
## 简介
systrace的功能包括跟踪系统的I/O操作、内核工作队列、CPU负载以及Android各个子系统的运行状况等。在Android平台中，它主要由3部分组成：

+ 内核部分：Systrace利用了Linux Kernel中的ftrace功能。所以，如果要使用systrace的话，必须开启kernel中和ftrace相关的模块。
+ 数据采集部分：Android定义了一个Trace类。应用程序可利用该类把统计信息输出给ftrace。同时，Android还有一个atrace程序，它可以从ftrace中读取统计信息然后交给数据分析工具来处理。
+ 数据分析工具：Android提供一个systrace.py（python脚本文件，位于Android SDK目录 `/sdk/platform-tools/systrace` 中，其内部将调用atrace程序）用来配置数据采集的方式（如采集数据的标签、输出文件名等）和收集ftrace统计数据并生成一个结果网页文件供用户查看。

[理解和使用systrace | 风中老狼的博客](https://maoao530.github.io/2017/02/06/systrace/)

## 基本用法

> The systrace command allows you to collect and inspect timing information across all processes running on your device at the system level.
> systrace combines data from the Android kernel, such as the CPU scheduler, disk activity, and app threads, to generate an HTML report
> The report highlights frames that systrace believes may not have been rendered properly.
> systrace doesn't collect information about code execution within your app process. 
> Think of the Alerts panel as a list of bugs to be fixed. Often, a tiny change or improvement in one area can eliminate an entire class of alerts from your app.
> One approach is to add trace markers (see Instrument your app code) to methods that you think are causing these bottlenecks to see those function calls appear in systrace.

注意：

+ 使用 Android Studio CPU Profiler 对方法进行更细粒度的性能分析，systrace 无法进行这种性能分析
+ On devices running Android 9 (API level 28) or higher, you can use a system app called System Tracing to [Record system traces on device](https://developer.android.com/studio/profile/systrace-on-device)
+ 可以多次调用 `Trace.endSection()`，但只有第一次调用才生效
+ 必须在同一个线程中调用 `Trace.endSection()`
+ 在 `finally` 语句块中调用 `Trace.endSection()` 是一个好的实践

位置：`<sdk>/platform-tools/systrace`

用法：`python systrace.py -e  8GP7N18531003308 -a com.tencent.igame  -t 5 gfx input view sched freq app`

常用命令选项：

```
Options:

  -e DEVICE_SERIAL_NUMBER, --serial=DEVICE_SERIAL_NUMBER
  -t N, --time=N        trace for N seconds
  -l, --list-categories
                        list the available categories and exit
  Atrace options:
    -a APP_NAME, --app=APP_NAME
                        enable application-level tracing for comma-separated
                        list of app cmdlines              
```

关于 `-a` 选项的说明如下：

> Enable tracing for apps, specified as a comma-separated list of process names. The apps must contain tracing instrumentation calls from the Trace class. You should specify this option whenever you profile your app—many libraries, such as RecyclerView, include tracing instrumentation calls that provide useful information when you enable app-level tracing. 

常用 categorie：

```
         gfx - Graphics
       input - Input
        view - View System
     webview - WebView
          wm - Window Manager
          am - Activity Manager
          sm - Sync Manager
       audio - Audio
       video - Video
      camera - Camera
         hal - Hardware Modules
         app - Application
         res - Resource Loading
      dalvik - Dalvik VM
          rs - RenderScript
      bionic - Bionic C Library
       power - Power Management
          pm - Package Manager
          ss - System Server
    database - Database
     network - Network
         adb - ADB
       sched - CPU Scheduling
        freq - CPU Frequency
        idle - CPU Idle
        load - CPU Load
  memreclaim - Kernel Memory Reclaim
  binder_driver - Binder Kernel driver
  binder_lock - Binder global lock trace
```

几个问题用于检查是否掌握 systrace：

+ 查看帮助，如何导航
+ 如何分析已有数据
+ 检查 frames 和 alerts
+ 如何添加向 systrace report 添加 flag

## demo
[手把手教你使用Systrace（一） - 知乎](https://zhuanlan.zhihu.com/p/27331842)

[面试Tip之Android优化工具Systrace - 掘金](https://juejin.im/post/5b3cce09e51d45198651069f)

无法代码控制开始和结束怎么办?

> 我们可以用自定义Trace Label解决；Android SDK中提供了`android.os.Trace#beginSection`和`android.os.Trace#endSection` 这两个接口；我们可以在代码中插入这些代码来分析某个特定的过程；比如我们觉得Fragment的onCreateView过程有问题，那就在`onCreateView` 中加上代码：

> 比如你要分析App的冷启动过程，那就在Application类的attachBaseContext调用`Trace.beginSection("Boot Procedure")`，然后在App首页的`onWindowFocusChanged` 或者你认为别的合适的启动结束点调用`Trace.endSection`就可以到启动过程的信息；比如下图是我的Label：

## 高级话题

[systrace](https://source.android.google.cn/devices/tech/debug/systrace)

[ftrace](https:/e.android.google.cn/devices/tech/debug/ftrace)

一个深入的问题：

+ systrace 的工作原理？
+ `Trace` 类的工作原理

见 [理解和使用systrace | 风中老狼的博客](https://maoao530.github.io/2017/02/06/systrace/)

如何在 release 版本中开启 systrace 功能？

Application的`attachBaseContext` 中添加如下代码：

```
Class<?> trace = Class.forName("android.os.Trace");
Method setAppTracingAllowed = trace.getDeclaredMethod("setAppTracingAllowed", boolean.class);
setAppTracingAllowed.invoke(null, true);
```

## 参考

+ [systrace  |  Android Developers](https://developer.android.com/studio/command-line/systrace)
+ [使用Systrace分析UI性能 - 简书](https://www.jianshu.com/p/b492140a555f)
+ [理解和使用systrace | 风中老狼的博客](https://maoao530.github.io/2017/02/06/systrace/)
+ [性能工具Systrace - Gityuan博客 | 袁辉辉博客](http://gityuan.com/2016/01/17/systrace/)

# Traceview 和 CPU Profiler

> Traceview is deprecated. If you're using Android Studio 3.2 or later, you should instead use CPU Profiler to inspect .trace files captured by instrumenting your app with the Debug class, record new method traces, save .trace files, and inspect real-time CPU usage of your app's processes.

Traceview 是用于图形化显示 `Debug` 类生成的 tracing 日志( `.trace` 文件 )的工具。`Debug` 生成的 `.trace` 日志非常精确，因为可以在代码中指定什么时候进行 trace。不过Android Studio 3.2 之后 [Traceview](https://developer.android.com/studio/profile/traceview) 已被 CPU Profiler 取代。

```java
Debug.startMethodTracing()
...
Debug.stopMethodTracing()
```

默认日志位置：`/sdcard/Android/data/<package_name>/files/dmtrace.trace`

打开日志：Profiler > Session > Load from file


[CPU profiler](https://developer.android.com/studio/profile/cpu-profiler.html)

[generate trace logs](https://developer.android.com/studio/profile/generate-trace-logs.html)


# dm
tracedump
[dmtracedump](https://developer.android.com/studio/command-line/dmtracedump) 以树状图的形式显示方法调用过程。

注意：dmtracedump 依赖 graphviz。Mac上可以通过 `brew` 安装： `brew install graphviz`

# 查看内存限制

查看 dalvik 的 heap size 限制：

`adb shell getprop | grep dalvik.vm.heapgrowthlimit`

查看 ram 的使用情况：

`adb shell cat /proc/meminfo`

查看 app 的内存使用：

`adb shell dumpsys meminfo + packagename/pid`

查看进程内存信息：

`adb shell procrank`

参考自 [Android进程的内存管理分析 - yulongfei的专栏 - CSDN博客](https://blog.csdn.net/gemmem/article/details/8920039)
