# JVM体系结构概览

![1629115648319](img\1629115648319.png)

## 类装载器ClassLoader

​	负责加载class文件，class 文件在文件开头有特定的文件标识，并且ClassLoader 只负责Class文件的加载，至于它是否可以运行，则由 执行引擎 Excution Engine决定

![1629115933998](img\1629115933998.png)

## 类装载器ClassLoader2

虚拟机自带的加载器：

​	启动加载器 BootStrap C++

​	扩展类加载器 Extension Java

​	应用程序类加载器 AppClassLoader Java 

用户自定义加载器：

​	Java.lang.ClassLoader的子类，用户可以定制类的加载方式



# JVM 内存区域

## PC寄存器 （程序计数器）

程序计数器是一块较小的内存空间，**是当前线程执行的字节码的行号指示器**

程序计数器属于线程私有的(每条线程都要有一个独立的程序计数器)

如果线程执行的是java方法，记录的是正在执行的虚拟机字节码指令的地址，如果是native 方法，这个计数器值为undefined

这个区域是内存区域唯一一个在虚拟机中没有规定任何OutOfMemoryError情况的区域

## 栈区

  			栈也叫栈内存，主管Java程序的运行，是在线程创建时创建，它的生命期是跟随线程的生命期，线程结束栈内存也就释放了。对于栈来说不存在垃圾回收的问题，只要线程一结束该栈就Over了。
  			栈也叫栈内存，是描述Java方法执行的内存模型，每个方法在执行的同时会相应的创建一个栈帧（Stack Frame）用于存储局部变量表、操作数栈、动态链接、方法出口等信息。每个方法从调用直至执行完成的过程，就对应一个栈帧在虚拟机栈中入站到出站的过程。
  			栈帧是用来存储数据和部分过程的数据结构、同时也被用来处理动态链接、方法返回值和异常分派。栈帧随方法的调用而创建、结束而销毁。无论方法正常完成还是异常完成、都算作方法结束

栈区也是线程私有的，8种基本类型的变量+对象的引用变量+实例方法都是在函数的栈内存中分配。

#### 栈存储什么：

1. 局部变量表：输入参数和输出参数以及方法内的变量类型；局部变量表在编译期间完成分配，当进入一个方法时，这个方法在栈帧中分配多少内存是固定的
2. 栈操作：记录出栈、入栈的操作
3. 动态链接
4. 方法出口

​	栈溢出： StackOverflowError,OutOfMemory  

## Native Interface 本地接口

本地接口的作用是融合不同的编程语言为Java所用

## Native Method Stack 本地方法区	

它的具体做法是Native Method Stack 中登记native方法，在Execution Engin执行时加载本地方法库

 每执行一个方法都会产生一个栈 帧，保存到栈(后进先出)的顶部， 顶部栈就是当前的方法，该方法 执行完毕 后会自动将此栈帧出 栈 

## 方法区

Method Area  方法区是被所有线程共享的，所有字段和方法字节码，以及一些特殊方法如构造函数，接口代码也在此定义。简单书，所有定义的方法信息都保存在此区域，此区域属于共享区间。

类信息

​	类的版本

​	字段

​	方法

​	接口

静态变量

常量

类信息（构造方法/接口定义）

运行时常量池

#### 方法区与永久代

​		永久存储区是一个常驻内存区域，用于存放JDK自身携带的Class、Interface的元数据。也就是说它存储的是运行环境必须的类信息，被装载进此区域的数据是不会被垃圾回收器回收的。关闭JVM才会释放此区域所占用的内存。

​		如果出现java.lang.OutOfMemoryError:PermGen space,说明是Java 虚拟机对永久代Prem内存设置不够，一般不会出现这种情况，都是程序启动需要加载大量的第三方jar包，例如Tomcat 下部署了太多的应用，或者大量动态发射生成的类不断被加载，最终导致Perm区被沾满。

Jdk1.6及之前：有永久代 常量池1.6在方法区

