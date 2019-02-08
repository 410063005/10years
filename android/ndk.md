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