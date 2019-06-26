
https://developer.android.com/about/versions/nougat/android-7.0-changes

Android 7.0 开始，系统禁止app动态链接到非公开的NDK库。这样做是为了app在系统升级以及在不同设备上有一致的体验。有可能你的app自己并没有链接到私有库，但app使用第三方静态库有这么做。所以开发者应该检查并确保app不会在Android 7.0设备上crash。如果app使用native代码，应当只使用[public NDK APIs](https://developer.android.com/ndk/guides/stable_apis.html)。

App不应该使用NDK以外的native库，因为它们会在不同的Android版本中发生变化甚至被移除。举个例子，Android 从 OpenSSL 切换到 BoringSSL。另外，对于不包含在NDK中的系统库并没有兼容性要求，所以不同设备可能提供不同级别的兼容性支持。

为了减少上述限制对已发布的app的影响，部分被广泛使用的库，比如`libandroid_runtime.so`，`libcutils.so`，`libcrypto.so`以及`libssl.so`在Android 7.0(API level 24)上暂时还可以被访问(targeting API level 为23及以下)。如果你的应用加载这些库，logcat会打印一条警告日志并且在app中弹toast提醒。

所有app在访问既非公开也不能暂时访问的API时都会发生运行时错误。`System.loadLibrary()`和`dlopen(3)`都返回`NULL`，可能导致app崩溃。NUL


