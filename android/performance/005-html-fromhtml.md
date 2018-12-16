
```
textHtml.setText(Html.fromHtml("<font color=\"#9b9b9b\">恭喜</font><font color=\"#4A4A4A\">安迪迪、华华</font><font color=\"#9b9b9b\">成功挑战了“</font><font color=\"#4A4A4A\">亚瑟/后羿小队</font><font color=\"#9b9b9b\">”，每人获得</font><font color=\"#4A4A4A\">30福利券、50福利券</font>"))
```

![](005-html-fromhtml/1.png)

![](005-html-fromhtml/2.png)

![](005-html-fromhtml/3.png)

# Html.fromHtml 分析

+ app 中 fromHtml() 慢
+ [类似案例]([性能优化](http://cameoh.github.io/blog/2014/10/22/performance-optimization/))

为什么慢

# SpannableString
[Android TextView富文本深入探索 - 简书](https://www.jianshu.com/p/aa53ee98d954)

# 缓存

# unbescape

> Just tried a simple benchmark a while ago. Html.fromHtml() took 27.501 seconds to finish, versus 3.015 seconds for HtmlEscape.unescapeHtml() for the exact same test batch. It's a very significant improvement indeed. Thanks for the tip! 


[unbescape/unbescape: Advanced yet easy to use escaping library for Java](https://github.com/unbescape/unbescape)


