##  Redis的安装 

 redis约定次版本号（第一个小数点后的数字）为偶数版本是稳定版，如2.8、3.0， 奇数版本为非稳定版，生产环 境需要使用稳定版；目前最新版本为Redis4.0.9, 我们本次课仍然以3.2作为演示版本  

###  安装配置 

1. 下载redis的安装包 

2. tar -zxvf 解压

3.  cd 到解压后的目录

4. 执行make 完成编译 

   ```txt
   #可能遇到的错误
   1. 需要安装tcl yum install tcl 、 yum install gcc
   2. error: jemalloc/jemalloc.h: No such file or directory
   说关于分配器allocator， 如果有MALLOC 这个 环境变量， 会有用这个环境变量的 去建立Redis。
   而且libc 并不是默认的 分配器， 默认的是 jemalloc, 因为 jemalloc 被证明 有更少的 fragmentation
   problems 比libc。
   但是如果你又没有jemalloc 而只有 libc 当然 make 出错。 所以加这么一个参数。
   解决办法
   make MALLOC=libc
   ```

5. make test 测试编译状态

6. make install {PREFIX=/path} 

### 启动停止redis  

 安装完redis后的下一步就是怎么去启动和访问，我们首先先了解一下Redis包含哪些可执行文件 

| Redis-server     | Redis服务器               |
| ---------------- | ------------------------- |
| Redis-cli        | Redis命令行客户端         |
| Redis-benchmark  | Redis性能测试工具         |
| Redis-check-aof  | Aof文件修复工具           |
| Redis-check-dump | Rdb文件检查工具           |
| Redis-sentinel   | Sentinel服务器（2.8以后） |

 我们常用的命令是redis-server和redis-cli 

#### 直接启动 

 redis-server ../redis.conf 

 服务器启动后默认使用的是6379的端口 ，通过--port可以自定义端口 ； 6379在手机键盘上MERZ对应，MERZ是 一名意大利歌女的名字  

 Redis-server --port 6380 

 以守护进程的方式启动，需要修改redis.conf配置文件中daemonize yes  

#### 停止redis 

 redis-cli SHUTDOWN 

考虑到redis有可能正在将内存的数据同步到硬盘中，强行终止redis进程可能会导致数据丢失，正确停止redis的方 式应该是向Redis发送SHUTDOW命令 

当redis收到SHUTDOWN命令后，会先断开所有客户端连接，然后根据配置执行持久化，最终完成退出 

#### 数据类型 

#####  字符串类型 String

 字符串类型是redis中最基本的数据类型，它能存储任何形式的字符串，包括二进制数据。你可以用它存储用户的 邮箱、json化的对象甚至是图片。一个字符类型键允许存储的最大容量是512M  

#####  内部数据结构  

 在Redis内部，String类型通过 int、SDS(simple dynamic string)作为结构存储，int用来存放整型数据，sds存放字 节/字符串和浮点型数据。在C的标准字符串结构下进行了封装，用来提升基本操作的性能，同时也充分利用已有的 C的标准库，简化实现逻辑。我们可以在redis的源码中【sds.h】中看到sds的结构如下； 

 typedef char *sds; 

 redis3.2分支引入了五种sdshdr类型，目的是为了满足不同长度字符串可以使用不同大小的Header，从而节省内 存，每次在创建一个sds时根据sds的实际长度判断应该选择什么类型的sdshdr，不同类型的sdshdr占用的内存空 间不同。这样细分一下可以省去很多不必要的内存开销，下面是3.2的sdshdr定义 

```sh
`struct __attribute__ ((__packed__)) sdshdr8 { 8表示字符串最大长度是2^8-1 （长度为255）``
uint8_t len;//表示当前sds的长度(单位是字节)`` uint8_t alloc; //表示已为sds分配的内存大小(单
位是字节)`` unsigned char flags; //用一个字节表示当前sdshdr的类型，因为有sdshdr有五种类型，所
以至少需要3位来表示000:sdshdr5，001:sdshdr8，010:sdshdr16，011:sdshdr32，100:sdshdr64。高5位
用不到所以都为0。`` char buf[];//sds实际存放的位置``};`
```

 sdshdr8的内存布局 

![1630741407364](img\1630741407364.png)

#####  列表类型 list

 列表类型(list)可以存储一个有序的字符串列表，常用的操作是向列表两端添加元素或者获得列表的某一个片段。 

 列表类型内部使用双向链表实现，所以向列表两端添加元素的时间复杂度为O(1), 获取越接近两端的元素速度就越 快。这意味着即使是一个有几千万个元素的列表，获取头部或尾部的10条记录也是很快的 

![1630757295402](img\1630757295402.png)

######  内部数据结构 

 redis3.2之前，List类型的value对象内部以linkedlist或者ziplist来实现, 当list的元素个数和单个元素的长度比较小 的时候，Redis会采用ziplist（压缩列表）来实现来减少内存占用。否则就会采用linkedlist（双向链表）结构。 

 redis3.2之后，采用的一种叫quicklist的数据结构来存储list，列表的底层都由quicklist实现。 

 这两种存储方式都有优缺点，双向链表在链表两端进行push和pop操作，在插入节点上复杂度比较低，但是内存开 销比较大； ziplist存储在一段连续的内存上，所以存储效率很高，但是插入和删除都需要频繁申请和释放内存； 

 quicklist仍然是一个双向链表，只是列表的每个节点都是一个ziplist，其实就是linkedlist和ziplist的结合，quicklist 中每个节点ziplist都能够存储多个数据元素，在源码中的文件为【quicklist.c】，在源码第一行中有解释为：A doubly linked list of ziplists意思为一个由ziplist组成的双向链表 

![1630757602496](img\1630757602496.png)

#####  hash类型 

![1630757920823](img\1630757920823.png)

######  数据结构 

 map提供两种结构来存储，一种是hashtable、另一种是前面讲的ziplist，数据量小的时候用ziplist. 在redis中，哈 希表分为三层，分别是，源码地址【dict.h】  

 **dictEntry **

 管理一个key-value，同时保留同一个桶中相邻元素的指针，用来维护哈希桶的内部链； 

```c
typedef struct dictEntry {
    void *key;
    union { //因为value有多种类型，所以value用了union来存储
    void *val;
    uint64_t u64;
    int64_t s64;
    double d;
    } v;
    struct dictEntry *next;//下一个节点的地址，用来处理碰撞，所有分配到同一索引的元素通过next指针链接起来形成链表key和v都可以保存多种类型的数据
} dictEntry;

