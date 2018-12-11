[Kotlin Coroutines on Android: Things I Wish I Knew at the Beginning](https://medium.com/capital-one-tech/kotlin-coroutines-on-android-things-i-wish-i-knew-at-the-beginning-c2f0b1f16cff)

[(2) KotlinConf 2017 - Introduction to Coroutines by Roman Elizarov - YouTube](https://www.youtube.com/watch?v=_hfBv0a09Jc)

https://github.com/Kotlin/kotlinx.coroutines

# 为什么要使用协程

```kotlin
data class Token(val data: String)

data class Item(val data: String)

data class Post(val data: String)
```

假设这个场景：

```kotlin
fun requestToken(): Token {
    val token = Token("123")
    // 耗时操作
    // TimeUnit.MILLISECONDS.sleep(100) 
    return token
}

fun createPostItem(token: Token, item: Item): Post {
    val post = Post("123")
    // 耗时操作
    // TimeUnit.MILLISECONDS.sleep(300) 
    return post
}

fun processPost(post: Post) {
    // println("process post $post")
}

fun postItem(item: Item) {
    val token = requestToken()
    val post = createPostItem(token, item)
    processPost(post)
}
```

## 多线程

`requestToken()` 和 `createPostItem()` 中是耗时操作，为了不阻塞主线程，一般会将这些操作扔到工作线程中处理。

多线程存在的问题: 不支持高并发。你不能开启 100000 个线程

## 异步回调

```kotlin
fun requestTokenAsync(cb: (Token) -> Unit) {
    thread {
        cb(requestToken())
    }
}

fun createPostAsync(token: Token, item: Item, cb: (Post) -> Unit) {
    thread {
        cb(createPostItem(token, item))
    }
}

fun postItem2(item: Item) {
    requestTokenAsync { token ->
        createPostAsync(token, item) { post ->
            processPost(post)
        }
    }
}
```

引起回调地狱问题。上述代码是简化后的代码，如何涉及到异常处理，实际会更复杂。

## Future/Promise/Rx

```

fun requestTokenAsync(): Promise<Token> {
    return FuturePromise(requestToken())
}

fun createPostAsync(token: Token, item: Item): Promise<Post> {
    return FuturePromise(createPostItem(token, item))
}

fun postItem3(item: Item) {
    requestTokenAsync()
            .thenCompose(createPostAsync())
            .thenAccept()
}
```

// 复杂的操作符. 你记得几种Rx操作符, map与flatMap的区别

// Java: Future
// OkHttp: Call
// Javascript: Promise
// Rx: Observable/Flowable
// Kotlin: Deferred

---

Kotlin 1.2 之前 coroutine 还是实验性功能。需要通过独立库才能使用 `compile 'org.jetbrains.kotlinx:kotlinx-coroutines-core:0.30.2'`

(0.30.2 是最后一个实验性版本)

Kotlin 1.3 开始 coroutine 成为正式功能。

