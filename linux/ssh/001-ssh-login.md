
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
