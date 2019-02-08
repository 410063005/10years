# 常见错误
问题现象： Android Studio 3.2，打开一个旧工程，编译提示"No toolchains found in the NDK toolchains folder for ABI with prefix: mips64el-linux-android"

> This version of the NDK is incompatible with the Android Gradle plugin
>   version 3.0 or older. If you see an error like
>   `No toolchains found in the NDK toolchains folder for ABI with prefix: mips64el-linux-android`,
>   update your project file to [use plugin version 3.1 or newer]. You will also
>   need to upgrade to Android Studio 3.1 or newer.

问题原因：新版本的NDK与3.0及以前旧版的Android Gradle plugin插件不兼容

解决办法：升级 Android Gradle plugin 版本

---

问题现象：报错 "> ABIs [armeabi] are not supported for platform. Supported ABIs are [arm64-v8a, armeabi-v7a, x86, x86_64]."

解决办法：

```groovy
android {
    defaultConfig {
        ndk {
            abiFilters 'armeabi', 'armeabi-v7a', 'arm64-v8a'
        }
    }
}
```

修改为：

```groovy
android {
    defaultConfig {
        ndk {
            abiFilters 'x86', 'x86_64', 'armeabi-v7a', 'arm64-v8a'
        }
    }
}
```
---
问题现象：强制使用 gcc 编译时 提示 "CMake Error: CMAKE_C_COMPILER not set, after EnableLanguage"

```groovy
android {

    defaultConfig {
        externalNativeBuild {
            cmake {
                cppFlags "-std=c++11"
                arguments "-DANDROID_TOOLCHAIN=gcc"
            }
        }
    }
}
```

> GCC is no longer supported. It will be removed in NDK r18.

问题原因：最新版本的 NDK 中移除了 GCC，而这个项目是旧项目，它使用 GCC 编译。尝试使用 clang 编译，但各种报错，放弃。

解决办法： 下载 [NDK r17c](https://developer.android.com/ndk/downloads/older_releases#ndk-17c-downloads)，解压到 <SDK>/ndk-bundle 目录中

