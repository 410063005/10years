
# systrace
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
+ 

一个深入的问题：

+ systrace 的工作原理？
+ `Trace` 类的工作原理

[systrace  |  Android Developers](https://developer.android.com/studio/command-line/systrace)

[使用Systrace分析UI性能 - 简书](https://www.jianshu.com/p/b492140a555f)

# CPU Profiler


[CPU profiler](https://developer.android.com/studio/profile/cpu-profiler.html)

# Trace

[generate trace logs](https://developer.android.com/studio/profile/generate-trace-logs.html)