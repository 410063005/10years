

[django Django 日志 - 刘江的django教程](http://www.liujiangblog.com/course/django/176)

[django日志](https://blog.csdn.net/haeasringnar/article/details/82053714)

[python django 实现文件上传 - wanglei_storage的博客 - CSDN博客](https://blog.csdn.net/wanglei_storage/article/details/52947594)

[Django 上传文件 - python学习 - SegmentFault 思否](https://segmentfault.com/a/1190000010440618)

---

记录一个使用pymysql时遇到的`KeyError: 255`问题。解决办法参考[这里](https://stackoverflow.com/questions/45368336/error-keyerror-255-when-executing-pymysql-connect)。

对`connectors.py`进行修改。

修改前(1268-1269行)

```python
self.server_language = lang
self.server_charset = charset_by_id(lang).name 
```

修改后(1268-1272)

```python
self.server_language = lang
try:
    self.server_charset = charset_by_id(lang).name
except KeyError:
    self.server_charset = None
```
