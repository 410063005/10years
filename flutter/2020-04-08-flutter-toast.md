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
