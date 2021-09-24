| 参数                                                         | 含义                                                         | 说明                                                         |
| ------------------------------------------------------------ | ------------------------------------------------------------ | ------------------------------------------------------------ |
| -XX:CICompilerCount=3                                        | 最大并行编译数                                               | 如果设置大于1，虽然编译速度 会提高，但是同样影响系统稳 定性，会增加JVM崩溃的可能 |
| -XX:InitialHeapSize=100M                                     | 初始化堆大小                                                 | 简写-Xms100M                                                 |
| -XX:MaxHeapSize=100M                                         | 最大堆大小                                                   | 简写-Xms100M                                                 |
| -XX:NewSize=20M                                              | 设置年轻代的大小                                             |                                                              |
| -XX:MaxNewSize=50M                                           | 年轻代最大大小                                               |                                                              |
| -XX:OldSize=50M                                              | 设置老年代大小                                               |                                                              |
| -XX:MetaspaceSize=50M                                        | 设置方法区大小                                               |                                                              |
| -XX:MaxMetaspaceSize=50M                                     | 方法区最大大小                                               |                                                              |
| -XX:+UseParallelGC                                           | 使用UseParallelGC                                            | 新生代，吞吐量优先                                           |
| -XX:+UseParallelOldGC                                        | 使用UseParallelOldGC                                         | 老年代，吞吐量优先                                           |
| -XX:+UseConcMarkSweepGC                                      | 使用CMS                                                      | 老年代，停顿时间优先                                         |
| -XX:+UseG1GC                                                 | 使用G1GC                                                     | 新生代，老年代，停顿时间优 先                                |
| -XX:NewRatio                                                 | 新老生代的比值                                               | 比如-XX:Ratio=4，则表示新生 代:老年代=1:4，也就是新生代 占整个堆内存的1/5 |
| -XX:SurvivorRatio                                            | 两个S区和Eden区的比值                                        | 比如-XX:SurvivorRatio=8，也 就是(S0+S1):Eden=2:8，也就 是一个S占整个新生代的1/10 |
| -XX:+HeapDumpOnOutOfMemoryError                              | 启动堆内存溢出打印                                           | 当JVM堆内存发生溢出时，也 就是OOM，自动生成dump文 件         |
| -XX:HeapDumpPath=heap.hpro                                   | 指定堆内存溢出打印目录                                       | 表示在当前目录生成一个 heap.hprof文件                        |
| -XX:+PrintGCDetails - XX:+PrintGCTimeStamps - XX:+PrintGCDateStamps -Xloggc:g1- gc.log | 打印出GC日志                                                 | 可以使用不同的垃圾收集器， 对比查看GC情况                    |
| -Xss128k                                                     | 设置每个线程的堆栈大小                                       | 经验值是3000-5000最佳                                        |
| -XX:MaxTenuringThreshold=6                                   | 提升年老代的最大临界值                                       | 默认值为 15                                                  |
| -XX:InitiatingHeapOccupancyPercent                           | 启动并发GC周期时堆内存使用占比                               | G1之类的垃圾收集器用它来触 发并发GC周期,基于整个堆的使 用率,而不只是某一代内存的使 用比. 值为 0 则表示”一直执行 GC循环”. 默认值为 45. |
| -XX:G1HeapWastePercent                                       | 允许的浪费堆空间的占比                                       | 默认是10%，如果并发标记可 回收的空间小于10%,则不会触 发MixedGC。 |
| -XX:MaxGCPauseMillis=200ms                                   | G1最大停顿时间                                               | 暂停时间不能太小，太小的话 就会导致出现G1跟不上垃圾产 生的速度。最终退化成Full GC。所以对这个参数的调优是 一个持续的过程，逐步调整到 最佳状态 |
| -XX:ConcGCThreads=n                                          | 并发垃圾收集器使用的线程数量                                 | 默认值随JVM运行的平台不同 而不同                             |
| -XX:G1MixedGCLiveThresholdPercent=65                         | 混合垃圾回收周期中要包括的旧区 域设置占用率阈值              | 默认占用率为 65%                                             |
| -XX:G1MixedGCCountTarget=8                                   | 设置标记周期完成后，对存活数据 上限为 G1MixedGCLIveThresholdPercent 的旧区域执行混合垃圾回收的目标 次数 | 默认8次混合垃圾回收，混合回 收的目标是要控制在此目标次 数以内 |
| XX:G1OldCSetRegionThresholdPercent=1                         | 描述Mixed GC时，Old Region被加 入到CSet中                    | 默认情况下，G1只把10%的 Old Region加入到CSet中               |
| -XX:GCTimeRatio                                              | 直接设置吞吐量的大小。                                       |                                                              |
|                                                              |                                                              |                                                              |
|                                                              |                                                              |                                                              |

## 常用命令

###  jps 

 查看java进程 

###  jinfo 

 （1）实时查看和调整JVM配置参数 

 （2）查看用法 

 jinfo -flag name PID 查看某个java进程的name属性的值 

