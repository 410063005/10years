# Index

+ [toast](2020-04-08-flutter-toast.md)
+ [box constraints](2020-03-16-box-constraints.md)

---

[声明式UI与命令式UI](https://flutter.dev/docs/get-started/flutter-for/declarative)

![](/images/15864961109147.jpg)

> there is only one code path for any state of the UI. You describe what the UI should look like for any given state, once—and that is it. [ref](https://flutter.dev/docs/development/data-and-backend/state-mgmt/declarative)

什么是状态？

> whatever data you need in order to rebuild your UI at any moment in time [ref](https://flutter.dev/docs/development/data-and-backend/state-mgmt/ephemeral-vs-app)

+ 单个 Widget 中包含的 state 是 Ephemeral state (也称 UI state 或 local state)
+ 多个部分共享的或者多个会话中希望保持的 state 是 App state (shared state)

两种状态的区别并不绝对的。

![](/images/15864969533582.jpg)


## 命令

+ [flutter run](2020-03-16-fluter-run.md)
+ [flutter build](2020-03-16-flutter-build.md)
+ [flutter attach](2020-03-16-flutter-attach.md)

# 难点

+ 边界约束
+ Flutter 为什么性能好

# 精品

[Flutter的绘制流程简述 - 掘金](https://juejin.im/post/5dbed32ee51d456bbe38c6c0)

[The Magic of Flutter（副本） - Google 幻灯片](https://docs.google.com/presentation/d/1zuE2EYRmAecrjDvtRkg0DpwgYCPA1ex02aswbrJcbBA/edit?ouid=108895815075520001467&usp=slides_home&ths=true)

https://renato.athaydes.com/posts/interesting-dart-features.html#quick-dart-overview

# 总结

## 异步编程

### 线程模型

单线程模型

+ Isolate 
    + 内存隔离
    + 事件循环
    + 消息传递
+ 事件循环
    + 一个循环
    + 两个队列

event loop 图

### Future

疑问：阻塞调用不会影响UI吗？如何做到的？

+ 介绍
    + 如何创建 Future
    + 如何使用 Future
    + 如何处理异常
+ 学习基于 Future 的 API

### Stream


# 基础

+ [Element 的生命周期](2020-05-12-element-lifecycyle.md)
+ [Element 树的更新规则](2020-05-12-element-tree-rules.md)
+ [App 的生命周期](https://programmer.help/blogs/acquisition-of-flutter-life-cycle.html)
+ [State 的生命周期](2020-05-12-state-lifecycle.md)

1. RenderObjectToWidgetAdapter.attachToRenderTree 用于处理根 widget 
2. 由 Widget 创建 Element (Element.inflateWidget() 由 Widget 创建新的 Widget 并作为当前 Element 的子节点)
3. performRebuild 调用，performRebuild 调用 StatelessWidget.build() 或 State.build()
    1. 初始时，Element.mount 引起 performRebuild 调用
    2. 状态变化时，State.setState 引起 performRebuild 调用。
4. StatelessWidget.build() 或 State.build() 创建 Widget
5. 由 Widget 创建 Element


# 我的博客

+ [(译) Dart 异步编程之 Isolate 和事件循环 | Sunmoon的博客](https://www.sunmoonblog.com/2019/11/26/dart-async-isolate-eventloop/)