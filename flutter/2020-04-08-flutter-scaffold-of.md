# Flutter Scaffold.of() 方法

`Scaffold.of()` 方法签名如下：

```dart
ScaffoldState of (
    BuildContext context,
    {bool nullOk: false}
)
```

其作用是返回持有指定 context 的、距离当前 widget 最近的 Scaffold 实例。

TODO 补充示意图

典型的用法是在 `Scaffold` 的子节点的 `build()` 方法中调用 `Scaffold.of()`。如下：

```dart
import 'package:flutter/material.dart';

// ...

void main() => runApp(MyApp());

// ...

class MyApp extends StatelessWidget {
  // This widget is the root of your application.
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Flutter Code Sample for Scaffold.of.',
      theme: ThemeData(
        primarySwatch: Colors.blue,
      ),
      home: Scaffold(
        body: MyScaffoldBody(),
        appBar: AppBar(title: Text('Scaffold.of Example')),
      ),
      color: Colors.white,
    );
  }
}

// ...

class MyScaffoldBody extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return Center(
      child: RaisedButton(
        child: Text('SHOW A SNACKBAR'),
        onPressed: () {
          Scaffold.of(context).showSnackBar(
            SnackBar(
              content: Text('Have a snack!'),
            ),
          );
        },
      ),
    );
  }
}
```

不过，如果 `Scaffold` 实例是在同一个 `build()` 方法中创建的话，`build()` 方法的 `context` 参数**不能**用于获取 Scaffold 实例 (since it's "above" the widget being returned in the widget tree。 如何理解？)。在这种情况下，可以使用 [Builder](https://api.flutter.dev/flutter/widgets/Builder-class.html) 来提供一个新的在 Scaffold 之下的 (a new scope with a BuildContext that is "under" the Scaffold) [BuildContext](https://api.flutter.dev/flutter/widgets/BuildContext-class.html)

TODO 补充图片说明 above 和 under 的关系

```dart
Widget build(BuildContext context) {
  return Scaffold(
    appBar: AppBar(
      title: Text('Demo')
    ),
    body: Builder(
      // Create an inner BuildContext so that the onPressed methods
      // can refer to the Scaffold with Scaffold.of().
      builder: (BuildContext context) {
        return Center(
          child: RaisedButton(
            child: Text('SHOW A SNACKBAR'),
            onPressed: () {
              Scaffold.of(context).showSnackBar(SnackBar(
                content: Text('Have a snack!'),
              ));
            },
          ),
        );
      },
    ),
  );
}
```

另外一个有效的方法是将 build 函数拆分成若干小的 Widget。拆分后新引入的 context 可用于获取 Scaffold。这种方案中，外层 Widget 创建的 Scaffold 是内层 Widget 的父节点。在内层 Widget 中可以使用 `Scaffold.of`。

另一种不太优雅但是更快捷的方法是为 Scaffold 指定 [GlobalKey](https://api.flutter.dev/flutter/widgets/GlobalKey-class.html)，然后使用 `key.currentState` 属性而不是 `Scaffold.of` 函数来获取 ScaffoldState。

总结一下获取 ScaffoldState 的几种方式：

+ 直接调用 `Scaffold.of`
+ 在 builder 中调用 `Scaffold.of`
+ 拆分 Widget
+ 使用 GlobalKey

注意，如果当前范围内没有 [Scaffold](https://api.flutter.dev/flutter/material/Scaffold-class.html)，`Scaffold.of` 函数会抛出异常。传入 `nullOk: true` 可以避免没有 Scaffold 时返回 null 而不是抛出异常。