Jdk1.7:		       有永久代，但已经逐步“去永久代”，常量池1.7在堆

Jdk1.8及之后： 无永久代，常量池1.8在元空间

## 方法区-运行时常量池

它是方法区的一部分，用于存放编译期间生成的各种字面变量和符号引用，这部分内容将在类记载后存放到常量池中

​		方法区(Method Area),是各个线程共享的内存区域，它用于存储虚拟机加载的：类信息+普通常量+静态常量+编译器编译后的代码等等，虽然JVM规范将方法区描述为堆的一个逻辑部分，但它却还有一个别名叫Non-Heap(非堆),目的就是要和堆分开。

​		对于HotSpot虚拟机，很多开发者习惯将方法区称为“永久代(Paramanent Gen)“，但严格本质上说两者不同，或者说使用永久代来实现方法区而已，永久代是方法区（相当于是一个接口Interface）的一个实现，jdk1.7的版本中，已经将原本放在永久代的字符串常量池移走。

​		常量池（Constand Pool） 是方法区的一部分，class文件除了有类的版本，字段、方法、接口等描述信息外，还有一项信息就是常量池。这部分内容将在类加载后进入方法区的运行时常量池中存放。

## Heap 堆

​		一个JVM实例只存放在一个堆内存，堆内存的大小是可以调节的，类加载器读取了类文件后，需要把类、方法、常变量放到堆内存中，保存所有引用类型的真实信息，以方便执行器执行，堆分为三部分

新生代 Young Generation Space 

老年代  Tenure generation space

永久区 Permanent Space

##  Heap堆(Java8) 

​		 一个JVM实例只存在一个堆内存，堆内存的大小是可以调节的。 类加载器读取了类文件后，需要把类、方法、常变量放到堆内存中，保 存所有引用类型的真实信息，以方便执行器执行 。

​       堆内存逻辑上分为三部分：新生代+老年代+方法区

新生代：用来存放新生的对象。一般占据堆三分之一的空间，由于频繁的创建对象，所以新生代会频繁触发MinorGC进行垃圾回收。新生代又分为Eden区、SurvivorFrom、survivorTo三个区

![1629127791691](img\1629127791691.png)

JDK7：

![1629127887068](img\1629127887068.png)

JDK1.8

DK 1.8之后将最初的永久代取消了，由元空间取代 

![1629128011029](img\1629128011029.png)

## 对象创建

 给对象分配内存 

线程安全性问题 

初始化对象 

执行构造方法 

new 类名 ----> 根据new 的参数在常量池中定位一个符号引用 ---->  如果没有找到这个符号引用，则执行类的加载，执行类的加载、验证、初始化  ---> 虚拟机为对象分配内存 ---->  将分配的内存初始化为零值  ----> 调用对象的<init>方法

## 给对象分配内存

· 指针碰撞

​		指针碰撞是指在不一个连续的内存区域中，移动一个指针来分配内存的过程。具体来说，Eden区是一个内存连续的区域，JVM维护一个指针，指向下一个可用的内存位置

- 所代表的垃圾收集器有Serial、ParNew，用算法标记-整理算法
- 可能导致线程不安全问题，可看下方tip1：指针碰撞所带来的线程安全问题。

· 空间列表

- 所代表的垃圾收集器CMS，采用标记清除算法

 ## 线程安全性问题 

 • 线程同步

 • 本地线程分配缓冲(TLAB) 

 ##  对象的结构 

· Header(对象头)

​		自身运行时数据(Mark Word)

​			哈希值

​			GC分代年龄

​			锁状态标志

​			线程持有锁

​			偏向线程ID

​			偏向时间戳

​		类型指针

​		数组长度（只有数组对象才有）

· InstanceData

​		相同宽度的数据分配到一起（long,double）

· Padding (对其填充)

​	8个字节的整数倍

