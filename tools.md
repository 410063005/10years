[TOC]

# Github

+ 在 GitHub 的 Search 框，输入 java stars:>10000，可以搜索到星数最多、最热门的一些 Java 开源框架
+ 如果想发掘 Java 前沿的开源项目，可以选择 Explore，点击 Trending 按钮，就可以看到近期热门的一些开源项目。
+ 可以选择 today「当天热门」、this week「一周热门」和 this month「一月热门」，还可以在右侧选择语言来查看，比如选择 Java 语言

[来源](https://mp.weixin.qq.com/s/NIn2hOUZDAq4sdA4TsSNYg)

# git

+ [git tutorial](https://www.atlassian.com/git/tutorials)
+ [geeeeeeeeek/git-recipes: 🥡 Git recipes in Chinese by Zhongyi Tong. 高质量的Git中文教程.](https://github.com/geeeeeeeeek/git-recipes)

## 找出被删除文件的历史记录

```
// 不知道文件名或文件路径时
git log --all --full-history -- **/thefile.*
// 知道文件名或文件路径时
git log --all --full-history -- <path-to-file>
```

## 免密码操作

```
git config credential.helper store
```

执行上一条命令后，当git push的时候输入一次用户名和密码就会被记录

注意：这样保存的密码是明文的，保存在用户目录~的.git-credentials文件中

## git:// 代理

1. 从[gist](https://gist.github.com/sit/49288)下载脚本作为可执行文件 `gitproxy-socat`
2. `git config --global core.gitproxy gitproxy-socat` 配置代理。 参考[ref](https://git-scm.com/docs/git-config)

```
#!/bin/sh
# Use socat to proxy git through an HTTP CONNECT firewall.
# Useful if you are trying to clone git:// from inside a company.
# Requires that the proxy allows CONNECT to port 9418.
#
# Save this file as gitproxy somewhere in your path (e.g., ~/bin) and then run
#   chmod +x gitproxy
#   git config --global core.gitproxy gitproxy
#
# More details at http://tinyurl.com/8xvpny

# Configuration. Common proxy ports are 3128, 8123, 8000.
_proxy=proxy.yourcompany.com
_proxyport=3128

exec socat STDIO PROXY:$_proxy:$1:$2,proxyport=$_proxyport
```

参考:

+ [stackoverflow](https://stackoverflow.com/questions/5860888/git-through-proxy)
+ [v2ex](https://www.v2ex.com/t/332816)
+ [A simple wrapper around socat to use as a git proxy command](https://gist.github.com/sit/49288)
+ [Git - git-config Documentation](https://git-scm.com/docs/git-config)

## git rebase

+ [git rebase | Atlassian Git Tutorial](https://www.atlassian.com/git/tutorials/rewriting-history/git-rebase)
+ [5.1 代码合并：Merge、Rebase 的选择 · geeeeeeeeek/git-recipes Wiki](https://github.com/geeeeeeeeek/git-recipes/wiki/5.1-%E4%BB%A3%E7%A0%81%E5%90%88%E5%B9%B6%EF%BC%9AMerge%E3%80%81Rebase-%E7%9A%84%E9%80%89%E6%8B%A9)

# Python

`easy_install` 被废弃了, 使用以下方式安装 `pip`。

```
curl https://bootstrap.pypa.io/get-pip.py -o get-pip.py
sudo python get-pip.py
```

# Android SDK

远程 Linux 机器上安装 Android SDK

```
wget https://dl.google.com/dl/android/studio/ide-zips/3.5.0.21/android-studio-ide-191.5791312-linux.tar.gz
wget https://dl.google.com/android/repository/platform-tools_r29.0.3-linux.zip
wget https://dl.google.com/android/android-sdk_r24.4.1-linux.tgz
```

更新 Android SDK

```
android update sdk --no-ui --proxy-host <host> --proxy-port <port>
```

远程 Linux 机器安装 Java

```
//yum install java
yum install java-1.8.0-openjdk-devel
```

+ [参考](https://www.vogella.com/tutorials/JenkinsAndroid/article.html)
+ [参考](https://developer.android.com/studio/command-line/sdkmanager)
+ [参考](https://developer.android.com/studio/command-line)
+ [参考](https://www.jianshu.com/p/92cf851c6620)


# curl

curl 发送 POST 请求

```
curl -X POST http://aaa/bbb -H @appmap.prop -d '
{
    "a": 1,
    "b", 2
}
'
```

curl 上传文件

```
curl http://ip:8000/log/ -F "file=@average.png" -v
curl -F data=@path/to/local/file UPLOAD_ADDRESS
```

`data` 对应于 form 表单中跟文件对应的字段

# 图片压缩
[pngquant — lossy PNG compressor](https://pngquant.org/)

[ImageOptim — better Save for Web](https://imageoptim.com/mac)

[TinyPNG](https://tinypng.com/)

[gruntjs/grunt-contrib-imagemin](https://github.com/gruntjs/grunt-contrib-imagemin)

# ss

参考自 [Mac 端如何配置 ss – 冰冰的小屋](http://16bing.com/2017/02/18/mac-shadowsocks/)

## 安装

```
pip3 install ss
```

## 启动

通过命令行指定参数：

```
ss -p 8080 -k password -m rc4-md5 -d start
```

通过配置文件指定参数：

```
ss -c /etc/ss.json -d start
```

## 配置
配置文件见 `/etc/ss.json`：

```json
{
  "server": "0.0.0.0",
  "server_port": 8080,
  "local_port": 1080,
  "password": "123456",
  "timeout": 600,
  "method": "rc4-md5"
}
```

# Anki

## 安装

```
easy_install AnkiServer
```

## 配置

```
mkdir Anki
cd Anki
cp /usr/local/lib/python2.7/dist-packages/AnkiServer-2.0.6-py2.7.egg/examples/example.ini production.ini
vi producion.ini
```

配置文件内容如下：

```
host= 10.XX.XX.XX  #自己服务器的地址
allowed_hosts=0.0.0.0 #允许同步的客户端ip地址，使用0.0.0.0表示允许任何ip地址连接
```

添加anki用户

```
ankiserverctl.py adduser username
```

## 启动

调试模式启动anki

```
ankiserverctl.py debug [configfile]
```

正常启动anki

```
ankiserverctl.py start [cofigfile]
```

# vscode

vscode 支持远程开发。配置步骤如下：

+ 生成 ssh-key - `ssh-keygen -t rsa -b 4096 -f ~/.ssh/id_rsa-remote-ssh`
+ 上传 ssh-key - `ssh-copy-id -p 36000 root@1.1.1.2`
+ 配置 vscode ssh-remote 插件

![-w841](media/15671473755624.jpg)


配置文件：

```
# Read more about SSH config files: https://linux.die.net/man/5/ssh_config
Host dev-host
    HostName 1.1.1.2
    User root
    Port 36000
    IdentityFile ~/.ssh/id_rsa-remote-ssh
```

这里记录配置过程中遇到的一个问题：远程机需要代理上网，vscode 一直卡在 `connect vscode remote server retry` 这个地方。

卡住的原因是 vscode 成功通过 ssh 连接上远程机上后还需要在远程机上下载安装一些命令，代理原因导致这一步不成功，所以卡住了。

解决办法：可以手动从 https://update.code.visualstudio.com/commit:<commit-id>/server-linux-x64/stable 下载文件，并且保存在如下位置。

```
~/.vscode-server/bin/2213894ea0415ee8c85c5eea0d0ff81ecc191529/vscode-server-linux-x64.tar.gz
```

重新打开 vscode remote 插件进行配置，成功！

+ [#issue 78](https://github.com/microsoft/vscode-remote-release/issues/78)
+ [Developing on Remote Machines using SSH and Visual Studio Code](https://code.visualstudio.com/docs/remote/ssh)

# node

这里记录一个在 centos 上安装 node 遇到的问题：[javascript - node: relocation error: node: symbol SSL_set_cert_cb, version libssl.so.10 not defined in file libssl.so.10 with link time reference - Stack Overflow](https://stackoverflow.com/questions/46473376/node-relocation-error-node-symbol-ssl-set-cert-cb-version-libssl-so-10-not-d)

## express 搭建文件上传服务

+ [express-fileupload - npm](https://www.npmjs.com/package/express-fileupload)
+ [express-fileupload](https://github.com/richardgirges/express-fileupload/blob/master/example/server.js)

## node热加载

`nodemon` 可用于 node 应用时热加载，避免开发过程修改代码后需要频繁关闭启动服务。

# phpmyadmin 的安装配置

安装配置步骤简单总结如下：

1. 下载 [phpMyAdmin](https://www.phpmyadmin.net/)
2. 将 zip 压缩包解压到 `/Library/WebServer/Documents` 下，命名为 `phpmyadmin`
3. `sudo chmod -R 755 phpmyAdmin/` 修改目录权限
4. 配置

## 检查 Apache 和 PHP

Mac 上已安装 Apache 和 PHP。

首先检查 php 版本：

```
➜  Documents php -v
PHP 7.1.23 (cli) (built: Nov  7 2018 18:20:35) ( NTS )
Copyright (c) 1997-2018 The PHP Group
Zend Engine v3.1.0, Copyright (c) 1998-2018 Zend Technologies
```

Apache 管理命令如下。

```
sudo apachectl start
sudo apachectl stop
sudo apachectl restart
```

启动 Apache 服务后，可以访问 localhost 检查服务是否正常。正常情况下显示如所示：

![-w403](https://blog-1251688504.cos.ap-shanghai.myqcloud.com/mweb/2019/15651604088791.jpg)

## PHP 配置

开启 Apache PHP 模块的方法是在 `/etc/apache2/httpd.conf` 文件中开启如下配置：

```
LoadModule php7_module libexec/apache2/libphp7.so
```

同一文件中添加如下配置，允许访问 `phpadmin`。

```
<Directory "/Library/WebServer/Documents/phpmyadmin">
    Options FollowSymLinks Multiviews
    MultiviewsMatch Any
    AllowOverride All
    Allow from all
</Directory>
```

编辑 `config.inc.php` 下的配置，添加数据库地址和端口。

```
/**
 * First server
 */
$i++;
/* Authentication type */
$cfg['Servers'][$i]['auth_type'] = 'cookie';
/* Server parameters */
$cfg['Servers'][$i]['host'] = '地址';
$cfg['Servers'][$i]['port'] = '端口';
$cfg['Servers'][$i]['compress'] = false;
$cfg['Servers'][$i]['AllowNoPassword'] = false;
```

## 使用 phpmyadmin

配置完成后启动 Apache 服务：

```
sudo apachectl start
```

访问 `http://localhost/phpmyadmin/index.php` 即可开始使用 phpadmin 管理数据库。

![-w567](https://blog-1251688504.cos.ap-shanghai.myqcloud.com/mweb/2019/15651611418611.jpg)

# MySql

安装

```
sudo apt install mysql-server
sudo mysql_secure_installation // 配置 root 用户
```

创建可以从本地登录的用户

```
CREATE USER 'sammy'@'localhost' IDENTIFIED BY 'password
```

创建可以从远程登录的用户

```
CREATE USER 'sammy'@'%' IDENTIFIED BY 'password
```

参考自：

+ [How To Install MySQL on Ubuntu 18.04 | DigitalOcean](https://www.digitalocean.com/community/tutorials/how-to-install-mysql-on-ubuntu-18-04)
+ [Allow all remote connections, MySQL - Stack Overflow](https://stackoverflow.com/questions/10236000/allow-all-remote-connections-mysql)

# screen

离开会话 Ctrl + A + D

查看会话列表 screen -ls

重新连接会话 screen -r <id>


[screen命令用法和快捷键 | Web开发笔记](https://www.magentonotes.com/screen-command-shortcuts.html)

+ C-a ?	显示所有键绑定信息
+ C-a w	显示所有窗口列表
+ C-a C-a	切换到之前显示的窗口
+ C-a c	创建一个新的运行shell的窗口并切换到该窗口
+ C-a n	切换到下一个窗口
+ C-a p	切换到前一个窗口(与C-a n相对)
+ C-a 0..9	切换到窗口0..9
+ C-a a	发送 C-a到当前窗口
+ C-a d	暂时断开screen会话
+ C-a k	杀掉当前窗口
+ C-a [	进入拷贝/回滚模

[ref](https://www.ibm.com/developerworks/cn/linux/l-cn-screen/)

# brew


# ssh

## 无法连接
有时ssh需要通过代理才能使用。

在`~/.ssh/config`文件中配置代理：

```
ProxyCommand=corkscrew 127.0.0.1 12759 %h %p
```

记得先安装`corkscrew`工具。

```
➜  corkscrew
corkscrew 2.0 (agroman@agroman.net)

usage: corkscrew <proxyhost> <proxyport> <desthost> <destport> [authfile]
```

## Permission denied

```
$ ssh user@server
Permission denied (publickey)
```

原因是无法密码登录导致ssh-copy-id执行失败。

解决办法如下

+ 使用root登录到server (digitalocean的话只能通过web console登录上去了)
+ 编辑`vi /etc/ssh/sshd_config`

添加如下配置，允许密码登录

```
PasswordAuthentication yes
```

重启sshd服务，`/etc/init.d/sshd restart`

使用 ssh-copy-id 命令手动将公钥文件拷贝到目标机器

```
ssh-copy-id -i .ssh/id_rsa.pub  用户名字@192.168.x.xxx
```

`Permission denied (publickey)`问题解决后记得关闭密码登录功能。

```
PasswordAuthentication no
```

参考 [ref](https://www.digitalocean.com/community/questions/error-permission-denied-publickey-when-i-try-to-ssh)

# pip



pip指定私有库

```
pip install -r requirements.txt -i http://pypi.alibaba.com/simple/ --trusted-host pypi.alibaba.com
```

pip指定版本

```
$ pip install SomePackage==1.0.4  ##指定版本的安装
```

pip卸载包

```
$ pip uninstall package-name  ##卸载

```

pip安装离线包

```
$ pip install ./downloads/SomePackage-1.0.4.tar.gz
```

pip按名字查询
```
$ pip search "query"   ##查询package的具体名称
```

pip列出已安装的包

```
pip list
```

Mac上提示Permission问题时，使用`sudo`安装。`sudo pip install`

---

多个版本的 Python 共存时，比如 Python 2.7 和 Python 3.7 共存时，如何为指定给哪个版本的 Python 安装包呢？

(这时可能有 pip, pip3 等多个命令)，所以答案是

> You can install Python packages with
>   pip3 install <package>
> They will install into the site-package directory
>   /usr/local/lib/python3.7/site-packages

---

```
➜  trunk brew install https://raw.githubusercontent.com/Homebrew/homebrew-core/f2a764ef944b1080be64bd88dca9a1d80130c558/Formula/python.rb
...
Error: python 3.7.1 is already installed
To install 3.6.5_1, first run `brew unlink python`
➜  trunk brew unlink python
Unlinking /usr/local/Cellar/python/3.7.1... 24 symlinks removed
➜  trunk brew install https://raw.githubusercontent.com/Homebrew/homebrew-core/f2a764ef944b1080be64bd88dca9a1d80130c558/Formula/python.rb
```

# pipenv


+ [pipenv](https://github.com/pypa/pipenv)
+ [Python新利器之pipenv - 简书](https://www.jianshu.com/p/00af447f0005)

## 实例
从网上下载了一个python项目`cdntool`，它只是简单地说明要依赖python3.6, tornado 4.5+, xmltodict(但没有给出任何相关地配置)。直接运行该项目立即报错提示缺少依赖

```
ModuleNotFoundError: No module named 'tornado'
```

如何使用pipenv给`cdntool`添加依赖？

## pipenv命令

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





## 运行程序
并不是安装依赖后就可以直接运行`cdntool`。直接运行`cdntool`的话你会发现依赖提示`ModuleNotFoundError`。正确的做法是先执行`pipenv shell`激活virtualenv：

```
➜  cdntool pipenv shell
Launching subshell in virtual environment…
 . /Users/kingcmchen/.local/share/virtualenvs/cdntool-q0oUnpG_/bin/activate
➜  cdntool  . /Users/kingcmchen/.local/share/virtualenvs/cdntool-q0oUnpG_/bin/activate
```

然后在这个虚拟环境中运行`cdntool`。

# IDEA

[导入 maven 工程至 IDEA](https://www.lagomframework.com/documentation/1.4.x/java/IntellijMaven.html)

# bash

[bash](https://github.com/410063005/the-art-of-command-line/blob/master/README-zh.md)

# 参考
+ [手把手教你搭建自己专属的Anki服务器 - 简书](https://www.jianshu.com/p/c50e3feec878)
+ [anki-sync-server](https://github.com/dsnopek/anki-sync-server)
+ [Anki-Android](https://github.com/ankidroid/Anki-Android)
+ [anki](https://github.com/dae/anki)
