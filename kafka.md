- # 官网


<u>[kafka 中文翻译](https://kafka.apachecn.org/)</u>

> 应用场景

```markdown
由于kafka具有更好的吞吐量、内置分区、冗余及容错性的有点（kafka每秒可以处理几十万条消息），让kafka成为可一个很好的大规模消息处理应用的解决方案。所以再企业级应用中，主要会应用于如下几个方面
    行为追踪：
   kafka可以用于跟踪用户浏览页面、搜索及其他行为。通过发布-订阅模式实时记录到对应的
topic中，通过后端大数据平台接入处理分析，并做更进一步的实时处理和监控
    日志收集:
   ：日志收集方面，有很多比较优秀的产品，比如Apache Flume，很多公司使用kafka代理日志
聚合。日志聚合表示从服务器上收集日志文件，然后放到一个集中的平台（文件服务器）进行处理。在实际应用开发中，我们应用程序的log都会输出到本地的磁盘上，排查问题的话通过linux命令来搞定，如果应用程序组成了负载均衡集群，并且集群的机器有几十台以上，那么想通过日志快速定位到问题，就是很麻烦的事情了。所以一般都会做一个日志统一收集平台管理log日志用来快速查询重要应用的问题。所以很多公司的套路都是把应用日志集中到kafka上，然后分别导入到es和hdfs上，用来做实时检索分析和离线统计数据备份等。而另一方面，kafka本身又提供了很好的api来集成日志并且做日志收集
	数据集成
	大数据领域
	流计算集成

```

> 名词解释

- **Broker**

​	Kafka集群包含一个或多个服务器，这种服务器被称为broker。broker端不维护数据的消费状态，提升 了性能。直接使用磁盘进行存储，线性读写，速度快：避免了数据在JVM内存和系统内存之间的复制， 减少耗性能的创建对象和垃圾回收。

- **Producter**

​	负责发布消息到Kafka broker

- **Consumer**

​	消息消费者，向Kafka broker读取消息的客户端，consumer从broker拉取(pull)数据并进行处理

- **Topic**

​	每条发布到Kafka集群的消息都有一个类别，这个类别被称为Topic。（物理上不同Topic的消息分开存储，逻辑上一个Topic的消息虽然保存于一个或多个broker上但用户只需指定消息的Topic即可生产或消费数据而不必关心数据存于何处）

- **Partition**

​	 Parition是物理上分区的概念，每个Topic包含一个或多个Partition

- **Consumer Group**

​	每个Consumer属于一个特定的Consumer Group（可为每个Consumer指定group name，若不指定 group name则属于默认的group）

- **Topic & Partition**

​	Topic在逻辑上可以被认为是一个queue，每条消费都必须指定它的Topic，可以简单理解为必须指明把 这条消息放进哪个queue里。为了使得Kafka的吞吐率可以线性提高，物理上把Topic分成一个或多个 Partition，每个Partition在物理上对应一个文件夹，该文件夹下存储这个Partition的所有消息和索引文 件。若创建topic1和topic2两个topic，且分别有13个和19个分区，则整个集群上会相应会生成共32个 文件夹（本文所用集群共8个节点，此处topic1和topic2 replication-factor均为1）

# kafka安装与命令

> 下载连接  https://archive.apache.org/dist/kafka/3.9.0/kafka_2.12-3.9.0.tgz 

```shell
# 安装依赖：JDK 8+ 

wget https://archive.apache.org/dist/kafka/3.9.0/kafka_2.12-3.9.0.tgz
#解压
tar -zxvf  kafka_2.12-3.9.0.tgz 
cd kafka_2.12-3.9.0


#启动 Kafka 环境
#Kafka 依赖 Zookeeper，安装包中已经包含。
#启动 Zookeeper
bin/zookeeper-server-start.sh config/zookeeper.properties

#默认 Zookeeper 运行在端口 2181。



#2. 启动 Kafka Broker
bin/kafka-server-start.sh config/server.properties
# 默认 Kafka 运行在端口 9092。

```

## 启动和停止kafka 

```shell
# 

## 修改server.properties, 增加zookeeper的配置
zookeeper.connect=localhost:2181

##启动kafka
 sh kafka-server-start.sh -damoen config/server.properties

##  sh kafka-server-stop.sh -daemon config/server.properties

```

## kafka的基本操作 

```shell
# 创建topic
sh kafka-topics.sh --create --zookeeper localhost:2181 
--replication-factor 1 --partitions 1 --topic test

## Replication-factor 表示该topic需要在不同的broker中保存几份，这里设置成1，表示在两个broker中保存两份 Partitions 分区数


# 查看topic
sh kafka-topics.sh --list --zookeeper localhost:2181

#查看topic属性
sh kafka-topics.sh --describe --zookeeper localhost:2181 --topic first_topic

# 消费消息
sh kafka-console-consumer.sh --bootstrap-server 192.168.13.106:9092 
--topic test --from-beginning


# 发送消息
sh kafka-console-producer.sh --broker-list 192.168.244.128:9092 --topic 
first_topic
```

## 集群环境安装

### 环境准备  

- 准备三台虚拟机

- 分别把kafka的安装包部署在三台机器上

### 修改配置

  以下配置修改均为server.properties 

- 分别修改三台机器的server.properties配置，同一个集群中的每个机器的id必须唯一

  > broker.id=0
  >
  > broker.id=1
  >
  > broker.id=2 

- 修改zookeeper的连接配置 

  > zookeeper.connect=192.168.13.106:2181

-  修改listeners配置 

  如果配置了listeners，那么消息生产者和消费者会使用listeners的配置来进行消息的收发，否则， 会使用localhost PLAINTEXT表示协议，默认是明文，可以选择其他加密协议 

  > listeners=PLAINTEXT://192.168.13.102:9092 

  分别启动三台服务器 

  > sh kafka-server-start.sh -daemon ../config/server.properties
