
# invoke

Objects with invoke() method can be invoked as a function  [ref](https://try.kotlinlang.org/#/Kotlin%20Koans/Conventions/Invoke/Task.kt) [ref2](https://kotlinlang.org/docs/reference/operator-overloading.html)

```kotlin
fun Int.invoke() { println(this) }

1() //huh?..

class Invokable {
    var numberOfInvocations: Int = 0
        private set
    operator fun invoke(): Invokable {
        numberOfInvocations++
        return this
    }
}

fun invokeTwice(invokable: Invokable) = invokable()()
```

# init 关键字

主构造函数不能包含任何的代码。初始化的代码可以放到以 init 关键字作为前缀的初始化块（initializer blocks）中。 [ref](https://www.kotlincn.net/docs/reference/classes.html)

在实例初始化期间，初始化块按照它们出现在类体中的顺序执行，与属性初始化器交织在一起：

```kotlin
class InitOrderDemo(name: String) {
    val firstProperty = "First property: $name".also(::println)

    init {
        println("First initializer block that prints ${name}")
    }

    val secondProperty = "Second property: ${name.length}".also(::println)

    init {
        println("Second initializer block that prints ${name.length}")
    }
}
```

输出：

```
First property: hello
First initializer block that prints hello
Second property: 5
Second initializer block that prints 5
```
