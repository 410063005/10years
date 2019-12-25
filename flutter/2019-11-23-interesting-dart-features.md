# Interesting Features of the Dart Programming Language

[Renato Athaydes](https://renato.athaydes.com/posts/interesting-dart-features.html)


+ Quick Dart Overview
    + statically-typed
    + supports free functions and classes
    + interpreted mode (Dart VM)
    + machine code (dart2aot, dartaotruntime)
    + generate JavaScript (dart2js)
+ Dart Language Features
    + Namespaced import (`as`)
    + whitelisting & blacklisting import (`show`, `hidden`)
    + 3 different types of constructors (normal, named, factory) (`factory`)
    + shorthand notation initialize
    + named arguments (`{}`, 对 Flutter 非常友好)
    + required arguments (`@required `) & Optional parameters (`[]`)
    + cascade notation (`..`)
    + null-safe operators (`?.`)
    + dynamic (`dynamic` 使用 json 解析说明它的好处)
    + Callable Objects (`call()` 方法)
    + spread operator (`...` 使用 js 中 `Math.max()` 方法说明这个操作符的好处, 使用 Making Dart a better language for UI. 说明这个操作符的好处)
    + 异步编程 (`async/await`)
    + Future & Stream 
        + [Streams Documentation](https://dart.dev/tutorials/language/streams)
        + [RxDart](https://pub.dev/packages/rxdart) ???
    + Isolates
+ Dart Platform Features
    + Multiple compilation modes ( jit & aot)
    + pub
    + jit & aot
    + dart2js
    + flutter
    + 
+ 工具
    + `dart`, `dart2js`
    + `pub`
    + 调试

这是一篇介绍 Dart 编程语言的文章。

Dart 在 2011年出现，Google 想用它取代 Javascript，但是失败

2017年5月 Google 在 Flutter 中使用 Dart。

> In May 2017, Google released the first public alpha version of Flutter

2018年8月 Dart 2 发布。

> releases of Dart 2 in August 2018

Dart 同时支持函数和类

> Dart supports free functions. It also has classes

Dart 代码通常是单线程运行

> Dart code is normally run in a single thread