
https://android.googlesource.com/platform/frameworks/base/+/master/core/java/android/app/ActivityThread.java#6575    ActivityThread.attach() 调用  BinderInternal.addGcWatcher 监听是否到达内存上限  ( 到达 heap limit 75% 就开始清理 activity )

https://android.googlesource.com/platform/frameworks/base/+/4f868ed/services/core/java/com/android/server/am/ActivityManagerService.java#4676    ActivityManagerService.releaseSomeActivities()   清理  activity 以释放内存   (调用 ActivityStackSupervisor 的同名方法)

https://android.googlesource.com/platform/frameworks/base/+/4f868ed/services/core/java/com/android/server/am/ActivityStackSupervisor.java#2891  ActivityStackSupervisor.releaseSomeActivitiesLocked() 清理 activity 以释放内存  (继续调用  ActivityStack 的同名方法)

# 参考
[Android 内存回收机制：回收Activity，还是杀掉Process? - 简书](https://www.jianshu.com/p/387d6225def9)

[android系统Context初始化过程 - 简书](https://www.jianshu.com/p/7d4b605f5060)

[第6章 深入理解ActivityManagerService - 深入理解 Android 卷II - 极客学院Wiki](http://wiki.jikexueyuan.com/project/deep-android-v2/activity.html)

[Framework源码分析（一）：ActivityManagerService - 简书](https://www.jianshu.com/p/194a37755fea)
