# Drawable

Drawable具有轻量级的、高效性、复用性强的特点。

+ [LoadingDrawable](https://github.com/dinuscxj/LoadingDrawable) 包含许多酷炫的加载动画
+ [ProgressBarWidthNumber](https://github.com/hongyangAndroid/Android-ProgressBarWidthNumber) 相关[博文](https://blog.csdn.net/lmj623565791/article/details/43371299)



# 常用的 Drawable

与view相比，drawable不可交互

+ BitmapDrawable
+ DrawableWrapper
 - ScaleDrawable
 - RotateDrawable
 - ? TranslateDrawable
+ DrawableContainer
 - AnimationDrawable
 - StateListDrawable
 - LevelListDrawable
+ LayerDrawable

ColorDrawable 是最简单的Drawable

BitmapDrawable 比 ColorDrawable 要复杂。它包含以下两个属性:

+ gravity (学习Gravity.apply()的用法)
+ tileMode


# 不常用的 Drawable

+ PictureDrawable api 23 added  drawing a sequence from a picture can be faster than the equivalent API 这个类是为了更好的性能？
+ PaintDrawable

# 自定义 Drawable

+ [Custom Drawable — Part 1 – Rey Pham – Medium](https://medium.com/@rey5137/custom-drawable-part-1-6fb26bb25690)
+ [Custom Drawable — Part 2 – Rey Pham – Medium](https://medium.com/@rey5137/custom-drawable-part-2-33a2671f9bbc) 自己实现StateBorderDrawable时遇到问题了，发现state change不生效。给view添加`clickable=true`和`focusable=true`后正常
+ [Custom Drawable — Part 3 – Rey Pham – Medium](https://medium.com/@rey5137/custom-drawable-part-3-b7adfd97d0b3)

[TranslateDrawable](https://github.com/410063005/TranslateDrawable) 是我实现的一个自定义Drawable，它参考了:

+ RotateDrawable和ScaleDrawable
+ AnimationDrawable和LayerDrawable

坑: 一定要实现setVisible()方法

TranslateDrawable内存占用优于AnimationDrawable的原因，因为生成了更少的drawable!

源码见 [github](https://github.com/410063005/TranslateDrawable)

# selector

[利用 Tint 属性优化 Selector - Android - 掘金](https://juejin.im/entry/59426875ac502e006c6021bd)

# Drawable 与 Activity 的关系

[Android Drawable 详解 - 牧秦丶 - CSDN博客](https://blog.csdn.net/arnozhang12/article/details/52621191)

> View实现了Drawable.Callback。当drawable.invalidateSelft时，会最终通知给View，让view来把控draw()过程