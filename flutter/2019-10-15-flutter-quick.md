[TOC]
# 问题记录

## 图片 url 为 null 的问题

![-w337](media/15711047214876.jpg)

修复方式：判断是否为 `null`

```dart
Image.network(headPic ?? "")
```

## 自定义 Action Bar

```dart
class ShortAppBar extends StatelessWidget {
  const ShortAppBar({this.onBackPressed});

  final VoidCallback onBackPressed;

  @override
  Widget build(BuildContext context) {
    return SizedBox(
      width: MediaQuery.of(context).size.width,
      height: 50,
      child: Material(
        color: Colors.transparent.withAlpha(0x00),
        child: Row(
          mainAxisAlignment: MainAxisAlignment.start,
          children: <Widget>[
            IconButton(
              //icon: const Icon(Icons.arrow_back),
              icon: const ImageIcon(
                AssetImage('images/actionbar_arrow_left.png'),
                color: Colors.white,
              ),
              tooltip: 'Back',
              onPressed: onBackPressed,
            ),
            Expanded(
              child: Text('组队达人榜',
                  textAlign: TextAlign.center,
                  style: TextStyle(color: Colors.white, fontSize: 16)),
            ),
            SizedBox(
              width: 40,
            )
          ],
        ),
      ),
    );
  }
}
```

![-w451](media/15711089803330.jpg)

## 监听 ListView 滚动

