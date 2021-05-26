#### Flink	

```shell
nc -lk  7777  #启动发送socket 文本流服务器  nc linux自带的一个命令 l 表示监听，监听某一端口 k 表示keep 保持当前的链接
```





## Flinl 部署

 #### Standalone模式

安装：

解压缩。flink-1.10.1-bin-scala_2.12.tgz 进入conf目录中。

​	1、修改 flink/conf/flink-conf.yaml 文件

      ```properties
jobmamanger.rpc.address:hadoop1
      ```

2、修改/conf/slaves 文件：

```properties
hadoop2
hadoop3
```

3、分发给另外两台机子

```shell
[xxxx@xx conf]$ xsync flink-1.10.0
```

4、启动

```shell
[xxxx@xx bin]$ ./start-cluster.sh
```





```properties
taskmanager.numberOfTaskSlots:1 #并行最大的能力,针对几个task而言 slots：插槽

parallelism.default: 1  # env中的配置， 代码中写了 这里就不写了

```