```sh
jinfo -flag MaxHeapSize PID
jinfo -flag UseG1GC PID
```

 （3）修改 

 参数只有被标记为manageable的flags可以被实时修改 

```sh
jinfo -flag [+|-] PID
jinfo -flag <name>=<value> PID
```

 （4）查看曾经赋过值的一些参数  

```sh
jinfo -flags PID
```

###  jstat  

 （1） 查看虚拟机性能统计信息 

 （2）查看类装载信息 

```sh
jstat -class PID 1000 10 查看某个java进程的类装载信息，每1000毫秒输出一次，共输出10次
```

 （3）查看垃圾收集信息 

```sh
jstat -gc PID 1000 10
```

###  jstack 

 （1）查看线程堆栈信息 

 （2）用法 

```sh
jstack PID
```

###  jmap 

 （1）生成堆转储快照  

 （2）打印出堆内存相关信息 

```sh
jmap -heap PID

jinfo -flag UsePSAdaptiveSurvivorSizePolicy 35352
-XX:SurvivorRatio=8
```

 （3）dump出堆内存相关信息 

```sh
jmap -dump:format=b,file=heap.hprof PID
```

 （4）要是在发生堆内存溢出的时候，能自动dump出该文件就好了 

 一般在开发中，JVM参数可以加上下面两句，这样内存溢出时，会自动dump出该文件 -`XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=heap.hprof `

 （5）关于dump下来的文件 





 吞吐量=运行用户代码的时间/(运行用户代码的时间+垃圾收集时间) 

 比如虚拟机总共运行了100分钟，垃圾收集时间用了1分钟，吞吐量=(100-1)/100=99%。 

##  CMS  

```txt
(1)初始标记 CMS initial mark 标记GC Roots直接关联对象，不用Tracing，速度很快
(2)并发标记 CMS concurrent mark 进行GC Roots Tracing
(3)重新标记 CMS remark 修改并发标记因用户程序变动的内容
(4)并发清除 CMS concurrent sweep 清除不可达对象回收空间，同时有新垃圾产生，留着下次清理称为
浮动垃圾

优点：并发收集、低停顿
缺点：产生大量空间碎片、并发阶段会降低吞吐量，还会并发失败

backgroud模式为正常模式执行上述的CMS GC流程
forefroud模式为Full GC模式
```

```sh
相关参数：
//开启CMS垃圾收集器
-XX:+UseConcMarkSweepGC
//默认开启，与-XX:CMSFullGCsBeforeCompaction配合使用
-XX:+UseCMSCompactAtFullCollection
//默认0 几次Full GC后开始整理
-XX:CMSFullGCsBeforeCompaction=0
//辅助CMSInitiatingOccupancyFraction的参数，不然CMSInitiatingOccupancyFraction只会使
用一次就恢复自动调整，也就是开启手动调整。
-XX:+UseCMSInitiatingOccupancyOnly
//取值0-100，按百分比回收
-XX:CMSInitiatingOccupancyFraction 默认-1

注意：CMS并发GC不是“full GC”。HotSpot VM里对concurrent collection和full collection有
明确的区分。所有带有“FullCollection”字样的VM参数都是跟真正的full GC相关，而跟CMS并发GC无关
的
```

##  G1(Garbage-First )

 使用G1收集器时，Java堆的内存布局与就与其他收集器有很大差别，它将整个Java堆划分为多个 大小相等的独立区域（Region），虽然还保留有新生代和老年代的概念，但新生代和老年代不再 是物理隔离的了，它们都是一部分Region（不需要连续）的集合。 每个Region大小都是一样的，可以是1M到32M之间的数值，但是必须保证是2的n次幂 如果对象太大，一个Region放不下[超过Region大小的50%]，那么就会直接放到H中 设置Region大小：-XX:G1HeapRegionSize=M 所谓Garbage-Frist，其实就是优先回收垃圾最多的Region区域 

####  工作过程可以分为如下几步 

```
初始标记（Initial Marking） 标记以下GC Roots能够关联的对象，并且修改TAMS的值，需要暂
停用户线程
并发标记（Concurrent Marking） 从GC Roots进行可达性分析，找出存活的对象，与用户线程并发
执行
最终标记（Final Marking） 修正在并发标记阶段因为用户程序的并发执行导致变动的数据，需
暂停用户线程
筛选回收（Live Data Counting and Evacuation） 对各个Region的回收价值和成本进行排序，根据
用户所期望的GC停顿时间制定回收计划

```

#####  相关参数 

```sh
-XX: +UseG1GC 开启G1垃圾收集器
-XX: G1HeapReginSize 设置每个Region的大小，是2的幂次，1MB-32MB之间
-XX:MaxGCPauseMillis 最大停顿时间
-XX:ParallelGCThread 并行GC工作的线程数
-XX:ConcGCThreads 并发标记的线程数
-XX:InitiatingHeapOcccupancyPercent 默认45%，代表GC堆占用达到多少的时候开始垃圾收集
```

# 实践

##  CPU占用率高 

（1）top 

