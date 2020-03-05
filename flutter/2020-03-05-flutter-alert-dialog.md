# 如何在Flutter中优雅地自定义Dialog

[TOC]

先来说说 `AlertDialog`。

`AlertDialog` 通常作为 Widget 参数传给 `showDialog()` 方法，该方法弹出对话框并且返回一个 `Future` 对象。

# 对话框文档

## showDialog

先把 `showDialog()` 的官方文档看一遍。翻译如下：

> 在当前应用内容之上显示一个 Material 对话框，带有 Material 入场和出场动画、modal barrier color、modal barrier behavior(在 barrier 上点击一下对话框会消失)。

> 这个方法接受一个 `builder` 参数(通常用于构建一个 `Dialog` 组件)。被 `Dialog` 覆盖的内容会变暗(由 `ModalBarrier` 实现)。注意：`builder` 返回的组件跟 `showDialog()` 最初被调用的地方并不共享相同的 context。如果对话框需要动态更新，则应使用 `StatefulBuilder` 或自定义的 `StatefulWidget`。

> 这个方法的 `child` 参数被废弃，由 `builder` 取代。

> 这个方法的 `context` 参数用于为当前对话框查找 `Navigator` 和 `Theme`。这个 `context` 仅在当前方法被调用时使用(注：应该是指不能持有其该对象的引用，然后用在其他地方)。对话框关闭后其对应的组件可以安全地从组件树上移除。

> `useRootNavigator` 参数用于决定是否将当前对话框 push 到 `Navigator` 中远离还是告诉当前 `context` 的地方。默认时 `useRootNavigator` 为 `true`，且由当前方法创建的对话框被 push 到 root navigator。

> 如果当前应用有多个 `Navigator`，则有必要调用 `Navigator.of(context, rootNavigator: true).pop(result)` 来关闭对话框而不是简单地调用 `Navigator.pop(context, result)`。

> 这个方法返回一个 `Future` 对象。其值为关闭对话框时传给 `Navigator.pop()` 方法的参数。

总结：

+ `barrierDismissible` 参数对应于 Android 中的 `setCanceledOnTouchOutside()` 方法
+ `Future` 返回值对应于 Android 中的 `setOnDismissListener()` 方法

## AlertDialog

AlertDialog 文档翻译如下：

> 一个 Material Design 风格的对话框
> 
> AlertDialog 用于知会用户必须了解的某些信息。该对话框有一个可选的 `title`，以及一组可选的 `action`。`title` 在 `content` 之上显示，`action` 在 `content` 之下显示。
> 
> 如果 `content` 超过屏幕高度，则对话框会显示 `title` 和 `action` 而让 `content` overflow (这种情况比较少见)。这种情况下可考虑使用可滚动组件来包裹 `content`，比如 `SingleChildScrollView`，以避免 overflow。(另外要注意，AlertDialog 使用其子组件的 intrinsic dimensions 来确定自身大小，所以类似 `ListView`、`GridView` 和 `CustomScrollView` 这些使用 lazy viewport 的组件不能正常工作。这种情况下考虑直接使用 `Dialog` 而不是 `AlertDialog`)
> 
> 如果需要提供多个选项供用户选择，考虑使用 `SimpleDialog`

## Dialog

> 一个 Material 设计风格的对话框
> 
> 这个对话框组件对于其内容无任何要求。不建议直接使用 `Dialog`，而是优先使用 `AlertDialog` 或 `SimpleDialog`，这两者实现了特定的 Material 设计规范

## ButtonBar

> 一排靠 end 方向(通常是靠右)排列的按钮。如果水平方向不够的话，则以列的方式排列。
> 
> 根据 `buttonPadding` 来水平排列按钮。子组件以 `MainAxisAlignment.end` 的方式在 `Row` 中排列。当 `Directionality` 是 `TextDirection.ltr` 时，button bar 的子组件靠右排列，即最后一个按钮最靠右。当 `Directionality` 是 `TextDirection.rtl` 时，button bar 的子组件靠左排列，即最后一个按钮最靠左。
> 
> 当 button bar 的宽度超过最大宽度限制时，以列的方式来排列。主要不同在于 `MainAxisAlignment` 作为 cross-axis/horizontal 排列。比如，如果 button bar 宽度超出，且 `ButtonBar.alignment` 为 `MainAxisAligment.start`，则按钮会在 button bar 水平方向上从左开始排列。

