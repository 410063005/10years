# Flutter Studio

翻译自 [Flutter Studio - Timothy - Medium](https://medium.com/@timtech4u/flutter-studio-dad96fc39c36)

<!--more-->

如果你是 Flutter 新手，你应该知道Flutter 是一个基于 Dart 语言的框架，可以使用同一份代码来同时构建 Android 和 iOS 应用。

如果你用过 Flutter，你应当试试 [Flutter Studio](https://flutterstudio.app)：这是一个基于 Web 的 Flutter 界面构建工具，它让 GUI 的开发更容易。

另一个好消息是，Flutter Studio(当前版本v2)本身也是基于 Dart 开发的。Flutter Studio 主要界面截图如下：

![Model View](https://miro.medium.com/max/4794/1*h_mS51ec_rhzeEjrgA89EQ.png)

![Source Code File](/images/15832879034404.jpg)

![Our Pubspec.yaml file](/images/15832879190534.jpg)

# 如何使用

用法很简单，只要将控件从工具面板拖放到画布上即可。点击 **source** 标签查看生成的 Flutter 代码。选中控件后可以在控件属性面板中编辑。

~~唯一比较难使用的是 icon 控件。首先要拖放到画布上，然后再来编辑：颜色、大小和边距~~ (译者注：最新的 Flutter Studio 中 icon 控件用起来其实并不难)。

通过点击来选中画布中的控件。被选中的控件显示一个绿色边框(译者注：最新的 Flutter Studio 中的是蓝色边框)。可以使用右侧的编辑器调整控件的属性，包括：颜色、大小、边距、对齐、字体等等。编辑属性后生成的代码实时更新。

简而言之，它有以下特性：

+ 它是响应式的
+ 在 Web 上准确展示真实 Android 和 iOS 设备上的显示效果
+ 生成和展示完美像素的设计
+ 提供大量可用控件(包括主题)
+ 可直观地编辑控件，并且生成正确的代码
+ 专注于设计(代码和 `pubspec` 在另外的标签下)
+ 生成完整的、可运行的应用代码和 `pubspec` 文件

欢迎试用 Flutter Studio，来体验一下它的强大吧。

以下是这些链接让你可以更好地使用 Flutter：

+ [Official Documentation](https://flutter.io)
+ [Flutter Newsletter](https://flutterweekly.net)
+ [Flutter Codelabs](https://flutter.io/codelabs)

最后奉上我的最爱 [awesome-flutter](https://github.com/Solido/awesome-flutter)，一个非常棒的资源列表，它包含最佳的 Flutter 库、工具、指南以及文章。