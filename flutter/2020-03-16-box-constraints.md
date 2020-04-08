# Flutter 布局与 Box Constraints

[TOC]

总结：

+ [RenderBox](https://api.flutter-io.cn/flutter/rendering/RenderBox-class.html) 的大小由 width 和 height 表示。
+ [BoxConstraints]() - RenderBox 的布局约束。
+ Box layout is performed by passing a [BoxConstraints] object down the tree. In determining its size, the child must respect the constraints given to it by its parent
+ Render objects in the Flutter framework are laid out by a one-pass layout model which walks down the render tree passing constraints (即 BoxConstraints), then walks back up the render tree passing concrete geometry (即 Size).
+ layout 模型中 BoxConstraints 沿着 render tree 从上往下传，Size 则是反方向从下往上传。
+ Size 应当满足 BoxConstraints
+ RenderBox 从其父节点接受 BoxConstraints，对子节点布局完成后选择一个满足 BoxConstraints 的 Size
+ RenderBox 对子节点定位(position)的过程跟对子节点布局(layout)的过程是独立的。通常使用子节点的 Size 来对其定位(position)
+ 子节点不知道自己的位置，当位置变化时并不一定重新布局和绘制

## BoxConstraints 与 Size

```dart
class BoxConstraints extends Constraints {
  /// The minimum width that satisfies the constraints.
  final double minWidth;

  /// The maximum width that satisfies the constraints.
  ///
  /// Might be [double.infinity].
  final double maxWidth;

  /// The minimum height that satisfies the constraints.
  final double minHeight;

  /// The maximum height that satisfies the constraints.
  ///
  /// Might be [double.infinity].
  final double maxHeight;
}

class Size extends OffsetBase {
  /// The horizontal extent of this size.
  double get width => _dx;

  /// The vertical extent of this size.
  double get height => _dy;
}

abstract class OffsetBase {
  final double _dx;
  final double _dy;
}
```

BoxConstraints 与 Size 应当满足的关系：

+ `minWidth` <= `Size.width` <= `maxWidth`
+ `minHeight` <= `Size.height` <= `maxHeight`

BoxConstraints 自身应当满足的关系：

+ 0.0 <= `minWidth` <= `maxWidth` <= `double.infinity`
+ 0.0 <= `minHeight` <= `maxHeight` <= `double.infinity`

注意：对于约束来说 `double.infinity` 是有效值。

## 约束类型

+ tight 轴 - 同一轴向的 `minWidth` == `maxWidth`
+ loose 轴 - `min` == 0 (不管 `max` 是多大。如果 `max` == 0，则它既是 tight 也是 loose)
+ bounded 轴 - `max` 不是 `double.infinity`
+ unbounded 轴 - `max` 是 `double.infinity`
+ expanding 轴  `min` 和 `max` 都是 `double.infinity` (同时也可以认为它是 tightly infinite )
+ infinite 轴 - `min` 是 `double.infinity`
+ constrained Size - 满足某个 BoxConstraints

## 约束处理

一般来说，从如何处理约束的角度来看，有以下三种类型的渲染框：

+ 尽可能大。比如 Center (**loose**) 、ListView (**unbounded**) 以及默认的 Container (**expanding**) 的渲染框。
+ 与子 widget 一样大，比如 Transform 和 Opacity 的渲染框。
+ 特定大小，比如 Image 和 Text 的渲染框。

### 案例一 - Container

Container 默认是*尽可能大*的，也可以给它指定*特定大小*。

```dart
class Container extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    Widget current = child;

    if (child == null && (constraints == null || !constraints.isTight)) {
      current = LimitedBox(
        maxWidth: 0.0,
        maxHeight: 0.0,
        // 默认尽可能大
        // BoxConstraints.expand() 返回 min, max 均为 double.infinity 的约束
        child: ConstrainedBox(constraints: const BoxConstraints.expand()),
      );
    }
    ...
    if (constraints != null)
      // 也可指定特定大小
      current = ConstrainedBox(constraints: constraints, child: current);
  }
```

### 案例二 - RenderView

约束有时是**紧密**(tight)的。RenderView 是 render tree 的根。它要求其子节点(一个 RenderBox)按约束填满整个屏幕，约束由 `BoxConstraints.tight(_size)` 给出。

```dart
/// The root of the render tree.
///
/// The view represents the total output surface of the render tree and handles
/// bootstrapping the rendering pipeline. The view has a unique child
/// [RenderBox], which is required to fill the entire output surface.
class RenderView extends RenderObject with RenderObjectWithChildMixin<RenderBox> {
  @override
  void performLayout() {
    assert(_rootTransform != null);
    _size = configuration.size;
    assert(_size.isFinite);

    if (child != null)
      // 要求子节点按紧密约束来布局
      child.layout(BoxConstraints.tight(_size));
  }
```

### 案例三 - Center

有些渲染框**放松**了约束，即：约束中只有最大宽度，最大高度，但没有最小宽度，最小高度，例如 Center。

```dart
class Center extends Align {}

class Align extends SingleChildRenderObjectWidget {
  @override
  RenderPositionedBox createRenderObject(BuildContext context) {
    return RenderPositionedBox(...);
  }
}

class RenderPositionedBox extends RenderAligningShiftedBox {
  @override
  void performLayout() {
    ...
    if (child != null) {
      // 这里**放松**了约束
      // BoxConstraints.loose() 返回 min 均为 0 的约束
      child.layout(constraints.loosen(), parentUsesSize: true);
      size = ...;
      alignChild();
    } else {
      ...
    }
  }
}
```

### 案例四 - Opacity

有些 Widget 与子 widget 一样大，比如 Transform 和 Opacity 的渲染框。

```dart
class Opacity extends SingleChildRenderObjectWidget {
  @override
  RenderOpacity createRenderObject(BuildContext context) {
    return RenderOpacity(...);
  }
}

class RenderOpacity extends RenderProxyBox {}

// A proxy box has a single child and simply mimics all the properties of that child
// For example, a proxy box determines its size by asking its child
// to layout with the same constraints and then matching the size
class RenderProxyBox extends RenderBox with RenderObjectWithChildMixin<RenderBox>, RenderProxyBoxMixin<RenderBox> {}

mixin RenderProxyBoxMixin<T extends RenderBox> on RenderBox, RenderObjectWithChildMixin<T> {
  @override
  void performLayout() {
    if (child != null) {
      // 使用相同约束对子节点布局
      child.layout(constraints, parentUsesSize: true);
      // 大小跟子节点相同
      size = child.size;
    } else {
      performResize();
    }
  }
}
```

### 案例五 - ListView

> 在某些情况下，传递给框的约束是 无边界 的或无限的。这意味着约束的最大宽度或最大高度为double.INFINITY。
> 
> 渲染框具有无边界约束的最常见情况是：当其被置于 flex boxes (Row 和 Column)内以及 可滚动区域(ListView 和其它 ScrollView 的子类)内时

ListView 的边界是无界的(bounded)。


```dart
class ListView extends BoxScrollView {
  @override
  Widget buildChildLayout(BuildContext context) {
    if (itemExtent != null) {
      return SliverFixedExtentList(
        delegate: childrenDelegate,
        itemExtent: itemExtent,
      );
    }
    return SliverList(delegate: childrenDelegate);
  }
}

abstract class BoxScrollView extends ScrollView {
  /// Subclasses should override this method to build the layout model.
  @protected
  Widget buildChildLayout(BuildContext context);
}

abstract class ScrollView extends StatelessWidget {
  @protected
  Widget buildViewport(
    BuildContext context,
    ViewportOffset offset,
    AxisDirection axisDirection,
    List<Widget> slivers,
  ) {
    if (shrinkWrap) {
      return ShrinkWrappingViewport(
        axisDirection: axisDirection,
        offset: offset,
        slivers: slivers,
      );
    }
    return Viewport(
      axisDirection: axisDirection,
      offset: offset,
      slivers: slivers,
      cacheExtent: cacheExtent,
      center: center,
      anchor: anchor,
    );
  }

}

class Viewport extends MultiChildRenderObjectWidget {}

class RenderViewport extends RenderViewportBase<SliverPhysicalContainerParentData> {}

  @override
  void performLayout() {
    if (firstChild == null) {
      switch (axis) {
        case Axis.vertical:
          assert(constraints.hasBoundedWidth);
          // 垂直的 ListView 与父节点一样宽
          size = Size(constraints.maxWidth, constraints.minHeight);
          break;
        case Axis.horizontal:
          // 水平的 ListView 与父节点一样高
          assert(constraints.hasBoundedHeight);
          size = Size(constraints.minWidth, constraints.maxHeight);
          break;
    }
  }
}
```

// TODO

## 案例六 - Flex

在有边界约束条件下，它们在给定方向上会尽可能大。

在无边界约束条件下，它们试图让其子 widget 自适应这个给定的方向

// TODO

# LayoutBuilder 与 constraint



# 参考

+ [处理边界约束 (Box constraints) 的问题 - Flutter 中文文档 - Flutter 社区中文资源](https://flutter.cn/docs/development/ui/layout/box-constraints)