```

**dictht **

 实现一个hash表会使用一个buckets存放dictEntry的地址，一般情况下通过hash(key)%len得到的值就是buckets的 索引，这个值决定了我们要将此dictEntry节点放入buckets的哪个索引里,这个buckets实际上就是我们说的hash 表。dict.h的dictht结构中table存放的就是buckets的地址  

```C
typedef struct dictht {
    dictEntry **table;//buckets的地址
    unsigned long size;//buckets的大小,总保持为 2^n
    unsigned long sizemask;//掩码，用来计算hash值对应的buckets索引
    unsigned long used;//当前dictht有多少个dictEntry节点
} dictht;
```

**dict **

 dictht实际上就是hash表的核心，但是只有一个dictht还不够，比如rehash、遍历hash等操作，所以redis定义了 一个叫dict的结构以支持字典的各种操作，当dictht需要扩容/缩容时，用来管理dictht的迁移，以下是它的数据结 构,源码在 

```C
typedef struct dict {
    dictType *type;//dictType里存放的是一堆工具函数的函数指针，
    void *privdata;//保存type中的某些函数需要作为参数的数据
    dictht ht[2];//两个dictht，ht[0]平时用，ht[1] rehash时用
    long rehashidx; //当前rehash到buckets的哪个索引，-1时表示非rehash状态
    int iterators; //安全迭代器的计数。
} dict;

