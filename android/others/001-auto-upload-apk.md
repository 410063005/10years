
# 现状分析

打渠道包的方式: 

上传渠道包的方式: web上传

上传数量300个以上，每个APK大小30MB。web上传耗时多，易失败。

# 初步方案

使用cdntool加速上传

测试数据：72个包，每个APK大小30MB，上传耗时3分56秒

# 改进方案

需要实现的功能及对应的方案：

+ 向web服务上传一个原始APK包 - 蓝鲸平台(Python)
+ 该服务基于原始APK生成渠道包 - [VasDolly](https://github.com/Tencent/VasDolly/tree/master/command)(Java)
+ 该服务使用cdntool上传渠道包 - cdntool(Python)

优势：

+ 自动生成渠道包
+ 自动上传渠道包

实现上存在的问题

+ 如何从python调用Java
+ 如何将cdntool集成到蓝鲸

# 如何从python调用Java
+ [java - Python: How can I execute a jar file through a python script - Stack Overflow](https://stackoverflow.com/questions/7372592/python-how-can-i-execute-a-jar-file-through-a-python-script)
+ [Python标准库06 子进程 (subprocess包) - Vamei - 博客园](http://www.cnblogs.com/vamei/archive/2012/09/23/2698014.html)

```python
import subprocess
x = subprocess.Popen(['java', '-jar', 'VasDolly.jar'], stderr=subprocess.PIPE, stdout=subprocess.PIPE)
out, err = x.communicate()
```

# django上传文件
[kendo js]