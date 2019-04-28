# 背景知识
[Component capabilities](https://docs.gradle.org/current/userguide/managing_transitive_dependencies.html#sec:capabilities)

依赖图中经常会出现同一个 API 的多个实现。这一情况在日志框架中尤为常见。比如，同一个接口有多个可用的绑定，或者一个库选择这个绑定但另一个库选择其他绑定。Because those implementations live at different GAV coordinates (译注，GAV(group, artifact, version))，构建工具通常无法识别出这些库之间的冲突。为了解决这一问题，Gradle 定义了 *capability* 的概念。

在一个依赖图中出现两个组件可以提供相同的 *capability* 是非法的。直观上，如果 Gradle 发现 classpath 上两个组件提供相同的东西，构建失败并提示哪里出现冲突。

*capability* 由 `(group, module, version)` 三元组定义。

```
dependencies {
    // This dependency will bring log4:log4j transitively
    implementation 'org.apache.zookeeper:zookeeper:3.4.9'

    // We use log4j over slf4j
    implementation 'org.slf4j:log4j-over-slf4j:1.7.10'
}
```

以上脚本中暗含着冲突。


# 问题一
使用 Gradle 时遇到以下依赖冲突问题：

```
SLF4J: Class path contains multiple SLF4J bindings.
SLF4J: Found binding in [jar:file:/Users/king/.gradle/caches/modules-2/files-2.1/org.slf4j/slf4j-log4j12/1.7.21/7238b064d1aba20da2ac03217d700d91e02460fa/slf4j-log4j12-1.7.21.jar!/org/slf4j/impl/StaticLoggerBinder.class]
SLF4J: Found binding in [jar:file:/Users/king/.gradle/caches/4.10.1/generated-gradle-jars/gradle-api-4.10.1.jar!/org/slf4j/impl/StaticLoggerBinder.class]
SLF4J: See http://www.slf4j.org/codes.html#multiple_bindings for an explanation.
SLF4J: Detected both log4j-over-slf4j.jar AND bound slf4j-log4j12.jar on the class path, preempting StackOverflowError. 
SLF4J: See also http://www.slf4j.org/codes.html#log4jDelegationLoop for more details.
```

我的依赖配置是这样的：

```
dependencies {
    implementation fileTree(include: ['*.jar'], dir: 'libs')
    implementation gradleApi()
    implementation('com.qcloud:cos_api:4.7')
}
```

按错误日志的字面意思，猜测应该是 classpath 中包含了重复的库。[slf4j 官方](http://www.slf4j.org/codes.html#multiple_bindings)对这个问题作出如下解释：

> Multiple bindings were found on the class path

从设计上讲，SLF4J API 只能绑定到一个底层日志库。如果 classpath 中有多个日志库，SLF4J 会发出警告并且列出这些底层日志库的具体位置。

如果 classpath 路径中同时有多个绑定可用，应当选择使用其中的一个绑定并移除别外的。比如，classpath 中同时有 slf4j-simple-1.8.0-beta4.jar 和 slf4j-nop-1.8.0-beta4.jar，而你想使用 nop (no-operation) 那个，那就应当将 slf4j-simple-1.8.0-beta4.jar 从 classpath 中移除。

SLF4J 警告中列出的位置通常提供足够信息用于找出给项目引入不必要 SLF4J 绑定的 dependency transitively 问题。解决方法是在项目的 `pom.xml` 文件中排除掉可疑的依赖。比如，cassandra-all v0.8.1 将 log4j 和 slf4j-log4j12 作为编译期依赖。所以当你将 cassandra-all 作为项目依赖时，cassandra-all 会导致项目将 slf4j-log4j12.jar 和 log4j.jar 两个文件均作为依赖。如果你不想使用 log4j 作为 SLF4J 绑定，可以使用如下方式让 Maven 排除该文件：

```xml
<dependencies>
  <dependency>
    <groupId> org.apache.cassandra</groupId>
    <artifactId>cassandra-all</artifactId>
    <version>0.8.1</version>

    <exclusions>
      <exclusion> 
        <groupId>org.slf4j</groupId>
        <artifactId>slf4j-log4j12</artifactId>
      </exclusion>
      <exclusion> 
        <groupId>log4j</groupId>
        <artifactId>log4j</artifactId>
      </exclusion>
    </exclusions> 

  </dependency>
</dependencies>
```

注意：SLF4J 仅仅只是发出警告。即使 classpath 中出现多个绑定，SLF4J 也会选择并绑定其中一个。但 SLF4J 选择某个绑定是由 JVM 决定的，而且可以认为其选择是完全随机的。第三方库或框架应该依赖 SLF4J API 而不是 SLF4J binding。


参考 [Gradle 文档](https://docs.gradle.org/current/userguide/managing_transitive_dependencies.html)，修改后的配置如下：

```
dependencies {
    implementation fileTree(include: ['*.jar'], dir: 'libs')
    implementation gradleApi()
    implementation('com.qcloud:cos_api:4.7') {
        exclude group: 'org.slf4j', module: 'slf4j-log4j12'
    }
}
```

问题解决！但是还没完。

# 问题二
考虑这样如下的另外一种场景。

lib1 的配置：

```
dependencies {
    implementation('com.qcloud:cos_api:4.7') 
}
```

lib2 的配置：

```
dependencies {
    implementation fileTree(include: ['*.jar'], dir: 'libs')
    implementation gradleApi()
    implementation project(':lib1') {
        exclude group: 'org.slf4j', module: 'slf4j-log4j12'        
    }
}
```

这种场景下仍然会出现 "Class path contains multiple SLF4J bindings." 问题，即使已经在 lib2 中已添加 "exclude" 选项(貌似不起作用)。

解决办法：

lib1 的配置：

```
dependencies {
    implementation('com.qcloud:cos_api:4.7') {
        exclude group: 'org.slf4j', module: 'slf4j-log4j12'        
    }        
}
```

lib2 的配置：

```
dependencies {
    implementation fileTree(include: ['*.jar'], dir: 'libs')
    implementation gradleApi()
    implementation project(':lib1') 
}
```

上述修改可以保证 lib2 不出现 "Class path contains multiple SLF4J bindings." 问题，但 lib1 由于缺少 `org.slf4j` 依赖而无法正常运行。

(**貌似对 `project` 依赖进行 `exclude` 操作并不生效**)

# 理解 transitively 参数

```
dependencies {
    implementation('com.qcloud:cos_api:4.7') {
        transitive = false
    }
}
```

当 `transitive = false` 时，使用 `gradlew dependencies` 查看依赖，依赖关系如下：

```
compileClasspath - Compile classpath for compilation 'main' (target  (jvm)).
+--- com.squareup.okhttp3:okhttp:3.13.1
|    \--- com.squareup.okio:okio:1.17.2
+--- com.google.code.gson:gson:2.8.5
+--- dom4j:dom4j:1.6.1
|    \--- xml-apis:xml-apis:1.0.b2
+--- org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.3.21
|    +--- org.jetbrains.kotlin:kotlin-stdlib:1.3.21
|    |    +--- org.jetbrains.kotlin:kotlin-stdlib-common:1.3.21
|    |    \--- org.jetbrains:annotations:13.0
|    \--- org.jetbrains.kotlin:kotlin-stdlib-jdk7:1.3.21
|         \--- org.jetbrains.kotlin:kotlin-stdlib:1.3.21 (*)
+--- junit:junit:4.12
|    \--- org.hamcrest:hamcrest-core:1.3
\--- com.qcloud:cos_api:4.7
```

当 `transitive = true` 时，使用 `gradlew dependencies` 查看依赖，依赖关系如下：

```
compileClasspath - Compile classpath for compilation 'main' (target  (jvm)).
+--- com.squareup.okhttp3:okhttp:3.13.1
|    \--- com.squareup.okio:okio:1.17.2
+--- com.google.code.gson:gson:2.8.5
+--- dom4j:dom4j:1.6.1
|    \--- xml-apis:xml-apis:1.0.b2
+--- org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.3.21
|    +--- org.jetbrains.kotlin:kotlin-stdlib:1.3.21
|    |    +--- org.jetbrains.kotlin:kotlin-stdlib-common:1.3.21
|    |    \--- org.jetbrains:annotations:13.0
|    \--- org.jetbrains.kotlin:kotlin-stdlib-jdk7:1.3.21
|         \--- org.jetbrains.kotlin:kotlin-stdlib:1.3.21 (*)
+--- junit:junit:4.12
|    \--- org.hamcrest:hamcrest-core:1.3
\--- com.qcloud:cos_api:4.7
     +--- org.apache.httpcomponents:httpclient:4.5.3
     |    +--- org.apache.httpcomponents:httpcore:4.4.6
     |    +--- commons-logging:commons-logging:1.2
     |    \--- commons-codec:commons-codec:1.9 -> 1.10
     +--- org.apache.httpcomponents:httpcore:4.4.6
     +--- org.apache.httpcomponents:httpmime:4.5.2
     |    \--- org.apache.httpcomponents:httpclient:4.5.2 -> 4.5.3 (*)
     +--- org.json:json:20140107
     +--- org.slf4j:slf4j-log4j12:1.7.21
     |    +--- org.slf4j:slf4j-api:1.7.21
     |    \--- log4j:log4j:1.2.17
     +--- commons-codec:commons-codec:1.10
     \--- junit:junit:4.12 (*)
```
