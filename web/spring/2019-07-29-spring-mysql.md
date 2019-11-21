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

# 参考
+ [How To Install MySQL on Ubuntu 18.04 | DigitalOcean](https://www.digitalocean.com/community/tutorials/how-to-install-mysql-on-ubuntu-18-04)
+ [Allow all remote connections, MySQL - Stack Overflow](https://stackoverflow.com/questions/10236000/allow-all-remote-connections-mysql)