由于HotSpot虚拟机的自动内存管理系统要求对象起始地址必须是8字节的整数倍，换句话说就是任何对象的大小都必须是8字节的整数倍。对象头部分已经被精心设计成正好是8字节的倍数（1倍或者2倍），因此，如果对象实例数据部分没有对齐的话，就需要通过对齐填充来补全。

通过注解 @Contended 进行填充

jvm启动参数：【-XX:RestrictContended】

##  Hotspot虚拟机对象头 Mark Word  

![1629130305492](img\1629130305492.png)

##  对象的访问定位 

 **• 使用句柄**

#### 概念

句柄是一种间接访问对象的方式。在句柄模式下，JVM会在堆内存中创建一个句柄池，每个对象在句柄池中有一个唯一的句柄。句柄包含对象实例数据的指针和对象类型数据的指针。

#### 结构

- **句柄池**：一个固定大小的数组，每个元素是一个句柄。

- 句柄

  ：包含两个指针：

  - 指向对象实例数据的指针。
  - 指向对象类型数据的指针。

#### 访问过程

1. **获取句柄**：通过对象的引用获取句柄。
2. **访问实例数据**：通过句柄中的指针访问对象的实例数据。
3. **访问类型数据**：通过句柄中的指针访问对象的类型数据。

#### 优点

- **稳定性**：即使对象被移动（例如在垃圾回收过程中），句柄中的指针可以更新，而对象的引用保持不变。这使得对象的引用更加稳定。
- **灵活性**：可以在句柄中添加更多的信息，例如对象的状态标志等。

#### 缺点

- **额外的间接层**：每次访问对象都需要两次指针跳转，增加了访问成本。

- **内存开销**：每个对象都需要一个句柄，增加了内存开销。

  

 **• 直接指针**

#### 概念

直接指针是一种直接访问对象的方式。在直接指针模式下，对象的引用直接指向对象在堆内存中的地址。对象的实例数据和类型数据都存储在一起。

#### 结构

- **对象引用**：直接指向对象在堆内存中的地址。
- **对象**：包含实例数据和类型数据。

#### 访问过程

1. **获取对象引用**：通过对象的引用直接获取对象的地址。
2. **访问实例数据**：直接通过对象引用访问对象的实例数据。
3. **访问类型数据**：直接通过对象引用访问对象的类型数据。

#### 优点

- **高效**：每次访问对象只需要一次指针跳转，减少了访问成本。
- **简洁**：没有额外的间接层，内存开销较小。

#### 缺点

- **不稳定**：如果对象被移动（例如在垃圾回收过程中），对象的引用需要更新，否则会导致访问错误。
- **复杂性**：在对象移动时需要更新所有的引用，增加了垃圾回收的复杂性。

### 总结

- **句柄**：适合需要高度稳定的引用场景，例如在频繁发生垃圾回收的情况下。虽然有额外的间接层和内存开销，但引用更加稳定。
- **直接指针**：适合对性能要求较高的场景，例如在对象移动较少的情况下。访问效率更高，但需要处理对象移动时的引用更新问题。

 

##  栈+堆+方法区的交互关系 

 HotSpot是使用指针的方式来访问对象： Java堆中会存放访问类元数据的地址， reference存储的就直接是对象的地址  

![1629130531835](img\1629130531835.png)

##  垃圾回收 

 如何判断对象为垃圾对象 

引用计数法 

可达性分析



• 如何回收 

​		回收策略 

​			• 标记清除

​			• 复制 

​			• 标记整理 

​			• 分代算法 

垃圾回收器 ： Serial 、 ParNew 、CMS、G1 、ZGC

何时回收  ：默认标记两次后，进行回收，并不会标记后马上被回收

###  引用计数法 

 对象被加载到内存的时候，JVM就会对该对象的类信息、引用地址进行监测，

在对象中添加一个引用计数 器，当有地方引用这个对象的时候， 计数器+1，当失效的时候，计数器-1 

###  可达性分析法 

 作为GCRoot的对象 

 • 虚拟机栈(局部变量表中的)

 • 方法区的类属性所引用的对象

 • 方法区的常量所引用的对象 

