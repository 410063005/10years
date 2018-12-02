
HTTP caching is **disabled** by default

打开缓存的方法如下：

```java
protected void onCreate() {
	File httpCacheDir = ...
	long httpCacheSize = ...
	HttpResponseCache.install(httpCacheDir, httpCacheSize)
}


protected void onStop() {
	HttpResponseCache cache = HttpResponseCache.getInstalled()
	if (cache != null) {
		cache.flush()
	}
}
```

这个缓存会应用到下列场景：

Using URL

`BitmapFactory.decodeStream((InputStream) new URL(pathToImage).getContent())`

Using HTTP

```
HttpClient client = new DefaultHttpClient()
```

也会应用到OkHttp吗？


缓存被移除的情形：

+ 缓存満
+ Cache-Control策略

有些时候无法直接使用HTTP缓存(比如服务器端根本就没有Cache-Control策略)，要自己实现缓存策略。建议使用`DiskLruCache`


建议使用网络库，它们的缓存策略足够好

+ Volley
+ OkHttp
+ Picasso

建议使用工具进行分析

+ Android Studio自带的工具
+ ARO tool
