# Glide 图片加载问题总结之避免加载原图

为 SimpleTarget 设置合理大小避免 Glide 加载原图。


## 问题描述
![-w1242](https://blog-1251688504.cos.ap-shanghai.myqcloud.com/2019/12/25/15610117692948.jpg)

擂台页使用以上 `ImageLoader.loadImage()` 方法加载 url 指定的图片。代码如下：

![-w1003](https://blog-1251688504.cos.ap-shanghai.myqcloud.com/2019/12/25/15610137017742.jpg)

结果发现没有按预期加载小图，而是加载原图。加载原图可能引起流量大、内存高等一系列其他问题.

## 方案一
修改 Glide 图片加载模块，通过获取上层 Target 的大小来生成 url，以保证**全局统一**选择合理的小图。

很早就在项目中添加了这个方法。但为什么对擂台页不生效？

分析发现有以下现象：

+ 在控件(ImageViewTarget) 中加载图片，会加载符合预期的小图
+ 在 SimpleTarget 中加载图片，仍然加载原图，不符合预期

结论是：擂台页使用 SimpleTarget 时没有传大小，导致最终加载原图。

![-w1220](https://blog-1251688504.cos.ap-shanghai.myqcloud.com/2019/12/25/15610151398822.jpg)

如果在创建 `SimpleTarget` 时不指定大小，就会使用缺省的大小。缺省大小会导致**全局统一**策略不生效。


## 方案二

在调用 `ImageLoader.loadImage()` 修改 url，以保证当前代码会选择合理的小图。

![-w999](https://blog-1251688504.cos.ap-shanghai.myqcloud.com/2019/12/25/15610137497383.jpg)

## 总结

+ **建议使用加载图片时如果用到 `SimpleTarget`，务必指定其大小！**
+ 不建议使用方案二