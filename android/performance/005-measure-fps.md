如何准确测量帧率？

# GT
需要 root 手机才能使用

# Choreographer

```java
public class Fps {

    private static final int FIRST_FRAME = -1;

    private final Choreographer mChoreographer;
    private long mLastFrameTimeMs = FIRST_FRAME;
    private List<Long> mReportList;
    private boolean mEnabled;

    private final Choreographer.FrameCallback mFPSMeasuringCallback = new Choreographer.FrameCallback() {
        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
        @Override
        public void doFrame(long frameTimeNanos) {
            long frameTimeMs = nsToMs(frameTimeNanos);

            if (mLastFrameTimeMs == FIRST_FRAME) {
                mLastFrameTimeMs =  frameTimeMs;
                mChoreographer.postFrameCallback(mFPSMeasuringCallback);
                return;
            }
            long millisSecondDelay = frameTimeMs - mLastFrameTimeMs;
            mLastFrameTimeMs = frameTimeMs;
            onFrameRendered(millisSecondDelay);
            mChoreographer.postFrameCallback(mFPSMeasuringCallback);
        }
    };

    public Fps(Choreographer choreographer) {
        this.mChoreographer = choreographer;
        mReportList = new ArrayList<>();
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public void enable() {
        mEnabled = true;
        mLastFrameTimeMs = FIRST_FRAME;
        mChoreographer.postFrameCallback(mFPSMeasuringCallback);
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public void disable() {
        report();
        mChoreographer.removeFrameCallback(mFPSMeasuringCallback);
        mEnabled = false;
    }

    public boolean isEnabled() {
        return mEnabled;
    }

    private void report() {
        for (int i = 0; i < mReportList.size(); i++) {
            System.out.println("frame " + i + " costs " + mReportList.get(i));
        }
        mReportList.clear();
    }

    private void onFrameRendered(long millisSecondDelay) {
        System.out.println("onFrameRendered " + millisSecondDelay);
        mReportList.add(millisSecondDelay);
    }

    private long nsToMs(long frameTimeNanos) {
        return frameTimeNanos / 1000000;
    }
}
```

1. TODO 来源 youtube? 

[从 FrameCallback 理解 Choreographer 原理及简单帧率监控应用 - Android - 掘金](https://juejin.im/entry/58c83f3f8ac247072018d926)

如何修改 `debug.choreographer.skipwarning`? 

+ 方法一，反射 [ref](https://www.jianshu.com/p/d126640eccb1)
+ 方法二，setprop (需要root权限) [ref](https://blog.zzzmode.com/2016/04/24/use-tencent-gt-test-app-smooth/)

```java
static {
        try {
            Field field = Choreographer.class.getDeclaredField("SKIPPED_FRAME_WARNING_LIMIT");
            field.setAccessible(true);
            field.set(Choreographer.class,1);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
```

setprop

```
getprop debug.choreographer.skipwarning      //读取
setprop debug.choreographer.skipwarning 1    //修改
setprop debug.choreographer.skipwarning 30
setprop ctl.restart surfaceflinger; setprop ctl.restart zygote    //重启
```


# Takt 
添加依赖:

```
dependencies {
    compile 'jp.wasabeef:takt:1.0.5'
}
```

在 Application 中调用：

```java
public class MyApplication extends Application {
  public void onCreate() {
    super.onCreate();
    Takt.stock(this);
  }
}
```

[Takt](https://github.com/wasabeef/Takt)

# TinyDancer

[TinyDancer](https://github.com/friendlyrobotnyc/TinyDancer)

# adb shell

```
// 总时间
adb shell dumpsys gfxinfo <pagekagename>

// 精确时间
adb shell dumpsys gfxinfo <PACKAGE_NAME> framestats

// 重置
adb shell dumpsys gfxinfo <PACKAGE_NAME> reset
```

[(1) Google I/O 2012 - For Butter or Worse: Smoothing Out Performance in Android UIs - YouTube](https://www.youtube.com/watch?v=Q8m9sHdyXnE&hl=zh-cn)

# 其他方案

[Android性能专项FPS测试实践 - wpyily的专栏 - CSDN博客](https://blog.csdn.net/wpyily/article/details/52593826)

# 参考

[Android App卡顿分析](https://blog.csdn.net/mengfeicheng2012/article/details/53768954)

[从 FrameCallback 理解 Choreographer 原理及简单帧率监控应用 - Android - 掘金](https://juejin.im/entry/58c83f3f8ac247072018d926)

[当我们讨论流畅度的时候，我们究竟在说什么？ - 简书](https://www.jianshu.com/p/0ac95813f16a)

[使用腾讯GT测试Android app流畅度｜zzzmode's blog](https://blog.zzzmode.com/2016/04/24/use-tencent-gt-test-app-smooth/)