[ref](https://medium.com/@diegoveloper/flutter-lets-know-the-scrollcontroller-and-scrollnotification-652b2685a4ac)

```dart
    _controller = ScrollController();
    _controller.addListener(() {
      final offset = _controller.offset;
      if (offset < 0) {
        setState(() { ... });
      } else if (offset < 50) {
        setState(() { ... });
      } else {
        setState(() { ... });
      }
```

## SafeArea 的用处

使用 SafeArea

![-w458](media/15711240783179.jpg)

不使用 SafeArea

![-w369](media/15711240277385.jpg)

## 大图片性能问题

大图片后显示导致界面抖动。

## 编译 release 包问题

2019-10-22 11:50:40.701 18842-18842/? E/AndroidRuntime: FATAL EXCEPTION: main
    Process: com.tencent.igame, PID: 18842
    java.lang.RuntimeException: Unable to start activity ComponentInfo{com.tencent.igame/com.tencent.igame.view.flutter.FlutterContainerActivity}: java.lang.RuntimeException: Failed to call observer method
        at android.app.ActivityThread.performLaunchActivity(ActivityThread.java:3303)
        at android.app.ActivityThread.handleLaunchActivity(ActivityThread.java:3411)
        at android.app.ActivityThread.-wrap12(Unknown Source:0)
        at android.app.ActivityThread$H.handleMessage(ActivityThread.java:1994)
        at android.os.Handler.dispatchMessage(Handler.java:108)
        at android.os.Looper.loop(Looper.java:166)
        at android.app.ActivityThread.main(ActivityThread.java:7529)
        at java.lang.reflect.Method.invoke(Native Method)
        at com.android.internal.os.Zygote$MethodAndArgsCaller.run(Zygote.java:245)
        at com.android.internal.os.ZygoteInit.main(ZygoteInit.java:921)
     Caused by: java.lang.RuntimeException: Failed to call observer method
        at android.arch.lifecycle.ClassesInfoCache$MethodReference.invokeCallback(ClassesInfoCache.java:225)
        at android.arch.lifecycle.ClassesInfoCache$CallbackInfo.invokeMethodsForEvent(ClassesInfoCache.java:193)
        at android.arch.lifecycle.ClassesInfoCache$CallbackInfo.invokeCallbacks(ClassesInfoCache.java:184)
        at android.arch.lifecycle.ReflectiveGenericLifecycleObserver.onStateChanged(ReflectiveGenericLifecycleObserver.java:36)
        at android.arch.lifecycle.LifecycleRegistry$ObserverWithState.dispatchEvent(LifecycleRegistry.java:354)
        at android.arch.lifecycle.LifecycleRegistry.addObserver(LifecycleRegistry.java:180)
        at io.flutter.facade.Flutter.createView(Flutter.java:91)
        at com.tencent.igame.view.common.fragment.flutter.TipFlutterFragment.onCreateView(TipFlutterFragment.java:89)
        at com.tencent.igame.view.common.fragment.flutter.TipFlutterFragment.onCreateView(TipFlutterFragment.java:29)
        at android.support.v4.app.Fragment.performCreateView(Fragment.java:2439)
        at android.support.v4.app.FragmentManagerImpl.moveToState(FragmentManager.java:1460)
        at android.support.v4.app.FragmentManagerImpl.moveFragmentToExpectedState(FragmentManager.java:1784)
        at android.support.v4.app.FragmentManagerImpl.moveToState(FragmentManager.java:1852)
        at android.support.v4.app.BackStackRecord.executeOps(BackStackRecord.java:802)
        at android.support.v4.app.FragmentManagerImpl.executeOps(FragmentManager.java:2625)
        at android.support.v4.app.FragmentManagerImpl.executeOpsTogether(FragmentManager.java:2411)
        at android.support.v4.app.FragmentManagerImpl.removeRedundantOperationsAndExecute(FragmentManager.java:2366)
        at android.support.v4.app.FragmentManagerImpl.execPendingActions(FragmentManager.java:2273)
        at android.support.v4.app.FragmentManagerImpl.dispatchStateChange(FragmentManager.java:3273)
        at android.support.v4.app.FragmentManagerImpl.dispatchActivityCreated(FragmentManager.java:3229)
        at android.support.v4.app.FragmentController.dispatchActivityCreated(FragmentController.java:201)
        at android.support.v4.app.FragmentActivity.onStart(FragmentActivity.java:620)
        at android.support.v7.app.AppCompatActivity.onStart(AppCompatActivity.java:178)
        at com.tencent.igame.base.view.activity.BaseActionBarActivity.onStart(BaseActionBarActivity.java:99)
        at android.app.Instrumentation.callActivityOnStart(Instrumentation.java:1339)
        at android.app.Activity.performStart(Activity.java:7403)
        at android.app.ActivityThread.performLaunchActivity(ActivityThread.java:3266)
        at android.app.ActivityThread.handleLaunchActivity(ActivityThread.java:3411) 
        at android.app.ActivityThread.-wrap12(Unknown Source:0) 
        at android.app.ActivityThread$H.handleMessage(ActivityThread.java:1994) 
        at android.os.Handler.dispatchMessage(Handler.java:108) 
        at android.os.Looper.loop(Looper.java:166) 
        at android.app.ActivityThread.main(ActivityThread.java:7529) 
        at java.lang.reflect.Method.invoke(Native Method) 
        at com.android.internal.os.Zygote$MethodAndArgsCaller.run(Zygote.java:245) 
        at com.android.internal.os.ZygoteInit.main(ZygoteInit.java:921) 
     Caused by: java.lang.NoClassDefFoundError: Failed resolution of: Lio/flutter/plugins/pathprovider/PathProviderPlugin;
        at io.flutter.plugins.GeneratedPluginRegistrant.registerWith(GeneratedPluginRegistrant.java:15)
        at io.flutter.facade.Flutter$2.onCreate(Flutter.java:98)
        at java.lang.reflect.Method.invoke(Native Method)
        at android.arch.lifecycle.ClassesInfoCache$MethodReference.invokeCallback(ClassesInfoCache.java:215)
        at android.arch.lifecycle.ClassesInfoCache$CallbackInfo.invokeMethodsForEvent(ClassesInfoCache.java:193) 
        at android.arch.lifecycle.ClassesInfoCache$CallbackInfo.invokeCallbacks(ClassesInfoCache.java:184) 
        at android.arch.lifecycle.ReflectiveGenericLifecycleObserver.onStateChanged(ReflectiveGenericLifecycleObserver.java:36) 
        at android.arch.lifecycle.LifecycleRegistry$ObserverWithState.dispatchEvent(LifecycleRegistry.java:354) 
        at android.arch.lifecycle.LifecycleRegistry.addObserver(LifecycleRegistry.java:180) 
        at io.flutter.facade.Flutter.createView(Flutter.java:91) 
        at com.tencent.igame.view.common.fragment.flutter.TipFlutterFragment.onCreateView(TipFlutterFragment.java:89) 
        at com.tencent.igame.view.common.fragment.flutter.TipFlutterFragment.onCreateView(TipFlutterFragment.java:29) 
        at android.support.v4.app.Fragment.performCreateView(Fragment.java:2439) 
        at android.support.v4.app.FragmentManagerImpl.moveToState(FragmentManager.java:1460) 
        at android.support.v4.app.FragmentManagerImpl.moveFragmentToExpectedState(FragmentManager.java:1784) 
        at android.support.v4.app.FragmentManagerImpl.moveToState(FragmentManager.java:1852) 
        at android.support.v4.app.BackStackRecord.executeOps(BackStackRecord.java:802) 
        at android.support.v4.app.FragmentManagerImpl.executeOps(FragmentManager.java:2625) 
        at android.support.v4.app.FragmentManagerImpl.executeOpsTogether(FragmentManager.java:2411) 
        at android.support.v4.app.FragmentManagerImpl.removeRedundantOperationsAndExecute(FragmentManager.java:2366) 
        at android.support.v4.app.FragmentManagerImpl.execPendingActions(FragmentManager.java:2273) 
        at android.support.v4.app.FragmentManagerImpl.dispatchStateChange(FragmentManager.java:3273) 
        at android.support.v4.app.FragmentManagerImpl.dispatchActivityCreated(FragmentManager.java:3229) 
        at android.support.v4.app.FragmentController.dispatchActivityCreated(FragmentController.java:201) 
        at android.support.v4.app.FragmentActivity.onStart(FragmentActivity.java:620) 
        at android.support.v7.app.AppCompatActivity.onStart(AppCompatActivity.java:178) 
        at com.tencent.igame.base.view.activity.BaseActionBarActivity.onStart(BaseActionBarActivity.java:99) 
        at android.app.Instrumentation.callActivityOnStart(Instrumentation.java:1339) 
        at android.app.Activity.performStart(Activity.java:7403) 
        at android.app.ActivityThread.performLaunchActivity(ActivityThread.java:3266) 
        at android.app.ActivityThread.handleLaunchActivity(ActivityThread.java:3411) 
        at android.app.ActivityThread.-wrap12(Unknown Source:0) 
        at android.app.ActivityThread$H.handleMessage(ActivityThread.java:1994) 
        at android.os.Handler.dispatchMessage(Handler.java:108) 
        at android.os.Looper.loop(Looper.java:166) 
        at android.app.ActivityThread.main(ActivityThread.java:7529) 
        at java.lang.reflect.Method.invoke(Native Method) 
        at com.android.internal.os.Zygote$MethodAndArgsCaller.run(Zygote.java:245) 
        at com.android.internal.os.ZygoteInit.main(ZygoteInit.java:921) 

![-w1332](media/15717166226676.jpg)

![-w379](media/15717166564224.jpg)



---

## 加载图片时界面抖动的问题

[参考]( https://www.youtube.com/watch?v=pK738Pg9cxc)

![-w601](media/15717122665027.jpg)

## 优化前

+ 使用 `Image.network` 加载图片
+ 使用 `ListView` 作为列表。 跟 Android 类似, 缺点是要等后台数据返回后才能显示 UI

## 优化后

+ 加载图片优化
    + 方案一: `FadeInImage.assetNetwork()`
    + 方案二: `Image.asset()`
+ 列表优化。使用 `CustomScrollView` 作为列表

![Jietu20191022-204442](media/Jietu20191022-204442.jpg)


SliverList 可以很方便地解决 Android 列表中有多种类型 item 的问题

## 内存问题