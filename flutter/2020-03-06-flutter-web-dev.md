# Flutter Web 开发的一点小技巧

# 常规步骤

常规的 Flutter Web 开发步骤是这样的：

1. `flutter channel beta` - Flutter 1.12 中 beta channel 才支持 Web，所以要切换到这个 channel
2. `flutter upgrade`
3. `flutter config --enable-web` - 开启 Flutter Web
4. 创建 Web 项目
    + 创建全新 Web 项目 `flutter create myapp`
    + 或为旧项目添加 Web 支持 `flutter create .`
5. `flutter run -d chrome`

理想情况以上步骤是可以跑通的，但实际中常常会遇到问题。我分别展开来讲。

## channel 切换问题

首先来说第1步的问题。我遇到的问题就是自己的本地机器要在 stable channel 上为公司的 Flutter 项目(已上线产品)开发新功能，只是部分时间想在 beta channel 上体验一下 Flutter Web 功能。但实践中**频繁切换 Flutter channel 存在一些问题**，

+ 一是切来切去效率低，很繁琐耗时
+ 二是从 stable channel 切换到 beta channel 后某些 Flutter 代码可能编译出错(接口变了)

所以我的做法是在公司分配的**远程机器安装 Flutter 环境并切换到 beta channel，专门用来体验 Flutter Web**

## 添加 Web 支持问题

然后来看第4步的问题。一般来说，创建全新的 Flutter Web 项目通常不会遇到问题，为旧项目添加 Web 支持可能遇到问题。对王生人生 Flutter 添加 Web 支持时，出现以下错误。

+ `flutter create` 提示无效包名。原因是项目名中有 `-`，而包名中不允许出现 `-`。解决办法是直接改项目名，将 `-` 全部替换成 `_`
+ `flutter create` 提示 `Ambiguous organization in existing files`。原因未知。解决办法是 `flutter create --org <你的org名> .`。注意这里 `--org` 参数的位置是紧接着 `create`

## 运行 Web 服务问题

最后来看第5步的问题。我遇到的两个问题如下：

+ 运行 `flutter run` 时提示 `missing index.html`，但实际上已执行过 `flutter create .` 并提示成功添加 Web 支持。之所以报 `missing index.html` 错误是因为项目中并没有生成 `web` 目录及相关文件(`web` 目录下包含一个 `index.html`)。(简单粗暴但有效的)解决办法是直接从全新生成的 Flutter Web 项目拷贝 `web` 目录到本项目中
+ 运行 `flutter run -d chrome` 时提示无设备。原因是我的远程机器上并没有安装浏览器。一个变通的方式是使用 `flutter run -d web-server`，执行后将在远程机器上某个随机端口上启动 Web 服务。

解决上述一些问题后，成功在我们的项目中添加了 Web 支持。访问效果如下图：

![-w360](/images/15834628246867.jpg)
(注：Web 服务是通过 VS Code 中的 ssh 插件启动的，所以可以通过 127.0.0.1 访问)

# 改进后的步骤

总结一下改进后 Flutter Web 开发步骤。如下：

1. 登录 *远程机器* 安装 Flutter
2. `flutter channel beta` - Flutter 1.12 中 beta channel 才支持 Web，所以要切换到这个 channel
3. `flutter upgrade`
4. `flutter config --enable-web` - 开启 Flutter Web
5. 为旧项目添加 Web 支持 `flutter create .` - 这一步遇到问题的话请参考前一节的内容
6. 安装 VS Code 及 ssh 插件
7. VS Code 中打开 *远程机器* 上的项目并启动 Web 服务，`flutter run -d web-server` (**重要**：VS Code Terminal 中执行该命令)
8. *本地机器* 浏览器访问 *远程机器* 的 Web 服务
9. VS Code 中编辑 Flutter 代码并保存，VS Code Terminal 中输出 `r` 热加载
10. *本地机器* 浏览器刷新，新代码生效

# 更新

发现 VS Code 打开远程的 Flutter 项目后，代码高亮和自动提示并不生效。原因是要在 SSH 环境中安装 Flutter 插件和 Dart 插件，如下图：

![-w420](/images/15839804475962.jpg)

安装后重启 VS Code，一切就跟本地开发一样了。完美！

----

# 开启 Flutter Web 支持

(这是以前某次折腾后的记录，保留)

[Building a web application with Flutter - Flutter](https://flutter.dev/docs/get-started/web)

1. 切换到 master 分支, `flutter channel master`
2. 开启 web 支持, `flutter config --enable-web`
3. 检查是否开启, `flutter devices` 能看到 chrome
4. 在项目目录中执行
    1. `flutter create .` (已存在的项目)
    2. ` flutter create myapp` (新项目)
5. 运行 web 项目, `flutter run -d chrome`
6. 打包 web 项目, `flutter build web`

第2步后 `~/.flutter_settings` 文件内容修改为：

```
{
  "enable-web": true
}
```

第3步检查结果如下：

```
➜  tip_flutter_web git:(master) ✗ flutter devices
2 connected devices:

Chrome     • chrome     • web-javascript • Google Chrome 78.0.3904.108
Web Server • web-server • web-javascript • Flutter Tools
➜  tip_flutter_web git:(master) ✗
```

如果提示 `Ambiguous organization in existing files: {com, com.igame}. The --org command line argument must be specified to recreate project.`

```
flutter create --org com.igame .
```

----

# 参考

+ [Building a web application with Flutter - Flutter](https://flutter.dev/docs/get-started/web)
+ [Build and release a web app - Flutter](https://flutter.dev/docs/deployment/web)
+ [Web support for Flutter - Flutter](https://flutter.dev/web)