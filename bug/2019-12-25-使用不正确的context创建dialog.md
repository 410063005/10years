# 使用不正确的 context 来创建 Dialog 

使用不正确的 context 来创建 Dialog，崩溃并提示 `is your activity running?`

<!--more-->

# 异常日志

堆栈信息：

```
android.view.WindowManager$BadTokenException: Unable to add window -- token null is not valid; is your activity running?
android.view.ViewRootImpl.setView(ViewRootImpl.java:881)
android.view.WindowManagerGlobal.addView(WindowManagerGlobal.java:372)
android.view.WindowManagerImpl.addView(WindowManagerImpl.java:128)
android.app.Dialog.show(Dialog.java:454)
```

机型：各机型均有发生，API level 25, 26, 27, 28

# 源码
根据以上堆栈不难找到对应源码。

`Dialog.show()` 源码如下：

```java
public class Dialog {
    private final WindowManager mWindowManager;
    
    public void show() {
        ...
        mWindowManager.addView(mDecor, l);    
        ...
    }    
}
```

[WindowManagerImpl.addView()](https://android.googlesource.com/platform/frameworks/base/+/refs/heads/marshmallow-release/core/java/android/view/WindowManagerImpl.java#83) 源码如下：

```java
public final class WindowManagerImpl implements WindowManager {
    private final WindowManagerGlobal mGlobal = WindowManagerGlobal.getInstance();
    
    ...
    
    @Override
    public void addView(@NonNull View view, @NonNull ViewGroup.LayoutParams params {
        applyDefaultToken(params);
        mGlobal.addView(view, params, mDisplay, mParentWindow);
    }
}
```

[WindowManagerGlobal.addView()](https://android.googlesource.com/platform/frameworks/base/+/refs/heads/marshmallow-release/core/java/android/view/WindowManagerGlobal.java#231) 源码如下：

```java
public final class WindowManagerGlobal {
    public void addView(View view, ViewGroup.LayoutParams params,
            Display display, Window parentWindow) {
    
        ...
        ViewRootImpl root;
        ...
        root = new ViewRootImpl(view.getContext(), display);
        ...
        // do this last because it fires off messages to start doing things
        try {
            root.setView(view, wparams, panelParentView);
        } catch (RuntimeException e) {
            // BadTokenException or InvalidDisplayException, clean up.
            synchronized (mLock) {
                final int index = findViewLocked(view, false);
                if (index >= 0) {
                    removeViewLocked(index, true);
                }
            }
            throw e;
        }        
    }
}
```

[ViewRootImpl.setView()](https://android.googlesource.com/platform/frameworks/base/+/refs/heads/marshmallow-release/core/java/android/view/ViewRootImpl.java#447) 源码如下：

```java
public final class ViewRootImpl {
    final IWindowSession mWindowSession;

    public void setView(View view, WindowManager.LayoutParams attrs, View panelParentView) {
        ...
        int res; /* = WindowManagerImpl.ADD_OKAY; */
        ...
        res = mWindowSession.addToDisplay();
        if (res < WindowManagerGlobal.ADD_OKAY) {
            switch (res) {
                    case WindowManagerGlobal.ADD_BAD_APP_TOKEN:
                    case WindowManagerGlobal.ADD_BAD_SUBWINDOW_TOKEN:
                        throw new WindowManager.BadTokenException(
                                "Unable to add window -- token " + attrs.token
                                + " is not valid; is your activity running?");
                ...
            }
        }     
    }
}
```

# 根源

```kotlin
view.postDelayed({

    val d = Dialog(this@MainActivity.applicationContext)
    val tv = TextView(this@MainActivity)
    tv.text = "hello"
    d.setContentView(tv)
    d.show()

}, 3000)
```

偶然发现上述代码的输出日志也是 "is your activity running?"，才注意到问题的根源是因为创建 Dialog 时传入的 Context 不正确。

```
2019-07-11 18:01:15.808 18243-18243/com.sunmoonblog.cmdemo E/AndroidRuntime: FATAL EXCEPTION: main
    Process: com.sunmoonblog.cmdemo, PID: 18243
    android.view.WindowManager$BadTokenException: Unable to add window -- token null is not valid; is your activity running?
        at android.view.ViewRootImpl.setView(ViewRootImpl.java:884)
        at android.view.WindowManagerGlobal.addView(WindowManagerGlobal.java:372)
        at android.view.WindowManagerImpl.addView(WindowManagerImpl.java:128)
        at android.app.Dialog.show(Dialog.java:454)
        at com.sunmoonblog.cmdemo.MainActivity$clAnimation2$1.run(MainActivity.kt:85)
        at android.os.Handler.handleCallback(Handler.java:808)
        at android.os.Handler.dispatchMessage(Handler.java:101)
        at android.os.Looper.loop(Looper.java:166)
        at android.app.ActivityThread.main(ActivityThread.java:7529)
        at java.lang.reflect.Method.invoke(Native Method)
        at com.android.internal.os.Zygote$MethodAndArgsCaller.run(Zygote.java:245)
        at com.android.internal.os.ZygoteInit.main(ZygoteInit.java:921)
```

修改后的代码如下：

```kotlin
view.postDelayed({

    val d = Dialog(this@MainActivity)
    val tv = TextView(this@MainActivity)
    tv.text = "hello"
    d.setContentView(tv)
    d.show()

}, 3000)
```

注意不要使用 applicationContext 来创建 Dialog。

# 参考
[弹出 Dialog 时出现异常](https://stackoverflow.com/questions/5796611/dialog-throwing-unable-to-add-window-token-null-is-not-for-an-application-wi)