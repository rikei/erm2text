---
title: erm2text-ermaster文件可视化比对工具
date: 2020-09-04 20:35:09
tags: 小工具
---

![BCompare使用erm2text效果](https://cdn.ruo-shui.cn/2020/2bdd7bd4a6e254eff47549593c04e590.png)



## 为什么做这个工具

*.erm文件可以通过ermaster插件进行修改和查看，其本质是一个xml文件。

当多个人协同开发时或者多个代码分支都修改了erm文件时，则没有很好的方法或工具可以比对出两个erm的差异有哪些，很用已就把别人修改覆盖或者丢失了一部分修改。

人工对比文件效率低下且容易出错，所以有了比较erm文件的需求，结合现有的一些资料，开发了这个小工具

## 主要功能

能够将*.erm的文件转换为纯文本文件

支持转换下列内容：

- 表名（中文名，英文名）
- 列名（中文名，英文名，字段类型，是否为空）
- 索引信息
- 唯一索引

## 使用方法

### 直接运行

语法

```
jar -jar erm2text-1.0-SNAPSHOT-jar-with-dependencies.jar example.erm  example.txt
```

`example.txt`即为转换后的文件

![命令形式执行erm2text](https://cdn.ruo-shui.cn/2020/4a982288d52d239d5da60426b6f587ac.png)

### BeyondCompare插件

打开BeyondCompare软件，点击 工具->导入设置， 选择对应的BCFormat_win.bcpkg或者BCFormat_mac.bcpkg文件，然后下一步，下一步，完成

![image-20200904204055792](https://cdn.ruo-shui.cn/2020/b2235395949d8720ffa4c4711aa1b2fb.png)



## 如何编译

```bash
mvn clean install
或者
mvn package assembly:single
```


## 使用效果展示

![BCompare使用erm2text效果](https://cdn.ruo-shui.cn/2020/2bdd7bd4a6e254eff47549593c04e590.png)


## 其他

### 测试环境

操作系统 Mac 10.15.6 /Win10

Jdk1.8

Maven 3.6.3

Beyond Compare 4.3.5



## 下载链接

可执行程序和BCompare插件

https://github.com/rikei/erm2text/releases/tag/1.0-SNAPSHOT
