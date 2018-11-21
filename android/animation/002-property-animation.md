# 理解property animation

property animation包括以下属性：

+ duration - 动画持续时间。默认为300ms
+ time interpolation - 指定如何根据动画的当前时间来计算属性值
+ repeat count & behavior - 指定动画结束后是否重复，重复次数，以及重复方式
+ animator sets - 将多个动画分为一组
+ frame refresh rate - 帧刷新率。指定动画帧的刷新率，默认是10ms刷新一次。(最终刷新率以系统为准)

## 工作原理

![](valueanimator.png)

上图显示了是如何计算`ValueAnimator`的。

+ `ValueAnimator`对象记录了动画运行时间，以及属性当前的值。
+ `ValueAnimator`封装了`TimeInterpolator`，interpolator用于定义动画插值。