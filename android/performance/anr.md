# 分析方法

+ [Android ANR问题（一）-基本分析方法 - 简书](https://www.jianshu.com/p/082045769443)
+ [官方文档](https://developer.android.com/training/articles/perf-anr.html)


ANR (Application Not Responding)，即主线程在超时时间内无响应。当 Android 检测到以下某一项条件时，便会针对特定应用显示 ANR 对话框：

+ 在 5 秒内对输入事件（例如按键或屏幕轻触事件）没有响应。
+ BroadcastReceiver 在 10 秒后尚未执行完毕。

几个关键：

+ 现象
+ 时机
+ 日志 - 发生 ANR 时将 tracing 打印到 `/data/anr/traces.txt`
+ 原因

# 实例

![-w1179](/images/15790557980407.jpg)

```
BroadcastQueue: Timeout of broadcast BroadcastRecord{43b5ef98 u-1 android.intent.action.TIME_TICK} - receiver=android.os.BinderProxy@435f4d48, started 10000ms ago 
am_anr : [0,10015,com.xxx.yyy,12074564,Broadcast of Intent { act=android.intent.action.TIME_TICK flg=0x50000014 (has extras) }] 
```

从日志来看，这是个 `BroadcastTimeout` 类型的 ANR。

<!--
# 结论

拿到 trace 日志后并无有价值的信息，不过结合测试截图，发现出现 ANR 的时间点我们的应用有"分享到微信"的操作，这个拉起过程(微信冷启动)相当长，超过10秒。另外，搜索我们的源码，找不到 `android.intent.action.TIME_TICK` 相关内容，可能是某个第三方SDK使用了 `TIME_TICK`。
-->