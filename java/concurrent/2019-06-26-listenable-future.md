翻译自 [guava ListenableFuture](https://github.com/google/guava/wiki/ListenableFutureExplained)

源文件地址 [wiki](https://github.com/google/guava.wiki.git)

# ListenableFuture

并发是个难题，但可以通过强大简单地抽象来极大地简化这个难题。为了能简化并发，Guava 定义了 [`ListenableFuture`]，它扩展自 JDK 的 `Future` 接口。

**我们强烈建议你在所有代码中总是使用 `ListenableFuture` 来代替 `Future` 接口**, 原因如下:

*   多数 `Futures` 相关的方法要求使用 `ListenableFuture`
*   这样做好过以后迁移到 `ListenableFuture`
*   工具方法提供方不需要提供在方法中提供 `Future` 和 `ListenableFuture` 变量

## 接口
传统的 `Future` 表示异步调用结果：该调用可能已完成，也可能尚未完成所以还没结果。一个 `Future` 可以作为正在进行中的调用的 handle，或者一个向我们提供结果的服务的 promise。

而 `ListenableFuture` 允许你注册回调方法，一旦计算完成则方法被回调。当计算已完成，则立即回调。添加这个简单的新特性可以有效地支持很多 `Future` 接口无法提供的操作。

`ListenableFuture` 添加的基本操作是 [`addListener(Runnable, Executor)`]，这个方法的作用是，当前 `Future` 表示的调用过程完成后，指定的 `Runnable` 将在指定的 `Executor` 中运行。

## 添加回调

大部分用户在回调方法轻量且执行速度快时倾向于使用 [`Futures.addCallback(ListenableFuture<V>,
FutureCallback<V>, Executor)`]， 或者使用 [缺省使用 `MoreExecutors.directExecutor()` 的版本][no-executor-add-callback]。 [`FutureCallback<V>`] 实现以下两个方法：

*   [`onSuccess(V)`], future 执行成功时的操作
*   [`onFailure(Throwable)`], future 执行失败时的操作

## 创建

与 JDK 使用 [`ExecutorService.submit(Callable)`] 来初始化异步调用类似，Guava 提供
 [`ListeningExecutorService`] 接口, 这个接口返回 `ListenableFuture`，而
 `ExecutorService` 则返回 `Future`。如果想要将 `ExecutorService` 转换成 `ListeningExecutorService`，使用 [`MoreExecutors.listeningDecorator(ExecutorService)`] 即可。

```java
ListeningExecutorService service = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(10));
ListenableFuture<Explosion> explosion = service.submit(new Callable<Explosion>() {
  public Explosion call() {
    return pushBigRedButton();
  }
});
Futures.addCallback(explosion, new FutureCallback<Explosion>() {
  // we want this handler to run immediately after we push the big red button!
  public void onSuccess(Explosion explosion) {
    walkAwayFrom(explosion);
  }
  public void onFailure(Throwable thrown) {
    battleArchNemesis(); // escaped the explosion!
  }
});
```

另外，如果你是转换基于 [`FutureTask`] 的 API, Guava 提供  [`ListenableFutureTask.create(Callable<V>)`] 和
[`ListenableFutureTask.create(Runnable, V)`]。与 JDK 不同，`ListenableFutureTask` 不是直接用于继承的。

如果并不想实现一个方法来计算值，而是在抽象层中设置 future 的值，可以考虑使用 [`AbstractFuture<V>`] 或直接使用 [`SettableFuture`]。

如果一定得将另一个 API 提供的 `Future` 转换成 `ListenableFuture`，你可能别无选择，只能使用重量级的 [`JdkFutureAdapters.listenInPoolThread(Future)`] 来进行转换。有可能的话，应该优先考虑修改原有代码返回 `ListenableFuture`。

## 应用
使用 `ListenableFuture` 的最重要理由是异步操作的复杂调用链成为可能。

```java
ListenableFuture<RowKey> rowKeyFuture = indexService.lookUp(query);
AsyncFunction<RowKey, QueryResult> queryFunction =
  new AsyncFunction<RowKey, QueryResult>() {
    public ListenableFuture<QueryResult> apply(RowKey rowKey) {
      return dataService.read(rowKey);
    }
  };
ListenableFuture<QueryResult> queryFuture =
    Futures.transformAsync(rowKeyFuture, queryFunction, queryExecutor);
```

使用 `ListenableFuture` 还能有效地支持其他操作，而这些操作 `Future` 是无法支持的。不同的操作由不同的 executor 完成，多个操作同时等待单一的 `ListenableFuture`。

当另一个操作开始后，其他若干操作应当立即开始 -- 称之为 "fan-out"，`ListenableFuture` 就开始工作了：它触发所有的回调。略微多点工作，我们还能实现 "fan-in" 效果，即在其他若干 futures _全部_ 完成后立即触发一个 `ListenableFuture`。示例见 [the implementation of `Futures.allAsList`]。

| Method | Description | See also |
|:-------|:------------|:---------|
| [`transformAsync(ListenableFuture<A>, AsyncFunction<A, B>, Executor)`]`*` | Returns a new `ListenableFuture` whose result is the product of applying the given `AsyncFunction` to the result of the given `ListenableFuture`.  | [`transformAsync(ListenableFuture<A>, AsyncFunction<A, B>)`] |
| [`transform(ListenableFuture<A>, Function<A, B>, Executor)`] | Returns a new `ListenableFuture` whose result is the product of applying the given `Function` to the result of the given `ListenableFuture`. | [`transform(ListenableFuture<A>, Function<A, B>)`] |
| [`allAsList(Iterable<ListenableFuture<V>>)`] | Returns a `ListenableFuture` whose value is a list containing the values of each of the input futures, in order.  If any of the input futures fails or is cancelled, this future fails or is cancelled. | [`allAsList(ListenableFuture<V>...)`] |
| [`successfulAsList(Iterable<ListenableFuture<V>>)`] | Returns a `ListenableFuture` whose value is a list containing the values of each of the successful input futures, in order.  The values corresponding to failed or cancelled futures are replaced with `null`. | [`successfulAsList(ListenableFuture<V>...)`] |

`*` [`AsyncFunction<A, B>`] 只有一个方法, `ListenableFuture<B> apply(A
input)`。这个方法用于对值进行异步转换。

```java
List<ListenableFuture<QueryResult>> queries;
// The queries go to all different data centers, but we want to wait until they're all done or failed.

ListenableFuture<List<QueryResult>> successfulQueries = Futures.successfulAsList(queries);

Futures.addCallback(successfulQueries, callbackOnSuccessfulQueries);
```

## 避免嵌套 Futures

一些调用通用接口并返回 Future 时，可能会出现嵌套 `Future` 的问题。如下代码：

```java
executorService.submit(new Callable<ListenableFuture<Foo>() {
  @Override
  public ListenableFuture<Foo> call() {
    return otherExecutorService.submit(otherCallable);
  }
});
```
  
会返回 `ListenableFuture<ListenableFuture<Foo>>`。这种代码是错误的，因为如果对外部 future 的 `cancel` 操作如果跟其结束操作有竞争关系时，取消操作可能没法传播到内部 future。使用 `get()` 或监听器来检查其他 future 是否失败也是一种常见的错误，但除非特别小心 `otherCallable` 抛出的异常会被禁止。  为避免这种情况，Guava 中所有的 future-handling 方法 ( 以及部分来自 JDK 的方法) 者有 *Async 版本的方法以安全地 unwrap 嵌套关系 - [`transform(ListenableFuture<A>, Function<A, B>,
Executor)`] and [`transformAsync(ListenableFuture<A>, AsyncFunction<A, B>,
Executor)`], or [`ExecutorService.submit(Callable)`] and
[`submitAsync(AsyncCallable<A>, Executor)`], 等等。
    
[`ListenableFuture`]: https://google.github.io/guava/releases/snapshot/api/docs/com/google/common/util/concurrent/ListenableFuture.html
[`addListener(Runnable, Executor)`]: https://google.github.io/guava/releases/snapshot/api/docs/com/google/common/util/concurrent/ListenableFuture.html#addListener-java.lang.Runnable-java.util.concurrent.Executor-
[`Futures.addCallback(ListenableFuture<V>, FutureCallback<V>, Executor)`]: https://google.github.io/guava/releases/snapshot/api/docs/com/google/common/util/concurrent/Futures.html#addCallback-com.google.common.util.concurrent.ListenableFuture-com.google.common.util.concurrent.FutureCallback-java.util.concurrent.Executor-
[no-executor-add-callback]: https://google.github.io/guava/releases/snapshot/api/docs/com/google/common/util/concurrent/Futures.html#addCallback-com.google.common.util.concurrent.ListenableFuture-com.google.common.util.concurrent.FutureCallback-
[`FutureCallback<V>`]: https://google.github.io/guava/releases/snapshot/api/docs/com/google/common/util/concurrent/FutureCallback.html
[`onSuccess(V)`]: https://google.github.io/guava/releases/snapshot/api/docs/com/google/common/util/concurrent/FutureCallback.html#onSuccess-V-
[`onFailure(Throwable)`]: https://google.github.io/guava/releases/snapshot/api/docs/com/google/common/util/concurrent/FutureCallback.html#onFailure-java.lang.Throwable-
[`ExecutorService.submit(Callable)`]: http://docs.oracle.com/javase/8/docs/api/java/util/concurrent/ExecutorService.html#submit-java.util.concurrent.Callable-
[`ListeningExecutorService`]: https://google.github.io/guava/releases/snapshot/api/docs/com/google/common/util/concurrent/ListeningExecutorService.html
[`MoreExecutors.listeningDecorator(ExecutorService)`]: https://google.github.io/guava/releases/snapshot/api/docs/com/google/common/util/concurrent/MoreExecutors.html#listeningDecorator-java.util.concurrent.ExecutorService-
[`FutureTask`]: http://docs.oracle.com/javase/8/docs/api/java/util/concurrent/FutureTask.html
[`ListenableFutureTask.create(Callable<V>)`]: https://google.github.io/guava/releases/snapshot/api/docs/com/google/common/util/concurrent/ListenableFutureTask.html#create-java.util.concurrent.Callable-
[`ListenableFutureTask.create(Runnable, V)`]: https://google.github.io/guava/releases/snapshot/api/docs/com/google/common/util/concurrent/ListenableFutureTask.html#create-java.lang.Runnable-V-
[`AbstractFuture<V>`]: https://google.github.io/guava/releases/snapshot/api/docs/com/google/common/util/concurrent/AbstractFuture.html
[`SettableFuture`]: https://google.github.io/guava/releases/snapshot/api/docs/com/google/common/util/concurrent/SettableFuture.html
[`JdkFutureAdapters.listenInPoolThread(Future)`]: https://google.github.io/guava/releases/snapshot/api/docs/com/google/common/util/concurrent/JdkFutureAdapters.html
[`JdkFutureAdapters.listenInPoolThread(Future)`]: https://google.github.io/guava/releases/snapshot/api/docs/com/google/common/util/concurrent/JdkFutureAdapters.html
[the implementation of `Futures.allAsList`]: https://google.github.io/guava/releases/snapshot/api/docs/src-html/com/google/common/util/concurrent/Futures.html#line.1276
[`transformAsync(ListenableFuture<A>, AsyncFunction<A, B>, Executor)`]: https://google.github.io/guava/releases/snapshot/api/docs/com/google/common/util/concurrent/Futures.html#transformAsync-com.google.common.util.concurrent.ListenableFuture-com.google.common.util.concurrent.AsyncFunction-java.util.concurrent.Executor-
[`transformAsync(ListenableFuture<A>, AsyncFunction<A, B>)`]: https://google.github.io/guava/releases/snapshot/api/docs/com/google/common/util/concurrent/Futures.html#transformAsync-com.google.common.util.concurrent.ListenableFuture-com.google.common.util.concurrent.AsyncFunction-
[`transform(ListenableFuture<A>, Function<A, B>, Executor)`]: https://google.github.io/guava/releases/snapshot/api/docs/com/google/common/util/concurrent/Futures.html#transform-com.google.common.util.concurrent.ListenableFuture-com.google.common.base.Function-java.util.concurrent.Executor-
[`transform(ListenableFuture<A>, Function<A, B>)`]: https://google.github.io/guava/releases/snapshot/api/docs/com/google/common/util/concurrent/Futures.html#transform-com.google.common.util.concurrent.ListenableFuture-com.google.common.base.Function-
[`allAsList(Iterable<ListenableFuture<V>>)`]: https://google.github.io/guava/releases/snapshot/api/docs/com/google/common/util/concurrent/Futures.html#allAsList-java.lang.Iterable-
[`allAsList(ListenableFuture<V>...)`]: https://google.github.io/guava/releases/snapshot/api/docs/com/google/common/util/concurrent/Futures.html#allAsList-com.google.common.util.concurrent.ListenableFuture...-
[`successfulAsList(Iterable<ListenableFuture<V>>)`]: https://google.github.io/guava/releases/snapshot/api/docs/com/google/common/util/concurrent/Futures.html#successfulAsList-java.lang.Iterable-
[`successfulAsList(ListenableFuture<V>...)`]: https://google.github.io/guava/releases/snapshot/api/docs/com/google/common/util/concurrent/Futures.html#successfulAsList-com.google.common.util.concurrent.ListenableFuture...-
[`AsyncFunction<A, B>`]: https://google.github.io/guava/releases/snapshot/api/docs/com/google/common/util/concurrent/AsyncFunction.html
[`submitAsync(AsyncCallable<A>, Executor)`]:
https://google.github.io/guava/releases/snapshot/api/docs/com/google/common/util/concurrent/Futures.html#submitAsync-com.google.common.util.concurrent.AsyncCallable-java.util.concurrent.Executor-    
    
    