# BufferQueue

+ BufferQueue 是 Android 显示系统的核心
+ BufferQueue 是生产者-消费者模型

![](/images/15843238675377.jpg)

GraphicBuffer 用 BufferState 来表示其状态：

+ FREE - 空闲
+ DEQUEUED - 被生产者获取
+ QUEUED - 生产者已填充数据
+ ACQUIRED - 被消费者获取

BufferQueue 内部实现是 BufferQueueCore。

![](/images/15843240303590.jpg)

+ 当生产者可以生产图形数据时，首先调用函数 `BufferQueueProducer.dequeueBuffer()` 向 BufferQueue 中申请一块  GraphicBuffer
+ 当生产者填充完图像数据后，调用 `BufferQueueProducer.queueBuffer()` 函数将 GraphicBuffer 入队 （数据写完了，可以传给生产者-消费者队列，让消费者来消费了）
+ 在消费者接收到 `onFrameAvailable` 回调时或者消费者主动想要消费数据，调用`acquireBuffer()` 尝试向 BufferQueueCore 获取一个数据以供消费
+ 在消费者获取到 Slot 后开始消费数据（典型的消费如 SurfaceFlinger 的 UI 合成），消费完毕后，调用 `releaseBuffer()` 告知 BufferQueueCore 这个Slot被消费者消费完毕了，可以给生产者重新生产数据

> 使用方创建并拥有 BufferQueue 数据结构，并且可存在于与其生产方不同的进程中。当生产方需要缓冲区时，它会通过调用 dequeueBuffer() 从 BufferQueue 请求一个可用的缓冲区，并指定缓冲区的宽度、高度、像素格式和使用标记。然后，生产方填充缓冲区并通过调用 queueBuffer() 将缓冲区返回到队列。接下来，使用方通过 acquireBuffer() 获取该缓冲区并使用该缓冲区的内容。当使用方操作完成后，它会通过调用 releaseBuffer() 将该缓冲区返回到队列 [ref](https://source.android.com/devices/graphics/arch-bq-gralloc)

通常不直接使用 BufferQueue。Surface 作为 BufferQueue 的生产者，SurfaceTexture 作为 BufferQueue 的消费者。

# SurfaceView

Android中，`SurfaceView` 作为系统提供的组件，因为可以在子线程中绘制提高性能，`SurfaceView` 拥有自身的 Surface，不需要和 Activity 的 Surface 共享。

> 在Android系统中，有一种特殊的视图，称为SurfaceView，它拥有独立的绘图表面，即它不与其宿主窗口共享同一个绘图表面。由于拥有独立的绘图表面，因此SurfaceView的UI就可以在一个独立的线程中进行绘制。又由于不会占用主线程资源，SurfaceView一方面可以实现复杂而高效的UI，另一方面又不会导致用户输入得不到及时响应 [ref](https://cloud.tencent.com/developer/article/1033903)

![](/images/15843249407479.jpg)

`SurfaceView.onAttachedToWindow()` 做了两件重要的事：

+ 第一件事情是调用 `requestTransparentRegion()` 方法通知父视图，当前正在处理的 SurfaceView 需要在宿主窗口的绘图表面上挖一个洞
+ 第二件事情是调用从父类View继承下来的成员函数 `getWindowSession()` 来获得一个实现了IWindowSession接口的Binder代理对象，并且将该Binder代理对象保存在SurfaceView类的成员变量mSession中

`SurfaceView.updateSurface()` 是最重要的方法。

```java
public class SurfaceView extends View implements ViewRootImpl.WindowStoppedCallback {

    final ReentrantLock mSurfaceLock = new ReentrantLock();
    final Surface mSurface = new Surface();       // Current surface in use
    
    /** @hide */
    protected void updateSurface() {
        if (!mHaveFrame) {
            return;
        }
```

# 参考

+ group/22112/articles/show/374931
+ [android SurfaceView绘制实现原理解析 - 云+社区 - 腾讯云](https://cloud.tencent.com/developer/article/1033903)
+ [BufferQueue 和 Gralloc  |  Android 开源项目  |  Android Open Source Project](https://source.android.com/devices/graphics/arch-bq-gralloc)