```

 比如我们要讲一个数据存储到hash表中，那么会先通过murmur计算key对应的hashcode，然后根据hashcode取 模得到bucket的位置，再插入到链表中 

#####  集合类型 

 集合类型中，每个元素都是不同的，也就是不能有重复数据，同时集合类型中的数据是无序的。一个集合类型键可 以存储至多232-1个 。集合类型和列表类型的最大的区别是有序性和唯一性  

 集合类型的常用操作是向集合中加入或删除元素、判断某个元素是否存在。由于集合类型在redis内部是使用的值 为空的散列表(hash table)，所以这些操作的时间复杂度都是O(1) 

![1630759687721](img\1630759687721.png)

######  数据结构 

 Set在的底层数据结构以intset或者hashtable来存储。当set中只包含整数型的元素时，采用intset来存储，否则， 采用hashtable存储，但是对于set来说，该hashtable的value值用于为NULL。通过key来存储元素 

#####  有序集合 

![1630760116075](img\1630760116075.png)

 有序集合类型，顾名思义，和前面讲的集合类型的区别就是多了有序的功能  

 在集合类型的基础上，有序集合类型为集合中的每个元素都关联了一个分数，这使得我们不仅可以完成插入、删除 和判断元素是否存在等集合类型支持的操作，还能获得分数最高(或最低)的前N个元素、获得指定分数范围内的元 素等与分数有关的操作。虽然集合中每个元素都是不同的，但是他们的分数却可以相同 

######  数据结构 

 zset类型的数据结构就比较复杂一点，内部是以ziplist或者skiplist+hashtable来实现，这里面最核心的一个结构就 是skiplist，也就是跳跃表 ![1630760205690](img\1630760205690.png)

##  Redis的原理分析

###   过期时间设置 

 在Redis中提供了Expire命令设置一个键的过期时间，到期以后Redis会自动删除它。这个在我们实际使用过程中用 得非常多。  

 EXPIRE命令的使用方法为 

 `EXPIRE key seconds` 

 其中seconds 参数表示键的过期时间，单位为秒。 

 EXPIRE 返回值为1表示设置成功，0表示设置失败或者键不存在 

 如果向知道一个键还有多久时间被删除，可以使用TTL命令  

 `TTL key`

 当键不存在时，TTL命令会返回-2  

 而对于没有给指定键设置过期时间的，通过TTL命令会返回-1 

 如果向取消键的过期时间设置（使该键恢复成为永久的），可以使用PERSIST命令，如果该命令执行成功或者成功 清除了过期时间，则返回1 。 否则返回0（键不存在或者本身就是永久的）  

 EXPIRE命令的seconds命令必须是整数，所以最小单位是1秒，如果向要更精确的控制键的过期时间可以使用 PEXPIRE命令，当然实际过程中用秒的单位就够了。 PEXPIRE命令的单位是毫秒。即PEXPIRE key 1000与EXPIRE key 1相等；对应的PTTL以毫秒单位获取键的剩余有效时间  

 还有一个针对字符串独有的过期时间设置方式 

` setex(String key,int seconds,String value)  `

###  过期删除的原理

 Redis 中的主键失效是如何实现的，即失效的主键是如何删除的？实际上，Redis 删除失效主键的方法主要有两 种： 

####  消极方法（passive way） 

 在主键被访问时如果发现它已经失效，那么就删除它  

####  积极方法（active way） 

 周期性地从设置了失效时间的主键中选择一部分失效的主键删除 

 对于那些从未被查询的key，即便它们已经过期，被动方式也无法清除。因此Redis会周期性地随机测试一些key， 已过期的key将会被删掉。Redis每秒会进行10次操作，具体的流程：  

 对于那些从未被查询的key，即便它们已经过期，被动方式也无法清除。因此Redis会周期性地随机测试一些key， 已过期的key将会被删掉。Redis每秒会进行10次操作，具体的流程：  

1. 随机测试 20 个带有timeout信息的key； 
2.  删除其中已经过期的key；  
3.  如果超过25%的key被删除，则重复执行步骤1； 

 这是一个简单的概率算法（trivial probabilistic algorithm），基于假设我们随机抽取的key代表了全部的key空 间。  

###  Redis发布订阅 

 Redis提供了发布订阅功能，可以用于消息的传输，Redis提供了一组命令可以让开发者实现“发布/订阅”模式 (publish/subscribe) . 该模式同样可以实现进程间的消息传递，它的实现原理是  

 发布/订阅模式包含两种角色，分别是发布者和订阅者。订阅者可以订阅一个或多个频道，而发布者可以向指定的 频道发送消息，所有订阅此频道的订阅者都会收到该消息 

 发布者发布消息的命令是PUBLISH， 用法是  

 `PUBLISH channel message  `

 比如向channel.1发一条消息:hello 

 `PUBLISH channel.1 “hello” `

 这样就实现了消息的发送，该命令的返回值表示接收到这条消息的订阅者数量。因为在执行这条命令的时候还没有 订阅者订阅该频道，所以返回为0. 另外值得注意的是消息发送出去不会持久化，如果发送之前没有订阅者，那么后 续再有订阅者订阅该频道，之前的消息就收不到了 

 订阅者订阅消息的命令是  

 SUBSCRIBE channel [channel …]  

 该命令同时可以订阅多个频道，比如订阅channel.1的频道。 SUBSCRIBE channel.1 执行SUBSCRIBE命令后客户端会进入订阅状态 

####  结构图 

 channel分两类，一个是普通channel、另一个是pattern channel（规则匹配）， producer1发布了一条消息 【publish abc hello】,redis server发给abc这个普通channel上的所有订阅者，同时abc也匹配上了pattern channel的名字，所以这条消息也会同时发送给pattern channel *bc上的所有订阅 

![1630770268286](img\1630770268286.png)

###  Redis的数据是如何持久化的？ 

 Redis支持两种方式的持久化，一种是RDB方式、另一种是AOF（append-only-file）方式。前者会根据指定的规 则“定时”将内存中的数据存储在硬盘上，而后者在每次执行命令后将命令本身记录下来。两种持久化方式可以单独 使用其中一种，也可以将这两种方式结合使用  

####  RDB方式 

 当符合一定条件时，Redis会单独创建（fork）一个子进程来进行持久化，会先将数据写入到一个临时文件中，等 到持久化过程都结束了，再用这个临时文件替换上次持久化好的文件。整个过程中，主进程是不进行任何IO操作 的，这就确保了极高的性能。如果需要进行大规模数据的恢复，且对于数据恢复的完整性不是非常敏感，那RDB方 式要比AOF方式更加的高效。RDB的缺点是最后一次持久化后的数据可能丢失 

 --fork的作用是复制一个与当前进程一样的进程。新进程的所有数据（变量、环境变量、程序计数器等）数值都和 原进程一致，但是是一个全新的进程，并作为原进程的子进程

 Redis会在以下几种情况下对数据进行快照  

1. 根据配置规则进行自动快照 
2.  用户执行SAVE或者GBSAVE命令 
3.  执行FLUSHALL命令 
4.  执行复制(replication)时 

#####  根据配置规则进行自动快照  

 Redis允许用户自定义快照条件，当符合快照条件时，Redis会自动执行快照操作。快照的条件可以由用户在配置文 件中配置。配置格式如下 

 `save  `

 第一个参数是时间窗口，第二个是键的个数，也就是说，在第一个时间参数配置范围内被更改的键的个数大于后面 的changes时，即符合快照条件。redis默认配置了三个规则 

` save 900 1 `

 `save 300 10 `

` save 60 10000` 

 每条快照规则占一行，每条规则之间是“或”的关系。 在900秒（15分）内有一个以上的键被更改则进行快照。 

#####  用户执行SAVE或BGSAVE命令 

 除了让Redis自动进行快照以外，当我们对服务进行重启或者服务器迁移我们需要人工去干预备份。redis提供了两 条命令来完成这个任务 

1. ###### save命令 

 当执行save命令时，Redis同步做快照操作，在快照执行过程中会阻塞所有来自客户端的请求。当redis内存中的数 据较多时，通过该命令将导致Redis较长时间的不响应。所以不建议在生产环境上使用这个命令，而是推荐使用 bgsave命令 

2. ###### bgsave命令  

 bgsave命令可以在后台异步地进行快照操作，快照的同时服务器还可以继续响应来自客户端的请求。执行BGSAVE 后，Redis会立即返回ok表示开始执行快照操作。 通过LASTSAVE命令可以获取最近一次成功执行快照的时间； （自动快照采用的是异步快照操作） 

#####  执行FLUSHALL命令 

 该命令会清除redis在内存中的所有数据。执行该命令后，只要redis中配置的快照规则不为空，也就 是save 的规则存在。redis就会执行一次快照操作。不管规则是什么样的都会执行。如果没有定义快照规则，就不 会执行快照操作 

#####  执行复制时 

 该操作主要是在主从模式下，redis会在复制初始化时进行自动快照。这个会在后面讲到； 这里只需要了解当执行复制操作时，及时没有定义自动快照规则，并且没有手动执行过快照操作，它仍然会生成 RDB快照文件 

####  AOF方式  

 当使用Redis存储非临时数据时，一般需要打开AOF持久化来降低进程终止导致的数据丢失。AOF可以将Redis执行 的每一条写命令追加到硬盘文件中，这一过程会降低Redis的性能，但大部分情况下这个影响是能够接受的，另外 使用较快的硬盘可以提高AOF的性能 

#####  开启AOF  

 默认情况下Redis没有开启AOF（append only file）方式的持久化，可以通过appendonly参数启用，在redis.conf 中找到 appendonly yes 

 开启AOF持久化后每执行一条会更改Redis中的数据的命令后，Redis就会将该命令写入硬盘中的AOF文件。AOF文 件的保存位置和RDB文件的位置相同，都是通过dir参数设置的，默认的文件名是apendonly.aof. 可以在redis.conf 中的属性 appendfilename appendonlyh.aof修改 

#####  AOF的实现 

 AOF文件以纯文本的形式记录Redis执行的写命令例如开启AOF持久化的情况下执行如下4条命令 

 set foo 1 

 set foo 2 

 set foo 3 

 get 

```txt
我们会发现AOF文件的内容正是Redis发送的原始通信协议的内容，从内容中我们发现Redis只记录了3
条命令。然后这时有一个问题是前面2条命令其实是冗余的，因为这两条的执行结果都会被第三条命令覆
盖。随着执行的命令越来越多，AOF文件的大小也会越来越大，其实内存中实际的数据可能没有多少，
那这样就会造成磁盘空间以及redis数据还原的过程比较长的问题。因此我们希望Redis可以自动优化
AOF文件，就上面这个例子来说，前面两条是可以被删除的。 而实际上Redis也考虑到了，可以配置一
个条件，每当达到一定条件时Redis就会自动重写AOF文件，这个条件的配置问 auto-aof-rewritepercentage 100 auto-aof-rewrite-min-size 64mb
```

` auto-aof-rewrite-percentage` 表示的是当目前的AOF文件大小超过上一次重写时的AOF文件大小的百分之多少时会 再次进行重写，如果之前没有重写过，则以启动时AOF文件大小为依据 

 `auto-aof-rewrite-min-size` 表示限制了允许重写的最小AOF文件大小，通常在AOF文件很小的情况下即使其中有很 多冗余的命令我们也并不太关心。  

 另外，还可以通过BGREWRITEAOF 命令手动执行AOF，执行完以后冗余的命令已经被删除了  

 在启动时，Redis会逐个执行AOF文件中的命令来将硬盘中的数据载入到内存中，载入的速度相对于RDB会慢一些 

#####  AOF的重写原理 

 Redis 可以在 AOF 文件体积变得过大时，自动地在后台对 AOF 进行重写： 重写后的新 AOF 文件包含了恢复当前 数据集所需的最小命令集合。 

 重写的流程是这样，主进程会fork一个子进程出来进行AOF重写，这个重写过程并不是基于原有的aof文件来做 的，而是有点类似于快照的方式，全量遍历内存中的数据，然后逐个序列到aof文件中。在fork子进程这个过程 中，服务端仍然可以对外提供服务，那这个时候重写的aof文件的数据和redis内存数据不一致了怎么办？不用担 心，这个过程中，主进程的数据更新操作，会缓存到aof_rewrite_buf中，也就是单独开辟一块缓存来存储重写期间 收到的命令，当子进程重写完以后再把缓存中的数据追加到新的aof文件。 

 当所有的数据全部追加到新的aof文件中后，把新的aof文件重命名为，此后所有的操作都会被写入新的aof文件。  

 如果在rewrite过程中出现故障，不会影响原来aof文件的正常工作，只有当rewrite完成后才会切换文件。因此这个 rewrite过程是比较可靠的 

####  Redis内存回收策略 

 Redis中提供了多种内存回收策略，当内存容量不足时，为了保证程序的运行，这时就不得不淘汰内存中的一些对 象，释放这些对象占用的空间，那么选择淘汰哪些对象呢？ 

 其中，默认的策略为noeviction策略，当内存使用达到阈值的时候，所有引起申请内存的命令会报错 

 **allkeys-lru**：从数据集（server.db[i].dict）中挑选最近最少使用的数据淘汰 

​	 适合的场景： 如果我们的应用对缓存的访问都是相对热点数据，那么可以选择这个策略 

 **allkeys-random：**随机移除某个key。  

​	 适合的场景：如果我们的应用对于缓存key的访问概率相等，则可以使用这个策略  

 **volatile-random：**从已设置过期时间的数据集（server.db[i].expires）中任意选择数据淘汰 

 **volatile-lru：**从已设置过期时间的数据集（server.db[i].expires）中挑选最近最少使用的数据淘汰。 

 **volatile-ttl：**从已设置过期时间的数据集（server.db[i].expires）中挑选将要过期的数据淘汰  

 	适合场景：这种策略使得我们可以向Redis提示哪些key更适合被淘汰，我们可以自己控制 

###  总结 

 实际上Redis实现的LRU并不是可靠的LRU，也就是名义上我们使用LRU算法淘汰内存数据，但是实际上被淘汰的键 并不一定是真正的最少使用的数据，这里涉及到一个权衡的问题，如果需要在所有的数据中搜索最符合条件的数 据，那么一定会增加系统的开销，Redis是单线程的，所以耗时的操作会谨慎一些。为了在一定成本内实现相对的 LRU，早期的Redis版本是基于采样的LRU，也就是放弃了从所有数据中搜索解改为采样空间搜索最优解。Redis3.0 版本之后，Redis作者对于基于采样的LRU进行了一些优化，目的是在一定的成本内让结果更靠近真实的LRU。  

###  Redis是单进程单线程？性能为什么这么快 

 Redis采用了一种非常简单的做法，单线程来处理来自所有客户端的并发请求，Redis把任务封闭在一个线程中从而 避免了线程安全问题；redis为什么是单线程？ 官方的解释是，CPU并不是Redis的瓶颈所在，Redis的瓶颈主要在机器的内存和网络的带宽。那么Redis能不能处 理高并发请求呢？当然是可以的，至于怎么实现的，我们来具体了解一下。 【注意并发不等于并行，并发性I/O 流，意味着能够让一个计算单元来处理来自多个客户端的流请求。并行性，意味着服务器能够同时执行几个事情， 具有多个计算单元】 

####  多路复用  

 Redis 是跑在单线程中的，所有的操作都是按照顺序线性执行的，但是由于读写操作等待用户输入或输出都是阻塞 的，所以 I/O 操作在一般情况下往往不能直接返回，这会导致某一文件的 I/O 阻塞导致整个进程无法对其它客户提 供服务，而 I/O 多路复用就是为了解决这个问题而出现的。 

 了解多路复用之前，先简单了解下几种I/O模型  

 （1）同步阻塞IO（Blocking IO）：即传统的IO模型。 ---BIO

 （2）同步非阻塞IO（Non-blocking IO）：默认创建的socket都是阻塞的，非阻塞IO要求socket被设置为 NONBLOCK。---NIO  

 （3）IO多路复用（IO Multiplexing）：即经典的Reactor设计模式，也称为异步阻塞IO，Java中的Selector和 Linux中的epoll都是这种模型。 

 （4）异步IO（Asynchronous IO）：即经典的Proactor设计模式，也称为异步非阻塞IO。 ---AIO

 同步和异步，指的是用户线程和内核的交互方式 

 阻塞和非阻塞，指用户线程调用内核IO操作的方式是阻塞还是非阻塞 

 就像在Java中使用多线程做异步处理的概念，通过多线程去执行一个流程，主线程可以不用等待。而阻塞和非阻塞 我们可以理解为假如在同步流程或者异步流程中做IO操作，如果缓冲区数据还没准备好，IO的这个过程会阻塞，这 个在之前讲TCP协议的时候有讲过.  

###  在Redis中使用Lua脚本 

 我们在使用redis的时候，会面临一些问题，比如  

####  原子性问题  

 redis虽然是单一线程的，当时仍然会存在线程安全问题，当然，这个线程安全问题不是来源安于 Redis服务器内部。而是Redis作为数据服务器，是提供给多个客户端使用的。多个客户端的操作就相当于同一个进 程下的多个线程，如果多个客户端之间没有做好数据的同步策略，就会产生数据不一致的问题。举个简单的例子  

 多个客户端的命令之间没有做请求同步，导致实际执行顺序可能会不一致，最终的结果也就无法满足原子性了。 

####  效率问题 

 redis本身的吞吐量是非常高的，因为它首先是基于内存的数据库。在实际使用过程中，有一个非常重要的因素影 响redis的吞吐量，那就是网络。我们在使用redis实现某些特定功能的时候，很可能需要多个命令或者多个数据类 型的交互才能完成，那么这种多次网络请求对性能影响比较大。当然redis也做了一些优化，比如提供了pipeline管 道操作，但是它有一定的局限性，就是执行的多个命令和响应之间是不存在相互依赖关系的。所以我们需要一种机 制能够编写一些具有业务逻辑的命令，减少网络请求 

####  Lua  

 Redis中内嵌了对Lua环境的支持，允许开发者使用Lua语言编写脚本传到Redis中执行，Redis客户端可以使用Lua 脚本，直接在服务端原子的执行多个Redis命令。  

 使用脚本的好处： 

1. 减少网络开销，在Lua脚本中可以把多个命令放在同一个脚本中运行 
2.  原子操作，redis会将整个脚本作为一个整体执行，中间不会被其他命令插入。换句话说，编写脚本的过程中无 需担心会出现竞态条件  
3.  复用性，客户端发送的脚本会永远存储在redis中，这意味着其他客户端可以复用这一脚本来完成同样的逻辑 

Lua是一个高效的轻量级脚本语言(javascript、shell、sql、python、ruby…)，用标准C语言编写并以源代码形式开 放， 其设计目的是为了嵌入应用程序中，从而为应用程序提供灵活的扩展和定制功能; 

#### Redis与Lua 

 先初步的认识一下在redis中如何结合lua来完成一些简单的操作  

#####  在Lua脚本中调用Redis命令 

 在Lua脚本中调用Redis命令，可以使用redis.call函数调用。比如我们调用string类型的命令 

 redis.call(‘set’,’hello’,’world’) 

 local value=redis.call(‘get’,’hello’)  

 redis.call 函数的返回值就是redis命令的执行结果。前面我们介绍过redis的5中类型的数据返回的值的类型也都不 一样。redis.call函数会将这5种类型的返回值转化对应的Lua的数据类型 

#####  从Lua脚本中获得返回值 

 在很多情况下我们都需要脚本可以有返回值，毕竟这个脚本也是一个我们所编写的命令集，我们可以像调用其他 redis内置命令一样调用我们自己写的脚本，所以同样redis会自动将脚本返回值的Lua数据类型转化为Redis的返回 值类型。 在脚本中可以使用return 语句将值返回给redis客户端，通过return语句来执行，如果没有执行return， 默认返回为nil。 

#####  EVAL命令的格式是 

` [EVAL][脚本内容] [key参数的数量][key …] [arg …] `

 可以通过key和arg这两个参数向脚本中传递数据，他们的值可以在脚本中分别使用KEYS和ARGV 这两个类型的全 局变量访问。比如我们通过脚本实现一个set命令，通过在redis客户端中调用，那么执行的语句是：  

 lua脚本的内容为：

` return redis.call(‘set’,KEYS[1],ARGV[1]) `//KEYS和ARGV必须大写 

 `eval "return redis.call('set',KEYS[1],ARGV[1])" 1 lua1 hello `

 注意：EVAL命令是根据 key参数的数量-也就是上面例子中的1来将后面所有参数分别存入脚本中KEYS和ARGV两个 表类型的全局变量。当脚本不需要任何参数时也不能省略这个参数。如果没有参数则为0 

`eval "return redis.call('get','lual')" 0`

#####  EVALSHA命令 

 考虑到我们通过eval执行lua脚本，脚本比较长的情况下，每次调用脚本都需要把整个脚本传给redis，比较占用带 宽。为了解决这个问题，redis提供了EVALSHA命令允许开发者通过脚本内容的SHA1摘要来执行脚本。该命令的用 法和EVAL一样，只不过是将脚本内容替换成脚本内容的SHA1摘要 

1. Redis在执行EVAL命令时会计算脚本的SHA1摘要并记录在脚本缓存中  
2. 执行EVALSHA命令时Redis会根据提供的摘要从脚本缓存中查找对应的脚本内容，如果找到了就执行脚本，否则 返回“NOSCRIPT No matching script,Please use EVAL”  

 通过以下案例来演示EVALSHA命令的效果 

 script load "return redis.call('get','lua1')" 将脚本加入缓存并生成sha1命令 

 evalsha "a5a402e90df3eaeca2ff03d56d99982e05cf6574" 0  

 我们在调用eval命令之前，先执行evalsha命令，如果提示脚本不存在，则再调用eval命令 



##  集群 

 先来简单了解下redis中提供的集群策略, 虽然redis有持久化功能能够保障redis服务器宕机也能恢复并且只有少量 的数据损失，但是由于所有数据在一台服务器上，如果这台服务器出现硬盘故障，那就算是有备份也仍然不可避免 数据丢失的问题。 在实际生产环境中，我们不可能只使用一台redis服务器作为我们的缓存服务器，必须要多台实现集群，避免出现 单点故障； 

###  主从复制  

 复制的作用是把redis的数据库复制多个副本部署在不同的服务器上，如果其中一台服务器出现故障，也能快速迁 移到其他服务器上提供服务。 复制功能可以实现当一台redis服务器的数据更新后，自动将新的数据同步到其他服 务器上 

主从复制就是我们常见的master/slave模式， 主数据库可以进行读写操作，当写操作导致数据发生变化时会自动将 数据同步给从数据库。而一般情况下，从数据库是只读的，并接收主数据库同步过来的数据。 一个主数据库可以有 多个从数据库 

![1630840344862](img\1630840344862.png)





####  配置 

 在redis中配置master/slave是非常容易的，只需要在从数据库的配置文件中加入slaveof 主数据库地址 端口。 而 master 数据库不需要做任何改变 

```txt
准备两台服务器，分别安装redis ， server1 server2
\1. 在server2的redis.conf文件中增加 slaveof server1-ip 6379 、 同时将bindip注释掉，允许所
有ip访问
\2. 启动server2
\3. 访问server2的redis客户端，输入 INFO replication
\4. 通过在master机器上输入命令，比如set foo bar 、 在slave服务器就能看到该值已经同步过来了

