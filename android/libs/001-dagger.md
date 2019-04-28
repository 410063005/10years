

# 背景
[wiki](https://en.wikipedia.org/wiki/Dependency_injection)

什么是依赖注入？

+ 什么是依赖
+ 什么是注入

> In software engineering, dependency injection is a technique whereby one object (or static method) supplies the dependencies of another object. A dependency is an object that can be used (a service). An injection is the passing of a dependency to a dependent object (a client) that would use it. The service is made part of the client's state.[1] Passing the service to the client, rather than allowing a client to build or find the service, is the fundamental requirement of the pattern.

这个模式的关键在于，将依赖传给客户，而不是让客户寻找依赖

依赖注入中的角色?

+ client
+ service (dependencies)
+ injector
+ interfaces that define how the client may use the services

注：injector 可能有很多别名， assembler, provider, container, factory, builder, spring, construction code, or main

依赖注入的好处？

> The intent behind dependency injection is to decouple objects to the extent that no client code has to be changed simply because an object it depends on needs to be changed to a different one. This permits following the Open / Closed principle.

+ 解耦
+ 开闭原则

依赖注入的坏处？

+ 代码可读性变差
+ 反射方式实现的依赖注入性能差

客户需要依赖。借助依赖注入客户将提供依赖的这一职责交由外部代码 (injector)。客户不能访问 injector code，injecting code 负责构建依赖并调用客户代码将依赖注入。客户不必知道 injecting code，不必知道如何构建依赖，甚至不必知道正在使用哪个依赖。客户只需要知道如何使用依赖提供的接口。

依赖注入的方式?

+ setter-based injection
+ interface-based injection
+ constructor-based injection

无论哪种方式，都需要 injector 充当 client 和 dependencies 之间的媒介。

```java
// An example without dependency injection
public class Client {
    // Internal reference to the service used by this client
    private ExampleService service;

    // Constructor
    Client() {
        // Specify a specific implementation in the constructor instead of using dependency injection
        service = new ExampleService();
    }

    // Method within this client that uses the services
    public String greet() {
        return "Hello " + service.getName();
    }
}

// Constructor
Client(Service service) {
    // Save the reference to the passed-in service inside this client
    this.service = service;
}

// Setter method
public void setService(Service service) {
    // Save the reference to the passed-in service inside this client.
    this.service = service;
}

// Service setter interface.
public interface ServiceSetter {
    public void setService(Service service);
}

// Client class
public class Client implements ServiceSetter {
    // Internal reference to the service used by this client.
    private Service service;

    // Set the service that this client is to use.
    @Override
    public void setService(Service service) {
        this.service = service;
    }
}
```

三种方式的对比：

+ setter-based injection - 灵活，但不安全
+ interface-based injection
+ constructor-based injection - 简单安全，不灵活


依赖注入解决的问题：

+ How can an application or class be independent of how its objects are created?
+ How can the way objects are created be specified in separate configuration files?
+ How can an application support different configurations?

直接为 client 创建 dependencies 的坏处？

+ inflexible - client 与特定的 dependencies 绑定，难以重用
+ hard to test - 难以将真实的 dependencies 替换成 mock objects，难以测试

**依赖注入与 [Abstract Factory](https://en.wikipedia.org/wiki/Abstract_factory_pattern) 的区别？**

![](https://upload.wikimedia.org/wikipedia/commons/1/10/W3sDesign_Dependency_Injection_Design_Pattern_UML.jpg)

# 实现

# 使用
## Java
Idea 2017.2.5 版本使用 Dagger 2.16 时遇到的问题。

+ 无法生成代码
+ 无法使用生成的代码

对第一个问题要开启 annotation processing，第二个问题将生成的代码添加到 sourceSet。

**File | Settings | Build, Execution, Deployment | Compiler | Annotation Processors**

最终的完整配置如下：

```
buildscript {
    repositories {
        maven {
            url "https://plugins.gradle.org/m2/"
        }
    }
    dependencies {
        classpath "net.ltgt.gradle:gradle-apt-plugin:0.19"
    }
}

apply plugin: 'java'
apply plugin: "net.ltgt.apt"
apply plugin: 'idea'

repositories {
    mavenCentral()
}

dependencies {
    compile 'com.google.dagger:dagger:2.16'
    apt 'com.google.dagger:dagger-compiler:2.16'
}

idea {
    module {
        sourceDirs += file("$buildDir/generated/source/apt/main")
        testSourceDirs += file("$buildDir/generated/source/apt/test")
    }
}
```

[ref](https://google.github.io/dagger/users-guide)

+ @Inject
+ @Provides, @Binds
+ @Module
+ @Component
+ @Singleton
+ [Lazy](https://google.github.io/dagger/api/latest/dagger/Lazy.html)
+ [Provider](https://google.github.io/dagger/users-guide)
+ @Named

## Android
[Android 中为什么要使用 Dagger 2](https://google.github.io/dagger/android)

Dagger2 相比其他依赖注入框架的优势在于编译期生成代码而不是运行期反射。所以性能不是问题。

> One of the central difficulties of writing an Android application using Dagger is that many Android framework classes are instantiated by the OS itself, like Activity and Fragment, but Dagger works best if it can create all the injected objects. Instead, you have to perform members injection in a lifecycle method. 

[ref](https://google.github.io/dagger/android)

[ref2](https://medium.com/@Zhuinden/that-missing-guide-how-to-use-dagger2-ef116fbea97)

在 Android 项目中使用 Dagger 2 时遇到各种问题。建议参考 [todo mvp dagger](https://github.com/googlesamples/android-architecture/tree/todo-mvp-dagger/)，通过这个项目来熟悉 Dagger 2 在 Android 项目中的应用。

[dagger/java/dagger/example/android/simple at master · google/dagger](https://github.com/google/dagger/tree/master/java/dagger/example/android/simple)

# Dagger 的坏处？

[Why we stopped using dagger](https://www.lvguowei.me/post/why-remove-dagger/)

[Keeping the Daggers Sharp ⚔️ – Square Corner Blog – Medium](https://medium.com/square-corner-blog/keeping-the-daggers-sharp-%EF%B8%8F-230b3191c3f)


# 参考

[ref](https://google.github.io/dagger/)

[slides](https://docs.google.com/presentation/d/1fby5VeGU9CN8zjw4lAb2QPPsKRxx6mSwCe9q7ECNSJQ/pub?start=false&loop=false&delayms=3000)

[Android注入框架Dagger2学习笔记 | Night Piece](https://limuzhi.com/2016/03/06/Android%E6%B3%A8%E5%85%A5%E6%A1%86%E6%9E%B6Dagger2%E5%AD%A6%E4%B9%A0%E7%AC%94%E8%AE%B0/)