• 本地方法栈所引用的对象 

采用向量图进行存储，根为 gcRoot

###  标记清除算法 

 分成标记和清除两个阶段

 缺点  • 效率问题 

​           • 内存碎片 

###  复制算法 

 • 堆

​	 新生代 

​			Eden 

​			Survivor 

​     老年代 

​			Tenured Gen 

• 方法区 

• 虚拟机栈 

• 本地方法栈 

• 程序计数器 

###  标记-整理算法 

##  内存分配策略 

 • 优先分配Eden区 

 • 大对象直接分配到老年代  

 		-XX:PretenureSizeThreshold   // 大于这个值的大对象也直接在老年代分配。 

 •长期存活的对象分配老年代 

​		 -XX:MaxTenuringThreshold=15   // 设置新生代垃圾的最大年龄 

 • 空间分配担保 

​		 -XX:+HandlePromotionFailure  ##是否设置空间分配担保 

​			检查老年代最大可用的连续空间是否大于历次晋升到老年代对象的平均大小。 

 • 动态对象年龄对象 

​		 如果在Survivor空间中相同年龄所有对象大小的总和大于Survivor空间的一 半，年龄大于或等于该年龄的对象就可以直接进入老年代 

​		 -XX:TargetSurvivorRatio  ## Survivor区对象使用率80%，默认是50% 



```shell
 * -XX:+PrintFlagsInitial : 查看所有的参数的默认初始值
 * -XX:+PrintFlagsFinal  ：查看所有的参数的最终值（可能会存在修改，不再是初始值）
 *      具体查看某个参数的指令： jps：查看当前运行中的进程
 *                           jinfo -flag SurvivorRatio 进程id
 *
 * -Xms：初始堆空间内存 （默认为物理内存的1/64）
 * -Xmx：最大堆空间内存（默认为物理内存的1/4）
 * -Xmn：设置新生代的大小。(初始值及最大值)
 * -XX:NewRatio：配置新生代与老年代在堆结构的占比
 * -XX:SurvivorRatio：设置新生代中Eden和S0/S1空间的比例
 * -XX:MaxTenuringThreshold：设置新生代垃圾的最大年龄
 * -XX:+PrintGCDetails：输出详细的GC处理日志
 * 打印gc简要信息：① -XX:+PrintGC   ② -verbose:gc
 * -XX:HandlePromotionFailure：是否设置空间分配担保
```

#  Class 文件

##  Class文件结构  

 Class 文件是一组以 8 位字节为基础单位的二进制流，各个数据 项目严格按照顺序紧凑地排列在 Class 文件之中，中间没有添加任何 分隔符，这使得整个 Class 文件中存储的内容几乎全部是程序运行的 必要数据，没有空隙存在。 当遇到需要占用 8 位字节以上空间的数据项时，则会按照高位在 前（Big-Endian）的方式分割成若干个 8 位字节进行存储。 Class 文件只有两种数据类型：无符号数和表  

 链接：https://docs.oracle.com/javase/specs/jvms/se8/html/jvms-4.html  



 魔数

 Class文件版本

 常量池 

访问标志

 类索引，父类索引，接口索引集合 

字段表集合

方法表集合 

属性表集合  

```c
ClassFile { 
u4 magic; // 魔法数字，表明当前文件是.class文件，固定0xCAFEBABE
u2 minor_version; // 分别为Class文件的副版本和主版本
u2 major_version; 
u2 constant_pool_count; // 常量池计数
cp_info constant_pool[constant_pool_count-1]; // 
u2 access_flags; // 类访问标识
u2 this_class; // 当前类
u2 super_class; // 父类
u2 interfaces_count; // 实现的接口数
u2 interfaces[interfaces_count]; // 实现接口信息
u2 fields_count; // 字段数量
field_info fields[fields_count]; // 包含的字段信息
u2 methods_count; // 方法数量
method_info methods[methods_count]; // 包含的方法信息
u2 attributes_count; // 属性数量
attribute_info attributes[attributes_count]; // 各种属性
}

```

