# TouchEvent 分发流程

![w600](/images/15772651879447.jpg)

![w600](/images/15772651749777.jpg)

+ 注意 **分发** 与 **处理** 的不同
+ The touch event is dispatched from top to bottom but handled from bottom to top
+ The event is dispatched by calling dispatchTouchEvent and handled by onTouchEvent

[图片及参考来源](https://stackoverflow.com/questions/7449799/how-are-android-touch-events-delivered)

注意：**上图比实际代码要简单**，具体看下面的技术细节

# TouchEvent 结构和流水线

![](/images/15772664044536.jpg)

[Android UI Internal : Pipeline of View's Touch Event Handling](https://pierrchen.blogspot.com/2014/03/pipeline-of-android-touch-event-handling.html)

(注：window 那一步似乎不正确?)

# 技术细节

比如，在 Activity 和 ViewGroup A (根布局)之间有 Window 和 DecorView。我没有画出它们是因为通常不会影响触摸事件处理流程。但接下来我会讲到它们。以下的描述是从源码角度来看的

(注意：源码可能有更新，所以行号不对)

+ Activity 的 [dispatchTouchEvent()](https://android.googlesource.com/platform/frameworks/base/+/master/core/java/android/app/Activity.java#3288) 方法收到触摸事件。触摸事件作为 `MotionEvent` 传进来，其中包含 x 坐标、y 坐标、时间、事件类型以及其他信息
+ 触摸事件分发给 Window 的 [superDispatchTouchEvent()](https://android.googlesource.com/platform/frameworks/base/+/master/core/java/android/view/Window.java#1593) 方法。`Window` 是一个抽象类。其实现是 [PhoneWindow](https://android.googlesource.com/platform/frameworks/base/+/696cba573e651b0e4f18a4718627c8ccecb3bda0/policy/src/com/android/internal/policy/impl/PhoneWindow.java#1241)。
+ 接下来是 DecorView 的 [superDispatchTouchEvent](https://android.googlesource.com/platform/frameworks/base.git/+/master/core/java/com/android/internal/policy/DecorView.java#444)。`DecorView` 类处理 status bar、navigation bar、content area 等等。实际上它是 `FrameLayout` 的子类，而 `FrameLayout` 是 `ViewGroup` 的子类。
+ 再接下来是 Activity 的 content view，它是在 Android Studio 的 Layout Editor 中的 xml 布局文件的根布局。`RelativeLayout`、`LinearLayout` 以及 `ConstraintLayout` 等布局，都是 `ViewGroup` 的子类。ViewGroup 在 [dispatchTouchEvent()](https://android.googlesource.com/platform/frameworks/base/+/master/core/java/android/view/ViewGroup.java#2473) 中收到触摸事件。这里的 ViewGroup 即图中的 ViewGroup A。
+ ViewGroup 会[将触摸事件分发给其子节点](https://android.googlesource.com/platform/frameworks/base/+/master/core/java/android/view/ViewGroup.java#2983)，包括任何 ViewGroup 类型的子节点。这里的 ViewGroup 子节点即图中的 ViewGroup B。
+ ViewGroup 可以通过在 [onInterceptTouchEvent()](https://android.googlesource.com/platform/frameworks/base/+/master/core/java/android/view/ViewGroup.java#3123) 方法中返回 `true` 的方式来 [short-circuit](https://android.googlesource.com/platform/frameworks/base/+/master/core/java/android/view/ViewGroup.java#2504)
+ 假设没有 ViewGroup 来结束触摸事件传递流程，该事件会分发给 View 的 [dispatchTouchEvent()](http://%60dispatchtouchevent%28%29%60/)
+ 这一步是处理触摸事件。[如果有 OnTouchListener](https://android.googlesource.com/platform/frameworks/base/+/android-4.3_r2.1/core/java/android/view/View.java#7379)，会首先调用 [OnTouchListener.onTouch](https://android.googlesource.com/platform/frameworks/base/+/android-4.3_r2.1/core/java/android/view/View.java#17783) 来处理触摸事件。否则，[View.onTouchEvent](https://android.googlesource.com/platform/frameworks/base/+/android-4.3_r2.1/core/java/android/view/View.java#8302) 来处理触摸事件
+ 所有的 ViewGroup 和 View 都有同样的机会处理触摸事件。图中没有说明的一点是 ViewGroup 是 View 的子类，所在 `OnTouchListener.onTouch()` 和 `onTouchEvent()` 同样也适用于 ViewGroup
+ 最后由 Activity [onTouchEvent()](https://android.googlesource.com/platform/frameworks/base/+/master/core/java/android/app/Activity.java#3024) 来处理触摸事件

## FAQ
**什么时候覆盖 dispatchTouchEvent()？**

当需要在任何 View 处理触摸事件之前获取这些事件时，需要覆盖 `Activity.dispatchTouchEvent()`。对于 ViewGroup，只用覆盖 `onInterceptTouchEvent()` 和 `onTouchEvent()` 即可。

**什么时候覆盖 onInterceptTouchEvent()?**

覆盖该方来窥探触摸事件，记得返回 `false`.

但是该方法的主要目是是让 ViewGroup 可以处理一些特定类的触摸事件，而让子节点可以处理另外一些类型的事件。比如，`ScrollView` 就是这样来处理 scrolling 的，同时允许子节点处理类似 Button 点击这样的操作。反之，如果子节点不想让父节点窥探触摸事件，它可以调用 `requestDisallowTouchIntercept()`。

**触摸事件有哪些类型?**

主要类型包括：

+ `ACTION_DOWN` - 这是触摸事件的开始。如果想处理触摸事件，遇到 `ACTION_DOWN` 类型事件时总是要返回 `true`。否则，你将无法接收到接下来的触摸事件。 
+ `ACTION_MOVE` - 接下来当你手指在屏幕上移动时会收到这种类型的事件。
+ `ACTION_UP` - 这是最后的事件。

[这里](https://developer.android.com/reference/android/view/MotionEvent.html)可以看到更多的类型的触摸事件。另外需要注意的是，Android 支持多点触摸。

# 实例 - Manage touch events in a ViewGroup


在 ViewGroup 中处理 touch event 要特别小心，因为常常是 ViewGroup 中不同的子节点作为 touch event 的目标，而非 ViewGroup 自己。可以覆盖 [onInterceptTouchEvent()](https://developer.android.com/reference/android/view/ViewGroup.html#onInterceptTouchEvent(android.view.MotionEvent)) 方法以保证每个子节点正确地收到自己的 touch event。

## 在 ViewGroup 中拦截 touch event
当检查到 ViewGroup 上面有 touch event 事件时，[onInterceptTouchEvent()](https://developer.android.com/reference/android/view/ViewGroup.html#onInterceptTouchEvent(android.view.MotionEvent)) 方法会被调用。如果 `onInterceptTouchEvent()` 返回 `true`，就会拦截 MotionEvent，这意味 touch event 不会被传递给子节点，而是传递给 ViewGroup 的 `onTouchEvent()`。

怎么理解以上这段话的意思？

如果所有地方都是缺省处理，touch event 的处理路径是这样的。

```
Activity.dispatchTouchEvent() ->
    ViewGroup.onInterceptTouchEvent() ->
        (event handler) View.OnTouchListener.onTouch() ->
            (default handler) View.onTouchEvent() ->
                ViewGroup.onTouchEvent() ->
                    Activity.onTouchEvent()
```

如果 `ViewGroup.onInterceptTouchEvent()` 返回 `true`，touch event 的处理路径是这样的，View 完全被忽略。

```
Activity.dispatchTouchEvent() ->
    ViewGroup.onInterceptTouchEvent() ->
            ViewGroup.onTouchEvent() ->
                Activity.onTouchEvent()
```

`onInterceptTouchEvent()` 方法让父节点可以在所有子节点收到 touch event 之前就能处理该事件。如果你从 `onInterceptTouchEvent()` 返回 `true`，之前处理 touch event 的子节点会收到 `ACTION_CANCEL` 事件，并且这这后 touch event 会传递给父节点的 `onTouchEvent()` 进行处理。`onInterceptTouchEvent()` 也可以简单地返回 `false`，不做其他处理只是简单地观察一下 touch event 是如何在 view 结构中传递给目标节点。


以下代码片断中，`MyViewGroup` 继承自 ViewGroup。`MyViewGroup` 包含多个子节点。你在子节点上水平拖动时，这些子节点不会收到 touch event，`MyViewGroup` 处理 touch event 的方式是滚动自身内容。但是当你点击子节点或者水平滚动子节点时，父节点并不会拦截 touch event，因为这时候子节点是 touch event 的目标。这种情况下，`onInterceptTouchEvent()` 返回 `false`，MyViewGroup 的 `onTouchEvent()` 也不会被调用。

```java
public class MyViewGroup extends ViewGroup {

    private int mTouchSlop;

    ...

    ViewConfiguration vc = ViewConfiguration.get(view.getContext());
    mTouchSlop = vc.getScaledTouchSlop();

    ...

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        /*
         * This method JUST determines whether we want to intercept the motion.
         * If we return true, onTouchEvent will be called and we do the actual
         * scrolling there.
         */


        final int action = MotionEventCompat.getActionMasked(ev);

        // Always handle the case of the touch gesture being complete.
        if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP) {
            // Release the scroll.
            mIsScrolling = false;
            return false; // Do not intercept touch event, let the child handle it
        }

        switch (action) {
            case MotionEvent.ACTION_MOVE: {
                if (mIsScrolling) {
                    // We're currently scrolling, so yes, intercept the
                    // touch event!
                    return true;
                }

                // If the user has dragged her finger horizontally more than
                // the touch slop, start the scroll

                // left as an exercise for the reader
                final int xDiff = calculateDistanceX(ev);

                // Touch slop should be calculated using ViewConfiguration
                // constants.
                if (xDiff > mTouchSlop) {
                    // Start scrolling!
                    mIsScrolling = true;
                    return true;
                }
                break;
            }
            ...
        }

        // In general, we don't want to intercept touch events. They should be
        // handled by the child view.
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        // Here we actually handle the touch event (e.g. if the action is ACTION_MOVE,
        // scroll this container).
        // This method will only be called if the touch event was intercepted in
        // onInterceptTouchEvent
        ...
    }
}
```

注意：以上代码中 `ViewGroup.onInterceptTouchEvent()` 返回 `true` 之后，这个方法本身再也不会 `ACTION_CANCEL` 和 `ACTION_UP` 事件。`ACTION_CANCEL` 和 `ACTION_UP` 事件会被发送给 `ViewGroup.onTouchEvent()`

# 更多参考

+ Android onTouchEvent [Part 1](https://www.youtube.com/watch?v=SYoN-OvdZ3M), [Part 2](https://www.youtube.com/watch?v=nOcznwNEBf4), and [Part 3](https://www.youtube.com/watch?v=GIWQn90av54) (YouTube video - good summary of some of the links below)
+ [Mastering the Android Touch System](https://www.youtube.com/watch?v=EZAoJU-nUyI) (thorough video by Google developer)
+ [Android UI Internal : Pipeline of View's Touch Event Handling](http://pierrchen.blogspot.com/2014/03/pipeline-of-android-touch-event-handling.html)
+ [Managing Touch Events in a ViewGroup (Android docs)](https://developer.android.com/training/gestures/viewgroup.html)
+ [Input Events](https://developer.android.com/guide/topics/ui/ui-events.html) (Android docs)
+ [Gestures and Touch Events](https://github.com/codepath/android_guides/wiki/Gestures-and-Touch-Events)

# 来源 
+ [How are Android touch events delivered? - Stack Overflow](https://stackoverflow.com/questions/7449799/how-are-android-touch-events-delivered)
+ [How are Android touch events delivered](https://medium.com/@studymongolian/how-touch-events-are-delivered-in-android-eee3b607b038)