## _InputPadding

这是个比较坑的存在。我发现点击 `FlatButton` 出现水波纹效果时总其上下两边总存在一个预期外的边距(padding? margin?)。[Stackoverflow](https://stackoverflow.com/questions/54938971/flutter-button-unwanted-extra-top-and-bottom-padding) 也提到了这个问题：

![](/images/15834042950272.jpg)

用 DevTools 对自己写的 UI 观察，结果如下。

![](/images/15834041542694.jpg)

这个预期外的边距似乎是 `_InputPadding` 带来的。控件树上也的确存在一个 `_InputPadding` 对象。

![-w500](/images/15834036452226.jpg)

先了解一下 `FlatButton` 的继承关系：

```
FlatButton 继承 MaterialButton
MaterialButton.build() 生成 RawMaterialButton
```

再来看 `_RawMaterialButtonState` 的代码。

```dart
class _RawMaterialButtonState extends State<RawMaterialButton> {

  @override
  Widget build(BuildContext context) {
    ...
    Size minSize;
    switch (widget.materialTapTargetSize) {
      case MaterialTapTargetSize.padded:
        minSize = const Size(48.0, 48.0);
        break;
      case MaterialTapTargetSize.shrinkWrap:
        minSize = Size.zero;
        break;
    }
    return Semantics(
      container: true,
      button: true,
      enabled: widget.enabled,
      child: _InputPadding(
        minSize: minSize,
        child: result,
      ),
    );
  }
}
```

`_InputPadding` 的注释如下：

> 为 `MaterialButton` 内部的 `Material` 增加 padding
> 
> 这个组件将将其中的点击事件重新传播到其子组件的中心。它会增加按钮大小及按钮的 "tap target"，而不是 material 或 ink

其 `hitTest()` 实现如下：

```dart
  @override
  bool hitTest(BoxHitTestResult result, { Offset position }) {
    if (super.hitTest(result, position: position)) {
      return true;
    }
    final Offset center = child.size.center(Offset.zero);
    return result.addWithRawTransform(
      transform: MatrixUtils.forceToPoint(center),
      position: center,
      hitTest: (BoxHitTestResult result, Offset position) {
        assert(position == center);
        return child.hitTest(result, position: center);
      },
    );
  }
```

最后的解决办法是为 FlatButton 添加 `MaterialTapTargetSize.shrinkWrap`，完美去掉不需要的 padding。

```dart
materialTapTargetSize: MaterialTapTargetSize.shrinkWrap
```

## ButtonTheme

> `ButtonTheme` 使用 `ButtonThemeData` 来为按钮配置颜色和外形。
> 
> 按钮的主题使用 `ThemeData.buttonTheme` 可以在全局 Material 主题中指定。Material 主题中指定的按钮主题可以被 `ButtonTheme` 覆盖。
> 
> 按钮的实际外观由按钮主题、开启/禁用状态、elevation 以及全局主题决定。

# 要点

## Dialog 的继承关系

`AlertDialog`、`SimpleDialog` 并不继承自 `Dialog`。实际上这三者都直接继承自 `StatelessWidget`。

## Dialog 的宽度计算

AlertDialog 是如何自适应屏幕宽度的？

```dart
child: Center(
  child: ConstrainedBox(
    constraints: const BoxConstraints(minWidth: 280.0),
    child: Material()
  )
)
```

# 示例解析

## Gallery

## ColorPicker

[flutter_colorpicker](https://pub.dev/packages/flutter_colorpicker)

## 自定义 Dialog

# 参考

+ [AlertDialog Widget - Follow Flutter - Medium](https://medium.com/follow-flutter/alertdialog-widget-7362c50a7b66)