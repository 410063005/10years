# Dart Future 用法简介

# Future

## 从 Java Future 说起

先来看看 Java [Future](https://docs.oracle.com/javase/8/docs/api/index.html?java/util/concurrent/Future.html)。

> A Future represents the result of an asynchronous computation. 

Java Future 的主要方法包括：

+ `Future.isDone()` - 检查计算是否完成
+ `Future.get()` - 获取计算结果。**如果计算尚未完成，这个方法会阻塞**
+ `Future.cancel()` - 取消计算

不能直接创建，而是通过向线程池提交任务来创建 Future。

```java
     Future<String> future
       = executor.submit(new Callable<String>() {
         public String call() {
             return searcher.search(target);
         }});
```

Future 有非常多的设计问题：

+ 没法注册回调！(这导致 Future 几乎不可用)
+ 无法组合/链式调用
+ 无法处理异常

Java 社区对有有许多修正：

+ Guava [ListenableFuture](https://github.com/google/guava/wiki/ListenableFutureExplained) 解决了 Future 无法注册回调的问题
+ Java 8 引入 [CompletableFuture](https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/CompletableFuture.html)，支持组合/链式调用
+ [RxJava](https://github.com/ReactiveX/RxJava)

## Dart Future

再来看 Dart [Future](https://api.dartlang.org/stable/2.6.1/dart-async/Future-class.html)。

> An object representing a delayed computation.
> A [Future] is used to represent a potential value, or error,
> that will be available at some time in the future.

+ 表示**延迟计算(delayed computation)**的对象
+ 有**完成(completed)**和**未完成(uncompleted)**两种状态
+ 完成时返回某个**值(completed with a value)**或**错误(completed with an error)**

虽然都叫 Future，Dart Future 跟 Java Future 还是有非常明显的不同。

+ 首先 Dart Future 的接口设计跟 Java 8 [CompletableFuture](https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/CompletableFuture.html) 更像，而不是 Java Future
    + 支持注册回调
    + 支持组合/链式调用
    + 支持异常处理
+ Dart Future 并不会阻塞！

此外，Dart Future 的接口更友好易用，大部分时候你甚至不必关心线程/线程池之类的细节。后面会细讲

# Dart Future 介绍

## 创建

有不同的工厂方法用来创建 Future，各个工厂方法均接收一个 `computation` 参数。

```dart
// 生成 Future
// 如果参数不是 Future 类型, 则生成一个 completed 状态的 Future
// 如果参数是 Future 类型, 则生成一个 uncompleted 状态的 Future
Future.value('hello, world!');
// 同步执行 computation
Future.sync(() => 'hello, world!');
// 异步执行 computation, 使用 `Time.run()`
Future(() => 'hello, world! I am 3');
// 异步执行 computation, 使用 `Time.run()`, 加延时
Future.delayed(Duration(seconds: 1), () => 'hello, world! I am 4');
// 异步执行 computation, 使用 `scheduleMicrotask()`
Future.microtask(() => 'hello, world! I am 5');
```

`Timer.run()` vs `scheduleMicrotask()`

重点来看一下 `Future.microtask()` 这种异步执行方式有什么特别之处。下面分别是两种工厂方法的实现：

```dart
factory Future(FutureOr<T> computation()) {
  _Future<T> result = new _Future<T>();
  Timer.run(() {
    try {
      result._complete(computation());
    } catch (e, s) {
      _completeWithErrorCallback(result, e, s);
    }
  });
  return result;
}

factory Future.microtask(FutureOr<T> computation()) {
  _Future<T> result = new _Future<T>();
  scheduleMicrotask(() {
    try {
      result._complete(computation());
    } catch (e, s) {
      _completeWithErrorCallback(result, e, s);
    }
  });
  return result;
}
```

可见差异在于：

+ `Future()` 调用 `Timer.run()`，事件进入 Event Queue
+ `Future.microtask()` 调用 [scheduleMicrotask()](https://api.dartlang.org/stable/2.6.1/dart-async/scheduleMicrotask.html)，事件进入 Microtask Queue

`Future.microtask()` 提交的异步代码相对前者有更高的优先级。所以以下这段代码中，"executed" 永远不能打印出来。

```dart
main() {
  Timer.run(() { print("executed"); });  // Will never be executed.
  foo() {
    scheduleMicrotask(foo);  // Schedules [foo] in front of other events.
  }
  foo();
}
```

参考资料：

+ [Flutter--Dart中的异步](https://cloud.tencent.com/developer/article/1358024)
+ [Dart asynchronous programming: Isolates and event loops](https://medium.com/dartlang/dart-asynchronous-programming-isolates-and-event-loops-bffc3e296a6a)
+ [Dart by example: Microtasks](http://jpryan.me/dartbyexample/examples/microtasks/)

## 使用

`then()` 用于注册回调，它仍然返回一个 Future，所以可以很方便地进行链式处理。

```dart
  Future<String> f1 = Future.value('hello, world!');
  f1.then((v) {
    return Future.value('龙 $v');
  }).then((v) {
    return Future.value('接 $v');
  }).then((v) {
    return Future.value('来 $v');
  }).then((v) {
    return Future.value('家 $v');
  }).then((v) {
    return Future.value('大 $v');
  }).then((v) {
    print(v);
  });
```

## 异常处理

+ `catchError()`
+ `whenComplete()`

```dart
// Synchronous code.
try {
  int value = foo();
  return bar(value);
} catch (e) {
  return 499;
} finally {
  print('clean up');
}

// Asynchronous code.
Future(foo)
  .then((v) => bar(v))
  .catchError(() => 499)
  .whenComplete(() {
    print('clean up');  
  });
```

# async 和 await

TODO

# 参考资料

+ [Asynchronous programming: futures, async, await | Dart](https://dart.cn/codelabs/async-await)
+ [Future](https://youtu.be/OTS-ap9_aXc)
+ [Guide To CompletableFuture](https://github.com/410063005/10years/blob/master/java/concurrent/2019-06-26-completable-future.md)