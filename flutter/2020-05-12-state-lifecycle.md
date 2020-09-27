# State 生命周期

+ 初始化阶段
    + 创建 - `State()`
    + 初始化 - `State.initState()`
    + 依赖变化 - `State.didChangeDependencies()`
+ 状态变化阶段
    + `State.didUpdateWidget()`
+ 析构阶段
    + deactivate - `State.deactivate()`
    + dispose - `State.dispose()`

_StateLifecycle 定义了如下四个生命周期状态：

```dart
/// Tracks the lifecycle of [State] objects when asserts are enabled.
enum _StateLifecycle {
  /// The [State] object has been created. [State.initState] is called at this
  /// time.
  created,

  /// The [State.initState] method has been called but the [State] object is
  /// not yet ready to build. [State.didChangeDependencies] is called at this time.
  initialized,

  /// The [State] object is ready to build and [State.dispose] has not yet been
  /// called.
  ready,

  /// The [State.dispose] method has been called and the [State] object is
  /// no longer able to build.
  defunct,
}
```

生命周期状态的流转过程如下：

![](/images/15893703947409.jpg)


![](/images/15892718879319.jpg)

三个虚线框分别代表：

+ 初始化阶段 - 添加到 Render Tree
+ 状态变化阶段 - 在 Render Tree 中显示
+ 析构阶段 - 从 Render Tree 中移除

## 创建

通常由 `StatefulWidget.createState()` 方法调用 State 的构造方法来生成实例。自定义的 State 继承自 `State` 类。

```dart
class StateLifecyclePage extends StatefulWidget {
  const StateLifecyclePage({
    Key key,
  }) : super(key: key);

  @override
  _StateLifecyclePageState createState() => _StateLifecyclePageState();
}

class _StateLifecyclePageState extends State<StateLifecyclePage> {
  Widget build(BuildContext context) { return ...; }
}
```

## initState

+ 调用次数：仅1次
+ 调用时机：将对象加入到树中时调用
+ 适用场景：初始化(如订阅 ChangeNotifier)

```dart
  @protected
  @mustCallSuper
  void initState() {
    assert(_debugLifecycleState == _StateLifecycle.created);
  }
```

## didChangeDependencies

+ 调用次数：多次
+ 调用时机
    + 调用 `initState()` 之后会马上调用 `didChangeDependencies()`
    + Widget 中的 InheritedWidget 变化后会调用 `didChangeDependencies()`
+ 适用场景：一般不需要处理。少数场景下可能在这个方法中进行 network fetch

```dart
  @protected
  @mustCallSuper
  void didChangeDependencies() { }
```

## didUpdateWidget

+ 调用次数：多次
+ 调用时机：widget configuration changes (简单来说调用 `State.setState()` 后通常会引起调用 `didUpdateWidget`)
+ 调用关系：系统调用 `didUpdateWidget()` 会通常会调用 `build()`
+ 适用场景：响应 widget 的变化，例如播放动画

```dart
  @mustCallSuper
  @protected
  void didUpdateWidget(covariant T oldWidget) { }
```

## deactivate

+ 调用次数：多次
+ 调用时机：从树中移除 State 对象时调用
+ 适用场景：一般用于清理当前对象跟树中其他对象的关联。注意这个方法通常不用于清理 State 的资源，清理资源建议放在 dispose 中，原因是从树中移除的 State 对象可能再次加入到树中

```dart
  @protected
  @mustCallSuper
  void deactivate() { }
```

## dispose

+ 调用次数：1次
+ 调用时机：从树中永乐移除 State 对象时调用
+ 适用场景：清理 State 的资源，比如停止动画。注意这个方法中不能再调用 `setState`

# 坑

## 调用限制

`initState()` 中不能使用 `BuildContext.dependOnInheritedWidgetOfExactType` 方法

可以在 `didChangeDependencies` 使用 `BuildContext.dependOnInheritedWidgetOfExactType` 方法

## 订阅与取消订阅

```
  /// If a [State]'s [build] method depends on an object that can itself
  /// change state, for example a [ChangeNotifier] or [Stream], or some
  /// other object to which one can subscribe to receive notifications, then
  /// be sure to subscribe and unsubscribe properly in [initState],
  /// [didUpdateWidget], and [dispose]:
  ///
  ///  * In [initState], subscribe to the object.
  ///  * In [didUpdateWidget] unsubscribe from the old object and subscribe
  ///    to the new one if the updated widget configuration requires
  ///    replacing the object.
  ///  * In [dispose], unsubscribe from the object.
  ///
```

# StatefulElement

State 的生命周期方法都是空方法，这些方法由子类实现。State 的生命周期方法由 `StatefulElement` 调用

```
ComponentElement.mount

ComponentElement._firstBuild

ComponentElement.rebuild

ComponentElement.performRebuild

StatelessWidget.build 或 State.build
```

# 参考

[Flutter in the life cycle Widget - Code World](https://www.codetd.com/en/article/6486955)

![-w995](/images/15895254679272.jpg)

