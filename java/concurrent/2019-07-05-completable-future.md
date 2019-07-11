# Java CompletableFuture Tutorial with Examples

[Java CompletableFuture Tutorial with Examples | CalliCoder](https://www.callicoder.com/java-8-completablefuture-tutorial/)

Java 8 有非常多的特性和增强，比如 [Lambda expressions](https://www.callicoder.com/java-lambda-expression-tutorial/)、[Streams](https://docs.oracle.com/javase/8/docs/api/java/util/stream/package-summary.html) 以及 [CompletableFutures](https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/CompletableFuture.html) 等等。本文使用示例详细介绍 CompletableFutures 类及其相关方法。

# CompletableFuture 是什么

CompletableFuture 用于 Java 异步编程。异步编程是通过在独立线程而非主线程中运行任务的方式来实现非阻塞代码。任务的进度，以及任务结果(完成或失败)都会通知给主线程。 

如此一来，主线程可以并发执行其他任务而不必阻塞/等待当前任务完成。

这种并发可以极大地提升程序性能。

其他阅读：[Java Concurrency and Multithreading Basics](https://www.callicoder.com/java-concurrency-multithreading-basics/)

# Future vs CompletableFuture

CompletableFuture 是对 Java 5 引入的 [Java’s Future API](https://docs.oracle.com/javase/7/docs/api/java/util/concurrent/Future.html) 的扩展。

Future 用作异步调用的结果的引用。它提供 `isDone()` 方用于检查调用是否完成，以及 `get()` 方法用于获取调用结果。

你可以通过我的另一篇文章 [Callable and Future Tutorial](https://www.callicoder.com/java-callable-and-future-tutorial/) 来了解关于 Future 更多的内容。

Future API 是 Java 中迈向异步编程的一个良好步骤，但它缺少一些重要和有用的特性。

## Limitations of Future

1.**它无法手动完成**：

假设你写了一个函数用于从远程 API 获取某个电商产品的最新价格。这个 API 调用很耗时，所以在一个独立线程中调用该函数，并且函数返回 Future。

现在，假设这个远程 API 挂掉了，你想手动完成 Future (使用上次缓存的商品结果代替最新价格)。

可以使用 Future 实现吗？不行。

2.**You cannot perform further action on a Future’s result without blocking**:

Future 没法通知其完成结果。Future 的 `get()` 方法会一直阻塞直到结果可用。

没法向 Future 添加回调函数，让回调函数在 Future 结果可用时自动回调。

3.**Multiple Futures cannot be chained together**:

有时需要执行长时间计算，当计算结束时，将结果发向另一个长时间计算过程，如此持续进行。

你无法使用 Future 创建上述异步工作流。

4.**You can not combine multiple Futures together**:

假如你有10个不同的 Future，你想并发执行这些 Future 并且在所有 Future 完成后执行某些函数。你没法使用 Future 完成这类任务。

5.**No Exception Handling**:

Future API 不支持任何异常处理。

这么多限制？好吧，这就是为什么我们需要 CompletableFuture。你可以使用 CompletableFuture 突破上述这些限制。

CompletableFuture 实现 `Future` 和 `CompletionStage` 接口，并且提供大量便捷的方法用于创建、链式调用以及组合调用多个 Future。它还支持复杂的异常处理。

# Creating a CompletableFuture
1.一个小例子

可以使用无参构造方法创建一个 CompletableFuture：

```java
CompletableFuture<String> completableFuture = new CompletableFuture<String>();
```

这是最简单的 CompletableFuture。所有想得到该 CompletableFuture 结果的客户可以调用 `CompletableFuture.get()` 方法。

```java
String result = completableFuture.get();
```

`get()` 方法会阻塞直接 Future 结束。所以上面代码会一直阻塞，因为 Future 不会结束。

可以调用 `CompletableFuture.complete()` 来手动结束 Future。

```java
completableFuture.complete("Future's Result");
```

所以在 Future 上等待的客户将会接收到这个结果。重复调用 `completableFuture.complete()` 方法的话，后面的调用会被忽略。


2.Running asynchronous computation using runAsync()

如果你想异步执行某些后台任务，而且不需要从这些任务中返回结果，可以使用 `CompletableFuture.runAsync()` 方法。它接收一个 `Runnable` 对象作为参数，并且返回 `CompletableFuture<Void>`。

```java
// Run a task specified by a Runnable Object asynchronously.
CompletableFuture<Void> future = CompletableFuture.runAsync(new Runnable() {
    @Override
    public void run() {
        // Simulate a long-running Job
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            throw new IllegalStateException(e);
        }
        System.out.println("I'll run in a separate thread than the main thread.");
    }
});

// Block and wait for the future to complete
future.get()
```

也可以使用 [lambda expression](https://www.callicoder.com/java-lambda-expression-tutorial/) 的方法来传入 Runnable 对象。

```java
// Using Lambda Expression
CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
    // Simulate a long-running Job   
    try {
        TimeUnit.SECONDS.sleep(1);
    } catch (InterruptedException e) {
        throw new IllegalStateException(e);
    }
    System.out.println("I'll run in a separate thread than the main thread.");
});
```

3.Run a task asynchronously and return the result using supplyAsync()

`CompletableFuture.runAsync()` 作于不返回结果的任务非常有效。但如果你想从后台任务中返回结果呢？ 

这进就要使用 `CompletableFuture.supplyAsync()` 了。它接收 `Supplier<T>` 并且返回  `CompletableFuture<T>`，这里的 `T` 是调用指定 supplier 返回的值的类型。

```java
// Run a task specified by a Supplier object asynchronously
CompletableFuture<String> future = CompletableFuture.supplyAsync(new Supplier<String>() {
    @Override
    public String get() {
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            throw new IllegalStateException(e);
        }
        return "Result of the asynchronous computation";
    }
});

// Block and get the result of the Future
String result = future.get();
System.out.println(result);
```

[Supplier<T>](https://docs.oracle.com/javase/8/docs/api/java/util/function/Supplier.html) 是一个简单的函数式接口，它表示结果的提供者。该接口仅有一个 `get()` 方法用于返回结果。

```java
// Using Lambda Expression
CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
    try {
        TimeUnit.SECONDS.sleep(1);
    } catch (InterruptedException e) {
        throw new IllegalStateException(e);
    }
    return "Result of the asynchronous computation";
});
```

You might be wondering that - Well, I know that the runAsync() and supplyAsync() methods execute their tasks in a separate thread. But, we never created a thread right?

你可能好奇，我知道 `runAsync()` 和 `supplyAsync()` 方法是在独立线程中执行任务。但我们并没有创建线程啊？

对。CompletableFuture 在独立线程中执行任务，线程从 `ForkJoinPool.commonPool()` 获取。

但你也可以创建线程池并将其提供给 `runAsync()` 和 `supplyAsync()` 方法，让这些方法在指定的线程中运行任务。

CompletableFuture API 中所有的方法都有两个版本，一个接收 `Executor` 参数，另一个不接收。

```java
// Variations of runAsync() and supplyAsync() methods
static CompletableFuture<Void>  runAsync(Runnable runnable)
static CompletableFuture<Void>  runAsync(Runnable runnable, Executor executor)
static <U> CompletableFuture<U> supplyAsync(Supplier<U> supplier)
static <U> CompletableFuture<U> supplyAsync(Supplier<U> supplier, Executor executor)
```

创建线程池并将其传给 `supplyAsync()` 的示例如下：

```java
Executor executor = Executors.newFixedThreadPool(10);
CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
    try {
        TimeUnit.SECONDS.sleep(1);
    } catch (InterruptedException e) {
        throw new IllegalStateException(e);
    }
    return "Result of the asynchronous computation";
}, executor);
```

# Transforming and acting on a CompletableFuture

`CompletableFuture.get()` 方法会阻塞。它等待 Future 完成，并在完成后返回结果。

但如果这不是我们想要的行为怎么办？为了构建异步系统我们需要向 CompletableFuture 添加回调，该回调会在 Future 完成时自动被调用。

通过这种方式，我们不必一直等待结果，而是在回调函数中写逻辑，这些逻辑会在 Future 完成后被执行。

可以使用 `thenApply()`、`thenAccept()` 及 `thenRun()` 方法向 CompletableFuture 添加回调。

## thenApply()
使用 `thenApply()` 方法处理和变换 CompletableFuture 的结果。它接收 [Function<T,R>](https://docs.oracle.com/javase/8/docs/api/java/util/function/Function.html) 作为参数。[Function<T,R>](https://docs.oracle.com/javase/8/docs/api/java/util/function/Function.html) 是一个简单的函数式接口，它表示一个接收 `T` 参数并且产生 `R` 结果的函数。

```java
// Create a CompletableFuture
CompletableFuture<String> whatsYourNameFuture = CompletableFuture.supplyAsync(() -> {
   try {
       TimeUnit.SECONDS.sleep(1);
   } catch (InterruptedException e) {
       throw new IllegalStateException(e);
   }
   return "Rajeev";
});

// Attach a callback to the Future using thenApply()
CompletableFuture<String> greetingFuture = whatsYourNameFuture.thenApply(name -> {
   return "Hello " + name;
});

// Block and get the result of the future.
System.out.println(greetingFuture.get()); // Hello Rajeev
```

还可以在 CompletableFuture 上写一系列的 `thenApply()` 回调方法来实现连续变换。

```java
CompletableFuture<String> welcomeText = CompletableFuture.supplyAsync(() -> {
    try {
        TimeUnit.SECONDS.sleep(1);
    } catch (InterruptedException e) {
       throw new IllegalStateException(e);
    }
    return "Rajeev";
}).thenApply(name -> {
    return "Hello " + name;
}).thenApply(greeting -> {
    return greeting + ", Welcome to the CalliCoder Blog";
});

System.out.println(welcomeText.get());
// Prints - Hello Rajeev, Welcome to the CalliCoder Blog
```

## thenAccept() and thenRun()
如果只想在 Future 完成后执行一些代码，而不必从回调函数中返回任何值，可以使用 `thenAccept()` 或 `thenRun()` 方法。这些方法作为消费者，通常是回调链中的最后一个回调。

`CompletableFuture.thenAccept()` 接收 [Consumer<T>](https://docs.oracle.com/javase/8/docs/api/java/util/function/Consumer.html) 并返回 `CompletableFuture<Void>`。它可以访问到 CompletableFuture 的结果。

```java
// thenAccept() example
CompletableFuture.supplyAsync(() -> {
	return ProductService.getProductDetail(productId);
}).thenAccept(product -> {
	System.out.println("Got product detail from remote service " + product.getName())
});
```

与 `thenAccept()` 可以访问到 CompletableFuture 的结果不同，`thenRun()` 不能访问 CompletableFuture 的结果。它只是接收一个 `Runnable` 并且返回 `CompletableFuture<Void>`。

```java
// thenRun() example
CompletableFuture.supplyAsync(() -> {
    // Run some computation  
}).thenRun(() -> {
    // Computation Finished.
});
```

CompletableFuture 所有的回调方法都有两个异步版本：

```java
// thenApply() variants
<U> CompletableFuture<U> thenApply(Function<? super T,? extends U> fn)
<U> CompletableFuture<U> thenApplyAsync(Function<? super T,? extends U> fn)
<U> CompletableFuture<U> thenApplyAsync(Function<? super T,? extends U> fn, Executor executor)
```

这些异步回调允许在独立线程中执行回调任务，这有利于更好地并发调用。

考虑这个例子：

```java
CompletableFuture.supplyAsync(() -> {
    try {
       TimeUnit.SECONDS.sleep(1);
    } catch (InterruptedException e) {
      throw new IllegalStateException(e);
    }
    return "Some Result"
}).thenApply(result -> {
    /* 
      Executed in the same thread where the supplyAsync() task is executed
      or in the main thread If the supplyAsync() task completes immediately (Remove sleep() call to verify)
    */
    return "Processed Result"
})
```

这个例子中 `thenApply()` 中的任务在执行 `supplyAsync()` 任务的同一个线程中执行，如果 `supplyAsync()` 任务立即结束则 `thenApply()` 在主线程中运行。(可以移除 `sleep()` 调用来验证)

**我试了一下，`thenApply()` 果然可能在不同的线程中运行，感觉这是个坑**

要想更精确地控制回调任务在哪个线程执行，可以使用异步回调。如果使用 `thenApplyAsync()` 回调，则它将在不同的线程中执行，这个线程由 `ForkJoinPool.commonPool()` 获取。

```java
CompletableFuture.supplyAsync(() -> {
    return "Some Result"
}).thenApplyAsync(result -> {
    // Executed in a different thread from ForkJoinPool.commonPool()
    return "Processed Result"
})
```

另外，如果向 `thenApplyAsync()` 回调传一个 Executor 参数，则该回调任务会在 Executor 的线程池中获取到的线程中执行。

```java
Executor executor = Executors.newFixedThreadPool(2);
CompletableFuture.supplyAsync(() -> {
    return "Some result"
}).thenApplyAsync(result -> {
    // Executed in a thread obtained from the executor
    return "Processed Result"
}, executor);
```

## Combining two CompletableFutures together
1. Combine two dependent futures using thenCompose() -
Let’s say that you want to fetch the details of a user from a remote API service and once the user’s detail is available, you want to fetch his Credit rating from another service.

Consider the following implementations of getUserDetail() and getCreditRating() methods -

```java
CompletableFuture<User> getUsersDetail(String userId) {
	return CompletableFuture.supplyAsync(() -> {
		UserService.getUserDetails(userId);
	});	
}

CompletableFuture<Double> getCreditRating(User user) {
	return CompletableFuture.supplyAsync(() -> {
		CreditRatingService.getCreditRating(user);
	});
}
```

Now, Let’s understand what will happen if we use thenApply() to achieve the desired result -

```java
CompletableFuture<CompletableFuture<Double>> result = getUserDetail(userId)
.thenApply(user -> getCreditRating(user));
```

In earlier examples, the Supplier function passed to thenApply() callback would return a simple value but in this case, it is returning a CompletableFuture. Therefore, the final result in the above case is a nested CompletableFuture.

If you want the final result to be a top-level Future, use thenCompose() method instead -

```java
CompletableFuture<Double> result = getUserDetail(userId)
.thenCompose(user -> getCreditRating(user));
```

*So, Rule of thumb here - If your callback function returns a CompletableFuture, and you want a flattened result from the CompletableFuture chain (which in most cases you would), then use thenCompose().*

# 参考

[JDK中CompletableFuture类](https://mp.weixin.qq.com/s?__biz=MzU2MTA1OTgyMQ==&mid=2247484865&idx=1&sn=1133bfbb9598d327f1437253c4ebd8ce&chksm=fc7fc743cb084e55d2ad3e58b28b59cf41bb99232b6513e34efaa629decee099bc8c550818a8&mpshare=1&scene=1&srcid=0710Ax1qfElo71VMVCXOl0oq#rd)