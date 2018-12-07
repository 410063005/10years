对 app 做内存分析时发现在华为 nova 3e 手机上首页会出现内存泄漏，但找不到原因。

LeakCanary 和 Android Studio Profiler 无能测出这里的内存泄漏，但给出的引用链不太容易看明白。于是写了一个只有一个 Activity 的简单 demo 进行测试。

这一回结果很明显了。

这是 LeakCanary 的测试结果：

![](004-huawei-memory-leak/1.png)

这是 Android Studio Profiler 的测试结果

![](004-huawei-memory-leak/2.png)

上图表明某个静态字段最终引用到了 `HwPhoneWindow` 实例

![](004-huawei-memory-leak/3.png)

上图表明 `HwPhoneWindow.mContext` 引用了 `TestPrefActivity` 实例

于是 `TestPrefActivity` 最终发生泄漏。

总结一下：这个案例符合被静态字段引用导致内存泄漏的情形。但找到引用关系还是比较麻烦的，尤其是在较大的 app 中且又是系统本身的问题。这时建议不妨使用简单 demo 进行验证。

[demo 源码](https://gist.github.com/410063005/4501131fa2abcc26cce1ef81b2026176)