（2）top -Hp PID 查看进程中占用CPU高的线程id，即tid 

（3）jstack PID | grep tid  

![1631097723282](img\1631097723282.png)

###  常见问题 

 （1）内存泄漏与内存溢出的区别 

```txt
内存泄漏是指不再使用的对象无法得到及时的回收，持续占用内存空间，从而造成内存空间的浪费。
内存泄漏很容易导致内存溢出，但内存溢出不一定是内存泄漏导致的。
```

 （2）young gc会有stw吗？ 

```txt
不管什么 GC，都会发送 stop-the-world，区别是发生的时间长短。而这个时间跟垃圾收集器又有关
系，Serial、PartNew、Parallel Scavenge 收集器无论是串行还是并行，都会挂起用户线程，而 CMS
和 G1 在并发标记时，是不会挂起用户线程的，但其它时候一样会挂起用户线程，stop the world 的时
间相对来说就小很多了。
```

 （3）major gc和full gc的区别 

```txt
Major GC在很多参考资料中是等价于 Full GC 的，我们也可以发现很多性能监测工具中只有 Minor GC
和 Full GC。一般情况下，一次 Full GC 将会对年轻代、老年代、元空间以及堆外内存进行垃圾回收。触
发 Full GC 的原因有很多：当年轻代晋升到老年代的对象大小，并比目前老年代剩余的空间大小还要大
时，会触发 Full GC；当老年代的空间使用率超过某阈值时，会触发 Full GC；当元空间不足时（JDK1.7
永久代不足），也会触发 Full GC；当调用 System.gc() 也会安排一次 Full GC。
```

 （4）什么是直接内存  

```txt
Java的NIO库允许Java程序使用直接内存。直接内存是在java堆外的、直接向系统申请的内存空间。通
常访问直接内存的速度会优于Java堆。因此出于性能的考虑，读写频繁的场合可能会考虑使用直接内
存。由于直接内存在java堆外，因此它的大小不会直接受限于Xmx指定的最大堆大小，但是系统内存是
有限的，Java堆和直接内存的总和依然受限于操作系统能给出的最大内存。

```

 （5）垃圾判断的方式  

```txt
引用计数法：指的是如果某个地方引用了这个对象就+1，如果失效了就-1，当为0就会回收但是JVM没
有用这种方式，因为无法判定相互循环引用（A引用B,B引用A）的情况。
引用链法： 通过一种GC ROOT的对象（方法区中静态变量引用的对象等-static变量）来判断，如果有
一条链能够到达GC ROOT就说明，不能到达GC ROOT就说明可以回收。
```

 （6）不可达的对象一定要被回收吗？ 

```txt
即使在可达性分析法中不可达的对象，也并非是“非死不可”的，这时候它们暂时处于“缓刑阶段”，要真
正宣告一个对象死亡，至少要经历两次标记过程；可达性分析法中不可达的对象被第一次标记并且进行
一次筛选，筛选的条件是此对象是否有必要执行 finalize 方法。当对象没有覆盖 finalize 方法，或
finalize 方法已经被虚拟机调用过时，虚拟机将这两种情况视为没有必要执行。
被判定为需要执行的对象将会被放在一个队列中进行第二次标记，除非这个对象与引用链上的任何一个
对象建立关联，否则就会被真的回收。
```

 （7）为什么要区分新生代和老年代？  

```txt
 当前虚拟机的垃圾收集都采用分代收集算法，这种算法没有什么新的思想，只是根据对象存活周期的不 同将内存分为几块。一般将 java 堆分为新生代和老年代，这样我们就可以根据各个年代的特点选择合 适的垃圾收集算法。 比如在新生代中，每次收集都会有大量对象死去，所以可以选择复制算法，只需要付出少量对象的复制 成本就可以完成每次垃圾收集。而老年代的对象存活几率是比较高的，而且没有额外的空间对它进行分 配担保，所以我们必须选择“标记-清除”或“标记-整理”算法进行垃圾收集。
```

 （8）G1与CMS的区别是什么 

  ```txt
CMS 主要集中在老年代的回收，而 G1 集中在分代回收，包括了年轻代的 Young GC 以及老年代的 Mix
GC；G1 使用了 Region 方式对堆内存进行了划分，且基于标记整理算法实现，整体减少了垃圾碎片的
产生；在初始化标记阶段，搜索可达对象使用到的 Card Table，其实现方式不一样。
  ```

 （9）方法区中的无用类回收 

```txt
方法区主要回收的是无用的类，那么如何判断一个类是无用的类的呢？
判定一个常量是否是“废弃常量”比较简单，而要判定一个类是否是“无用的类”的条件则相对苛刻许多。
类需要同时满足下面 3 个条件才能算是 “无用的类” :
a-该类所有的实例都已经被回收，也就是 Java 堆中不存在该类的任何实例。
b-加载该类的 ClassLoader 已经被回收。
c-该类对应的 java.lang.Class 对象没有在任何地方被引用，无法在任何地方通过反射访问该类的方法。
```

