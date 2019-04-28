
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
```

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

## 参考
+ [手把手教你搭建自己专属的Anki服务器 - 简书](https://www.jianshu.com/p/c50e3feec878)
+ [anki-sync-server](https://github.com/dsnopek/anki-sync-server)
+ [Anki-Android](https://github.com/ankidroid/Anki-Android)
+ [anki](https://github.com/dae/anki)
