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

### 实例
从网上下载了一个python项目`cdntool`，它只是简单地说明要依赖python3.6, tornado 4.5+, xmltodict(但没有给出任何相关地配置)。直接运行该项目立即报错提示缺少依赖

```
ModuleNotFoundError: No module named 'tornado'
```

如何使用pipenv给`cdntool`添加依赖？

### pipenv命令

运行`pipenv graph`，提示如下：

```➜  cdntool pipenv graph
Warning: No virtualenv has been created for this project yet! Consider running `pipenv install` first to automatically generate one for you or see`pipenv install --help` for further instructions.
```

运行`pipenv install`提示找不到`Pipfile`

运行`pipenv install tornado`。安装时还可以指定tornado版本 `pipenv install tornado==4.5`执行成功后在当前目录生成几个文件，其中包括`Pipfile`，`Pipfile`内容如下：

```
[[source]]
name = "pypi"
url = "https://pypi.org/simple"
verify_ssl = true

[dev-packages]

[packages]
tornado = "==4.5"

[requires]
python_version = "3.7"
```

(看到这个文件有点明白了，哦，原来pipenv就是python的Gradle啊，`Pipfile`文件就是`build.gradle`嘛)

按相同方式安装另一个依赖 `pipenv install xmltodict`


pipenv指定私有库

```
--pypi-mirror TEXT  Specify a PyPI mirror
```





### 运行程序
并不是安装依赖后就可以直接运行`cdntool`。直接运行`cdntool`的话你会发现依赖提示`ModuleNotFoundError`。正确的做法是先执行`pipenv shell`激活virtualenv：

```
➜  cdntool pipenv shell
Launching subshell in virtual environment…
 . /Users/kingcmchen/.local/share/virtualenvs/cdntool-q0oUnpG_/bin/activate
➜  cdntool  . /Users/kingcmchen/.local/share/virtualenvs/cdntool-q0oUnpG_/bin/activate
```

然后在这个虚拟环境中运行`cdntool`。