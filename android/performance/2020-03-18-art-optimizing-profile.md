# 使用云端 ART 优化配置文件来改进应用性能

Android 9 中的 [ART optimizing profiles in Play Cloud](https://youtu.be/Yi9-BqUxsno?list=PLWz5rJ2EKKc9Gq6FEnSXClhYkWAStbwlC&t=985) 改善了应用的启动性能。平均提升 15%，最高的甚至提升 30%。

[Android 7.0 Nougat](https://www.youtube.com/watch?v=fwMM6g7wpQ8) 中的 [Profile Guided Optimization](https://source.android.com/devices/tech/dalvik/jit-compiler) 最早提供该功能：

> allows the Android Runtime to help improve an app's performance by building a profile of the app's most important hot code and focusing its optimization effort on it

即，ART 运行时重点对应用中最重要的热点代码优化，以提升应的性能。

这个优化方式的问题是耗时过久，用户要几天才能看到效果。耗时久的原因是设备在空闲时才进行代码分析，探测热点。

![](/images/15844984670795.jpg)

ART optimizing profiles in Play Cloud 则将这个优化放到云端进行。

+ 大量用户及设备的应用有非常相同的代码使用路径(used code paths (hot code)，即"热点代码")。可以通过收集数据找到热点代码
+ 对于第一次发布的应用，可以从 alpha/beta 测试中收集数据。(即使收集不到，猜也能猜测出来吧)
+ code profile 作为种子让安装应用时的优化更有效

问题：

+ 如何构建 code profile
+ dex metadata files，跟 code profile 对应

# 参考

[Android Developers Blog: Improving app performance with ART optimizing profiles in the cloud](https://android-developers.googleblog.com/2019/04/improving-app-performance-with-art.html)