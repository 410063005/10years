# Flutter 看起来很好，但用起来很痛苦

翻译自 [[From Android dev] Flutter looks good, but is painful. Here are my frustrations with it.](https://medium.com/@bernaferrari/from-android-dev-flutter-looks-good-but-is-painful-here-are-my-frustrations-with-it-81b4bbe739f8)。

用过的人都说 Flutter 好，看看觉得 Flutter 不好的人怎么说吧。

(译者注：有时我们得听得进不同意见，不是么？)

<!--more-->

> 声明：我是 Android 开发者，[我在Github 有接近1000个 star](https://github.com/bernaferrari) 的 Kotlin 项目，我喜欢使用 Kotlin。我讨厌 Android 和 iOS 的 UI，很高兴看到 Swift 和 Compose UI 终于正在兴起。

每个原生的 Android 开发者都喜欢学学 Flutter(原文: loves to throw some fire at Flutter.  啥意思)，因为目前[在 Github 上它有超过7000个 issue](over 7 thousand open issues in GitHub)，它还被人拿来[跟 Adobe Flash 比较](https://twitter.com/JakeWharton/status/1105256725547044870)。好吧，在跨平台开发上有过很多挫折后我决定学学 Flutter，看看它是否真的能让我的开发工作变得容易。我无法忍受用户抱怨我没为 iOS 开发应用，而 Flutter 马上就要支持 Web 了，你不能忽略这一点。

![](/images/15834795379559.jpg)

[source](https://github.com/flutter/flutter/issues/31138#issuecomment-496577650)

我不会详细谈论 Stateful/Stateless 控件或一些通用的东西，我想说的是一些你通常在别的地方不会看到的东西，比如类似以上截图。

有三点内容你需要提前了解：

+ Flutter 官方使用 Github 上的点赞数量作为 issue 的优先级
+ "很多"重要的 issue 可能要几个月甚至几年才能解决，这非常让人沮丧——并且导致上面截图中让人非常不愉快的情况
+ 你常常需要依赖某个只有几十个赞的第三方库，你不确定作者是否会继续维护，但又别无选择。这些库不像 Android 界的 Square，或者 Google 的 Jetpack。我说这些的意思并不是我不反对开源，相反我喜欢他们。但我每天都能看到很多有用的库是4到10个月前更新的，我不太确定开发者是因为已经开发完成了，或是在等待着 Dart 版本更新后才发布新库

# 安装很漫长

(译者注：个人体验来说目前(2020年3月) Flutter 的安装体验还是不错的，很快速)

> 更新：我可能对这个反应过度了。安装时间很久，但并不像在 Linux 上无 Conda 直接配置 TensorFlow 那样困难。感谢 Reddit。更新后的内容如下

使用 Flutter 前我本以为第一步会很简单。但不幸的是，[并非如此](https://flutter.dev/docs/get-started/install/macos#get-the-flutter-sdk)。你需要下载一个 zip 包，配置正确的环境变量 PATH，然后调用 `flutter` 命令来安装。然后你还需要几条命令来安装额外的工具来在 iOS 上运行，或者下载 Android Studio 进行 Android 开发。对 iOS 而言，你需要使用 homebrew 来安装一些额外的库。

我个人喜欢使用 homebrew 安装 Flutter，只要一条命令。有一个 open 状态的 issue 请求支持 homebrew 安装 Flutter [实际上，这个 issue 点赞数排第4](https://github.com/flutter/flutter/issues/14050)。一些非官方的脚本也可用，但其他一些则不行。不过一些手工操作仍然不可避免：你只能使用 `flutter doctor` 来升级 Flutter 而不是 `brew upgrade`，而从 stable channel 切换到 beta channel 基本上意味着重装 Flutter，而且你还得知道 Flutter 装在哪里，因为 IntelliJ/Android Studio 要求配置 Dart 编译器路径。最近一个用户调查显示[完善安装流程跟提高用户留存有关](https://github.com/flutter/flutter/issues/37354)，看起来确实如此，幸运的是，这是个高优先级的 issue。

安装好了，就开始学吧。[学完最基础](https://flutter.dev/docs/get-started/codelab)的之后，你就被扔在荒野要自谋生路了，就像 Android 开发一样。我是花钱学 [The Complete Flutter Development Bootcamp Using Dart](https://www.appbrewery.co/p/flutter-development-bootcamp-with-dart)，这个课程是跟 Google Flutter Team 联合出品的，在 Udemy 上也有。我觉得很值，非常推荐。

# 错误的 Material Design

## 排版

我非常喜欢 Material Design。我知道何时正确使用 headline6, subtitle1, body1 等等。Android 和 iOS 上的 Material Design Components 都遵守 [official guidelines](https://material.io/design/typography/)，并且工作得很好。但到了 Flutter 上，却大不相同。不可思议的是，你可以选择使用老的风格以及老的名字(缺省)，或者新的风格和老的名字。在新风格中，如果你使用 body1，你实际使用的是 body2(反之亦然)。如果你想使用 headline5，你要用 display1。完全瞎扯。

![](/images/15834811615178.jpg)

[简直是恶梦](https://api.flutter.dev/flutter/material/TextTheme-class.html)

## Bottom Sheet

我想使用 Bottom Sheet 来实现这样的展示效果：收起来时是一个 view，而展开时是一个 list。这个效果并不奇特，就像你在 Google Maps 或其他 Google 应用中看到的那样。但你无法实现。原生的 Bottom Sheet 功能非常有限，[Bottom Sheet from not-the-Flutter-team](https://github.com/akshathjain/sliding_up_panel) 稍稍好用一些，但仍然无法实现类似 Google Maps 的效果。

## Icon

这块非常难用。没有矢量图、PDF 或 Lottie 支持(除了一个第三方库可以桥接原生的 iOS Lottie  和 Android Lottie)。Flutter 提供字体，你可以将自定义字体打包到应用中。Flutter 上的 Feather 图标是[10个月或7个月前更新的](https://github.com/hanneskuettner/feather_icons_flutter)，而[原始的 Feather 图标是2个月前更新的](https://github.com/feathericons/feather/releases)。

如果你使用 SVG (也需要一个[第三方库](https://github.com/dnfield/flutter_svg)，作者在 Google 工作)，它异步加载成一个图片。如果想要展示成一个图标，你还得额外做些工作。我对 SVG 唯一不满的是其异步性：我遇到一个控件先于 SVG 出来的场景，所以会觉得 SVG 出现得很突兀。

关于字体，如果你想使用2018年更新的 Material Design 图标，那很不幸。Flutter 自带的是标准版本的，如果你想要新版本的，要么自己下载和编译字体，要么下载下来当作 SVG 使用。

## Text Input

我使用 Text Input 遇到一些问题，[有许多相关 issue 是 open 状态](https://github.com/flutter/flutter/issues?q=is%3Aissue+is%3Aopen+sort%3Aupdated-desc+label%3A%22a%3A+text+input%22)。（其他控件可能也是类似的质量，但由于大部分应用要使用 text input，所以它的问题会更高频且让人恼火）。我最烦的是在某些特定场景时你无法使用清除按钮(经常发生)，否则你将看到下图中的这种情况。不错，这只是一个小问题，但它反映了某些小问题本不应该发生，这些问题需要更好地处理。

![](/images/15834831596156.jpg)


点击"x"来清除 text input 时遇到的一个 bug。约一个月前提出。stable channel 中可复现。[source](https://github.com/flutter/flutter/issues/35848)

# 其他挫折

## Scroll listener

在 Android 应用中我严重依赖 RecyclerView 的 `addOnScrollListener` 方法。许多应用，包括 Netflix，会使用该来在滚动时隐藏键盘。但得花点功夫才能在 Flutter 上实现类似效果。当你使用 ListView 时，列表会缓存 drag event，所以手势回调收不到任何事件。也可以监听列表是否变化，但这样一来，更新/过滤等操作都会导致监听器被调用，所以不可行。你还可以使用更底层的 Listener 来接收屏幕触摸事件。(更新：[szotp was able to use dragDetails with NotificationListener and find a solution](https://github.com/flutter/flutter/issues/36869))。当然，也有[第三方库](https://github.com/mcrovero/flutter_interactive_keyboard)来缓解这个问题(尤其是在 iOS 平台上)，但这又另当别论。

> 更新：as reported by /u/[filleduchaos](https://www.reddit.com/user/filleduchaos), there is [PointerMoveEvents](https://api.flutter.dev/flutter/gestures/PointerMoveEvent-class.html) which provides deltas, so while a verticalDelta from GestureDetector is not be available, this can mitigate the situation since Offsets contain both vertical and horizontal components. [reddit source](https://www.reddit.com/r/androiddev/comments/cm1o0v/from_android_dev_flutter_looks_good_but_is/evztf18/)

## Scroll perception

列表滚动时感觉怪怪的，像是"错的"，你能发现 Flutter 应用是不是原生的。[一些人正在尝试改进](https://github.com/flutter/flutter/issues/32448)，甚至有一些集成测试来比较二者，但即使在 Android 上你也能发现 Flutter 列表滚动和 Android 原生滚动感觉上的不同。我并不是说一个更流畅，另一个更慢。我只是说它们滚动不一致。React 中的滚动跟原生也不一致，但它更接近原生。

## PageView

Flutter 中只能使用 PageView 这种方式来实现循环列表效果(make carousels in Flutter)，但它有 bug。所有的第三方库都是基于 PageView 的。如果你滚动得太快，它会略过一些元素。你自定义 PageView 或使其像 Google Play Games 那样因为它根本就不允许。[我对这个问题提了一个 issue](https://github.com/flutter/flutter/issues/35712)。我或许能等等，但不幸的是，[十一月份的 bug 要到六月才能修复](https://github.com/flutter/flutter/issues/24763)。

## State

"状态"问题绝对可以花一篇文章的篇幅来写，但[来自 Square 的这个 issue](http://developer.squareup.com/blog/flutter-android-and-process-death) 让我很不开心，因为你不可能在不了解 Android 或 iOS 的情况下一头扎进 Flutter。Flutter 并非就在那里等着你，你还得处理各种细节。它仍然缺少 `onSaveInstance` 这些东西。当然，在不同平台上，状态相关的东西工作机制并不一样，甚至在 Android 上它也一直在变化，但我期待 Flutter 未来能处理这部分内容。

## Resizing

I am really curious to see they solving this now that Flutter is going to the web:

(译者注：没看懂。推测是在说一个跟 resizing 大小相关的 bug)

# 结论

从第三方库社区来看，Flutter 更接近 Javascript 而不是 Android。但 Javascript 追求的是 "moving faster"，而 Flutter 仍然在决定要变成什么样。即使造成混乱也要保持兼容？好吧。一大批质量不确实的库被创建又马上抛弃？好吧。我想喜欢 Flutter，但 Flutter 现在并不喜欢我。也许是因为 Flutter 团队人手不够需要增加人力，也许是他们需要重组并且调整优先级。只有他们知道。

我还会继续投入时间到 Flutter，并且用它开发一到两个应用。使用 Flutter 开发 UI 真的很棒，即使觉得 Dart 用起来有点受限——我甚至没有提到依赖注入之类的高级话题。即使开发一个 Flutter 应用可能也很困难，因为[他们花了大概一年时间来同时支持32位和64位 APK](https://github.com/flutter/flutter/issues/18494) [correction](https://news.ycombinator.com/item?id=20612055)。我们还是不要在 Flutter for Web 上花太多时间吧，不然会超过 10000 个公开的 issue。

本文不想成为一篇"抱怨Flutter 有各种问题，所以我讨厌它，要烧掉它"的文章，它想说的是"Flutter 并不完美，痛点正在显露和增长，但如果他们更小心的话，是可以成长市场团队期待的那样"。我们年复一年地在 Android 开发中使用各种奇技淫巧，现在终于有了 Flutter。但我真的希望它能有比现在更快的节奏发展。

![](/images/15834854600343.jpg)

我在看某个库的源码时遇到的一段注释 [source](https://github.com/flutter/flutter/issues/17168)

> 更新：你刚看完的这些内容，是更新后的内容。我修改了关于安装、图标、结论以及其他部分的内容，以求更准确。谢谢 Reddit 上的所有反馈。这是我的第一篇开发文章，我被读者的反应以及传播速度震惊了。

---

(译者注：为了完整性，顺便也翻译几条回复)

我是 Flutter 团队成员，我在这里多次直接或间接提到：

首先，你确实指出了一些真实的痛点。我想简单地回应其中的三点：

**安装**：这个过程应当做得更好。Flutter 对 Homebrew 的支持不太好，但这点会改变。也就是说，可以做些事情让这个过程变得更好。一些人也在评论中指出了这一点。

**Issue Tracker**：它处于一个糟糕的状态。随着我们壮大，越来越难以第一时间鉴别新的 issue，并确保它们正确打上标签以展示给恰当的有人，或者 issue 质量足够高，又或者没跟其他 issue 重复。我们正在处理这种情况，它要花些时间。我几天内就修复了文中提到的 bug，但它仍然是打开状态，因为我的修复只是简单处理，更完整的方案要花上一段时间。

**Asynchronicity of SVG**： 它的实际需求来自解析器从异步源加载数据这一事实(比如，网络或磁盘)。因此，如果你愿意的话，可以展示一个加载中的占位符。
