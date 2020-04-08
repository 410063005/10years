# Flutter Run

`flutter run` 源码位于 `flutter/packages/flutter_tools/`。

核心功能包括：

+ flutter build apk
    + flutter build aot
    + flutter build bundle
+ adb install <apk>
+ adb am start <app>

![](/images/15843482005233.jpg)

上述过程涉及到的主要代码是：

+ run.dart
+ gradle.dart
    + `buildGradleApp()` - 实际是调用 gradle 进行编译
+ flutter.gradle
+ build_aot.dart
    + `runCommand()` - 编译 kernel 和 so
+ build_apk.dart

# 参考

[源码解读Flutter run机制 - Gityuan博客 | 袁辉辉的技术博客](http://gityuan.com/2019/09/07/flutter_run/)