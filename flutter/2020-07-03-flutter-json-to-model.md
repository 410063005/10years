# Flutter JSON 转 Model

## [两种转换策略](https://flutter.dev/docs/development/data-and-backend/json#which-json-serialization-method-is-right-for-me)

+ 手工转换 - Manual serialization。这种方式适合小项目，具体操作方式是使用 `jsonDecode` 解码原始字符串，生成 `Map<String, dynamic>`，可以参考 [Serializing JSON manually using dart:convert](https://flutter.dev/docs/development/data-and-backend/json#manual-encoding)。
    + 这种方式的问题在于项目变大之后，手写解码操作很繁琐且容易出错。一个拼写错误也会引起运行时错误。
+ 自动转换 - Automated serialization using code generation。这种方式适合中型和大型项目，具体操作方式是使用外部第三方库从 model 类生成序列化/反序列化的模板文件。可以参考 [Serializing JSON using code generation libraries](https://flutter.dev/docs/development/data-and-backend/json#code-generation)
    + 这种方式的好处在于不必手写模板代码，拼写错误会引起编译错误而不是运行时错误
    + 这种方式的坏处在于需要事先进行一些配置。另外，生成的代码可能会让项目看起来有些混乱


两种常用的库包括：

+ [json_serializable](https://pub.dev/packages/json_serializable)
+ [built_value](https://pub.dev/packages/built_value)

## 手工转换

+ 内联转换
    + 优点是不必提前准备 model 类
    + 缺点是丢失了类型信息导致**对调用方而言**类型不安全，且不能代码提示，特别容易出错 (`jsonDecode()` 返回的是 `Map<String, dynamic>`)
+ modle 类内部转换，[示例](https://flutter.dev/docs/cookbook/networking/background-parsing)
    + 优点是**对调用方**类型是安全的，且能代码提示。拼写错误只会引起编译错误而不是运行错误
    + 缺点是要准备 model 类，工作量大


```dart
Map<String, dynamic> user = jsonDecode(jsonString);

print('Howdy, ${user['name']}!');
print('We sent the verification link to ${user['email']}.');


class User {
  final String name;
  final String email;

  User(this.name, this.email);

  User.fromJson(Map<String, dynamic> json)
      : name = json['name'],
        email = json['email'];

  Map<String, dynamic> toJson() =>
    {
      'name': name,
      'email': email,
    };
}
```

更多手工转换示例见 [Parsing complex JSON in Flutter - Flutter Community - Medium](https://medium.com/flutter-community/parsing-complex-json-in-flutter-747c46655f51)

## 自动转换

有很多库用于自动将 json 转换成 model，常用的有两个

+ [json_serializable](https://pub.dev/packages/json_serializable)
+ [built_value](https://pub.dev/packages/built_value)

[json_serializable](https://pub.dev/packages/json_serializable) 通过对普通类添加注解来实现序列化，[built_value](https://pub.dev/packages/built_value) 不需要注解，只要保证类的不变性即可实现序列化，我们重点看 json_serializable

```yaml
dependencies:
  # https://flutter.dev/docs/development/data-and-backend/json#setting-up-json_serializable-in-a-project
  # https://raw.githubusercontent.com/dart-lang/json_serializable/master/example/pubspec.yaml
  json_annotation: ^3.0.0
dev_dependencies:
  build_runner: ^1.0.0
  json_serializable: ^3.2.0
```

### json_serializable 注解

```dart
@JsonSerializable()

class User {
  User(this.name, this.email);

  String name;
  String email;

  factory User.fromJson(Map<String, dynamic> json) => _$UserFromJson(json);

  Map<String, dynamic> toJson() => _$UserToJson(this);
}
```

+ 给 `User` 类添加 `@JsonSerializable()` 注解，
+ json_serializable 库将为指定类生成 JSON 序列化方法，分别是 `_$UserFromJson()` 和 `_$UserToJson`

### 序列化配置

json_serializable 支持自定义配置。主要包括：

+ 自定义命名
    + `@JsonKey(name: 'new_name')`，应用于 Dart model class 的单个字段
    + `@JsonSerializable(fieldRename: FieldRename.xxx)`，应用于 Dart model class，等价于对所有字段使用 `@JsonKey`。支持的命名策略有
        +  `FieldRename.kebab`
        +  `FieldRename.snake`
        +  `FieldRename.pascalCase`
+ 字段检验
    + `@JsonKey(defaultValue: false)` - 指定字段的缺省值，JSON 中不包括该字段或字段为 `null` 时使用缺省值
    + `@JsonKey(required: true)` - 指定字段为必须字段，不包含该字段时会抛出异常
    + `@JsonKey(ignore: true)` - 忽略指定字段
+ [处理嵌套类](https://pub.dev/documentation/json_annotation/latest/json_annotation/JsonSerializable/explicitToJson.html)
    + `@JsonSerializable(explicitToJson: true)`

```dart
/// An annotation for the code generator to know that this class needs the
/// JSON serialization logic to be generated.
@JsonSerializable()

class User {
  User(this.registrationDateMillis, this.isAdult, this.id, this.verificationCode);

  /// Tell json_serializable that "registration_date_millis" should be
  /// mapped to this property.
  @JsonKey(name: 'registration_date_millis')
  final int registrationDateMillis;
  /// Tell json_serializable to use "defaultValue" if the JSON doesn't
  /// contain this key or if the value is `null`.
  @JsonKey(defaultValue: false)
  final bool isAdult;

  /// When `true` tell json_serializable that JSON must contain the key, 
  /// If the key doesn't exist, an exception is thrown.
  @JsonKey(required: true)
  final String id;

  /// When `true` tell json_serializable that generated code should 
  /// ignore this field completely. 
  @JsonKey(ignore: true)
  final String verificationCode;

  /// A necessary factory constructor for creating a new User instance
  /// from a map. Pass the map to the generated `_$UserFromJson()` constructor.
  /// The constructor is named after the source class, in this case, User.
  factory User.fromJson(Map<String, dynamic> json) => _$UserFromJson(json);

  /// `toJson` is the convention for a class to declare support for serialization
  /// to JSON. The implementation simply calls the private, generated
  /// helper method `_$UserToJson`.
  Map<String, dynamic> toJson() => _$UserToJson(this);
}
```

更多自动转换示例见 [json_serializable.dart/example.dart at master · google/json_serializable.dart](https://github.com/google/json_serializable.dart/blob/master/example/lib/example.dart)

### 生成代码

`User` 类首次使用 json_serializable 时会报错，因为这里还没有生成 `User`需要用到的代码。

![](/images/15937599067789.jpg)

有两种生成方式。在项目根目录下执行相应的 `flutter` 命令：

+ `flutter pub run build_runner build` - 一次性生成序列化代码
+ `flutter pub run build_runner watch` - 监听项目源码并自动生成序列化代码

## 更进一步

实际项目中类似 `User` 类的 model class 数量很多。如果每个 class 都这样来写，仍然有不小工作量，《Flutter实战》中提到了一种使用自动化生成模板的方式，有一定参价值。

这里不具体展示。《Flutter实战》作者将自动化生成模板的方式封装成 [Json_model](https://link.juejin.im/?target=https%3A%2F%2Fgithub.com%2Fflutterchina%2Fjson_model)，方便第三方使用。

## 工具网站

+ [JSON to Dart](https://javiercbk.github.io/json_to_dart/)

## 参考

+ [Flutter如何高效的JSON转Model - 掘金](https://juejin.im/post/5e12eb566fb9a0480b415de2)
+ [JSON 和序列化数据 - Flutter 中文文档 - Flutter 社区中文资源](https://flutter.cn/docs/development/data-and-backend/json)
+ [Json转Model · 《Flutter实战》](https://book.flutterchina.club/chapter10/json_model.html)