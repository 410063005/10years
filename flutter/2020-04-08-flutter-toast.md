# Flutter Toast

如何在 Flutter 中实现 toast 效果？[How to create Toast in Flutter? - Stack Overflow](https://stackoverflow.com/questions/45948168/how-to-create-toast-in-flutter) 中提到如下几种方式：

+ 使用 [SnackBar](https://api.flutter.dev/flutter/material/SnackBar-class.html)
+ 使用 [FlutterToast](https://github.com/PonnamKarthik/FlutterToast)
+ 使用 [Toast](https://github.com/appdev/FlutterToast)

三种方式的代码分别如下：

```dart
// SnackBar
Scaffold.of(context).showSnackBar();

// Fluttertoast
Fluttertoast.showToast(
        msg: "This is Toast messaget",
        toastLength: Toast.LENGTH_SHORT,
        gravity: ToastGravity.CENTER,
        timeInSecForIos: 1
    );

// Toast
Toast.show("Toast plugin app", context,
        duration: Toast.LENGTH_SHORT,
        gravity:  Toast.BOTTOM
    );
```

![-w600](/images/15863118321363.jpg)

![-w600](/images/15863118433395.jpg)

我们项目中需要实现以下样式的 Toast，

![-w383](/images/15863332003180.jpg)

该使用哪种实现方式？

+ ~~使用 [SnackBar](https://api.flutter.dev/flutter/material/SnackBar-class.html)~~
+ ~~使用 [FlutterToast](https://github.com/PonnamKarthik/FlutterToast)~~
+ 使用 [Toast](https://github.com/appdev/FlutterToast)

首先排除 SnackBar，因为它的样式不符合我们的需求，且样式不太容易修改。

其次排除 FlutterToast，因为它是以 Dart 插件方式调用原生代码，需要 Android 和 iOS 端分别支持特定样式的。

Toast 是纯 Dart 代码实现，可以满足我们的需求。当然，这个库不直接提供我们所需样式的 Toast，需要我们自己动手实现。

# Toast 库分析

整个库就 [toast.dart](https://github.com/appdev/FlutterToast/blob/master/lib/toast.dart) 一个源文件，非常简洁。

```
Toast.show()
 -> ToastView.createView()
  -> ToastWidget()
   -> OverlayEntry()
```

其核心代码如下：

```dart
// 获取 OverlayState
overlayState = Overlay.of(context);
// 创建 OverlayEntry (其中包含用于显示文本的 Widget)
_overlayEntry = new OverlayEntry()
...
// 添加 OverlayEntry
overlayState.insert(_overlayEntry);
...
// 延迟一段时间
await new Future.delayed(
  Duration(seconds: duration == null ? Toast.lengthShort : duration));
// 移除 OverlayEntry
_overlayEntry?.remove();
```

不过 `ToastView.createView()` 在创建 `ToastWidget` 时将 UI 内容写死，无法自定义 UI。 

```dart
class ToastView {

  static void createView() {
    overlayState = Overlay.of(context);

    Paint paint = Paint();
    paint.strokeCap = StrokeCap.square;
    paint.color = background;

    _overlayEntry = new OverlayEntry(
      builder: (BuildContext context) => ToastWidget(
          widget: Container(
            width: MediaQuery.of(context).size.width,
            child: Container(
                alignment: Alignment.center,
                width: MediaQuery.of(context).size.width,
                child: Container(
                  decoration: BoxDecoration(
                    color: background,
                    borderRadius: BorderRadius.circular(backgroundRadius),
                    border: border,
                  ),
                  margin: EdgeInsets.symmetric(horizontal: 20),
                  padding: EdgeInsets.fromLTRB(16, 10, 16, 10),
                  // 这里将 UI 写死，无法自定义
                  child: Text(msg, softWrap: true, style: textStyle),
                )),
          ),
          gravity: gravity),
    );
    ...
  }
```

主要的问题在于 `Text(msg, softWrap: true)` 将 UI 写死，没法自定义 UI。

# 自定义 ToastUI

## 方案一 - 改造 Toast 库

[这个 gist](https://gist.github.com/410063005/a465e32c8b73053cdcd6a48f453ad548) 基于 Toast 库进行改造，支持自定义 UI。

## 方案二 - 使用 [flutter_oktoast](https://github.com/OpenFlutter/flutter_oktoast) 库

另一种方法是使用 flutter_oktoast 库，它支持自定义 UI 以及少量动画效果。不过个人不太喜欢它的设计风格。

使用 flutter_oktoast 要求用 OKToast 包裹 MaterialApp，看着很别扭。showToast 应当简单自然。

```
class MyApp extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return OKToast( // 这一步
      child: new MaterialApp(
        title: 'Flutter Demo',
        theme: new ThemeData(
          primarySwatch: Colors.blue,
        ),
        home: new MyHomePage(),
      ),
    );
  }
}
```

## 方案三 - 参考 tooltip

[Tooltip class - material library - Dart API](https://api.flutter.dev/flutter/material/Tooltip-class.html)

# Overlay

# 参考

+ [FlutterToast/toast.dart at master · appdev/FlutterToast](https://github.com/appdev/FlutterToast/blob/master/lib/toast.dart)
+ [flutter toast插件 OKToast的介绍 | caijinglong的博客](https://www.kikt.top/posts/flutter/toast/oktoast/)
+ [OpenFlutter/flutter_oktoast: a pure flutter toast library](https://github.com/OpenFlutter/flutter_oktoast)