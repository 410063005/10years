
---

Reduce overdraw
An app may draw the same pixel more than once within a single frame, an event called overdraw. Overdraw is usually unnecessary, and best eliminated. It manifests itself as a performance problem by wasting GPU time to render pixels that don't contribute to what the user sees on the screen.

This document explains overdraw: what it is, how to diagnose it, and actions you can take to eliminate or mitigate it.

About overdraw
Overdraw refers to the system's drawing a pixel on the screen multiple times in a single frame of rendering. For example, if we have a bunch of stacked UI cards, each card hides a portion of the one below it.

However, the system still needs to draw even the hidden portions of the cards in the stack. This is because stacked cards are rendered according to the painter's algorithm: that is, in back-to-front order. This sequence of rendering allows the system to apply proper alpha blending to translucent objects such as shadows.

Find overdraw problems
The platform offers the following tools to help you determine if overdraw is affecting your app's performance.

Debug GPU overdraw tool
The Debug GPU Overdraw tool uses color-coding to show the number of times your app draws each pixel on the screen. The higher this count, the more likely it is that overdraw affects your app's performance.

For more information, see how to visualize GPU overdraw.

Profile GPU rendering tool
The Profile GPU Rendering tool displays, as a scrolling histogram, the time each stage of the rendering pipeline takes to display a single frame. The Process part of each bar, indicated in orange, shows when the system is swapping buffers; this metric provides important clues about overdraw.

On less performant GPUs, available fill-rate (the speed at which the GPU can fill the frame buffer) can be quite low. As the number of pixels required to draw a frame increases, the GPU may take longer to process new commands, and ask the rest of the system to wait until it can catch up. The Process bar shows that this spike happens as the GPU gets overwhelmed trying to draw pixels as fast as possible. Issues other than raw numbers of pixels may also cause this metric to spike. For example, if the Debug GPU Overdraw tool shows heavy overdraw and Process spikes, there's likely an issue with overdraw.

For more information, see how to profile GPU rendering speed.

Note: The Profile GPU Rendering tool does not work with apps that use the NDK. This is because the system pushes framework messages to the background whenever OpenGL takes a full-screen context. In such cases, you may find a profiling tool provided by the GPU manufacturer helpful.

Fix overdraw
There are several strategies you can pursue to reduce or eliminate overdraw:

Removing unneeded backgrounds in layouts.
Flattening the view hierarchy.
Reducing transparency.
This section provides information about each of these approaches.

Remove unneeded backgrounds in layouts
By default, a layout does not have a background, which means it does not render anything directly by itself. When layouts do have backgrounds, however, they may contribute to overdraw.

Removing unnecessary backgrounds is a quick way of improving rendering performance. An unnecessary background may never be visible because it's completely covered by everything else the app is drawing on top of that view. For example, the system may entirely cover up a parent's background when it draws child views on top of it.

To find out why you're overdrawing, walk through the hierarchy in the Layout Inspector tool. As you do so, look out for any backgrounds you can eliminate because they are not visible to the user. Cases where many containers share a common background color offer another opportunity to eliminate unneeded backgrounds: You can set the window background to the main background color of your app, and leave all of the containers above it with no background values defined.

Flatten view hierarchy
Modern layouts make it easy to stack and layer views to produce beautiful design. However, doing so can degrade performance by resulting in overdraw, especially in scenarios where each stacked view object is opaque, requiring the drawing of both seen and unseen pixels to the screen.

If you encounter this sort of issue, you may be able to improve performance by optimizing your view hierarchy to reduce the number of overlapping UI objects. For more information about how to accomplish this, see Optimize view hierarchies.

Reduce transparency
Rendering of transparent pixels on screen, known as alpha rendering, is a key contributor to overdraw. Unlike standard overdraw, in which the system completely hides existing drawn pixels by drawing opaque pixels on top of them, transparent objects require existing pixels to be drawn first, so that the right blending equation can occur. Visual effects like transparent animations, fade-outs, and drop shadows all involve some sort of transparency, and can therefore contribute significantly to overdraw. You can improve overdraw in these situations by reducing the number of transparent objects you render. For example, you can get gray text by drawing black text in a TextView with a translucent alpha value set on it. But you can get the same effect with far better performance by simply drawing the text in gray.

To learn more about performance costs that transparency imposes throughout the entire drawing pipeline, watch the video Hidden Costs of Transparency.