```

####  原理 

#####  全量复制 

 Redis全量复制一般发生在Slave初始化阶段，这时Slave需要将Master上的所有数据都复制一份。具体步骤 

![1630842531071](img\1630842531071.png)

 完成上面几个步骤后就完成了slave服务器数据初始化的所有操作，savle服务器此时可以接收来自用户的读请求。 

 master/slave 复制策略是采用乐观复制，也就是说可以容忍在一定时间内master/slave数据的内容是不同的，但是 两者的数据会最终同步。具体来说，redis的主从同步过程本身是异步的，意味着master执行完客户端请求的命令 后会立即返回结果给客户端，然后异步的方式把命令同步给slave。 

 这一特征保证启用master/slave后 master的性能不会受到影响。 

 但是另一方面，如果在这个数据不一致的窗口期间，master/slave因为网络问题断开连接，而这个时候，master 是无法得知某个命令最终同步给了多少个slave数据库。不过redis提供了一个配置项来限制只有数据至少同步给多 少个slave的时候，master才是可写的：  

 `min-slaves-to-write 3` 表示只有当3个或以上的slave连接到master，master才是可写的 

 `min-slaves-max-lag 10 `表示允许slave最长失去连接的时间，如果10秒还没收到slave的响应，则master认为该 slave以断开 

####  增量复制 

 从redis 2.8开始，就支持主从复制的断点续传，如果主从复制过程中，网络连接断掉了，那么可以接着上次复制的 地方，继续复制下去，而不是从头开始复制一份 

 master node会在内存中创建一个backlog，master和slave都会保存一个replica offset还有一个master id，offset 就是保存在backlog中的。如果master和slave网络连接断掉了，slave会让master从上次的replica offset开始继续 复制 

 但是如果没有找到对应的offset，那么就会执行一次全量同步 

####  无硬盘复制 

 前面我们说过，Redis复制的工作原理基于RDB方式的持久化实现的，也就是master在后台保存RDB快照，slave接 收到rdb文件并载入，但是这种方式会存在一些问题 

1. 当master禁用RDB时，如果执行了复制初始化操作，Redis依然会生成RDB快照，当master下次启动时执行该 RDB文件的恢复，但是因为复制发生的时间点不确定，所以恢复的数据可能是任何时间点的。就会造成数据出现问 题 

2. 当硬盘性能比较慢的情况下（网络硬盘），那初始化复制过程会对性能产生影响 

 因此2.8.18以后的版本，Redis引入了无硬盘复制选项，可以不需要通过RDB文件去同步，直接发送数据，通过以 下配置来开启该功能  

` repl-diskless-sync yes  `

 master**在内存中直接创建rdb，然后发送给slave，不会在自己本地落地磁盘了 

###  哨兵机制 

 在前面讲的master/slave模式，在一个典型的一主多从的系统中，slave在整个体系中起到了数据冗余备份和读写 分离的作用。当master遇到异常终端后，需要从slave中选举一个新的master继续对外提供服务，这种机制在前面 提到过N次，比如在zk中通过leader选举、kafka中可以基于zk的节点实现master选举。所以在redis中也需要一种 机制去实现master的决策，redis并没有提供自动master选举功能，而是需要借助一个哨兵来进行监控 

####  什么是哨兵 

 顾名思义，哨兵的作用就是监控Redis系统的运行状况，它的功能包括两个 

1. 监控master和slave是否正常运行 
2.  master出现故障时自动将slave数据库升级为master 

 哨兵是一个独立的进程，使用哨兵后的架构图  

![1630858287392](img\1630858287392.png)

 为了解决master选举问题，又引出了一个单点问题，也就是哨兵的可用性如何解决，在一个一主多从的Redis系统 中，可以使用多个哨兵进行监控任务以保证系统足够稳定。此时哨兵不仅会监控master和slave，同时还会互相监 控；这种方式称为哨兵集群，哨兵集群需要解决故障发现、和master决策的协商机制问题 

![1630858393677](img\1630858393677.png)



  sentinel之间的相互感知 

 sentinel节点之间会因为共同监视同一个master从而产生了关联，一个新加入的sentinel节点需要和其他监视相同 master节点的sentinel相互感知，首先 

1. 需要相互感知的sentinel都向他们共同监视的master节点订阅channel:sentinel:hello 
2.  新加入的sentinel节点向这个channel发布一条消息，包含自己本身的信息，这样订阅了这个channel的sentinel 就可以发现这个新的sentinel  
3.  新加入得sentinel和其他sentinel节点建立长连接 



![1630858547360](img\1630858547360.png)

####  master的故障发现 

 sentinel节点会定期向master节点发送心跳包来判断存活状态，一旦master节点没有正确响应，sentinel会把 master设置为“**主观不可用状态**”，然后它会把“主观不可用”发送给其他所有的sentinel节点去确认，当确认的 sentinel节点数大于>quorum时，则会认为master是“**客观不可用**”，接着就开始进入选举新的master流程；但是 这里又会遇到一个问题，就是sentinel中，本身是一个集群，如果多个节点同时发现master节点达到客观不可用状 态，那谁来决策选择哪个节点作为maste呢？这个时候就需要从sentinel集群中选择一个leader来做决策。而这里 用到了一致性算法Raft算法、它和Paxos算法类似，都是分布式一致性算法。但是它比Paxos算法要更容易理解； Raft和Paxos算法一样，也是基于投票算法，只要保证过半数节点通过提议即可; 























##  Redis Java客户端介绍 

##  已有的客户端支持 

 Redis Java客户端有很多的开源产品比如Redission、Jedis、lettuce  

###  差异 

 **Jedis**是Redis的Java实现的客户端，其API提供了比较全面的Redis命令的支持； 

 **Redisson**实现了分布式和可扩展的Java数据结构，和Jedis相比，功能较为简单，不支持字符串操作，不支持排 序、事务、管道、分区等Redis特性。Redisson主要是促进使用者对Redis的关注分离，从而让使用者能够将精力更 集中地放在处理业务逻辑上 

 **lettuce**是基于Netty构建的一个可伸缩的线程安全的Redis客户端，支持同步、异步、响应式模式。多个线程可以 共享一个连接实例，而不必担心多线程并发问题； 

###  jedis-sentinel原理分析 

####  原理 

 客户端通过连接到哨兵集群，通过发送Protocol.SENTINEL_GET_MASTER_ADDR_BY_NAME 命令，从哨兵机器中 询问master节点的信息，拿到master节点的ip和端口号以后，再到客户端发起连接。连接以后，需要在客户端建 立监听机制，当master重新选举之后，客户端需要重新连接到新的master节点 

####  源码分析 

```java
private HostAndPort initSentinels(Set<String> sentinels, final String masterName) {
    HostAndPort master = null;
    boolean sentinelAvailable = false;
    log.info("Trying to find master from available Sentinels...");
    // 有多个sentinels,遍历这些个sentinels
    for (String sentinel : sentinels) {
        // host:port表示的sentinel地址转化为一个HostAndPort对象。
        final HostAndPort hap = HostAndPort.parseString(sentinel);
        log.fine("Connecting to Sentinel " + hap);
        Jedis jedis = null;
 		try {
            // 连接到sentinel
            jedis = new Jedis(hap.getHost(), hap.getPort());
            // 根据masterName得到master的地址，返回一个list，host= list[0], port =// list[1]
            List<String> masterAddr = jedis.sentinelGetMasterAddrByName(masterName);
            // connected to sentinel...
            sentinelAvailable = true;
            if (masterAddr == null || masterAddr.size() != 2) {
                log.warning("Can not get master addr, master name: " + masterName + ".
                Sentinel: " + hap
                + ".");
                continue;
             }
            // 如果在任何一个sentinel中找到了master，不再遍历sentinels
            master = toHostAndPort(masterAddr);
            log.fine("Found Redis master at " + master);
            break;
		} catch (JedisException e) {
            // resolves #1036, it should handle JedisException there's another chance
            // of raising JedisDataException
            log.warning("Cannot get master address from sentinel running @ " + hap + ".
            Reason: " + e
            + ". Trying next one.");
            } finally {
            if (jedis != null) {
            jedis.close();
            }
		}
	}
// 到这里，如果master为null，则说明有两种情况，一种是所有的sentinels节点都down掉了，一种是master节
点没有被存活的sentinels监控到
if (master == null) {
if (sentinelAvailable) {
// can connect to sentinel, but master name seems to not
// monitored
throw new JedisException("Can connect to sentinel, but " + masterName
+ " seems to be not monitored...");
} else {
throw new JedisConnectionException("All sentinels down, cannot determine where is
"
+ masterName + " master is running...");
}
}
//如果走到这里，说明找到了master的地址
log.info("Redis master running at " + master + ", starting Sentinel listeners...");
//启动对每个sentinels的监听
为每个sentinel都启动了一个监听者MasterListener。MasterListener本身是一个线程，它会去订阅sentinel
上关于master节点地址改变的消息。
for (String sentinel : sentinels) {
final HostAndPort hap = HostAndPort.parseString(sentinel);
MasterListener masterListener = new MasterListener(masterName, hap.getHost(),
hap.getPort());
// whether MasterListener threads are alive or not, process can be stopped
masterListener.setDaemon(true);
masterListeners.add(masterListener);
masterListener.start();
}
return master;
}

