翻译自 [Guide To CompletableFuture](https://www.baeldung.com/java-completablefuture)

# 介绍
这篇文章介绍 Java 8 并发 API 中引入的 `CompletableFuture` 类的功能和用法。

# Java 中的异步调用
异步调用很困难。通常我们认为调用是串行过程。但一旦涉及到异步调用，**由回调表示的动作要么散布在代码中，要么嵌套很深**。考虑到还是处理其中可能出现的异常，事情变得更加复杂。

Java 5 中添加 `Future` 接口，用于表示异步调用的结果。但这个接口既没有方法用于组合调用，也不能处理错误/异常。

**Java 8 引入了 `CompletableFuture` 类**。除了继承自 `Future` 接口，它还实现了 `CompletionStage`，这个接口定义了一个异步调用如何跟其他步骤组合。

`CompletableFuture` 同时还是一个构建块以及一个拥有 50 多个不同方法的框架。这些方法用于 compose, combine, 以及执行异步和处理错误。

API 数量确实有点多，但它们大部分用于应对若干清晰和明确的使用场景。

# 将 `CompletableFuture` 用作 `Future`

首先，`CompletableFuture` 类实现了 `Future` 接口。所以你可以 **将 CompletableFuture 作为 Future 的实现来使用，但它有额外的 completion 逻辑**。

比如，你可以调用无参构造方法创建这个类的实例来表示异步调用的结果，并将其交给消费者，最后在未来某个时刻调用 `complete` 方法来完成这个异步调用。而消费者可以使用 `get` 方法阻塞当前线程，直到异步调用有结果。

下面这个例子中的方法创建了 `CompletableFuture` 的实例，然后在另一个线程执行计算，并立即将 `Future` 返回。

当调用完成后，调用 `complete` 方法来返回结果并结束 `Future`。

```java
public Future<String> calculateAsync() throws InterruptedException {
    CompletableFuture<String> completableFuture 
      = new CompletableFuture<>();
 
    Executors.newCachedThreadPool().submit(() -> {
        Thread.sleep(500);
        completableFuture.complete("Hello");
        return null;
    });
 
    return completableFuture;
}
```

使用 Executor API 在后台线程中进行异步调用，具体见 [“Introduction to Thread Pools in Java”](https://www.baeldung.com/thread-pool-java-and-guava)，但是这里 `calculateAsync()` 方法创建和返回的 `CompletableFuture` 可以跟任何其他并发机制或并发 API (包括原始的 Thread)一起使用。

注意 `calculateAsync` 返回的是 `Future` 实例。

我们只是简单地调用方法，接收返回的 `Future` 实例，并且调用 `get` 方法来等待结果。

另外要注意 `get` 方法会抛出受检查异常，`ExecutionException` (它封装了异步调用中发生的异常) 和 `InterruptedException` (这个异常表示线程执行方法时被中断)。

```java
Future<String> completableFuture = calculateAsync();
 
// ... 
 
String result = completableFuture.get();
assertEquals("Hello", result);
```

**如果已经知道调用结果**，可以使用静态方法 `completedFuture`。这个方法有一个参数，代表调用结果。这种情况下 `Future.get` 方法不会阻塞，而是立即返回结果。

```java
Future<String> completableFuture = 
  CompletableFuture.completedFuture("Hello");
 
// ...
 
String result = completableFuture.get();
assertEquals("Hello", result);
```

另外的场景中，**你可能想取消 `Future` 上的调用**。

假设没法找到结果，所以想取消异步调用。可以使用 `Future.cancel()` 方法取消。这个方法接收一个 `boolean` 类型的 `mayInterruptIfRunning` 参数。但这个参数对 `CompletableFuture` 无效，因为并不使用该参数来控制 `CompletableFuture` 的处理流程。

这是一个例子：

```java
public Future<String> calculateAsyncWithCancellation() throws InterruptedException {
    CompletableFuture<String> completableFuture = new CompletableFuture<>();
 
    Executors.newCachedThreadPool().submit(() -> {
        Thread.sleep(500);
        completableFuture.cancel(false);
        return null;
    });
 
    return completableFuture;
}
```

如果 future 被取消，阻塞的 `Future.get()` 会抛出 `CancellationException`。

```java
Future<String> future = calculateAsyncWithCancellation();
future.get(); // CancellationException
```

# CompletableFuture with Encapsulated Computation Logic
以上代码允许我们使用允许并发机制，但如果想跳过这些模板代码来简单地执行异步代码呢？

`runAsync` 和 `supplyAsync` 两个静态方法允许避开 `Runnable` 和 `Supplier` 来直接创建 `CompletableFuture` 实例。

归功于 Java 8 新特性，可以使用 `Runnable` 和 `Supplier` 实例作为 lambda 表达式。

`Runnable` 接口跟用于 thread 的老接口一致，它不能返回结果。

`Supplier` 接口是一个泛型函数接口，它有一个无参方法，并且返回一个参数化的值。

可以使用 `Supplier` 的实例来作为 lambda 表达式，这个表达式执行计算并且返回结果。

```java
CompletableFuture<String> future
  = CompletableFuture.supplyAsync(() -> "Hello");
 
// ...
 
assertEquals("Hello", future.get());
```

# Processing Results of Asynchronous Computations
处理调用结果的最通用方式是为其提供一个函数。`thenApply` 方法正是如此：接收一个 `Function` 实例，用这个实例处理结果并返回另一个 `Future`，它持有函数返回的值。

```java
CompletableFuture<String> completableFuture
  = CompletableFuture.supplyAsync(() -> "Hello");
 
CompletableFuture<String> future = completableFuture
  .thenApply(s -> s + " World");
 
assertEquals("Hello World", future.get());
```

如果不需要在 `Future` 链中返回值，可以使用 `Consumer` 接口。这个接口只有一个方法，该方法接收一个参数，无返回值。

`CompletableFuture.thenAccept` 方法可以用于这种场景。该方法接收 `Consumer`，并且将调用结果传给它。最终的 `future.get()` 调用返回 `Void` 类型的实例。

```java
CompletableFuture<String> completableFuture
  = CompletableFuture.supplyAsync(() -> "Hello");
 
CompletableFuture<Void> future = completableFuture
  .thenAccept(s -> System.out.println("Computation returned: " + s));
 
future.get();
```

最后，如果你既不需要计算结果，也不需要在调用链结束时收到任何返回值，可以传 `Runnable` lambda 表达式给 `thenRun` 方法。下面的例子中，`future.get()` 调用后，只是简单地打印一个字符串：

```java
CompletableFuture<String> completableFuture 
  = CompletableFuture.supplyAsync(() -> "Hello");
 
CompletableFuture<Void> future = completableFuture
  .thenRun(() -> System.out.println("Computation finished."));
 
future.get();
```

## Combining Futures
`CompletableFuture` API 最好的地方在于可以用链式方式来组合多个 `CompletableFuture` 实例。

这条链的结果也是一个 `CompletableFuture`，允许进一步链式调用和组合。这种方式在函数式编程语言中无处不在，深深被认为是 monadic design pattern。

在下面的例子中使用 `thenCompose` 来将两个 `Future` 串起来：

注意该方法接收一个函数并返回一个 `CompletableFuture` 实例。这个函数的参数是前一次调用的结果。这允许我们在下一个 `CompletableFuture` lambda 中使用该值：

```java
CompletableFuture<String> completableFuture 
  = CompletableFuture.supplyAsync(() -> "Hello")
    .thenCompose(s -> CompletableFuture.supplyAsync(() -> s + " World"));
 
assertEquals("Hello World", completableFuture.get());
```

`thenCompose` 和 `thenApply` 方法一直使用，实现了一个基本的 monadic pattern 构建块。它们跟 `Stream` 的 `map` 和 `flatMap` 方法密切相关。`Stream` 和 `Optional` 也是 Java 8 新提供的。

两个方法都接收一个函数，并且将其应用于调用结果，但是 `thenCompose` (flatMap) 方法接收函数并返回另一个有相同的类型的结果，。这个函数式结构允许将这种类型的实例组合成构建块。

如果想执行两个独立的 `Future` 并且处理它们的结果，可以使用 `thenCombine` 方法。该方法接收一个 `Future`，以及一个 `Function`。这个 `Function` 有两个参数，即前两个 `Future` 的结果。

```java
CompletableFuture<String> completableFuture 
  = CompletableFuture.supplyAsync(() -> "Hello")
    .thenCombine(CompletableFuture.supplyAsync(
      () -> " World"), (s1, s2) -> s1 + s2));
 
assertEquals("Hello World", completableFuture.get());
```

一个更简单的场景是如果你想对两个 `Future` 的结果进行处理，但并不想传任何结果到 `Future` 链。可以使用 `thenAcceptBoth` 方法：

```java
CompletableFuture future = CompletableFuture.supplyAsync(() -> "Hello")
  .thenAcceptBoth(CompletableFuture.supplyAsync(() -> " World"),
    (s1, s2) -> System.out.println(s1 + s2));
```

# Difference Between thenApply() and thenCompose()
前面几节中展示了 `thenApply()` 和 `thenCompose()` 相关的例子。这两个 API 都可以链式调用不同的 `CompletableFuture` 方法，但两者又有所不同。

## thenApply()
这个方法用于处理前一个请求的结果。但要记住一个关键点是其返回类型会用于所有调用。

所以我们想要转换 `CompletableFuture` 调用的结果时这个方法很有用。

```java
CompletableFuture<Integer> finalResult = compute().thenApply(s-> s + 1);
```

## thenCompose()

`thenCompose()` 方法跟 `thenApply()` 类似，它也返回一个新的 Completion Stage。但是，`thenCompose()` 将前一个 stage 视为参数。它会对其进行 flatten 处理并直接返回带有结果的 `Future`，而不是像 `thenApply()` 方法那样返回一个嵌套的 future。

```java
CompletableFuture<Integer> computeAnother(Integer i){
    return CompletableFuture.supplyAsync(() -> 10 + i);
}
CompletableFuture<Integer> finalResult = compute().thenCompose(this::computeAnother);
```

所以如果目的是链式处理 `CompletableFuture` 的方法，使用 `thenCompose()` 更好。

另外注意两者之间的不同跟[the difference between map() and flatMap()](https://www.baeldung.com/java-difference-map-and-flatmap)中提到的很像。

# Running Multiple Futures in Parallel
如果想并发执行多个 `Future`，通常我们会等待所有 `Future` 执行完毕后再来处理其结果。

`CompletableFuture.allOf` 静态方法允许等待多个 `Future` 执行完成：:

```java
CompletableFuture<String> future1  
  = CompletableFuture.supplyAsync(() -> "Hello");
CompletableFuture<String> future2  
  = CompletableFuture.supplyAsync(() -> "Beautiful");
CompletableFuture<String> future3  
  = CompletableFuture.supplyAsync(() -> "World");
 
CompletableFuture<Void> combinedFuture 
  = CompletableFuture.allOf(future1, future2, future3);
 
// ...
 
combinedFuture.get();
 
assertTrue(future1.isDone());
assertTrue(future2.isDone());
assertTrue(future3.isDone());
```

注意 `CompletableFuture.allOf()` 的返回值类型是 `CompletableFuture<Void>`。这个限制让该方法不能返回所有 Future 的结果。 (The limitation of this method is that it does not return the combined results of all Futures) 你得手动获取 `Future` 的结果。幸运的是， `CompletableFuture.join()` 方法以及 Java 8 Streams API 让事情变得简单:

```java
String combined = Stream.of(future1, future2, future3)
  .map(CompletableFuture::join)
  .collect(Collectors.joining(" "));
 
assertEquals("Hello Beautiful World", combined);
```

`CompletableFuture.join()` 方法跟 `get` 方法类似，但会在 `Future` 不能正常结束时抛出一个不受检查的异常。所以可以将其用作 `Stream.map()` 方法的引用。

# Handling Errors
链式异步调用时的错误处理，`throw/catch` 也有类似的变化。

`CompletableFuture` 类允许在一个特殊的 `handle` 方法中处理异常，而不是一个语句块。这个方法接收两个参数：调用结果 (如果成功结束的话)，以及抛出的异常 (如果某些调用没有正常结束)。

下面的例子中，如果异步调用因错误(找不到名字)而结束，会使用 `handle` 方法来提供缺省值。

```java
String name = null;
 
// ...
 
CompletableFuture<String> completableFuture  
  =  CompletableFuture.supplyAsync(() -> {
      if (name == null) {
          throw new RuntimeException("Computation error!");
      }
      return "Hello, " + name;
  })}).handle((s, t) -> s != null ? s : "Hello, Stranger!");
 
assertEquals("Hello, Stranger!", completableFuture.get());
```

另外一种场景是，假如我们想手动以某个值正常结束 `Future`，同时还保留以异常结束该 `Future` 的能力。这时可以使用 `completeExceptionally` 方法。下面例子中的 `completableFuture.get()` 抛出  `ExecutionException` 异常 ( 其 cause 为 `RuntimeException`)。

```java
CompletableFuture<String> completableFuture = new CompletableFuture<>();
 
// ...
 
completableFuture.completeExceptionally(
  new RuntimeException("Calculation failed!"));
 
// ...
 
completableFuture.get(); // ExecutionException
```

这个例子也可以使用 `handle` 方法异步处理异常。但使用 `get` 方法可以以更典型的同步方式进行异常处理。(In the example above we could have handled the exception with the handle method asynchronously, but with the get method we can use a more typical approach of a synchronous exception processing.)

# Async Methods
`CompletableFuture` 类流式 API 中的大部分方法有两个带 Async 后缀的变体。这些方法通常用于在另一个线程中运行指定的步骤。

不带 Async 后缀的方法使用当前线程来执行下一个 execution stage。不带 `Executor` 参数的 Async 方法在 `fork/join` 池实现的 `Executor` 中运行下一个步骤 ( 通过 `ForkJoinPool.commonPool()` 方法获取这个 `Executor`)。而带有 `Executor` 参数的 Async 方法在指定的 `Executor` 中运行下一个步骤。

下面例子中使用 `Function` 实例来处理前面的调用结果。但是在底层该函数被包装成 `ForkJoinTask` 实例(具体细节可以参考 fork/join 框架，见 [“Guide to the Fork/Join Framework in Java”](https://www.baeldung.com/java-fork-join))。这允许调用过程更允分地并发执行，以更充分地使用系统资源。

```java
CompletableFuture<String> completableFuture  
  = CompletableFuture.supplyAsync(() -> "Hello");
 
CompletableFuture<String> future = completableFuture
  .thenApplyAsync(s -> s + " World");
 
assertEquals("Hello World", future.get());
```

# JDK 9 CompletableFuture API
Java 9 中 `CompletableFuture` API 进一步增强，变更如下：

+ New factory methods added
+ Support for delays and timeouts
+ Improved support for subclassing.

引入新的 API：

+ `Executor defaultExecutor()`
+ `CompletableFuture<U> newIncompleteFuture()`
+ `CompletableFuture<T> copy()`
+ `CompletionStage<T> minimalCompletionStage()`
+ `CompletableFuture<T> completeAsync(Supplier<? extends T> supplier, Executor executor)`
+ `CompletableFuture<T> completeAsync(Supplier<? extends T> supplier)`
+ `CompletableFuture<T> orTimeout(long timeout, TimeUnit unit)`
+ `CompletableFuture<T> completeOnTimeout(T value, long timeout, TimeUnit unit)`

引入新的工具方法：

+ `Executor delayedExecutor(long delay, TimeUnit unit, Executor executor)`
+ `Executor delayedExecutor(long delay, TimeUnit unit)`
+ `<U> CompletionStage<U> completedStage(U value)`
+ `<U> CompletionStage<U> failedStage(Throwable ex)`
+ `<U> CompletableFuture<U> failedFuture(Throwable ex)`

最后 Java 9 还引入了两个新的函数来支持 timeout：

+ `orTimeout()`
+ `completeOnTimeout()`

进一步阅读： [Java 9 CompletableFuture API Improvements](https://www.baeldung.com/java-9-completablefuture)

# Conclusion
这篇文章中我们讨论了 `CompletableFuture` 类的方法以及典型使用场景。

本文的源码见[GitHub](https://github.com/eugenp/tutorials/tree/master/core-java-modules/core-java-concurrency-basic)。