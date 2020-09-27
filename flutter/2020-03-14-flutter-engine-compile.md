# 编译 Flutter 引擎

## [配置及下载代码](https://github.com/flutter/flutter/wiki/Setting-up-the-Engine-development-environment)

+ [安装 `depot_tools`](https://commondatastorage.googleapis.com/chrome-infra-docs/flat/depot_tools/docs/html/depot_tools_tutorial.html#_setting_up) 并添加到环境变量 (包含 `gclient` 脚本)
+ fork [flutter/engine](https://github.com/flutter/engine) (注意配置 ssh 访问)
+ 创建空的 `engine` 目录并在目录中创建 `.gclient` 配置文件
+ 在 `engine` 目录中执行 `gclient sync` (它会 `git clone` 必要的项目及其依赖)

```
git clone https://chromium.googlesource.com/chromium/tools/depot_tools.git
export PATH=/path/to/depot_tools:$PATH
# export PATH=/data/github/depot_tools:$PATH
mkdir engine
cd engine
touch .gclient

# edit .gclient

gclient sync
```

`.gitclient` 配置如下：

```
solutions = [
  {
    "managed": False,
    "name": "src/flutter",
    "url": "https://github.com/410063005/engine.git",
    "custom_deps": {},
    "deps_file": "DEPS",
    "safesync_url": "",
  },
]
```

# [编译](https://github.com/flutter/flutter/wiki/Compiling-the-engine)

注意编译前的一个重要操作是将源码切换到 **本地 Flutter SDK** 的 engine version  (一个 commit id) 对应的提交点，避免可能出现的报错。[Flutter Engine与SDK的定制化与编译 - 简书](https://www.jianshu.com/p/ff84455fb451)

```
# 查看 flutter 版本
vim src/flutter/bin/internal/engine.version

# 调整代码
cd engine/src/flutter
git reset --hard xxxxxxxxxxxxxxxxx
gclient sync -D --with_branch_heads --with_tags

# 准备构建文件
cd engine/src
./flutter/tools/gn --runtime-mode debug
./flutter/tools/gn --android --unoptimized --runtime-mode=debug --android-cpu=arm64

# 编译
ninja -C out/host_debug_unopt -j 16
ninja -C out/android_debug_unopt_arm64 -j 16
```

# 要点

+ `ninja` 命令在 `depot_tools` 工具中
+ `--unoptimized` 用于编译可调试的二进制文件，它以 checked mode (Debug 模式)运行 Dart 代码
+ 通常使用 `android_debug_unopt` 在设备上调试引擎
+ 通常使用 `android_debug_unopt_x64` 在模拟器上调试引擎
+ 需要有对应的 host 构建。即，如果你想使用 `android_debug_unopt`，应当确保已构建 `host_debug_unopt` 

# 错误记录

找不到 python 之类的错误

解决方法：检查 depot_tools 是否在环境变量中

```
Package freetype2 was not found in the pkg-config search path.
Perhaps you should add the directory containing `freetype2.pc'
to the PKG_CONFIG_PATH environment variable
No package 'freetype2' found
Could not run pkg-config.
```

解决办法：安装 `freetype-devel`

```
yum install freetype-devel
```

---

```
../../third_party/glfw/src/x11_platform.h:39:10: fatal error: 'X11/Xcursor/Xcursor.h' file not found
#include <X11/Xcursor/Xcursor.h>
         ^~~~~~~~~~~~~~~~~~~~~~~
```

编译 glfw 时报错。[原因](https://www.glfw.org/docs/latest/compile.html#compile_deps_x11)是：

```
../../third_party/glfw/src/x11_platform.h:42:10: fatal error: 'X11/extensions/Xrandr.h' file not found
#include <X11/extensions/Xrandr.h>
         ^~~~~~~~~~~~~~~~~~~~~~~~~
1 error generated.
```

>  For example, on Ubuntu and other distributions based on Debian GNU/Linux, you need to install the xorg-dev package, which pulls in all X.org header packages.

解决办法：安装相关的库

```
yum install libX11-devel
yum install libXcursor-devel
yum install libXrandr-devel
yum install libXxf86vm-devel
```

编译 glfw 提示找不到 OpenGL

```
../../third_party/glfw/include/GLFW/glfw3.h:171:12: fatal error: 'GL/gl.h' file not found
  #include <GL/gl.h>
```

解决办法：安装 OpenGL

```
yum install freeglut-devel
```

问题：`flutter/tools/gn` 相关的脚本各种奇怪报错

解决办法：使用 python 2.7 而不是 python 3。在 mac 系统上使用 [virtualenv](https://pypi.org/project/virtualenv/) 来创建虚拟的 python 2.7 环境，可以有效避免各种折腾 (我的 mac 机器上装了 conda，缺省使用 pyton 3 所有引起各种编译错误)

![-w1304](/images/15916888375110.jpg)


# 使用编译后的引擎

`gradle.properties` 文件中增加如下配置：

```
localEngineOut=/Users/chenming/wd/engine/src/out/android_debug_unopt_arm64
```

```
local-engine-repo=
local-engine-out=/Users/cm/android_debug_unopt/
local-engine-build-mode=debug
```

```
flutter run --local-engine-src-path <FLUTTER_ENGINE_ROOT>/engine/src --local-engine=ios_debug
flutter run --local-engine-src-path /path/to/engine/src --local-engine=android_debug
```


# 参考

+ articles/show/437772
+ [Compiling the engine · flutter/flutter Wiki](https://github.com/flutter/flutter/wiki/Compiling-the-engine#compiling-for-android-from-macos-or-linux)
+ articles/show/401729