###  魔数 

 版本 JDK1.8 = 52

 JDK1.8 = 51 

###  常量池 

 CP_INFO 

cp_info {

​	 u1 tag;

​	 u1 info[]; 

}  

| Constant Type               | Value |
| --------------------------- | ----- |
| CONSTANT_Class              | 7     |
| CONSTANT_Fieldref           | 9     |
| CONSTANT_Methodref          | 10    |
| CONSTANT_InterfaceMethodref | 11    |
| CONSTANT_String             | 8     |
| CONSTANT_Integer            | 3     |
| CONSTANT_Float              | 4     |
| CONSTANT_Long               | 5     |
| CONSTANT_Double             | 6     |
| CONSTANT_NameAndType        | 12    |
| CONSTANT_Utf8               | 1     |
| CONSTANT_MethodHandle       | 15    |
| CONSTANT_MethodType         | 16    |
| CONSTANT_InvokeDynamic      | 18    |

####  CONSTANT_Methodref_info 

 用于记录方法的信息

 CONSTANT_Methodref_info 

{   u1 tag; 

​	u2 class_index; 

​	u2 name_and_type_index; 

} 

###  字段表集合  

 字段表用于描述接口或者类中声明的变量 

##  Hostspot 垃圾收集器 

![1629132203423](img\1629132203423.png)

##  Parallel Scavenge 收集器 

 复制算法(新生代收集器) 

• 多线程收集器

 • 达到可控制的吞吐量

 • 吞吐量：CPU用于运行用户代码时间与CPU消耗总时间的 比值 

