```
com.fasterxml.jackson.databind.exc.InvalidDefinitionException: No serializer found for class okio.ByteString
```

# 理解ObjectWrapper
`ObjectWrapper`提供读写JSON的功能，既可以从基本的POJO转换，也可以从通用的JSON树模型(JsonNode)转换，并提供相关的常用功能。

它高度支持自定义，可用于不同风格的JSON内容，同时还支持一些高级的对象概念，如多态和对象同一性。

ObjectWrapper还可以作为更高级的ObjectReader类或ObjectWriter类的工厂。

Mapper(包括基于Mapper的ObjectReader和ObjectWriter)使用JsonParser和JsonGenerator的实例来实现实际的JSON读写。

注意：虽然大部分读写功能通过ObjectWrapper类提供，但一些功能通过ObjectReader和ObjectWriter暴露。特别要注意只能通过ObjectReader和ObjectWriter从IO流中读写值序列。(ObjectReader.readValues(InputStream)和ObjectWriter.writeValues(OutputStream))。

最简单的用法类似这样：

```java
  final ObjectMapper mapper = new ObjectMapper(); // can use static singleton, inject: just make sure to reuse!
  MyValue value = new MyValue();
  // ... and configure
  File newState = new File("my-stuff.json");
  mapper.writeValue(newState, value); // writes JSON serialization of MyValue instance
  // or, read
  MyValue older = mapper.readValue(new File("my-older-stuff.json"), MyValue.class);

  // Or if you prefer JSON Tree representation:
  JsonNode root = mapper.readTree(newState);
  // and find values by, for example, using a {@link com.fasterxml.jackson.core.JsonPointer} expression:
  int age = root.at("/personal/age").getValueAsInt(); 

```


主要的API在ObjectCodec中定义，所以这个类的具体实现不用向基于流的解析器和生成器暴露。而通常仅仅在这两种情况下使用ObjectCodec，一是无法使用ObjectMapper(数据来自streaming API)，二是没必要使用ObjectMapper(数据仅来自streaming API)。

Mapper实例是完全线程安全的，所以对实例的配置操作发生在所有读写操作之间。首次使用后修改配置可能生效也可能不生效，修改配置本身也可能失败。

如果想使用不同的配置，有两种方法：

+ 使用ObjectReader进行读操作，使用ObjectWriter进行写操作。这两个类型都是不可变的，所以可以放心地使用不同的配置调用ObjectMapper的工厂方法初始化新的实例。创建新的ObjectReader或ObjectWriter是非常轻量级的操作，所以可以在需要时重新创建即可。

+ ObjectReader和ObjectWriter拿不到特定的配置时可以使用多个ObjectMapper。这时可能需要用到`copy()`方法，它将使用特定配置创建ObjectMapper的一个拷贝，并且允许在使用前对拷贝的实例进行配置。注意`copy()`操作跟创建ObjectMapper一样重，所以如果要多次使用mapper的话应当对其重用。
