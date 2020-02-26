[TOC]

# 交互场景

+ Action 1 Update the content of the Child 1 widget from the Parent widget.
+ Action 2 Update the content of the Parent widget from the Child 1 widget.
+ Action 3 Update the content of the Child 2 widget from the Child 1 widget.
+ Action 4 Change the tab from Child 1 widget to Child 2 widget.

实例:

+ 例一 - 列表滚动时 app bar 透明度变化
+ 例二 - parent 与 child tab 交互

# 场景一 - 从父节点更新子节点

## 方式一 - 传参

这里提到的几种方式参考自[这里](https://medium.com/flutter-community/flutter-communication-between-widgets-f5590230df1e)。

关键代码如下：

```dart
class _ParentPageState extends State<ParentPage> {

  String updateChildTitle;

  Widget build(BuildContext context) {
        RaisedButton(
          child: Text("Action 1"),
          onPressed: () {
            setState(() {
              updateChildTitle = "Update from Parent";
            });
          },
        ),
  }
}
```

## 方式二 - GlobalKey

> Define a GlobalKey with State class of the Child 1

关键代码如下：

```dart
class _ParentPageState extends State<ParentPage> {
  GlobalKey<Child1PageState> _keyChild1 = GlobalKey();

  @override
  Widget build(BuildContext context) {
        RaisedButton(
          child: Text("Action 1"),
          onPressed: () {
            _keyChild1.currentState.updateText("Update from Parent");
          },
        ),
        
        Child1Page(
          key: _keyChild1,
        ),
  }
}
```

## 方式三 - InheritedWidget

> Using InheritedWidget to store the data from Parent Widget and restore the data from Child1 Widget.

```dart
class ParentProvider extends InheritedWidget {
  final String title;
  final Widget child;

  ParentProvider({this.title, this.child});

  @override
  bool updateShouldNotify(ParentProvider oldWidget) {
    return true;
  }

  static ParentProvider of(BuildContext context) =>
      context.inheritFromWidgetOfExactType(ParentProvider);
}

class _ParentPageState extends State<ParentPage> {

  String updateChildTitle;

  @override
  Widget build(BuildContext context) {
    return ParentProvider(
      title: updateChildTitle,
      child: ...);
}


class Child1PageState extends State<Child1Page> {
  String value = "Page 1";

  @override
  Widget build(BuildContext context) {
    final currentValue = ParentProvider.of(context).title;
  }
}
```

# 场景二 - 从子节点更新父节点

## 方式一 - 回调

在 parent widget 中定义方法，将这个方法作为回调传给 child widget

```dart
class _ParentPageState extends State<ParentPage> {
  String myTitle = "My Parent Title";

  _updateMyTitle(String text) {
    setState(() {
      myTitle = text;
    });
  }
}


class Child1Page extends StatefulWidget {
  final String title;

  final ValueChanged<String> parentAction;
 

class Child1PageState extends State<Child1Page> {
  String value = "Page 1";

  @override
  Widget build(BuildContext context) {
          RaisedButton(
            child: Text("Action 2"),
            onPressed: () {
              widget.parentAction("Update from Child 1");
            },
          ), 
  }
}
```

# 场景三 - 更新兄弟节点

## 方式一 - InheritedWidget

考虑到兄弟节点可能还没创建，所以使用 InheritedWidget 是最好的方法。

# 场景四 - 从 tab 1 切换到 tab2

## 方式一 - 传参 `tabController`

> Pass the tabController as a parameter from Parent Widget to Child 1 Widget.

child widget 直接操作 `tabController`

```dart
class Child1PageState extends State<Child1Page> {
          RaisedButton(
            child: Text("Action 4"),
            onPressed: () {
              widget.tabController.index = 1;
            },
          )
}
```

## 方式二 - 回调

> Pass a callBack from Parent to Child 1 Widget and change the index of the controller.

方式二和方式一本质上没区别。


## 方式三 - InheritedWidget

> Set the tabController inside InheritedWidget , call it from Child 1 Widget and change the index of the controller.

不同于方式一中的直接传参，方法三是使用 InheritedWidget 来间接传参。

# 参考

+ [Flutter: Communication between widgets - Flutter Community - Medium](https://medium.com/flutter-community/flutter-communication-between-widgets-f5590230df1e)
    + 几种不同同场景下的 widget 交互方式
+ [Widget Communication with Flutter using VoidCallback and Function(x) ← Alligator.io](https://alligator.io/flutter/widget-communication/)
    + 回调的用法
+ [InheritedWidget](https://api.flutter.dev/flutter/widgets/InheritedWidget-class.html)