ListView 的 ViewHolder模式。它的优点：

+ 减少 View 的创建
+ 减少 `findViewById()` 的调用

减少View。常见的策略包括：

+ `TextView` 和 `ImageView` 合并
+ 使用custom state。例子见[view-reduction](https://sriramramani.wordpress.com/2013/03/25/view-reduction/)，这个例子将一个复杂布局优化成只使用一个 `TextView`




