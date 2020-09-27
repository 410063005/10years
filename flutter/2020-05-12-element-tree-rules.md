# Element 树的更新规则

## BuildOwner

BuildOwner 用于管理需要重建的 Widget。主要的 BuildOwner 通常由 WidgetsBinding 所有。

BuildOwner 有一个 dirty element 列表，`scheduleBuildFor()` 方法将 element 添加到 dirty element 列表。

BuildOwner `buildScope()` 方法清空 dirty element 列表并建立一个用于更新 widget tree 的 scope。

```dart
/// Manager class for the widgets framework.
///
/// This class tracks which widgets need rebuilding
///
/// The main build owner is typically owned by the [WidgetsBinding], and is
/// driven from the operating system along with the rest of the
/// build/layout/paint pipeline.
class BuildOwner {
  final _InactiveElements _inactiveElements = _InactiveElements();

  // dirty element 列表
  final List<Element> _dirtyElements = <Element>[];
  bool _scheduledFlushDirtyElements = false;
  
  // 将 element 添加到 dirty element 列表
  /// Adds an element to the dirty elements list so that it will be rebuilt
  /// when [WidgetsBinding.drawFrame] calls [buildScope].
  void scheduleBuildFor(Element element) {}
  
  /// Establishes a scope for updating the widget tree, and calls the given
  /// `callback`, if any
  void buildScope(Element context, [ VoidCallback callback ]) {}
    
  // hot reload 支持
  /// Cause the entire subtree rooted at the given [Element] to be entirely
  /// rebuilt. This is used by development tools when the application code has
  /// changed and is being hot-reloaded, to cause the widget tree to pick up any
  /// changed implementations.
  ///
  /// This is expensive and should not be called except during development.
  void reassemble(Element root) {}
}
```

精简后的 `buildScope()` 代码如下：

```dart
  void buildScope(Element context, [ VoidCallback callback ]) {
    // 没有 callback 或 dirty element 列表为空时直接返回
    if (callback == null && _dirtyElements.isEmpty)
      return;
    // Timeline 记录开始
    Timeline.startSync('Build', arguments: timelineWhitelistArguments);
    try {
      _scheduledFlushDirtyElements = true;
      // callback 回调
      if (callback != null) {
        try {
          callback();
        } finally {
          ...
        }
      }
      // 对 dirty element 列表排序
      _dirtyElements.sort(Element._sort);
      _dirtyElementsNeedsResorting = false;
      int dirtyCount = _dirtyElements.length;
      // 处理 dirty element 列表，对每个 element 分别调用 rebuild
      int index = 0;
      while (index < dirtyCount) {
        try {
          _dirtyElements[index].rebuild();
        } catch (e, stack) {
          ...
        }
        index += 1;
        
        // 处理一种特殊情况：原生处于 inactive 状态的 widget 进入 dirty element 列表
        // 具体代码这里忽略
        // It is possible for previously dirty but inactive widgets to move right in the list.
        // We therefore have to move the index left in the list to account for this.
        ...
      }
    } finally {
      // 清空 dirty element 列表
      for (Element element in _dirtyElements) {
        assert(element._inDirtyList);
        element._inDirtyList = false;
      }
      _dirtyElements.clear();
      _scheduledFlushDirtyElements = false;
      _dirtyElementsNeedsResorting = null;
      // Timeline 记录结束
      Timeline.finishSync();
    }
  }
```

## 时序

![-w942](/images/15892695294899.jpg)

简单来说，调用 `setState()` 会将 Element 设置为 dirty。Element 会进入由 BuildOwner 管理的 dirty element 列表。当下次 vsync 信号触发 `WidgetsBinding.drawFrame()` 时，最终会调用 `Element.performRebuild()` 进行重建。

---

```
ComponentElement.performRebuild

Element.updateChild

Element.inflateWidget

Widget.createElement

Element.mount

```

## 更新规则

+ 新 widget 为 null，删除子树，流程结束
+ 新老 widget 是同一个，无需处理，流程结束
+ 新老 widget 不是同一个，但支持 update，流程结束
+ 其他情形，mount 新子树。注意：若原先子树不为 null，还要先删除子树

```dart
  @protected
  Element updateChild(Element child, Widget newWidget, dynamic newSlot) {
    ...
    
    // 1、新 widget 为 null (即 Widget.build() 返回 null)
    // 执行 deactivate child，即删除子树，流程结束
    if (newWidget == null) {
      if (child != null)
        deactivateChild(child);
      return null;
    }
    if (child != null) {
      if (child.widget == newWidget) {
        if (child.slot != newSlot)
          updateSlotForChild(child, newSlot);
        // 2、新老 widget 是同一个，流程结束
        return child;
      }
      if (Widget.canUpdate(child.widget, newWidget)) {
        if (child.slot != newSlot)
          updateSlotForChild(child, newSlot);
        // 3、新老 widget 不是同一个，但可更新，流程结束
        child.update(newWidget);
        ...
        return child;
      }
      // 4.1、其他情形，删除子树
      deactivateChild(child);
    }
    // 4.2、mount 新子树，流程结束
    return inflateWidget(newWidget, newSlot);
  }
```

|                     | **newWidget == null**  | **newWidget != null**   |
| :-----------------: | :--------------------- | :---------------------- |
|  **child == null**  |  返回 null         |  返回新的 Element |
|  **child != null**  |  删除旧子树并返回 null | 更新旧子树(可能的话)，返回旧子树或新的 Element |

# 参考

[深入了解Flutter界面开发 · 语雀](https://www.yuque.com/xytech/flutter/tge705#8uguax)