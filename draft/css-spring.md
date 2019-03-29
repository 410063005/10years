css-spring.md
========
原文地址：https://medium.com/@dtinth/spring-animation-in-css-2039de6e1a03

CSS 动画要么使用 CSS 提供的[默认的时间函数](http://www.w3.org/TR/2013/WD-css3-transitions-20131119/#transition-timing-function-property)，要么使用 [cub](http://cubic-bezier.com/) 创建自定义的时间函数以自定义动画。

但无论如何都无法使用这些基本的时间函数来模拟弹性动画。

本文教你如何使用 CSS 动画，再加上一点物理学和微积分知识来实现弹性动画。

当然，我不是物理学家或数学家。我只学了一些基本知识。所以本文中涉及这些知识时某些描述可能不恰当，甚至是错误的。我只是个 web 开发者。

# 为什么使用 CSS 动画？

# 补间动画基础
假设我想将以动画方式一个盒子从 left: 100px 处移动到 left: 200px 处。这意味着，随着时间推移，我希望这个盒子越来越远离 100px，越来越接近 200px。

我们来详细说明一下：随着动画进行，100px 的占比会减少(从 100% 到 0%)，而与此同时 200px 的占比会变多(从 0% 到 100%)。

我们将 200px 的占比作为动画的 **进度** (Progress)，或者简单称之为 p。所以对于任意指定的 p，都可以使用以下方程计算位置：

left: (1 - p) (100px) + p (200px)

让上面方程更通用一些。比如我们想对一个属性做从 A 值到 B 值进行 tween 操作。在 p 的变化过程中，属性的值如下：

(1 - p)A + p B

也可以将这个方程写成这样：

A + p(B - A)

即线性插值(linear interpolation，或简称为 lerp)。

但现实生活中的动画并不是线性的。比如，你想让动画先缓慢启动，然后加速结束。这也是 easing function 的由来，以便让动画过程不是线性的。

[easing function](http://easings.net/) 建立了 *时间* 跟 *动画进度* 之间的联系。

[an excellent tutorial about easing on mo · js website](http://mojs.io/tutorials/easing/path-easing/) 这篇文章非常不错。

# 物理学
以下是一个弹簧，它一头连接着一个木块。平衡状态下，这个木块的位置是 x = 1。

![](https://cdn-images-1.medium.com/max/1600/1*ao3cRiHW8MTed1WYnqPreA.png)

当你向左边推这个木块时，它的位置变成了 x = 0。

![](https://cdn-images-1.medium.com/max/1600/1*QrKicGZjcV9I04nUNL2nYQ.png)

当停止推动并释放时，弹簧会将木块弹回来，直动重新恢复平衡状态 x = 1。

这个过程跟 easing function 很像。easing function 是一个从 0% 开始，到时 100% 结束的函数。弹簧也是！

现在来介绍物理知识。你很可能学过关于弹力的方程。

首先，[spring force](https://en.wikipedia.org/wiki/Hooke%27s_law#For_linear_springs) 让木块最终回到平衡状态。在这里， X 表示木块到平衡位置的偏移：

Fs = -kX

[damping force](https://en.wikipedia.org/wiki/Damping#Example:_mass.E2.80.93spring.E2.80.93damper) 会减慢弹簧的运动。如果没有阻力，弹簧会一直振荡。

Fd = -cv

最终作用于弹簧上的力：

F = Fs + Fd = -kX - cv

牛顿第二定律告诉我们：

F = ma

简单起见，我们假设 m = 1。因此：

F = -kX - cv

ma = -kX - cv

a = -kX - cv

这里的 X 仍然表示木块到平衡位置的偏移。

这表示如果我们想从位置 x 移动到位置1，我们需要移动 (x - 1) 个单位。从而得到运动方程：

a = -k(x - 1) - cv

(k - 弹簧系数, x - 位置, c - 阻力系数， v - 速度)

# 微积分

如果学过简单的微积分，我们知道可以将一个物体的位置表示成时间的函数。

x = f(t)

位置对时间进行微分，得到速度：

v = dx / dt = f'(t)

速度对时间进行微分，得到加速度：

a = dv / dt = f''(t)

还记得前一节的运动方程：

a = -k(x - 1) - cv

替换其中的 x, v, a，得到以下方程：

f''(t) = -k(f(t) - 1) - c f'(t)

这是一个偏微分方程。求解该方程就是找出一个能满足该方程的 f(t) 函数。

只凭我浅薄的微积分知识是解不了这个方程的。我求助于 [Wolfram|Alpha](http://www.wolframalpha.com/)。

但使用 Wolfram|Alpha 前我们再对上述方面加一些限制让其简单一点：

木块开始运动前，它的位置是 x = 0。这是初始位置，即 t = 0 时刻的位置。因此

f(0) = 0

木块开始运行前，它的速度是 0。因此

f'(0) = 0

不过，如果 k 和 c 是变量的话，Wolfram|Alpha 无法解这个方程。所以需要给它们设置值。

从 react-motion 库的 [the wobble preset](https://github.com/chenglou/react-motion/blob/e93a3ce153a55f9b0f55cb594cbc4c0e3065f5a0/src/presets.js) 找到两个参考值。stiffness of k = 180，damping factor of c = 12，所以有下列方程组：

f(0) = 0

f'(0) = 0

f''(t) = -180 (f(t) - 1) - 12 f'(t)

在 Wolfram|Alpha 中求解方程：

f(0) = 0; f'(0) = 0; f''(t) = -180(f(t) - 1) - 12f'(t)

它给出这个解：

![](https://cdn-images-1.medium.com/max/1600/1*D_LHFgg2Xrt4kSi3WWidbw.png)

我不知道这其中的 ½, 6, sin 以及 cos 哪里来的。但看起来是正确的，因为 sin 和 cos 感觉像是振荡运动，就像弹簧一样。

通过从 t = 0 到 t = 1 取样(step 为 0.01)，将值绘制出来得到如下图形：

![](https://cdn-images-1.medium.com/max/1600/1*UrfQ6M96lBCHVf_ArhJfMA.png)

# CSS 动画

你不能在 CSS 中使用这么复杂的方程，但可以生成一些接近这个方程的 CSS。CSS 动画中只能指定具体的 keyframes，但我们的方程是个连续方程。

就像上面绘图一样，这一回不是绘图而是将值放到 CSS 的关键帧。比如，如果动画会持续1秒，CSS 可以这样写：

```
@keyframes keyframe-name {
  0%   { /* css code for t=0.00s */ }
  1%   { /* css code for t=0.01s */ }
  2%   { /* css code for t=0.02s */ }
  3%   { /* css code for t=0.03s */ }
         /* ... */
  99%  { /* css code for t=0.99s */ }
  100% { /* css code for t=1.00s */ }
}
```

显然，手工写这些代码是不现实的。我们可以使用 CSS 预处理语言来生成。我使用的是 [Stylus](https://learnboost.github.io/stylus/)

首先，基于 Wolfram|Alpha 的结果写一个 Stylus 函数：

```
spring-wobbly(t)
  return -0.5 * (2.71828 ** (-6 * t)) * (
    -2 * (2.71828 ** (6 * t)) + sin(12 * t) + 2 * cos(12 * t))
```

还记得补间动画基础那一节中我们讲到的插值函数方程吗。接下来写一个函数用于在两个值之间进行插值：

```
lerp(a, b, p)
  return a + p * (b - a)
```

接下来，生成关键帧：

```
@keyframes move
  for i in (0..100)
    {i + '%'}
      t = i / 100
      p = spring-wobbly(t)
      left: lerp(100px, 200px, p)
```

Stylus 会生成 CSS 代码：

```
@keyframes move {
  0%   { left: 100px; }
  1%   { left: 100.86376425736435px; }
  ...

  99%  { left: 199.8797988721998px; }
  100% { left: 199.8573305048547px; }
}
```

最后，你可以使用这个 CSS 代码。要确保你使用的时间函数是线性函数：

```
.box {
  animation: 1s move linear;
  animation-fill-mode: both;
}
```

# 自定义

你还可以进一步自定义动画。比如，你想尝试使用不同的 stiffness 和 damping factor。你还可以尝试修改 f(0) 的值。f(0) 为负值时，盒子会先往反方向弹。

也可以使用不同参数的同时修改动画时间，让动画更快或更慢。也可以将 bouncing 动画跟弹性动画比较。

结束。

以上是是物理知识和微积分知识在设计和编程开发中的简单应用。

当我学习软件工程时，就像其他工程师一样，我们不得不学习基本的微积分和物理。常常听到有人说：我学的是软件工程，学物理和微积分干嘛？我用得到它们吗？

在我看来，无论你是否知道如何解偏微分方程或复杂的物理问题，你可以很容易地使用 Google 或 Wolfram|Alpha 来解决这些问题。

我认为重要的是你是否有能力去使用这些知识解决那些你感兴趣的问题。