• 吞吐量=执行用户代码时间/(执行用户代码时间 + 垃圾回收 使用的时间） 

• -XX:MaxGCPauseMillis 垃圾收集停顿时间 

• -XX:GCTimeRatio 吞吐量大小 （0,100 

##  G1 收集器

  历史 • 2004年 Sun公司实验室发表了论文

 • JDk7 才使用了G1  

优势： 

​	并行与并发 、

 	分代收集 、

​	 空间整合 、

 	可预测的停顿 

步骤：  初始标记 -》 并发标记  -》 最终标记  -》  帅选回收 

##  CMS 收集器 (Concurrent Mark Sweep) 

工作工程：

  初始标记 -》 并发标记 -》 重新标记 -》 并发标记

优点： 并发收集、低停顿

缺点：占用CPU资源

​			无法处理浮动垃圾

​			出现 Concurrent Mode Failure 

 		   空间碎片  

```txt
 Concurrent Mode Failure 
 CMS垃圾收集器特有的错误，CMS的垃圾清理和引用线程是并行进行的，如果在并行清理的过程中老年代的空间不足以容纳应用产生的垃圾（**也就是老年代正在清理，从年轻代晋升了新的对象，或者直接分配大对象年轻代放不下导致直接在老年代生成，这时候老年代也放不下**），则会抛出“concurrent mode failure”。 
 concurrent mode failure影响
　　老年代的垃圾收集器从CMS退化为Serial Old，所有应用线程被暂停，停顿时间变长。

可能原因及方案
原因1：CMS触发太晚
方案：将-XX:CMSInitiatingOccupancyFraction=N调小；

原因2：空间碎片太多
方案：开启空间碎片整理，并将空间碎片整理周期设置在合理范围；

-XX:+UseCMSCompactAtFullCollection （空间碎片整理）

-XX:CMSFullGCsBeforeCompaction=n

原因3：垃圾产生速度超过清理速度
晋升阈值过小；

Survivor空间过小；

Eden区过小，导致晋升速率提高；

存在大对象；
```

##  CMS收集器详解 

 一、CMS收集过程 

```
初始标记（CMS initial mark）：独占CPU，stop-the-world, 仅标记GCroots能直接关联的对象,速度比较快；
并发标记（CMS concurrent mark）:可以和用户线程并发执行，通过GCRoots Tracing 标记所有可达对象；
重新标记（CMS remark）：独占CPU，stop-the-world, 对并发标记阶段用户线程运行产生的垃圾对象进行标记修正,以及更新逃逸对象；
并发清理（CMS concurrent sweep）：可以和用户线程并发执行，清理在重复标记中被标记为可回收的对象。
```



初始标记：这一步的作用是标记存活的对象，有两部分： 1. 标记老年代中所有的GC Roots对象，如下图节点1； 2. 标记年轻代中活着的对象引用到的老年代的对象，如下图节点2、3；  

![1629132833639](img\1629132833639.png)

 并发标记：从“初始标记”阶段标记的对象开始找出所有存活的对象 

![1629132938855](img\1629132938855.png)

 预清理阶段：这个阶段就是用来处理前一个阶段因为引用关系改变导致没有标记到的存活对象的， 它会扫描所有标记为Direty的Card 如下图所示， 在并发标记阶段，节点3的引用指向了6；则会把节点3的card标记为Dirty 

<img src="img\1629133024222.png" alt="1629133024222" />

 重新标记：该阶段的任务是完成标记整个年老代的所有的存活对象 

 并发清理：这个阶段主要是清除那些没有标记的对象并且回收空间 

 预清理：预清理，也是用于标记老年代存活的对象，目的是为了让重新标记阶段的STW尽可能短  

##  G1 收集器  

  G1的内存模型 

![1629169525569](img\1629169525569.png)



```txt
初始标记（Initial Marking）：标记一下GC Roots能直接关联到的对象，伴随着一次普通的Young GC发生，并修改NTAMS（Next Top at Mark Start）的值，让下一阶段用户程序并发运行时，能在正确可用的Region中创建新对象，此阶段是stop-the-world操作。
根区间扫描，标记所有幸存者区间的对象引用，扫描 Survivor到老年代的引用，该阶段必须在下一次Young GC 发生前结束。
并发标记（Concurrent Marking）：是从GC Roots开始堆中对象进行可达性分析，找出存活的对象，这阶段耗时较长，但可与用户程序并发执行，该阶段可以被Young GC中断。
最终标记（Final Marking）：是为了修正并发标记期间因用户程序继续运作而导致标记产生变动的那一部分标记记录，虚拟机将这段时间对象变化记录在线程Remembered Set Logs里面，最终标记阶段需要把Remembered Set Logs的数据合并到Remembered Set中，此阶段是stop-the-world操作，使用snapshot-at-the-beginning (SATB) 算法。
筛选回收（Live Data Counting and Evacuation）：首先对各个Region的回收价值和成本进行排序，根据用户所期望的GC停顿时间来制定回收计划,回收没有存活对象的Region并加入可用Region队列。这个阶段也可以做到与用户程序一起并发执行，但是因为只回收一部分Region，时间是用户可控制的，而且停顿用户线程将大幅提高收集效率。
```





# GC 调优步骤

1 打印GC日志

-XX:PrintGCDetails -XX:PrintGCTimeStamps -XX:+PrintGCDataStamps -Xloggc:./gc.log

Tomcat 则直接加在JAVA_OPTS变量里

​		·分析日志得到关键性指标

​		·分析GC原因，调优JVM参数



1、Parallel Scavenge收集器（默认）

分析 Parallel -gc.log

##### 调优:

第一次调优，设置Metaspace大小。增大元空间大小 -XX:MetaspaceSize=64M -XX:MaxMetaspaceSize = 64M



第一次调优,增大年轻代动态扩容增量，默认是20（%），可以减少 yong gc: 

--XX:YoungGenerationSizeIncrement=30



问题：

##### 哪些对象会被存放到⽼年代？ 

​	新⽣代对象每次经历⼀次minor gc，年龄会加1，当达到年龄阈值 （默认为15岁）会直接进⼊⽼年代； 

​	⼤对象直接进⼊⽼年代； 

​	新⽣代复制算法需要⼀个survivor区进⾏轮换备份，如果出现⼤量对 象在minor gc后仍然存活的情况时，就需	要⽼年代进⾏分配担保，让 survivor⽆法容纳的对象直接进⼊⽼年代； 

​	 如果在Survivor空间中相同年龄所有对象⼤⼩的总和⼤于Survivor空 间的⼀半，年龄⼤于或等于该年龄的对象	就可以直接进⼊年⽼代。 

##### 什么时候触发full gc？ 

调⽤System.gc时，系统建议执⾏Full GC，但是不必然执⾏

⽼年代空间不⾜ 

⽅法去空间不⾜ 

通过Minor GC后进⼊⽼年代的平均⼤⼩⼤于⽼年代的可⽤内存

由Eden区、From Space区向To Space区复制时，对象⼤⼩⼤于 To Space可⽤内存，则把该对象转存到⽼年代，且⽼年代的可⽤内存⼩ 于该对象⼤⼩ 

#####  jvm中哪些地⽅会出现oom？.



# JVM 发生OOM的四种情况

##### 1、[Java](http://lib.csdn.net/base/javase)堆溢出：heap

Java堆内存主要用来存放运行过程中所以的对象，该区域OOM异常一般会有如下错误信息;
java.lang.OutofMemoryError:[Java ](http://lib.csdn.net/base/java)heap space
此类错误一般通过Eclipse Memory Analyzer分析OOM时dump的内存快照就能分析出来，到底是由于程序原因导致的内存泄露，还是由于没有估计好JVM内存的大小而导致的内存溢出。

另外，Java堆常用的JVM参数：-Xms：初始堆大小，默认值为物理内存的1/64(<1GB)，默认(MinHeapFreeRatio参数可以调整)空余堆内存小于40%时，JVM就会增大堆直到-Xmx的最大限制.
-Xmn：年轻代大小(1.4or lator)，此处的大小是（eden + 2 survivor space)，与jmap -heap中显示的New gen是不同的。

##### 2、栈溢出：stack

栈用来存储线程的局部变量表、操作数栈、动态链接、方法出口等信息。如果请求栈的深度不足时抛出的错误会包含类似下面的信息：
java.lang.StackOverflowError

另外，由于每个线程占的内存大概为1M，因此线程的创建也需要内存空间。[操作系统](http://lib.csdn.net/base/operatingsystem)可用内存-Xmx-MaxPermSize即是栈可用的内存，如果申请创建的线程比较多超过剩余内存的时候，也会抛出如下类似错误：

java.lang.OutofMemoryError: unable to create new native thread

相关的JVM参数有：-Xss: 每个线程的堆栈大小,JDK5.0以后每个线程堆栈大小为1M,以前每个线程堆栈大小为256K.

运行时常量保存在方法区，存放的主要是编译器生成的各种字面量和符号引用，但是运行期间也可能将新的常量放入池中，比如String类的intern方法。

java.lang.OutofMemoryError: PermGen space

相关的JVM参数有：

-XX:PermSize：设置持久代(perm gen)初始值，默认值为物理内存的1/64

-XX:MaxPermSize：设置持久代最大值，默认为物理内存的1/4

 

##### 4、方法区溢出  directMemory

方法区主要存储被虚拟机加载的类信息，如类名、访问修饰符、常量池、字段描述、方法描述等。理论上在JVM启动后该区域大小应该比较稳定，但是目前很多框架，比如Spring和Hibernate等在运行过程中都会动态生成类，因此也存在OOM的风险。如果该区域OOM，错误结果会包含类似下面的信息：

相关的JVM参数可以参考运行时常量。

 

另外，在定位JVM内存问题的时候可以借助于一些辅助信息：

##### 1、日志相关

-XX:+PrintGC：输出形式: [GC 118250K->113543K(130112K), 0.0094143 secs]
-XX:+PrintGCDetails：输出形式:
[GC [DefNew: 8614K->8614K(9088K), 0.0000665 secs][Tenured: 112761K->10414K(121024K), 0.0433488 secs] 121376K->10414K(130112K), 0.0436268 secs]
-XX:+PrintGCApplicationStoppedTime：打印垃圾回收期间程序暂停的时间.

-Xloggc:filename：把相关日志信息记录到文件以便分析.

 

##### 2、错误调试相关：

-XX:ErrorFile=./hs_err_pid<pid>.log：如果JVM crashed，将错误日志输出到指定文件路径。-XX:HeapDumpPath=./java_pid<pid>.hprof：堆内存快照的存储文件路径。

 

##### 3、类装载相关

-XX:-TraceClassLoading：打印class装载信息到stdout。记Loaded状态。

-XX:-TraceClassUnloading：打印class的卸载信息到stdout。记Unloaded状态。



调优⽅案有哪些？ 

#### 调优时机： 

a. heap 内存（⽼年代）持续上涨达到设置的最⼤内存值； 

b. Full GC 次数频繁； c. GC 停顿时间过⻓（超过1秒）； 

d. 应⽤出现OutOfMemory 等内存异常； 

e. 应⽤中有使⽤本地缓存且占⽤⼤量内存空间； 

f. 系统吞吐量与响应性能不⾼或下降。 



#### 调优原则： 

a. 多数的Java应⽤不需要在服务器上进⾏JVM优化；

 b. 多数导致GC问题的Java应⽤，都不是因为我们参数设置错误， ⽽是代码问题； 

c. 在应⽤上线之前，先考虑将机器的JVM参数设置到最优（最适 合）； 

d. 减少创建对象的数量； 

e. 减少使⽤全局变量和⼤对象； 

f. JVM优化是到最后不得已才采⽤的⼿段；

 g. 在实际使⽤中，分析GC情况优化代码⽐优化JVM参数更好；

#### 调优⽬标： 

a. GC低停顿； 

b. GC低频率；

 c. 低内存占⽤； 

d. ⾼吞吐量； 



#### 调优步骤： 

a. 分析GC⽇志及dump⽂件，判断是否需要优化，确定瓶颈问题 点； 

b. 确定jvm调优量化⽬标； c. 确定jvm调优参数（根据历史jvm参数来调整）；

 d. 调优⼀台服务器，对⽐观察调优前后的差异； 

e. 不断的分析和调整，知道找到合适的jvm参数配置；

 f. 找到最合适的参数，将这些参数应⽤到所有服务器，并进⾏后续 跟踪。 

12. 平时有没有看过什么源码，请画出来。 深⼊理解：https://juejin.im/post/5caef238e51d456e27504b83 13. 有没有写过或者看过custom classloader？ 了解⼀下即可：https://www.jianshu.com/p/3036b46f1188 

#  垃圾收集器分类 

## 串行收集器->Serial和Serial Old 

 只能有一个垃圾回收线程执行，用户线程暂停。 

 适用于内存比较小的嵌入式设备 。 

## 并行收集器[吞吐量优先]->Parallel Scanvenge、Parallel Old 

 多条垃圾收集线程并行工作，但此时用户线程仍然处于等待状态。 

 适用于内存比较小的嵌入式设备 。 

## 并发收集器[停顿时间优先]->CMS、G1 

 用户线程和垃圾收集线程同时执行(但并不一定是并行的，可能是交替执行的)，垃圾收集线程在执行的 时候不会停顿用户线程的运行。 

 适用于相对时间有要求的场景，比如Web 。 

```sh
（1）串行
-XX：+UseSerialGC
-XX：+UseSerialOldGC
（2）并行(吞吐量优先)：
-XX：+UseParallelGC
-XX：+UseParallelOldGC
（3）并发收集器(响应时间优先)
-XX：+UseConcMarkSweepGC
-XX：+UseG1GC
```

