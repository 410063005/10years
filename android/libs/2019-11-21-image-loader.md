自测题：

+ **Glide 的设计?**
+ **Glide 与 Fresco 的区别?**
+ 如何实现 Bitmap 缓存?
+ 如何保存高效加载 Bitmap?
+ 实现图片加载框架时要考虑哪些问题? [Handling bitmaps  |  Android Developers](https://developer.android.com/topic/performance/graphics/)

延伸：

+ [Bitmap: 从出生到死亡](https://www.sunmoonblog.com/2019/06/15/bitmap-creation/)
+ [Bitmap: 监控与分析](https://github.com/410063005/10years-private/blob/master/2019-09-25-bitmap-%E7%9B%91%E6%8E%A7%E4%B8%8E%E5%88%86%E6%9E%90.md)

# Glide

+ 多种图片格式的缓存，适用于更多的内容表现形式（如Gif、WebP、缩略图、Video）
+ 生命周期集成（根据Activity或者Fragment的生命周期管理图片加载请求）
+ 高效处理Bitmap（bitmap的复用和主动回收，减少系统回收压力）
+ 高效的缓存策略，灵活（Picasso只会缓存原始尺寸的图片，Glide缓存的是多种规格），加载速度快且内存开销小（默认Bitmap格式的不同，使得内存开销是Picasso的一半）

# Fresco

+ 最大的优势在于5.0以下(最低2.3)的bitmap加载。在5.0以下系统，Fresco将图片放到一个特别的内存区域(Ashmem区)
+ 大大减少OOM（在更底层的Native层对OOM进行处理，图片将不再占用App的内存）
+ 适用于需要高性能加载大量图片的场景

# Piccoso

# 其他框架

+ 异步加载：线程池
+ 切换线程：Handler，没有争议吧
+ 缓存：LruCache、DiskLruCache
+ 防止OOM：软引用、LruCache、图片压缩、Bitmap像素存储位置
+ 内存泄露：注意ImageView的正确引用，生命周期管理
+ 列表滑动加载的问题：加载错乱、队满任务过多问题

# 参考

+ [面试官：简历上最好不要写Glide，不是问源码那么简单
](https://mp.weixin.qq.com/s?__biz=MzAxMTI4MTkwNQ==&mid=2650829338&idx=1&sn=2b4ed3b896abc689af6628e1925c9d64&chksm=80b7a684b7c02f9298440880790dfefdc7469a31b3b3973d27845e3663d29f8ecfed882c2d72&mpshare=1&scene=1&srcid=&sharer_sharetime=1574299362753&sharer_shareid=b5535657e3516bd6d7252ce5f5ed09f4&rd2werd=1#wechat_redirect)