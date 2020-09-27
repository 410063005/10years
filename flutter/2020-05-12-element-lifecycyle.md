# Element 生命周期

+ create - `Widget.createElement()`, 类似 Activity.onCreate()
+ mount - `Element.mount()`, 类似 Activity.onStart()
    + 将当前 Element 的 Render Object 关联到 Render Tree 
+ update - `Element.update()`, 
+ deactivate - `Element.deactivate()`, 类似 Activity.onPause()
+ activate - `Element.activate()`, 类似 Activity.onResume()
+ unmount - `Element.unmount()`, 类似 Activity.onStop()

_ElementLifecycle 定义了如下四个生命周期状态：

```dart
enum _ElementLifecycle {
  initial,
  active,
  inactive,
  defunct,
}
```

生命周期状态的流转过程如下：

![](/images/15892545393945.jpg)

+ Element 首次进入 active 状态是调用 `mount()` 而不是 `activate()`
+ Element 调用 `unmount()` 后进入 defunct 状态，之后无法再次进入 active 状态

unmount 后的 Element 由 `inactive` 进入 `defunct` 生命周期状态，由 `_InactiveElements` 统一管理

```dart
class _InactiveElements {
  bool _locked = false;
  final Set<Element> _elements = HashSet<Element>();

  void _unmount(Element element) {
    assert(element._debugLifecycleState == _ElementLifecycle.inactive);
    ...
    element.visitChildren((Element child) {
      assert(child._parent == element);
      _unmount(child);
    });
    element.unmount();
    assert(element._debugLifecycleState == _ElementLifecycle.defunct);
  }
}
```

## mount

`mount()` 将当前 Element 加入到 Element Tree 中指定的 slot 中。源码如下：

```dart
  @mustCallSuper
  void mount(Element parent, dynamic newSlot) {
    ...
    _parent = parent;
    _slot = newSlot;
    _depth = _parent != null ? _parent.depth + 1 : 1;
    // 修改 active 状态及生命周期状态
    _active = true;
    if (parent != null) // Only assign ownership if the parent is non-null
      _owner = parent.owner;
    }
    ...
    _debugLifecycleState = _ElementLifecycle.active;
  }
```

## update

`update()` 方法更新当前 Element 关联的 Widget。源码如下：

```dart
  @mustCallSuper
  void update(covariant Widget newWidget) {
    assert(
        // 检查当前是否处于 _ElementLifecycle.active 状态
        _debugLifecycleState == _ElementLifecycle.active
        ...
        && _active
        && Widget.canUpdate(widget, newWidget));
    _widget = newWidget;
  }
```

注意：

+ `update()` 仅在 Element 为 active 时被调用
+ `update()` 仅在 widget 和 newWidget 类型相同(见 `Widget.canUpdate()`)时被调用

## deactivate

`deactivate()` 方法将 Element 从 active 状态修改成 inactive 状态。`deactivate()` 实际是由 `deactivateChild()` 调用。

```dart
  @protected
  void deactivateChild(Element child) {
    ...
    child._parent = null;
    child.detachRenderObject();
    owner._inactiveElements.add(child); // this eventually calls child.deactivate()
    ...
  }
  
  @mustCallSuper
  void deactivate() {
    // 检查当前是否处于 _ElementLifecycle.active 状态
    assert(_debugLifecycleState == _ElementLifecycle.active);
    ...
    // 清理 InheritedElement 和 InheritedWidget
    if (_dependencies != null && _dependencies.isNotEmpty) {
      for (InheritedElement dependency in _dependencies)
        dependency._dependents.remove(this);
      ...
    }
    _inheritedWidgets = null;
    // 修改 active 状态及生命周期状态
    _active = false;
    ...
    _debugLifecycleState = _ElementLifecycle.inactive;
  }
```

## activate

`activate()` 方法将 Element 从 inactive 状态修改成 active 状态

```dart
  @mustCallSuper
  void activate() {
    // 检查当前是否处于 _ElementLifecycle.inactive 状态
    assert(_debugLifecycleState == _ElementLifecycle.inactive);
    ...
    final bool hadDependencies = (_dependencies != null && _dependencies.isNotEmpty) || _hadUnsatisfiedDependencies;
    _active = true;
    _dependencies?.clear();
    _hadUnsatisfiedDependencies = false;
    _updateInheritance();
    
    _debugLifecycleState = _ElementLifecycle.active;
    
    if (_dirty)
      owner.scheduleBuildFor(this);
    if (hadDependencies)
      didChangeDependencies();
```

## unmount

```dart
  @mustCallSuper
  void unmount() {
    // 检查当前是否处于 _ElementLifecycle.inactive 状态
    assert(_debugLifecycleState == _ElementLifecycle.inactive);
    ...
    if (widget.key is GlobalKey) {
      final GlobalKey key = widget.key;
      key._unregister(this);
    }
    assert(() {
      _debugLifecycleState = _ElementLifecycle.defunct;
      return true;
    }());
  }
```