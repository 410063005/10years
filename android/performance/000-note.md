性能指标

+ 架构
  + Android JetPack, LiveData, MVP, MVVC
  + Clean Arch
  + Retrofit
+ 内存 平均值，峰值，后台静置 
  + onTrimMemory()回调 
  + GC情况与内存抖动 - 对象池 
  + 内存泄漏与OOM - LeakCanary
+ CPU 平均值，峰值，静置 
  + ANR
+ 流量 
  + 接口 数据包大小 请求耗时 请求成功率
  + 图片 图片大小，缓存，格式
+ 启动时间 
  + 多进程问题
  + app启动时间 度量与优化技巧
  + activity启动时间
+ 帧率和流畅度
  + 过度绘制 注意 background属性
  + 自定义View的性能 如何度量View性能 ClipRect
  + 减少布局层级以及View数量 Hierachy Viewer
  + ConstraintLayout
+ 线程
  + 线程名
  + 线程的优先级 (尤其是 worker 线程，比如 HandlerThread)
+ 包大小 
  + 优化技巧
+ 构建及发布
  + 加快构建速度
  + 发布渠道包
  + 好的构建环境
+ crash
  + 热修复
  + RDM
  + lint, codecc 
+ 工具
  + Android Studio
  + GT
  + 过度绘制
  + 内存分析
  + LeakCanary
  + Systrace
  + StrictMode 
+ 新技术 
  + java 8
  + RxJava
  + Kotlin 的协程, 
  + weex
  + Flutter
  + Android JetPac

每个指标的分析方法，工具，安例

---

LRUCache 的正确用法(避免撑爆内存)

```java
int avaoilableBytes = am.getMemoryClass() * 1024 * 1024;
LruCache cache = new LruCache<String, Bitmap>(availBytes / 8);
```

注意 `availBytes / 8`只是预估值，实际中可能要调整

也可以继承 LRUCache

```java
public class ThumbnailCache extends LruCache<String, Bitmap> {
	protected sizeOf(String key, Bitmap value) {
		return ...
	}
}
```

