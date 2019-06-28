# completablefuture 跟 rxjava 的比较

[来源](https://stackoverflow.com/questions/35329845/difference-between-completablefuture-future-and-rxjavas-observable)

## Futures
[Futures](https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/Future.html) 是在 Java 5(2004) 引入进来的。它是尚未完成的调用过程的占位符。它们是一种承诺：一旦某个操作结束 `Future` 会持有它的结果。操作可以是已提交给 [ExecutorService](https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/ExecutorService.html) 的 [Runnable](https://docs.oracle.com/javase/8/docs/api/java/lang/Runnable.html) 或 [Callable](https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/Callable.html) 实例。操作的提交者可以使用 `Future` 对象检查操作是否已完成 ([isDone()](https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/Future.html#isDone--) 方法) 还是需要等待 ([get()](https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/Future.html#get-long-java.util.concurrent.TimeUnit-) 方法)。

例子：

```java
/**
* A task that sleeps for a second, then returns 1
**/
public static class MyCallable implements Callable<Integer> {

    @Override
    public Integer call() throws Exception {
        Thread.sleep(1000);
        return 1;
    }

}

public static void main(String[] args) throws Exception{
    ExecutorService exec = Executors.newSingleThreadExecutor();
    Future<Integer> f = exec.submit(new MyCallable());

    System.out.println(f.isDone()); //False

    System.out.println(f.get()); //Waits until the task is done, then prints 1
}
```

## CompletableFutures
[CompletableFutures](https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/CompletableFuture.html) 是在 Java 8(2014) 引入进来的。它实际上是常规 `Future` 的演进，并且受到 Google [Guava](https://github.com/google/guava) 库中 [Listenable Futures](https://github.com/google/guava/wiki/ListenableFutureExplained) 的启发。`CompletableFuture` 支持链式调用。你可以通过 `CompletableFuture` 告诉工作线程完成任务X，完成任务X后使用其结果来处理下一个任务。这里有一个简单的例子：

```java
/**
* A supplier that sleeps for a second, and then returns one
**/
public static class MySupplier implements Supplier<Integer> {

    @Override
    public Integer get() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            //Do nothing
        }
        return 1;
    }
}

/**
* A (pure) function that adds one to a given Integer
**/
public static class PlusOne implements Function<Integer, Integer> {

    @Override
    public Integer apply(Integer x) {
        return x + 1;
    }
}

public static void main(String[] args) throws Exception {
    ExecutorService exec = Executors.newSingleThreadExecutor();
    CompletableFuture<Integer> f = CompletableFuture.supplyAsync(new MySupplier(), exec);
    System.out.println(f.isDone()); // False
    CompletableFuture<Integer> f2 = f.thenApply(new PlusOne());
    System.out.println(f2.get()); // Waits until the "calculation" is done, then prints 2
}
```

## RxJava
[RxJava](https://github.com/ReactiveX/RxJava) 是一个完整的 [reactive programming](https://en.wikipedia.org/wiki/Reactive_programming) 代码库，由 Netflix。初看它跟 [Java 8's streams](http://www.oracle.com/technetwork/articles/java/ma14-java-se-8-streams-2177646.html) 的接口很像，但其实 RxJava 要强大得多。
 
跟 `Future` 类似，RxJava 也可以链式调用一堆同步或异步的操作来创建处理流。像跟只能单一使用的 `Future` 不同，RxJava 可以支持有0个或多个数据项的流，包括无上限、不会中止的流。归功于特别丰富的 [操作符](http://reactivex.io/documentation/operators.html)，RxJava 比 Future 灵活和强大许多。

跟 Java 8 Stream API 不同，RxJava 还有 [backpressure 机制](http://reactivex.io/documentation/operators/backpressure.html) ，这种机制用于处理管道中不同部分以不同的速度运行在不同线程的场景。

RxJava 的缺点是尽管文档丰富，但由于涉及到 paradigm shift，它还是一个有很大上手难度的库。RxJava 写的代码不好调试，尤其是涉及到多线程时，如果再考虑到背压情况更糟糕。

如果你想了解它，可以看这个[文档](http://reactivex.io/tutorials.html)，官网提供不同的教程。另外还有[官方文档](http://reactivex.io/documentation/observable.html) 和 [Javadoc](http://reactivex.io/RxJava/javadoc/)。也可以看看[这个视频](https://www.youtube.com/watch?v=_t06LRX0DV0)简单了解 Rx 以及它和 Future 的不同。

**Bonus: Java 9 Reactive Streams**

[Java 9's Reactive Streams](https://community.oracle.com/docs/DOC-1006738) aka [Flow API](https://docs.oracle.com/javase/9/docs/api/java/util/concurrent/Flow.html) 是一套接口，由不同响应式代码库(如 [RxJava 2](https://github.com/ReactiveX/RxJava/wiki/Reactive-Streams)，[Akka Streams](http://doc.akka.io/docs/akka-stream-and-http-experimental/1.0-M2/stream-design.html)，[Vertx](http://vertx.io/))实现。这套接口允许不同的代码库之间可以交互，并且保留 back-pressure 机制。