[Reduce overdraw  |  Android Developers](https://developer.android.com/topic/performance/rendering/overdraw)

---



Performance and view hierarchies
The way you manage the hierarchy of your View objects can have a substantial impact on your app’s performance. This page describes how to assess whether your view hierarchy is slowing your app down, and offers some strategies for addressing issues that may arise.

Layout and measure performance
The rendering pipeline includes a layout-and-measure stage, during which the system appropriately positions the relevant items in your view hierarchy. The measure part of this stage determines the sizes and boundaries of View objects. The layout part determines where on the screen to position the View objects.

Both of these pipeline stages incur some small cost per view or layout that they process. Most of the time, this cost is minimal and doesn’t noticeably affect performance. However, it can be greater when an app adds or removes View objects, such as when a RecyclerView object recycles them or reuses them. The cost can also be higher if a View object needs to consider resizing to main its constraints: For example, if your app calls SetText() on a View object that wraps text, the View may need to resize.

If cases like these take too long, they can prevent a frame from rendering within the allowed 16ms, so that frames are dropped, and animation becomes janky.

Because you cannot move these operations to a worker thread—your app must process them on the main thread—your best bet is to optimize them so that they can take as little time as possible.

Manage complexity: layouts matter
Android Layouts allow you to nest UI objects in the view hierarchy. This nesting can also impose a layout cost. When your app processes an object for layout, the app performs the same process on all children of the layout as well. For a complicated layout, sometimes a cost only arises the first time the system computes the layout. For instance, when your app recycles a complex list item in a RecyclerView object, the system needs to lay out all of the objects. In another example, trivial changes can propagate up the chain toward the parent until they reach an object that doesn’t affect the size of the parent.

The most common case in which layout takes an especially long time is when hierarchies of View objects are nested within one another. Each nested layout object adds cost to the layout stage. The flatter your hierarchy, the less time that it takes for the layout stage to complete.

If you are using the RelativeLayout class, you may be able to achieve the same effect, at lower cost, by using nested, unweighted LinearLayout views instead. Additionally, if your app targets Android 7.0 (API level 24), it is likely that you can use a special layout editor to create a ConstraintLayout object instead of RelativeLayout. Doing so allows you to avoid many of the issues this section describes. The ConstraintLayout class offers similar layout control, but with much-improved performance. This class uses its own constraint-solving system to resolve relationships between views in a very different way from standard layouts.

Double taxation
Typically, the framework executes the layout or measure stage in a single pass and quite quickly. However, with some more complicated layout cases, the framework may have to iterate multiple times on parts of the hierarchy that require multiple passes to resolve before ultimately positioning the elements. Having to perform more than one layout-and-measure iteration is referred to as double taxation.

For example, when you use the RelativeLayout container, which allows you to position View objects with respect to the positions of other View objects, the framework performs the following actions:

Executes a layout-and-measure pass, during which the framework calculates each child object’s position and size, based on each child’s request.
Uses this data, also taking object weights into account, to figure out the proper position of correlated views.
Performs a second layout pass to finalize the objects’ positions.
Goes on to the next stage of the rendering process.
The more levels your view hierarchy has, the greater the potential performance penalty.

Containers other than RelativeLayout may also give rise to double taxation. For example:

A LinearLayout view could result in a double layout-and-measure pass if you make it horizontal. A double layout-and-measure pass may also occur in a vertical orientation if you add measureWithLargestChild, in which case the framework may need to do a second pass to resolve the proper sizes of objects.
The GridLayout has a similar issue. While this container also allows relative positioning, it normally avoids double taxation by pre-processing the positional relationships among child views. However, if the layout uses weights or fill with the Gravity class, the benefit of that preprocessing is lost, and the framework may have to perform multiple passes if it the container were a RelativeLayout.
Multiple layout-and-measure passes are not, in themselves, a performance burden. But they can become so if they’re in the wrong spot. You should be wary of situations where one of the following conditions applies to your container:

It is a root element in your view hierarchy.
It has a deep view hierarchy beneath it.
There are many instances of it populating the screen, similar to children in a ListView object.
Diagnose view hierarchy issues
Layout performance is a complex problem with many facets. There are a couple of tools that can give you solid indications about where performance bottlenecks are occurring. A few other tools provide less definitive information, but can also provide helpful hints.

Systrace
One tool that provides excellent data about performance is Systrace, which is built into the Android SDK. The Systrace tool allows you to collect and inspect timing information across an entire Android device, allowing you to see when layout performance problems cause performance problems. For more information about Systrace, see Analyze UI performance with Systrace.

Profile GPU rendering
The other tool most likely to provide you with concrete information about performance bottlenecks is the on-device Profile GPU rendering tool, available on devices powered by Android 6.0 (API level 23) and later. This tool allows you to see how long the layout-and-measurestage is taking for each frame of rendering. This data can help you diagnose runtime performance issues, and help you determine what, if any layout-and-measure issues you need to address.

In its graphical representation of the data it captures, Profile GPU rendering uses the color blue to represent layout time. For more information about how to use this tool, see Profile GPU Rendering Walkthrough.

Lint
Android Studio’s Lint tool can help you gain a sense of inefficiencies in the view hierarchy. To use this tool, select Analyze > Inspect Code, as shown in Figure 1.


Figure 1. Locating Inspect Code in the Android Studio.

Information about various layout items appears under Android > Lint > Performance. To see more detail, you can click on each item to expand it, and see more information in the pane on the right side of the screen. Figure 2 shows an example of such a display.


Figure 2. Viewing information about specific issues that the lint tool has identified.

Clicking on one of these items reveals, in the pane to the right, the problem associated with that item.

To understand more about specific topics and issues in this area, see the Lint documentation.

Layout Inspector
Android Studio’s Layout Inspector tool provides a visual representation of your app’s view hierarchy. It is a good way to navigate the hierarchy of your app, providing a clear visual representation of a particular view’s parent chain, and allowing you to inspect the layouts that your app constructs.

The views that Layout Inspector presents can also help identify performance problems arising from double taxation. It can also provide an easy way for you to identify deep chains of nested layouts, or layout areas with a large amount of nested children, another potential source of performance costs. In these scenarios, the layout-and-measure stages can be particularly costly, resulting in performance issues.

For more information, see Debug your layout with layout inspector.

Solve view hierarchy issues
The fundamental concept behind solving performance problems that arise from view hierarchies is simple in concept, but more difficult in practice. Preventing view hierarchies from imposing performance penalties encompasses the dual goals of flattening your view hierarchy and reducing double taxation. This section discusses some strategies for pursuing these goals.

Remove redundant nested layouts
Developers often use more nested layouts than necessary. For example, a RelativeLayout container might contain a single child that is also a RelativeLayout container. This nesting amounts to redundancy, and adds unnecessary cost to the view hierarchy.

Lint can often flag this problem for you, reducing debugging time.

Adopt merge/include
One frequent cause of redundant nested layouts is the <include> tag. For example, you may define a re-usable layout as follows:

<LinearLayout>
    <!-- some stuff here -->
</LinearLayout>
And then an include tag to add this item to the parent container:

<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:orientation="vertical"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="@color/app_bg"
    android:gravity="center_horizontal">

    <include layout="@layout/titlebar"/>

    <TextView android:layout_width="match_parent"
              android:layout_height="wrap_content"
              android:text="@string/hello"
              android:padding="10dp" />

    ...

</LinearLayout>
The include unnecessarily nests the first layout within the second layout.

The merge tag can help prevent this issue. For information about this tag, see Re-using layouts with <include>.

Adopt a cheaper layout
You may not be able to adjust your existing layout scheme so that it doesn’t contain redundant layouts. In certain cases, the only solution may be to flatten your hierarchy by switching over to an entirely different layout type.

For example, you may find that a TableLayout provides the same functionality as a more complex layout with many positional dependencies. In the N release of Android, the ConstraintLayout class provides similar functionality to RelativeLayout, but at a significantly lower cost.

[Performance and view hierarchies  |  Android Developers](https://developer.android.com/topic/performance/rendering/optimizing-view-hierarchies)

---



ListView 的 ViewHolder模式。它的优点：

+ 减少 View 的创建
+ 减少 `findViewById()` 的调用

减少View。常见的策略包括：

+ `TextView` 和 `ImageView` 合并
+ 使用custom state。例子见[view-reduction](https://sriramramani.wordpress.com/2013/03/25/view-reduction/)，这个例子将一个复杂布局优化成只使用一个 `TextView`




