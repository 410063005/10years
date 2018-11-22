# python2和python3共存

## 安装python3
终端下`brew install python3`安装python3。

公司网络环境可能需要先配置代理：

```
export http_proxy="http://127.0.0.1:12759"
export https_proxy="http://127.0.0.1:12759"
```

出错，错误提示

```
Updating Homebrew...
Error: python 2.7.14_2 is already installed
To upgrade to 3.7.1, run `brew upgrade python`
```

通过错误提示知道要使用`brew upgrade python`安装python3

## python2和python3共存

安装python3后如果还想继续使用python2该怎么办？

一种情况是安装python3后仍然可以找到python2。

```
➜  ~ python
Python 2.7.10 (default, Oct  6 2017, 22:29:07)
[GCC 4.2.1 Compatible Apple LLVM 9.0.0 (clang-900.0.31)] on darwin
Type "help", "copyright", "credits" or "license" for more information.
>>> exit()
➜  ~ python3
Python 3.7.1 (default, Nov  6 2018, 18:45:35)
[Clang 10.0.0 (clang-1000.11.45.5)] on darwin
Type "help", "copyright", "credits" or "license" for more information.
>>>
```

另一种情况是找不到python2。这时需要重新安装python2：

```
brew install python2
// brew install python@2
```

## python包管理

推荐使用pipenv

+ [pipenv](https://github.com/pypa/pipenv)
+ [Python新利器之pipenv - 简书](https://www.jianshu.com/p/00af447f0005)