```

 从哨兵节点获取master信息的方法  

```java
public List<String> sentinelGetMasterAddrByName(String masterName) {
    client.sentinel(Protocol.SENTINEL_GET_MASTER_ADDR_BY_NAME, masterName);
    final List<Object> reply = client.getObjectMultiBulkReply();
    return BuilderFactory.STRING_LIST.build(reply);
}
```

###  Jedis-cluster原理分析 

####  连接方式 

```java
Set<HostAndPort> hostAndPorts=new HashSet<>();
HostAndPort hostAndPort=new HostAndPort("192.168.11.153",7000);
HostAndPort hostAndPort1=new HostAndPort("192.168.11.153",7001);
HostAndPort hostAndPort2=new HostAndPort("192.168.11.154",7003);
HostAndPort hostAndPort3=new HostAndPort("192.168.11.157",7006);
hostAndPorts.add(hostAndPort);
hostAndPorts.add(hostAndPort1);
hostAndPorts.add(hostAndPort2);
hostAndPorts.add(hostAndPort3);
JedisCluster jedisCluster=new JedisCluster(hostAndPorts,6000);
jedisCluster.set("mic","hello")
```

####  原理分析 

#####  程序启动初始化集群环境 

 1)、读取配置文件中的节点配置，无论是主从，无论多少个，只拿第一个，获取redis连接实例 

 2)、用获取的redis连接实例执行clusterNodes()方法，实际执行redis服务端cluster nodes命令，获取主从配置信 息 

 3)、解析主从配置信息，先把所有节点存放到nodes的map集合中，key为节点的ip:port，value为当前节点的 

 jedisPool 

 4)、解析主节点分配的slots区间段，把slot对应的索引值作为key，第三步中拿到的jedisPool作为value，存储在 slots的map集合中  

 就实现了slot槽索引值与jedisPool的映射，这个jedisPool包含了master的节点信息，所以槽和几点是对应的，与 redis服务端一致 

#####  从集群环境存取值 

 1)、把key作为参数，执行CRC16算法，获取key对应的slot值 

 2)、通过该slot值，去slots的map集合中获取jedisPool实例 

 3)、通过jedisPool实例获取jedis实例，最终完成redis数据存取工作 

###  Redisson客户端的操作方式 

####  redis-cluster连接方式 

```java
Config config=new Config();
config.useClusterServers().setScanInterval(2000).
        addNodeAddress("redis://192.168.11.153:7000",
                        "redis://192.168.11.153:7001",
                        "redis://192.168.11.154:7003","redis://192.168.11.157:7006");
RedissonClient redissonClient= Redisson.create(config);
RBucket<String> rBucket=redissonClient.getBucket("mic");
System.out.println(rBucket.get());
```

####  常规操作命令

```shell
getBucket-> 获取字符串对象；
getMap -> 获取map对象
getSortedSet->获取有序集合
getSet -> 获取集合
getList ->获取列表
```



##  redis实战 

###  分布式锁的实现 

 关于锁，其实我们或多或少都有接触过一些，比如synchronized、 Lock这些，这类锁的目的很简单，在多线程环 境下，对共享资源的访问造成的线程安全问题，通过锁的机制来实现资源访问互斥。那么什么是分布式锁呢？或者 为什么我们需要通过Redis来构建分布式锁，其实最根本原因就是Score（范围），因为在分布式架构中，所有的应 用都是进程隔离的，在多进程访问共享资源的时候我们需要满足互斥性，就需要设定一个所有进程都能看得到的范 围，而这个范围就是Redis本身。所以我们才需要把锁构建到Redis中。 Redis里面提供了一些比较具有能够实现锁特性的命令，比如SETEX(在键不存在的情况下为键设置值)，那么我们可 以基于这个命令来去实现一些简单的锁的操作 

###  分布式锁实战 