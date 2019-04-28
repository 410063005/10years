# log

log 的关键在于使用给 `OkHttpClient` 添加 `HttpLoggingInterceptor` 。

记得添加如下依赖：

`compile 'com.squareup.okhttp3:logging-interceptor:<latest version>'`

代码如下：

```java
HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
OkHttpClient client = new OkHttpClient.Builder().addInterceptor(interceptor).build();

Retrofit retrofit = new Retrofit.Builder()
        .baseUrl("https://backend.example.com")
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build();

return retrofit.create(ApiClient.class);
```

[java - Logging with Retrofit 2 - Stack Overflow](https://stackoverflow.com/questions/32514410/logging-with-retrofit-2) 
