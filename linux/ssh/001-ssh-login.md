
# 无法连接
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

# Permission denied

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

再次`ssh user@server`。这时会提示输入密码。正确输入密码后，下次就可以直接登录了。

`Permission denied (publickey)`问题解决后记得关闭密码登录功能。

```
PasswordAuthentication no
```

# 参考
[ref](https://www.digitalocean.com/community/questions/error-permission-denied-publickey-when-i-try-to-ssh)
