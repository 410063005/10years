
# 拦截WebView中的特定请求
比如说，要解决这个问题：

如何监控h5页面中的大图？

解决方法的要点是实现`WebViewClient.shouldInterceptRequest()`方法。

```java
webView.setWebViewClient(new WebViewClient() {
    @Override
    public WebResourceResponse shouldInterceptRequest(WebView webView, WebResourceRequest webResourceRequest) {}
```

参考：

+ [OkHttpWebViewClient](https://github.com/classycodeoss/nfc-sockets/blob/master/android/NFCSockets/app/src/main/java/com/classycode/nfcsockets/okhttp/OkHttpWebViewClient.java)
+ [How to use OkHttp for loading resources in WebView](http://artemzin.com/blog/use-okhttp-to-load-resources-for-webview/)
+ [上述做法的问题](https://artemzin.com/blog/android-webview-io/) [bug](https://github.com/bumptech/glide/issues/3081)



