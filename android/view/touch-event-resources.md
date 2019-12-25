
# TouchDelegate

[TouchDelegate](https://developer.android.com/reference/android/view/TouchDelegate.html) 是学习 `onTouchEvent()` 的一个好例子。

# 手势

[gestures](https://developer.android.com/training/gestures)

# GestureDetector

[GestureDetector](https://developer.android.com/reference/android/view/GestureDetector.html) 是学习 `onTouchEvent()` 的另一个好例子。

[Scroller](https://developer.android.com/training/custom-views/making-interactive.html) 常常跟 GestureDetector 一起使用。

这个类常常用于在自定义 View 中用于探测用户手势。

# Scroller

[Scroller](https://developer.android.com/reference/android/widget/Scroller.html)

[OverScroller](https://developer.android.com/reference/android/widget/OverScroller.html)

理解相关术语。

"Scrolling" is a word that can take on different meanings in Android, depending on the context.

Scrolling is the general process of moving the viewport (that is, the 'window' of content you're looking at). When scrolling is in both the x and y axes, it's called panning. The sample application provided with this class, InteractiveChart, illustrates two different types of scrolling, dragging and flinging:

Dragging is the type of scrolling that occurs when a user drags her finger across the touch screen. Simple dragging is often implemented by overriding onScroll() in GestureDetector.OnGestureListener. For more discussion of dragging, see Dragging and Scaling.
Flinging is the type of scrolling that occurs when a user drags and lifts her finger quickly. After the user lifts her finger, you generally want to keep scrolling (moving the viewport), but decelerate until the viewport stops moving. Flinging can be implemented by overriding onFling() in GestureDetector.OnGestureListener, and by using a scroller object. This is the use case that is the topic of this lesson.
It's common to use scroller objects in conjunction with a fling gesture, but they can be used in pretty much any context where you want the UI to display scrolling in response to a touch event. For example, you could override onTouchEvent() to process touch events directly, and produce a scrolling effect or a "snapping to page" animation in response to those touch events.

[Animate a scroll gesture](https://developer.android.com/training/gestures/scroll#java) 这篇很基础，信息量也很大。它介绍了 scroller 的用法。

[ViewPager](http://github.com/android/platform_frameworks_support/blob/master/v4/java/androidx/viewpager/widget/ViewPager.java) 也使用到了 scroller，可以作为学习资料。

# ScrollView

[ScrollView](https://developer.android.com/reference/android/widget/ScrollView.html)

# [VelocityTracker](https://developer.android.com/reference/android/view/VelocityTracker.html)

VelocityTracker helps you track the velocity of touch events. This is useful for gestures in which velocity is part of the criteria for the gesture, such as a fling.

# Drag and scale

https://developer.android.com/training/gestures/scale

# Snap

RecyclerView 的 Snap 效果。

# 参考

[View](https://developer.android.com/reference/android/view/View)

[Manage touch events in a ViewGroup](https://developer.android.com/training/gestures/viewgroup)

[Making the View Interactive](https://developer.android.com/training/custom-views/making-interactive.html)