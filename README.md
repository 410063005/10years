# Java
## 并发
[001-ThreadLocal的用法](java/concurrent/001-threadlocal.md)

[002-ConcurrentHashMap简介](java/concurrent/002-concurrent-hashmap.md)

# JVM

# Kotlin
[001-Kotlin中如何Mock](kotlin/001-kotlin-mock.md)

[002-Kotlin中的协程](kotlin/002-kotlin-coroutines.md)

# Python
## 基础
[001-关于tornado IOLoop的理解](python/basic/001-io-loop.md)

## web开发

[001-mysql](python/django/001-mysql.md)

[002-django](python/django/002-django.md)

[003-跨域问题](python/django/003-django-csrf.md)

[Python 2 和 Python 3 之间的差异](python/python2-vs-python3.md)

## 安装和配置

[001-python2和python3共存](python/install/001-python2-python3.md)

[002-pip](python/install/002-pip.md)

[待分类](python/todo.md)

# Android

[001-关于WebView](android/001-webview.md)

[002-Android常用库](android/002-android-lib.md)

[003-Lint使用技巧](android/003-lint.md)

[004-深入理解Activity](android/004-activity.md)

## 架构
[架构](android/lib/002-arch.md)

## 常用库
[001-dagger](android/lib/001-dagger.md)


## Bitmap

## Animation

[001-view动画](#)

[002-property动画](android/animation/002-property-animation.md)

## Notification

[001-检查通知栏是否开启](android/notification/001-are-notifications-enabled.md)

## 性能
[Android Performance Patterns](android/performance/000-note.md)

[001-记录华为手机系统引起的一个内存泄漏问题](android/performance/004-huawei-memory-leak.md)

[002-view优化技巧](android/performance/002-view-opt.md)

[003-内存优化技巧](android/performance/003-app-mem-opt.md)

[006-常用性能分析工具](android/performance/006-perf-tools.md)

## 代码
[值得一读的代码](android/code.md)

## 未归类

# Linux
## 常用工具

[001-常用工具之screen](linux/tools-screen.md)

## ssh
[001-ssh登录常见问题](linux/ssh/001-ssh-login.md)

---

pip3 install ss

ss -p 8080 -k password -m rc4-md5 -d start

```
vi /etc/ss.json

{
  "server": "0.0.0.0",
  "server_port": 8080,
  "local_port": 1080,
  "password": "123456",
  "timeout": 600,
  "method": "rc4-md5"
}

ss -c /etc/ss.json -d start
```


easy_install AnkiServer
mkdir Anki
cd Anki
cp /usr/local/lib/python2.7/dist-packages/AnkiServer-2.0.6-py2.7.egg/examples/example.ini production.ini
vi producion.ini

host= 10.XX.XX.XX  #自己服务器的地址

allowed_hosts=0.0.0.0 #允许同步的客户端ip地址，使用0.0.0.0表示允许任何ip地址连接

添加用户

ankiserverctl.py adduser username

调试模式启动

ankiserverctl.py debug [configfile]

正常启动
ankiserverctl.py start [cofigfile]
