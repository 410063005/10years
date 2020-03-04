# Flutter Studio 2

翻译自 [Flutter Studio, Version 2 - Paul Mutisya - Medium](https://medium.com/@pmutisya/flutter-studio-version-2-41cce10fcf3d)。我去体验了一把文中介绍的 Flutter Studio，bug 很多，仍然是个玩具，但其想法很有创意。非常期待能 Flutter 能出官方版，让 Flutter UI 开发更加高效。

<!--more-->

![-w800](/images/15833226645876.jpg)


Flutter Studio 已更新，更新后它更灵活和完整，有更好的响应式，生成的代码也更准确。Flutter Studio 现在可以更好地支持多种屏幕，准确地不同的屏幕和方向上展示应用，让你使用起来更连贯。另外，它现在生成完整可运行的代码以及构建文件，并且支持更多的新控件了。

+ 它是响应式的
+ 在 Web 上准确展示真实 Android 和 iOS 设备上的显示效果
+ 生成和展示完美像素的设计
+ 提供大量可用控件(包括主题)
+ 可直观地编辑控件，并且生成正确的代码
+ 专注于设计(代码和 `pubspec` 在另外的标签下)
+ 生成完整的、可运行的应用代码和 `pubspec` 文件

新版本可以在[这里](https://flutterstudio.app)体验。

# 响应式应用

Flutter Studio 的第一版使用一个静态画布，只支持一种设备(Pixel)，且有两个固定面板分别用于编辑代码和属性。不同控件使用同一种属性编辑器，不管编辑器是否适用于当该控件。显示的设备大小也永远相同而不管有多少空间。这种展示方式完全不适合笔记本屏幕，在大的显示器上会留下大片空白。当我在13” MacBook pro 上开发时，很难在同一个窗口中同时放下代码区、工具区和模型区。

我重新设计了其展示方式以适合更多的屏幕大小。

![](https://miro.medium.com/max/2800/0*IgK80u-XuRNZIvz_.)

如上图所示新版本中会调整设备和工具窗口大小以占满空白区域。屏幕越大，展示的设备也越大。只有一个属性编辑器，它会根据选择的控件变化。生成的代码和 pubspec 在单独的标签中。上图中可以看到很多特性，后面会详细讲。重点在于，现在可以在更多尺寸屏幕上进行设计了，从11寸的笔记本到30寸的大屏幕，都能看到工具区和属性编辑器。

# 一个新的像素级别的设计工具

在前一个版本中，我使用一个静态的画面，它大概是基于我的测试设备(一台Pixel)来设计的。这个静态画布非常不灵活，经常不准确。它缩放不准确，且将你限制一台设备。我希望能在任意设备上进行原型设计，包括那些可能并不拥抱的设备上。你可不愿意花1000美元买一台 iPhone X，或者能找到一台 2012 年产的 Nexus 7，但你的客户可能使用这种设备。

这个版本中，我使用主要从 https://material.io/devices/ 以及不同生产商网站收集到的数据创建了一个设备数据库，并且将这些设备数据应用到 Flutter Studio 的画布。每种设备，包括 Android One 和 iPhone X 都可以准确展示。只需简单地从下拉菜单中选择就可以展示新的设备。

我之前提到过我喜欢构建工具。我写了一个应用来管理设备，并且添加新设备也很方便。本着乐于分享的精神，我发布了一个公开版本。这个公开版本基于 Firebase 来运行。所以你可以从 https://devicedb.app/ 浏览和下载数据(JSON格式)。

![-w800](/images/15833077503040.jpg)

你也可以将整个数据库作为JSON文件下载。

但我能不能利用这些信息来帮助开发者呢。Flutter 使用了一个好的 Android 特性：图像大小使用设备无关的单位来测量(**dp** 或 **dip**，大约 160像素每英寸)。就算 Nexus 9 或 iPhone X 上的一个物理像素远小于 Nexus One，两类设备上一个 160x160dp 的按钮也有相同的大小。

有了准备的设备数据，以及以 dp 计的屏幕尺寸，我就可以在应用中以 dp 来构建一个逻辑模型，让任何设备和屏幕在你 的屏幕上准确缩放。

通过使用真实设备的数据来缩放 Flutter Studio 模型，可以让用户知道应用在特定设备上将如何展示。

![](/images/15833083201343.jpg)

an Android One reference phone (480x854 pixel screen; 320x569 dp screen; 4.5” diagonal)

![](/images/15833083277146.jpg)

a Nexus 9 tablet (2048x1539 pixels; 768x1024 dp; 8.9” diagonal):

注意看同一个元素在 Nexus 9 上占用更少的空间。

使用设备的真实模型并且允许不同比例(横屏或竖屏方向)，可以让开发者在使用实际设备前就能快速测试不同的布局。虽然 Flutter 开发已经很快了，但 Flutter Studio 是一个更快的原型工具，尤其是当你想在不同的设备上测试时。

有时在设备上准确缩放应用会让应用没法看清，尤其是在像 Nexus 9 这样的高分辨率设备上。比如，我在笔记本上很难看清楚。基于此，我增加一个切换按钮来关闭真实的设备切换。

![](/images/15833087251640.jpg)

上图是 Nexus 9 上一个典型的应用，有标准的 Material 风格的 [RaisedButton](https://docs.flutter.io/flutter/material/RaisedButton-class.html) [Image](https://docs.flutter.io/flutter/widgets/Image-class.html) 和 [FlutterLogo](https://docs.flutter.io/flutter/material/FlutterLogo-class.html)，并且打开了缩放。这跟真实设备上看起来是一致的，但在我的笔记本上几乎无法看清按钮上的文字。关闭缩放后可以看清文字：

![](/images/15833088896384.jpg)

关闭缩放后应用的可见区域变小了，但你现在可以看清应用细节，即使是在小的屏幕上。

允许你快速开关缩放让你不必重载或切换测试设备就可以进行布局设计工作。你(开发者)要做的工作越少，就越专注于实际价值。所以 Flutter Studio 这个工具可以减少在多设备上的测试工作。

我自己使用真实设备(包括Android和iOS)以及尺子等工具测试过这个应用，以保证这个应用在实际设备上的准确性。

# 更多的控件，更好的编辑器

Flutter Studio 的最初版本基于 Flutter 早期版本，支持非常有限的控件。这个版本中，我至少想支持 [https://flutter.io/widgets/](https://flutter.io/widgets/) 上列出来的控件。这是一个目标，到目前(May 2018, Beta 3)为止我已经支持这个列表中几乎所有的控件，包括一些较新的控件，比如 [FlutterLogo](https://docs.flutter.io/flutter/material/FlutterLogo-class.html) [RotatedBox](https://docs.flutter.io/flutter/widgets/RotatedBox-class.html) 和 [Opacity](https://docs.flutter.io/flutter/widgets/Opacity-class.html)。这对我个人很有帮助，因为我必须学习这些控件相关的知识才能实现它们。另外，FlutterLogo 这类控件让我不得不学习习 HTML canvas 和 Flutter transformation matrices。

某些控件和 decorations 有一些非常 tricky 的 API，很难知道到底该如何使用。我为属性设计了属性编辑器，希望能直观地使用属性。你可以尝试并观察代码是如何更新的，以便更容易学习。

比如，gradients 有个 API 非常诡异。所以 Flutter Studio 提供色彩编辑器，允许你快速调整颜色并检查生成的代码，学习起来也更容易。

![](/images/15833107374542.jpg)

除了来自 flutter.io 的控件，我还包含了一些我自己在开发 Flutter 应用时经常用到的控件，比如 [FractionallySizedBox](https://docs.flutter.io/flutter/widgets/FractionallySizedBox-class.html) (我经常用来从逻辑上分割应用，与屏幕大小无关) 和 [DropdownButton](https://docs.flutter.io/flutter/material/DropdownButton-class.html)。还包含了一些文档不是很全的控件，其中有两个要提一下：[RadialGradient](https://docs.flutter.io/flutter/painting/RadialGradient-class.html) 和 [LinearGradient](https://docs.flutter.io/flutter/painting/LinearGradient-class.html)。实际上它们并不是控件，不过真的很有用。

希望给他人提供工具能让人能在视觉上更容易地尝试，而生成可运行的代码则加速学习过程。这些控件，一旦你理解了，就非常容易使用。但难点在于，你不知如何开始，而且查找起来也很困难。

总结一下，上个版本的 Flutter Studio 支持26种控件。而目前版本支持50种控件。

# 图标更易用

旧版本的 Flutter Studio 中的图标选择器错综复杂。点击图标按钮后弹出选择器，然后修改图标。接下来将图标拖放到画布中。这是个问题。这个选择器跟跟其他只需一次操作的功能不同，它要两次操作，所以导致操作体验上不一致。另外其中一次操作入口不易发现，简直可以说是 3D Touch、长按或右键点击的 Web 版本。新版本中将图标以及图标按钮跟其他控件一视同仁，将其拖放到画布后再编辑即可。

![-w800](/images/15833124621347.jpg)

属性编辑器允许你选择颜色、大小及图标。

# 主题即控件

Flutter 中一切皆控件，包括主题。[Theme](https://docs.flutter.io/flutter/material/Theme-class.html) 定义了 material 应用排版及颜色。这意味着你可以将应用划分成不同的逻辑单位，分别为它们设置主题。可以在 **styling** 标签中找到主题控件。

点击右下角的灰色圆形按钮来修改整个应用的主题，这时属性编辑器显示应用主题。

下面的截图显示我将一个应用分成上下两部分(使用 [FractionallySizedBoxes](https://docs.flutter.io/flutter/widgets/FractionallySizedBox-class.html))。我在每个部分中放入一个 Theme。上半部分是深色主题。下半部分是浅色主题，primary swatch 为粉色。每个部分中的任何控件均会共享该主题的颜色和字体。

![-w800](/images/15833155954134.jpg)

# 完整的可运行代码

上一个版本的 Flutter Studio 只生成代码片断，没有处理所使用的资源。新版本的 Flutter Studio 生成完整的可运行代码以及 `pubspec.yaml` 文件。我学习如何使用 Flutter 资源的最大痛点之一是如何正确格式化 pubspec 文件以及添加图片文件。我相信你肯定也有 pubspec 格式不正确导致编译失败的经历。

Flutter Studio 现在让这个过程变得无脑了：生成整个 pubspec 文件，任何用到的字体或资源都加进来了并且被正确格式化(保证不会重复)。你可以添加主题、图片和按钮主题到核心内容中。Flutter Studio 过滤出那些用到的字体和资源，并且将它们添加到一个完整、可解析的 yaml 文件中。

Flutter Studio 会生成一个完整的、可在任何设备上运行的应用，而不仅仅是代码片断。

# 写在最后

如果你想构建一个大型 Web 应用，我建议你考虑 Dart。FlutterStudio 和 Device Database 都是使用 Dart 来写的。FlutterStudio 的最初版本是基于 Google Web Toolkit 来构建的，因为我知道它肯定能工作，只是用起来很恼火。接下来的两个版本是用 Dart 来写的。新的代码，包括所有新添加的特性，代码量明显比先前的 GWT 版本要少。这不仅仅是因为更好的编译器。

Dart 鼓励有效率地编码，并且在基础层面有数据结构和数据类型来支持有效编码(比如 Future 和 Stream)。由于 Dart 并非强类型语言，所以过去我很犹豫是否要强烈推荐它，因为从我过去的经验来看在大的代码库中动态类型语言容易引起问题。但从 Dart 2 开始，它改成静态类型了。

而且 Dart 更清晰和易于维护。Google 凭借从 GWT、Android 以及其他项目学到的经验来加大对 Dart (和 Flutter) 的投入。Dart 编译器( AOT 和 JIT )以及 VM 都很高效。而 Dart 编译器可以输出 Javascript(Web) 和 ARM(Flutter) 代码这个特性始终让我印象深刻。它有快速、小巧并且高效的代码。如果你是面向现代浏览器，那么 Dart 完全可以用于产品级开发。当然，我不敢对 Dart 用于 Web 应用开发的工作流和库的可用性有太多称赞。
