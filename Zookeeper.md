# Zookeeper

> 单机环境安装
>
> - 环境
>
>   Java8 +
>
>   Maven
>
> - 下载地址
>
>   https://zookeeper.apache.org/releases.html
>
>   ```shell
>   wget https://www.apache.org/dyn/closer.lua/zookeeper/zookeeper-3.7.1/apache-zookeeper3.7.1-bin.tar.gz
>   # 解压
>   tar -xvf apache-zookeeper-3.7.1-bin.tar.gz
>   
>   cd apache-zookeeper-3.7.1-bin
>   
>   # 设置zoo.cfg 文件
>   cp zoo_sample.cfg zoo.cfg
>   
>   zkServer.sh start # 启动
>   zkServer.sh start-foreground # 前端启动
>   zkServer.sh status # 查看状态
>   zkServer.sh stop # 停止
>   
>   ```

> Docker 环境安装
>
> - 打开https://hub.docker.com
>
> - 搜索zookeeper镜像，版本选择：3.7.1
>
> - 拉取镜像
>
>   > docker pull zookeeper:3.7.1
>
> - 运行zookeeper container
>
>   > docker run -d --name jack-zk-server -p 2281:2181 zookeeper:3.7.1
>
> - 查看启动结果
>
>   >docker ps 2 
>   >
>   >lsof -i:2281

## 操作

```shell
# 查看帮助
help

# 创建节点
create /test
create /test/like
create /test/james

# 创建节点并往节点上存数据
create /test/like/age 17
create /test/like/hobby coding

# 获取ZNode
ls /
ls /test
ls -R /test

## 查询某个 查询某个ZNode ZNode数据数据
get /test/like
get /test/like/age
get /test/like/hobby

# 修改ZNode的值
set /test/like/age 16
get /test/like/age

# 删除节点，当某个节点下存在子节点时，不能直接删除该节点
delete delete /test/like/age
ls -R /
deleteall deleteall /test
ls -R /


```

## 节点状态

https://zookeeper.apache.org/doc/current/zookeeperProgrammers.html#sc_zkStatStructure



# Watch

> 1、zkClient向zkServer注册watch
>
> 2、ZNode 数据或状态发生改变
>
> 3、出发客户端watch回调事件

```shell
# watch 命令
help
config [-c] [-w] [-s]
get [-s] [-w] path
ls [-s] [-w] [-R] path
stat [-w] path

# get stat 监听节点数据的变化 ls (-R) 针对(子)节点的变化
```

### 监听节点数据变化

```shell

# 创建节点并添加监听
create /zk-watch 111
get -w /zk-watch
# 来到另外一个客户端(其实用当前客户端也行,只是为了便于理解)
-------------------------------------------------------------------------------
set /zk-watch 222
# 观察第一个zkClient的变化，发现收到通知
WATCHER::
WatchedEvent state:SyncConnected type:NodeDataChanged path:/zk-watch
# 收到watch通知之后，就可以进行对应的业务逻辑处理。但如果再修改/zk-watch的值，发现就不会收到
watch通知了，因为该命令下的watch通知是一次性的，要想再收到，得继续添加watch监听
get -w /zk-watch
```

### 监听(子)节点的创建和删除

```shell

# 创建子节点并对父节点添加watch
create /zk-watch/sub1
get -w /zk-watch
# 修改/zk-watch/sub1节点的数据值，发现watch并没有生效，因为get只监听单个节点
set /zk-watch/sub1 111
# 通过ls添加对(子)节点的增加和删除监听
ls -w /zk-watch
create /zk-watch/sub2 111
# 继续添加zk-watch节点的子节点，发现并没有收到通知，因为ls也是一次性的
create /zk-watch/sub3 111
```

### 添加永久监听

> addWatch [-m mode] path # optional mode is one of [PERSISTENT, PERSISTENT_RECURSIVE] - default is PERSISTENT_RECURSIVE

```shell
create /zk-watch-update 666
addWatch /zk-watch-update
set /zk-watch-update 999
set /zk-watch-update 888
create /zk-watch-update/sub1
create /zk-watch-update/sub2
delete /zk-watch-update/sub1
set /zk-watch-update/sub2 222
create /zk-watch-update/sub2/sub1 111
delete /zk-watch-update/sub2/sub1
delete /zk-watch-update/sub2
delete /zk-watch-update
```

### ACL  (access control list）权限控制

- **组成**

  - schemes: 表示策略 

    - world

      该scheme只有一个id,为anyone,表示所有人，格式为

      > world:anyone:permission

    - auth

      该scheme表示需要认证登录，也就是对应注册用户需拥有权限才可以访问，格式为 

      > auth:user:password:permission

    - digest

      该scheme表示需要密码加密才能访问，格式为：

      > digest:user:BASE64(password):permission

    - ip

      该scheme表示指定的ip才能访问，格式为：

      > ip:host:permission

    - super

      该scheme表示超管，拥有所有权限

  - id：表示允许访问的用户

  - permission：表示访问的权限

    - CREATE
    - READ
    - WRITE
    - DELETE
    - ADMIN

```shell
# 创建节点并查看权限 'world,'anyone
: cdrwa
create /zk-acl 111
getAcl /zk-acl
# 设置某个用户对某个节点的权限
create /zk-jack 666
setAcl /zk-jack auth:jack:123:cdrwa
# 表示该用户还没有在zk中注册，注册一下
addauth digest jack:123
setAcl /zk-jack auth:jack:123:cdrwa
getAcl /zk-jack
# 这样一来，对于/zk-jack节点的操作，就需要先登录一下，打开另外一个客户端，执行如下命令，提示:
Insufficient permission : /zk-jack
ls /zk-jack
get /zk-jack
# 授权
addauth digest jack:123
get /zk-jack
```