[ref](https://www.youtube.com/watch?v=R5ON3iwx78M&list=PL8ktV16dN_6vKDQB-D7fAqA6zRFQOoKtI)
[ref2](https://www.youtube.com/watch?v=R5ON3iwx78M&index=44&list=PLWz5rJ2EKKc9CBxr3BVjPTPoDPLdPIFCE)

[关于 Cache 的理论知识](https://www.youtube.com/watch?v=JkwrNmCwFfA&list=PLWz5rJ2EKKc9CBxr3BVjPTPoDPLdPIFCE&index=19)
---

lint的用法

```
App Source Files --\
		    \
		     |	  
lint.xml ------------lint tool------lint Output(by issue category)
```

注意其中包括 Performance 问题

Android Studio 中调用 lint 的方式：

Analyze > Inspect Code 

Android Studio 中调整 lint 的配置：

File > Other Settings > Default Setting > Inspections

尤其应注意其中的这几项：

+ Performance issues 中的内容
+ Android Lint > Memory
+ Layout hierarchy is too deep
+ Overdraw


---
the hidden cost of transparency 

这两个 API 的用法：

+ ViewPropertyAnimator.alpha(0.0f).withLayer()
+ View.hasOverlappingRendering()

[ref](https://www.youtube.com/watch?v=wIy8g8yNhNk&list=PL8ktV16dN_6vKDQB-D7fAqA6zRFQOoKtI&index=3)

---

避免在 `onDraw()` 中分配内存

为什么要避免在 `onDraw()` 中分配内存：

+ `onDraw()` 运行在主线程，它被调用的次数可能很多 (16ms一次)
+ 分配内存可能触发 GC

[ref](https://www.youtube.com/watch?v=HAK5acHQ53E&index=4&list=PL8ktV16dN_6vKDQB-D7fAqA6zRFQOoKtI)

---
Strict Mode 的用法

`StrictMode` 的用法。

```java
StrictMode.noteSlowCall();

// 待检查的方法
....

StrictMode.setThreadPolicy(new 
	StrictMode.ThreadPolicy.Builder()
	.detectCustomSlowCalls()
	.penaltyFlashScreen()
	.build());
```

建议对 `synchronized` 方法都进行上述检查。

`VmPolicy` 的用法。


[ref](https://www.youtube.com/watch?v=oGrXdxpWgyY&index=5&list=PL8ktV16dN_6vKDQB-D7fAqA6zRFQOoKtI)

---

自定义 View 中的常见问题

问题一： useless calls to onDraw()

解决办法

+ 只在必要时调用 `View.invalidate()`
+ 调用 `View.invalidate()` 时总是带上 Rect 参数

可以通过 `Canvas.clip()` 方法得到 Rect。见 ClipRect 

draw() 的流程

+ dirty? no, do nothing
+ onscreen? no, do nothing
+ visible? no, do nothing
+ redraw

视频中的例子是一个扑克牌。

推荐使用工具：Profile GPU Rendering

问题二：wasted CPU cycles

自定义 View 的性能问题

[ref](https://www.youtube.com/watch?v=zK2i7ivzK7M&list=PL8ktV16dN_6vKDQB-D7fAqA6zRFQOoKtI&index=6)

---
batching work until later

耗电的模块

+ CPU, GPU
+ GPS, Compass, Gyro/Accelerometer
+ WiFi, WCDMA, Bluetooth

相关 API

+ AlarmManager
+ SyncAdapter
+ JobScheduler

建议使用的方法 `AlarmManager.setWindow()` 和 `AlarmManager.setInexactRepeating()`

不建议使用的方法 `AlarmManager.setExact()`

学习官网中关于 Transferring Data Using Sync Adapters

`JobScheduler` 比 `AlarmManager` 更好更强大，推荐使用

优化技术

- 批处理，减少唤醒次数
- 延迟处理，等待充电或 WiFi
- 缓存
- 合理预加载
- 数据压缩
- 

网络访问速度的几个经验值 [ref](https://www.youtube.com/watch?v=uzboHWX3Kvc&index=14&list=PLWz5rJ2EKKc9CBxr3BVjPTPoDPLdPIFCE)

- < 60ms - GOOD
- 60~220ms - OK
- > 220ms - BAD

推荐使用工具

- Emulator Throttling - 模拟较差的网络


[ref](https://www.youtube.com/watch?v=-3ry8PxcJJA&index=7&list=PL8ktV16dN_6vKDQB-D7fAqA6zRFQOoKtI)

[为什么要使用 JobScheduler](https://www.youtube.com/watch?v=fEEulSk1kNY&t=0s&list=PLWz5rJ2EKKc9CBxr3BVjPTPoDPLdPIFCE&index=39)

[如何优化网络性能](https://www.youtube.com/watch?v=l5mE3Tpjejs&t=159s)

[如何优化网络性能2](https://www.youtube.com/watch?v=Ecz5WDZoJok&index=37&list=PLWz5rJ2EKKc9CBxr3BVjPTPoDPLdPIFCE)

---

smaller pixel format

问题背景：Heap 中存在碎片，无法分配指定大小的内存，触发 GC

|Format | Bits Per Pixel|
|-------|---------------|
| ARG_8888| 32          |
| RGB_565 | 16          |
| ARGB_4444| 16         |
| ALPHA_8  | 8          |

+ 没有必要为不带透明度的(比如 jpg 图片就不支持透明度)图片使用 ARG_8888
+ 为小图片使用 ARGB_4444 格式

```java
mBitmapOptions = new BitmapFactory.Options();
mBitmapOptions.inPreferredConfig = Bitmap.Config.RGB_565;
BitmapFactory.decodeResource(getResoures(), R.drawable.firstBitmap, mBitmapOptions);
```


[ref](https://www.youtube.com/watch?v=1WqcEHXRWpM&index=8&list=PL8ktV16dN_6vKDQB-D7fAqA6zRFQOoKtI)

---
smaller png files

Google 关键字 Creating smaller PNG files

推荐使用工具： ScriptPNG

建议做法：服务器端为图片存储不同质量和尺寸的版本，客户端按需取用

[ref](https://www.youtube.com/watch?v=2TUvmlGoDrw&list=PL8ktV16dN_6vKDQB-D7fAqA6zRFQOoKtI&index=9)

[ref2](https://www.youtube.com/watch?v=ts5o6t7enOk&index=15&list=PLWz5rJ2EKKc9CBxr3BVjPTPoDPLdPIFCE)

---
scale bitmap

app中一个常见的场景是：后台提供的是高清图片，而实际上在手机屏幕上只是展示一个小的缩略图。

生成缩略图的一种方法如下：

createScaledBitmap(inBmp, w, h)

不过这个方法有个问题，它要求创建小图片之前已经加载大图片。

另一种生成缩略图的方法如下：

mBitmapOptions.inSampleSize = 4;
mCurrentMap = BitmapFactory.decodeFile(filename, mBitmapOptions);

inSampleSize 通常要求是2的幂。如果不是呢，该如何处理：

```java
mBitmapOptions.inScaled = true;
mBitmapOptions.inDensity = srcWidth;
mBitmapOptions.inTargetDensity = dstWidth;
```

视频中提到一个加快生成缩略图速度的技巧，结合 `inSampleSize`：

```java
mBitmapOptions.inScaled = true;
mBitmapOptions.inSampleSize = 4;
mBitmapOptions.inDensity = srcWidth;
mBitmapOptions.inTargetDensity = dstWidth * mBitmapOptions.inSampleSize;
```

视频中提到不对图片进行实际解码的情况下获取图片大小的技巧：

```java
mBitmapOptions.inJustDecodeBounds = true;
BitmapFactory.decodeFile(filename, mBitmapOptions);
srcWidth = mBitmapOptions.outWidth;
srcHeight = mBitmapOptions.outHeight;
```

[ref](https://www.youtube.com/watch?v=HY9aaXHx8yA&list=PL8ktV16dN_6vKDQB-D7fAqA6zRFQOoKtI&index=10)


---
reusing bitmap

视频中提到复用图片的技巧：

```java
mBitmapOptions.inBitmap = mCurrentBitmap;
mCurrentBitmap = BitmapFactory.decodeFile(filename, mBitmapOptions);
```

但这个技巧有限制：

+ 在 SDK 11-18中，要求 inBitmap (被重用的图片) 必须跟待解码的图片大小一致。被重用的图片(SDK 19 以上放宽了限制，inBitmap 比待解码的图片大即可)
+ 不能不同的pixel format 之间复用图片


[ref](https://www.youtube.com/watch?v=_ioFW3cyRV0&list=PL8ktV16dN_6vKDQB-D7fAqA6zRFQOoKtI&index=11)


---

使用内存分析工具

要点：

+ 图中哪个是 free memory
+ 图中哪个是 allocated memory
+ 图中哪里发生了 GC
+ 图片哪里短时间内发生了大量 GC

推荐工具：

+ Allocation Tracker (Android Studio 内置)

[ref](https://www.youtube.com/watch?v=P--rg1o7Cz4&index=12&list=PL8ktV16dN_6vKDQB-D7fAqA6zRFQOoKtI)

---

Rendering Performance  渲染效率


导致渲染效率低的常见原因：

+ 布局结构复杂，导致重绘时耗时多 (耗 CPU 性能)
+ 过度绘制
+ 大量使用动画

对应的工具：

+ Hierachy Viewer - 查看布局结构
+ Profile GPU Rendering, Show GPU Overdraw, GPU View Updates
+ Traceview


[ref](https://www.youtube.com/watch?v=HXQhu6qfTVU&list=PL8ktV16dN_6vKDQB-D7fAqA6zRFQOoKtI&index=15)

----
理解过度绘制

想像一下你给房子刷油漆，你先刷红色的，刷完红色又刷黄色的，结果刷红色油漆的工作算是白做了。

推荐工具：

+ Show GPU Overdraw

各种颜色：

+ 无颜色 - 没有重绘
+ 蓝色 - 重绘1次
+ 绿色 - 重绘2次
+ 粉色 - 重绘3次
+ 红色 - 重绘4次

要尽量减少重绘

减少过度重绘的技巧 [ref](https://developer.android.com/topic/performance/rendering/overdraw)

lint 也可以检查出 Overdraw 问题


[ref](https://www.youtube.com/watch?v=T52v50r-JfE&list=PL8ktV16dN_6vKDQB-D7fAqA6zRFQOoKtI&index=16)

[ref](https://www.youtube.com/watch?v=KFklLqiEG6w&index=21&list=PLWz5rJ2EKKc9CBxr3BVjPTPoDPLdPIFCE)


---

理解 VSYNC

+ 屏幕的 Refresh Rate - 理想值 60Hz
+ GPU 的 Frame Rate - 60fps 

两个值不一定完全相同，比如 Frame Rate 可能为 100fps，而 Refresh Rate 只有 70Hz。但两者不匹配的情况下，视觉上看来是有问题的。

GPU 渲染完成后数据在Back Buffer，先将数据拷贝到 Frame Buffer

屏幕展示数据是从 Frame Buffer 获取。

VSYNC 是用于保证当屏幕绘制完 Frame Buffer 中的数据时能及时从 Back Buffer 向 Frame Buffer 拷贝数据。

可以这样理解，当 Refresh Rate 大于 Frame Rate 时，VSYNC 就会起作用。如果 Refresh Rate 小于 Frame Rate 时，会发生什么呢？用户会感觉到卡顿。


[ref](https://www.youtube.com/watch?v=1iaHxmfZGGc&index=17&list=PL8ktV16dN_6vKDQB-D7fAqA6zRFQOoKtI)

---
Profile GPU Rendering

为什么是 60fps  [这里](https://www.youtube.com/watch?v=CaMTIgxCSqU&list=PL8ktV16dN_6vKDQB-D7fAqA6zRFQOoKtI&index=19)

[ref](https://www.youtube.com/watch?v=VzYkVL1n4M8&list=PL8ktV16dN_6vKDQB-D7fAqA6zRFQOoKtI&index=18)

---

HashMap 的问题

+ Autobox
+ 内存消耗大

SparseArray

适用场景：

+ 少于 1000 objects
+ map of map

[ref](https://www.youtube.com/watch?v=I16lz26WyzQ&list=PL8ktV16dN_6vKDQB-D7fAqA6zRFQOoKtI&index=20)

[ref2](https://www.youtube.com/watch?v=ORgucLTtTDI&list=PLWz5rJ2EKKc9CBxr3BVjPTPoDPLdPIFCE&index=28)

---

对象池用于减少内存抖动。

判断是否需要使用对象池的依据：观察短时间内是否有大量分配短生命周期的对象 (Heap Allocation)

注意：对象归还到对象池时应注意进行必要的清理，以免引起内存泄露

[ref](https://www.youtube.com/watch?v=bSOREVMEFnM&index=42&list=PLWz5rJ2EKKc9CBxr3BVjPTPoDPLdPIFCE)

---

是使用迭代还是使用索引？

+ 迭代 - 语法清晰，不易出错。但是耗性能
 + 每次调用`iterator()`获取 iterator 时会有一次对象分配；
 + 每次调用`next()`时很可能进行了某些条件检查，比如是否有被修改

测试数据表明使用索引比使用迭代的性能要好。


[ref](https://www.youtube.com/watch?v=MZOf3pOAM6A&list=PLWz5rJ2EKKc9CBxr3BVjPTPoDPLdPIFCE&index=43)

---

[为什么要避免使用 enum](https://www.youtube.com/watch?v=Hzs6OBcvNQE&list=PLWz5rJ2EKKc9CBxr3BVjPTPoDPLdPIFCE&index=31)

但有时 int 不如 enum 使用方便，如何解决这个问题呢？使用 `@IntDef`

```java
@Retention(CLASS)
@IntDef({NAVIGATION_MODE_STANDARD, ...})
public @interface NavigationMode {}

public static final int NAVIGATION_MODE_STANDARD = 0;

public abstract setNavigationMode(@NavigationMode int mode);

@NavigationMode public abstract int getNavigationMode();
```

有时 proguard 也会将 enum 优化成 int

---

`onTrimMemory()`和`onLowMemory()`回调的作用。

[ref](https://www.youtube.com/watch?v=x8Hddx1eOZo&list=PLWz5rJ2EKKc9CBxr3BVjPTPoDPLdPIFCE&index=32)

---

[避免View泄露](https://www.youtube.com/watch?v=BkbHeFHn8JY&index=33&list=PLWz5rJ2EKKc9CBxr3BVjPTPoDPLdPIFCE)

---

[Double Layout Taxation](https://www.youtube.com/watch?v=dB3_vgS-Uqo&list=PLWz5rJ2EKKc9CBxr3BVjPTPoDPLdPIFCE&index=35)

RelativeLayout 不要作为复杂界面的根布局

解决问题的方法：

+ Systrace
+ Hierachy Viewer - 减少 View
+ 不要随意调用 `requestLayout`

---

优化apk大小：使用proguard (压缩， 优化， 混淆)

```groovy
android {
	buildTypes {
		release {
			minifyEnabled true
			shrinkResources true
		}
	}
}
```

如果某些资源要保留，使用 `tools:keep`

如果某些资源要移除，使用 `tools:discard`

合理使用 buildFlavor

[ref](https://www.youtube.com/watch?v=5frxLkO4oTM&list=PLWz5rJ2EKKc9CBxr3BVjPTPoDPLdPIFCE&index=17)

[ref](https://www.youtube.com/watch?v=HxeW6DHEDQU&list=PLWz5rJ2EKKc9CBxr3BVjPTPoDPLdPIFCE&index=18)

---

实验验证一下[这个视频](https://www.youtube.com/watch?v=qBxeHkvJoOQ&list=PLWz5rJ2EKKc9CBxr3BVjPTPoDPLdPIFCE&index=25)当中提到的技巧 

Array of Structures v.s. Structure of Arrays

后者性能要好？

试用一下 FlatBuffers

---

AsyncTask 使用不当时非常容易出现内存泄漏，主要内部是内部类对外部类有一个不明确的引用。检查我们 app 中是否有这种情况

[ref](https://www.youtube.com/watch?v=jtlRNNhane0&list=PLWz5rJ2EKKc9CBxr3BVjPTPoDPLdPIFCE&index=4)


---

+ CPU
  + CPU 利用率, thread states, method trace, system trace
+ Memory
  + Allocation tracking, heap dump
+ Network
  + request, response, payloads
+ Energy
  + wakelocks, jobs, alarms, location


技巧一：根据实际调整 CPU recording config, 主要是采样时间。

技巧二：如何精确采样。

使用 `Debug` 获取 Trace 时应注意文件命名不要重复，以免同名文件被覆盖。

技巧三：搜索功能

技巧四：System trace 与  Trace 结合使用

相关视频

Google IO 2018 Improve app performance with Android Studio Profilers

Android Game Developer Summit 2018 Android Studio Profiling

+ filtering in CPU & memory profilers
+ WASD  快捷键用于 zoom 和 navigate
+ zoom to selection
+ memory allocation tracking (sample, full, none)
+ export/import CPU recordings & heap dump
+ consider depths in heap dumps
+ observe Activity and Fragment lifecycle


[Deep Dive into Android Studio Profilers (Android Dev Summit '18) - YouTube](https://www.youtube.com/watch?v=LGVbpobV-Yg&list=PLWz5rJ2EKKc_AZpvyAwl1QDg5WQp5hpRd)

--


https://proandroiddev.com/kotlin-coroutines-vs-rxjava-an-initial-performance-test-68160cfc6723

https://proandroiddev.com/forget-rxjava-kotlin-coroutines-are-all-you-need-part-1-2-4f62ecc4f99b

https://stackoverflow.com/questions/48106252/why-threads-are-showing-better-performance-than-coroutines

https://stackoverflow.com/questions/42066066/how-kotlin-coroutines-are-better-than-rxkotlin

https://stackoverflow.com/questions/49606471/why-and-when-to-use-co-routines-instead-of-threads-in-android-using-kotlin-as-th

https://expertise.jetruby.com/kotlin-coroutines-on-android-farewell-rxjava-56a580463af7

https://medium.com/capital-one-tech/coroutines-and-rxjava-an-asynchronicity-comparison-part-1-asynchronous-programming-e726a925342a

https://medium.com/capital-one-tech/kotlin-coroutines-on-android-things-i-wish-i-knew-at-the-beginning-c2f0b1f16cff
