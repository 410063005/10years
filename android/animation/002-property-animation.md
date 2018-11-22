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
+ `ValueAnimator`封装了`TimeInterpolator`以及`TypeEvaluator`，interpolator用于定义动画插值，evaluator用于定义如何计算属性值。
+ 为`ValueAnimator`指定属性的起始值，结束值以及持续时间后调用`start()`启动动画
+ `ValueAnimator`基于'the duration of the animation'和'how much time has elapsed'计算*elapsed fraction* (介于0和1)
+ `TimeInterpolator`基于*elapsed fraction*计算*interpolated fraction*
+ `TypeEvaluator`基于属性起始值，结束值以及*interpolated fraction*计算属性当前值

## 与view animation的区别

+ view动画的限制： view动画只能用于`View`对象，用于非`View`对象时只能自己另写代码。view动画只能使用`View`对象的少量属性，比如缩放和旋转等等，但不能使用背景色等其他属性。
+ view动画的缺点： view动画只是修改了View的绘制位置，而不是View本身。所以使用view动画移动了button后，看起来button的位置变化了，但实际可点击的位置并未改变。

property动画没有上述限制和缺点。它可以用于`View`对象和非`View`对象，而且会真的修改对象本身。

不过view动画代码量少且简单，如果view动画能满足要求就没必要使用property动画。当然也可以视情况混使用view动画和property动画。

## API
property动画有两方面：一是计算动画值，二是将值设置到对象。`ValueAnimator`负责第一项，而第二项是开发者自己完成(通过监听方式获取到已计算好的动画值)

evaluator从animator获取时间值，根据时间值计算属性值。



## 代码分析

## 例子