### 1. RabbitMQ 简介

 RabbitMQ 是一个开源的 AMQP 实现，服务器端用 Erlang 语言编写，支持多种客户端。用于在分布式系统中存储转发消息，在易用性、扩展性、高可用性等方面表现不俗 

支持协议：AMQP、STOMP、MQTT、HTTP、WebSocket

### 2. RabbitMQ 安装运行

#### 1. 安装依赖环境

######  安装 通用依赖 

``` shell
yum -y install gcc
yum install -y autoconf
yum install -y ncurses-devel
```

######  安装 erlang 

```shell
wget https://github.com/erlang/otp/archive/OTP-23.2.tar.gz
tar vxf OTP-23.2.tar.gz
cd otp-OTP-23.2/
./otp_build autoconf
./configure
make
make install
```

######  安装 socat 

```shell
sudo yum install -y socat
```

#### 2. 安装 RabbitMQ

######  下载 

```shell
wget -P /home/download https://github.com/rabbitmq/rabbitmq-server/releases/download/v3.8.21/rabbitmq-server-3.8.21-1.el7.noarch.rpm
```

- 可以在 [https://github.com/rabbitmq/rabbitmq-server/tags](https://www.oschina.net/action/GoToLink?url=https%3A%2F%2Fgithub.com%2Frabbitmq%2Frabbitmq-server%2Ftags) 下载历史版本。

######  安装 RabbitMQ 

```shell
sudo rpm -Uvh rabbitmq-server-3.7.23-1.el7.noarch.rpm 
```

- 假如还是报版本错误，添加 --nodeps 参数。

#### 3. 启动和关闭

1. 启动服务

   ```sh
   sudo systemctl start rabbitmq-server
   ```

2. 查看状态

   ```sh
   sudo systemctl status rabbitmq-server
   ```

3. 停止服务

   ```sh
   sudo systemctl stop rabbitmq-server
   ```

4. 设置开机启动

   ```sh
   sudo systemctl enable rabbitmq-server
   ```

#### 4. 设置防火墙

1. ###### 开放端口

   ```sh
   # 需开放 4369、5672、25672、15672
   sudo firewall-cmd --zone=public --add-port=4369/tcp --permanent
   sudo firewall-cmd --zone=public --add-port=5672/tcp --permanent
   sudo firewall-cmd --zone=public --add-port=25672/tcp --permanent
   sudo firewall-cmd --zone=public --add-port=15672/tcp --permanent
   
   ```

2. ###### 重启防火墙

   ```sh
   sudo firewall-cmd --reloade
   ```

#### 5.RabbitMQ 基本配置

RabbitMQ 有一套默认的配置，能够满足日常开发需求，如果需要修改，需要自己创建一个配置文件：

```touch /etc/rabbitmq/rabbitmq.conf```
配置文件示例：https://github.com/rabbitmq/rabbitmq-server/blob/master/docs/rabbitmq.conf.example

配置项说明：https://www.rabbitmq.com/configure.html#config-items


#### 6.RabbitMQ 端口

-  RabbitMQ 会绑定一些端口，安装完后，需要将这些端口添加至防火墙 

  | 端口         | 描述                                                         |
  | ------------ | ------------------------------------------------------------ |
  | 4369         | 是 Erlang 的端口/节点名称映射程序，用来跟踪节点名称监听地址，在集群中起到一个类似 DNS 的作用。 |
  | 5672、5671   | AMQP 0-9-1 和 1.0 客户端端口，没有使用 SSL 和使用 SSL 的端口。 |
  | 25672        | 用于 RabbitMQ 节点间和 CLI 工具通信，配合 4369 使用。        |
  | 15672        | HTTP_API 端口，管理员用户才能访问，用于管理 RabbitMQ，需要启用 management 插件。 |
  | 61613、61614 | 当 STOMP 插件启用的时候打开，作为 STOME 客户端端口（根据是否使用 TLS 选择）。 |
  | 1883、8883   | 当 MQTT 插件启用的时候打开，作为 MQTT 客户端端口（根据是否使用 TLS 选择） |
  | 15674        | 基于 WebSocket 的 STOMP 客户端端口（当插件 Web STOMP启用的时候打开）。 |
  | 15675        | 基于 WebSocket 的 MQTT 客户端端口（当插件 Web MQTT 启用的时候打开）。 |

  

#### 7.启用 RabbitMQ 管理界面

-  RabbitMQ 安装包中带有管理插件，但需要手动激活。 

  ```sh
  rabbitmq-plugins enable rabbitmq_management
  ```

-  RabbitMQ 有一个默认的用户“guest”，但这个用户默认只能通过本机访问，要让其他机器可以访问，需要创建一个新用户，为其分配权限。 

  ```sh
  # 添加用户
  rabbitmqctl add_user admin admin
  # 为用户分配权限
  rabbitmqctl set_user_tags admin administrator
  # 为用户分配资源权限
  rabbitmqctl set_permissions -p / admin ".*" ".*" ".*"
  ```

#### 8.RabbitMQ 的用户角色分类

``` none、management、policymaker、monitoring、administrator ```

- ###### none：

  不能访问 management plugin

- ###### management：

  用户可以通过 AMQP 做任何事情。
  列出自己可以通过 AMQP 登入的 virtual hosts。
  查看自己的 virtual hosts 中的 queues、exchanges、bindings。
  查看和关闭自己的 channels 和 connections。
  查看有关自己的 virtual hosts 的“全局”的统计信息，包含其他用户在这些 virtual hosts 中的活动。

- ###### policymaker

  management 可以做的任何事情。

  查看、创建和删除自己的 virtual hosts 所属的 policies 和 parameters。

- ###### monitoring

  management 可以做的任何事情。
  列出所有的 virtual hosts，包括他们不能登录的 virtual hosts。
  查看其他用户的 connections 和 channels。
  查看节点级别的数据如 clustering 和 memory 使用情况。
  查看真正的关于所有 virtual hosts 的全局的统计信息。

- ###### administrator

  policymaker 和 monitoring 可以做的任何事情。

  创建和删除 virtual hosts

  查看、创建和删除 users

  查看、创建和删除 permissions

  关闭其他用户的 connections

  

### 3.  RabbitMQ的使用

#### 1.在 Java 中使用 RabbitMQ

##### Maven 依赖

```xml
<dependency>
    <groupId>com.rabbitmq</groupId>
    <artifactId>amqp-client</artifactId>
    <version>5.8.0</version>
</dependency>
```

##### Producer

```java
/**
 * 简单队列生产者
 * 使用 RabbitMQ 的默认交换器发送消息
 */
public class Producer {
    public static void main(String[] args) throws IOException, TimeoutException {
        // 1. 创建连接工厂
        ConnectionFactory factory = new ConnectionFactory();
        // 2. 设置连接属性
        factory.setHost("114.67.85.157");
        factory.setUsername("admin");
        factory.setPassword("admin");
 
        Connection connection = null;
        Channel channel = null;
 
        try {
            // 3. 从连接工厂获取连接
            connection = factory.newConnection("生产者");
 
            // 4. 从连接中创建通道
            channel = connection.createChannel();
 
            /**
             * 5、声明（创建）队列
             * 如果队列不存在，才会创建
             * RabbitMQ 不允许声明两个队列名相同，属性不同的队列，否则会报错
             *
             * queueDeclare参数说明：
             * @param queue 队列名称
             * @param durable 队列是否持久化
             * @param exclusive 是否排他，即是否为私有的，如果为true,会对当前队列加锁，其它通道不能访问，
             *                  并且在连接关闭时会自动删除，不受持久化和自动删除的属性控制。
             *                  一般在队列和交换器绑定时使用
             * @param autoDelete 是否自动删除，当最后一个消费者断开连接之后是否自动删除
             * @param arguments 队列参数，设置队列的有效期、消息最大长度、队列中所有消息的生命周期等等
             */
            channel.queueDeclare("queue1", false, false, false, null);
 
            // 消息内容
            String message = "Hello World!";
 
            // 6. 发送消息
            channel.basicPublish("", "queue1", null, message.getBytes());
            System.out.println("消息已发送！");
        } finally {
            // 7. 关闭通道
            if (channel != null && channel.isOpen()) {
                channel.close();
            }
 
            // 8. 关闭连接
            if (connection != null && connection.isOpen()) {
                connection.close();
            }
        }
    }
}
```

##### Consumer

```java
/**
 * 简单队列消费者
 */
public class Consumer {
    public static void main(String[] args) throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
 
        factory.setHost("114.67.85.157");
        factory.setPort(5672);
        factory.setUsername("admin");
        factory.setPassword("admin");
 
        Connection connection = null;
        Channel channel = null;
 
        try {
            connection = factory.newConnection("消费者");
 
            channel = connection.createChannel();
 
            channel.queueDeclare("queue1", false, false, false, null);
 
            // 定义收到消息后的回调
            DeliverCallback callback = new DeliverCallback() {
                @Override
                public void handle(String consumerTag, Delivery message) throws IOException {
                    System.out.println("收到消息：" + new String(message.getBody(), "UTF-8"));
                }
            };
 
            // 监听队列
            channel.basicConsume("queue1", true, callback, new CancelCallback() {
                @Override
                public void handle(String consumerTag) throws IOException {
                }
            });
 
            System.out.println("开始接收消息！");
            System.in.read();
        } finally {
            if (channel != null && channel.isOpen()) {
                channel.close();
            }
            if (connection != null && connection.isOpen()) {
                connection.close();
            }
        }
    }
}
```

####  2.在 Spring 中使用 RabbitMQ

#### 1.Maven 依赖

```xml
<dependency>
    <groupId>org.springframework.amqp</groupId>
    <artifactId>spring-amqp</artifactId>
    <version>2.2.2.RELEASE</version>
</dependency>
<dependency>
    <groupId>org.springframework.amqp</groupId>
    <artifactId>spring-rabbit</artifactId>
    <version>2.2.2.RELEASE</version>
</dependency>
```

### 4. AMQP 协议

#### AMQP 是什么

- AMQP(Advanced Message Queuing Protocol) 高级消息队列协议，是应用层协议的一个开放标准，为面向消息的中间件设计

#### AMQP 结构

![1629860657276](img\1629860657276.png)

#### AMQP 生产者流转过程

![1629860709213](img\1629860709213.png)

#### AMQP 消费者流转过程

![1629860756385](img\1629860756385.png)

### 5. RabbitMQ 核心概念

![1629860791906](img\1629860791906.png)

#### 整体架构

![1629860819632](img\1629860819632.png)

#### Producer

- Producer：生产者，就是投递消息的一方。生产者创建消息，然后发布到 RabbitMQ 中。


- 消息一般分为两个部分：

  a.消息体（payload）：在实际应用中，消息体一般是一个带有业务逻辑结构的数据，比如一个 JSON 字符串。当然可以进一步对这个消息体进行序列化操作。
  b.附加消息：用来表述这条消息，比如目标交换器的名称、路由键和一些自定义属性等等。

#### Broker

- Broker：消息中间件的服务节点。
- 对于 RabbitMQ 来说，一个 RabbitMQ Broker 可以简单的地看作一个 RabbitMQ 服务节点，或者 RabbitMQ 服务实例。也可以将一个 RabbitMQ Broker 看作一台 RabbitMQ 服务器。

#### Virtual 

#### Host

- Virtual Host：虚拟主机，表示一批交换器、消息队列和相关对象。


- 虚拟主机是共享相同的身份认证和加密环境的独立服务器域。

- 每个 vhost 本质上就是一个 mini 版的 RabbitMQ 服务器，拥有自己的队列、交换器、绑定和权限机制。

- vhost 是 AMQP 概念的基础，必须在连接时指定，RabbitMQ 默认的 vhost /。
  

#### Channel

- Channel：频道或信道，是建立在 Connection 连接之上的一种轻量级的连接。

- 大部分的操作是在 Channel 这个接口中完成的，包括定义队列的声明 queueDeclare、交换机的声明 exchangeDeclare、队列的绑定 queueBind、发布消息 basicPublish、消费消息 basicConsume 等。


- 如果把 Connection 比作一条光纤电缆的话，那么 Channel 信道就比作光纤电缆中的其中一束光纤。一个 Connection 上可以创建任意数量的 Channel。


#### RoutingKey

- RoutingKey：路由健。生产者将消息发给交换器的时候，一般会指定一个 RoutingKey，用来指定这个消息的路由规则。

- RoutingKey 需要与交换器类型和绑定键（BindingKey）联合使用。


- 在交换器类型和绑定键（BindingKey）固定的情况下，生产者可以在发送消息给交换器时，通过指定 RoutingKey 来决定消息流向哪里。


#### Exchange

- Exchange：交换器，生产者将消息发送到 Exchange（交换器，通常也可以用大写的“X”来表示），由交换器将消息路由到一个或多个队列中。如果路由不到，或返回给生产者，或直接丢弃。

![1629861029820](img\1629861029820.png)

#### Queue

- Queue：队列，是 RabbitMQ 的内部对象，用于存储消息。

![1629861059502](img\1629861059502.png)

#### Binding

- Binding：绑定，RabbitMQ 中通过绑定将交换器与队列关联起来，在绑定的时候一般会指定一个绑定键（BindingKey），这样 RabbitMQ 就知道如何正确地将消息路由到队列了。

![1629861090944](img\1629861090944.png)

#### Consumer

- Consumer：消费者，就是接收消息的一方。消费者连接到 RabbitMQ 服务器，并订阅到队列上。

- 当消费者消费一条消息时，只是消费消息的消息体（payload）。在消息路由的过程中，消息的标签会丢弃，存入到队列中的消息只有消息体，消费者也只会消费到消息体，也就不知道消息的生产者是谁，当然消费者也不需要知道
  

### 6. Exchange 类型

``` RabbitMQ 常用交换器类型：fanout、direct、topic、headers 四种 ```

#### fanout 广播

- fanout：扇形交换机
- 它会把所有发送到该交换器的消息路由到所有与该交换器绑定的队列中。

![1629861198151](img\1629861198151.png)

#### direct 直连

- direct：直连交换机
- 它会把消息路由到那些 BindingKey 和 RoutingKey 完全匹配的队列中

![1629861230519](img\1629861230519.png)

#### topic

- topic：主题交换机。

- 与 direct 类似，但它可以通过通配符进行模糊匹配。

  ```# 代表匹配一个或多个单词```

  ``` * 代表匹配一个单词```

  ``` 每个单词用.隔开```

   

![1629861257254](img\1629861257254.png)

#### headers

- headers：头交换机。
- 不依赖于路由键的匹配规则来路由消息，而是根据发送的消息内容中的 headers 属性进行匹配。
- headers 类型的交换器性能很差，而且也不实用。

### 7. 运转流程

![1629861308132](img\1629861308132.png)

![1629861329381](img\1629861329381.png)

- 生产者发送消息的过程：

  生产者连接到 RabbitMQ Broker，建立一个连接（Connection），开启一个信道（Channel）。
  生产者声明一个交换器，并设置相关属性，比如交换机类型，是否持久化等。
  生产者声明一个队列并设置相关属性，比如是否排他、是否持久化、是否自动删除等。
  生产者通过路由键将交换器和队列绑定起来。
  生产者发送消息到 RabbitMQ Broker，其中包含路由键、交换器等信息。
  相应的交换器根据接收到的路由键查找相匹配的队列。
  如果找到，则将生产者发送过来的消息存入相应的队列中。
  如果没有找到，则根据生产者配置的属性丢弃还是回退给生产者。
  关闭信道，关闭连接。

- 消费者接收消息的过程：

  消费者连接到 RabbitMQ Broker，建立一个连接（Connection），开启一个信道（Channel）。
  消费者向 RabbitMQ Broker 请求消费相应队列中的消息，可能会设置相应的回调函数，以及做一些准备工作。
  等待 RabbitMQ Broker 回应并投递相应队列中的消息，消费者接收消息。
  消费者确认（ack）接收到的消息。
  RabbitMQ 从队列中删除相应已经被确认的消息。
  关闭信道、关闭连接。

### 8. 代码演示

```java
/**
 * Topic--生产者
 * 生产者将消息发送到topic类型的交换器上，和routing的用法类似，都是通过routingKey路由，但topic类型交换器的routingKey支持通配符
 */
public class Producer {
    public static void main(String[] args) throws IOException, TimeoutException {
        // 1. 创建工厂
        ConnectionFactory factory = new ConnectionFactory();
        // 2. 设置连接属性
        factory.setHost("114.67.85.157");
        factory.setPort(5672);
        factory.setUsername("admin");
        factory.setPassword("admin");
 
        Connection connection = null;
        Channel channel = null;
 
        try {
            // 3. 从连接工厂创建连接
            connection = factory.newConnection("生产者");
 
            // 4. 从连接中创建通道
            channel = connection.createChannel();
 
            // 路由关系如下：com.# --> queue-1  *.order.* --> queue-2
            // 消息内容
            String message = "Hello A";
            // 发送消息到 topic_test 交换器上
            channel.basicPublish("topic-exchange", "com.order.create", null, message.getBytes());
            System.out.println("消息 " + message + "已发送！");
 
            // 消息内容
            message = "Hello B";
            // 发送消息到topic_test交换器上
            channel.basicPublish("topic-exchange", "com.sms.create", null, message.getBytes());
            System.out.println("消息 " + message + " 已发送！");
 
            // 消息内容
            message = "Hello C";
            // 发送消息到topic_test交换器上
            channel.basicPublish("topic-exchange", "cn.order.create", null, message.getBytes());
            System.out.println("消息 " + message + " 已发送！");
        } catch (TimeoutException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // 7、关闭通道
            if (channel != null && channel.isOpen()) {
                channel.close();
            }
            // 8、关闭连接
            if (connection != null && connection.isOpen()) {
                connection.close();
            }
        }
    }
}
```

```java
/**
 * 路由--消费者
 * 消费者通过一个临时队列和交换器绑定，接收发送到交换器上的消息
 */
public class Consumer {
    private static Runnable receive = () -> {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("114.67.85.157");
        factory.setPort(5672);
        factory.setUsername("admin");
        factory.setPassword("admin");
 
        Connection connection = null;
        Channel channel = null;
        final String queueName = Thread.currentThread().getName();
 
        try {
            connection = factory.newConnection("消费者");
            channel = connection.createChannel();
            DeliverCallback callback = (consumerTag, message) -> {
                System.out.println(queueName + " 收到消息：" + new String(message.getBody(), "UTF-8"));
            };
            channel.basicConsume(queueName, true, callback, consumerTag -> {
            });
            System.out.println(queueName + " 开始接收消息");
            System.in.read();
        } catch (TimeoutException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (channel != null && channel.isOpen()) {
                try {
                    channel.close();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (TimeoutException e) {
                    e.printStackTrace();
                }
            }
            if (connection != null && connection.isOpen()) {
                try {
                    connection.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    };
 
    public static void main(String[] args) {
        new Thread(receive, "queue-1").start();
        new Thread(receive, "queue-2").start();
    }
}
```

### 9. 持久化与内存管理

RabbitMQ 中会有一个内存阈值，当内存大小到达阈值的时候，防止系统崩坏，会停止接收客户端的消息，并抛出异常，

此时可临时调整RabbitMQ的 参数

**相对模式**

``` rabbitmqctl set_vm_memory_high_watermark <fraction>```

fraction为内存阈值，默认为0.4， 表示RabbitMQ使用的内存超过系统内存的40%时，会产生内存告警,通过此命令修改的阈值在重启后会失效。可以通过修改配置文件的方式，使之永久生效，但是需要重启服务



**绝对模式**

```rabbitmqctl set_vm_memory_high_watermark absolute <value>```

absolute: 绝对值，固定大小，单位为KB、MB、GB



#### 内存换页

在RabbitMQ达到内存阈值并阻塞生产者之前，会尝试将内存中的消息换页到磁盘，以释放内存空间

``` vm_memory_high_watermark_paging_ratio=0.5```

当换页阈值大于1时，相当于禁用了换页功能

#### 磁盘控制

RabbitMQ 通过磁盘阈值参数控制磁盘的使用量，当磁盘剩余空间小于磁盘阈值时，RabbitMQ同样会阻塞生产者，避免磁盘空间耗尽

```shell
rabbitmqctl set_disk_free_limit <limit>
rabbitmqctl set_disk_free_limit mem_relative <fraction>
# limit为绝对值，KB，MB，GB
# fraction 为相对值 建议1.0~2.0之间

# rabbitmq.conf
disk_free_limit.relative = 1.5
#disk_free_limit.abslute=50MB
```

### 配置属性和描述

| 属性 | 描述 | 默认值 |
| ---- | ---- | ------ |
|      |      |        |
|      |      |        |
|      |      |        |

