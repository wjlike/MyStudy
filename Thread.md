# 四种常见的线程池详解

  ExecutorService是Java提供的用于管理线程池的类。该类的两个作用：控制线程数量和重用线程 

###  Executors.newCacheThreadPool()：

可缓存线程池，先查看池中有没有以前建立的线程，如果有，就直接使用。如果没有，就建一个新的线程加入池中，缓存型池子通常用于执行一些生存期很短的异步型任务 

```java
package com.mianshi.test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NewCachedThreadPoolTest {

    public static void main(String[] args) {
        // 创建一个可缓存线程池
        ExecutorService cachedThreadPool = Executors.newCachedThreadPool();
        for (int i = 0; i < 10; i++) {
            try {
                // sleep可明显看到使用的是线程池里面以前的线程，没有创建新的线程
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            cachedThreadPool.execute(new Runnable() {
                public void run() {
                    // 打印正在执行的缓存线程信息
                    System.out.println(Thread.currentThread().getName()
                            + "正在被执行");
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

}

pool-1-thread-1正在被执行
pool-1-thread-1正在被执行
pool-1-thread-1正在被执行
pool-1-thread-1正在被执行
pool-1-thread-1正在被执行
pool-1-thread-1正在被执行
pool-1-thread-1正在被执行
pool-1-thread-1正在被执行
pool-1-thread-1正在被执行
pool-1-thread-1正在被执行

线程池为无限大，当执行当前任务时上一个任务已经完成，会复用执行上一个任务的线程，而不用每次新建线程
```

###  Executors.newFixedThreadPool(int n)：

创建一个可重用固定个数的线程池，以共享的无界队列方式来运行这些线程。

```java
package com.mianshi.test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NewFixedThreadPoolTest {

    public static void main(String[] args) {
        // 创建一个可重用固定个数的线程池
        ExecutorService fixedThreadPool = Executors.newFixedThreadPool(3);
        for (int i = 0; i < 10; i++) {
            fixedThreadPool.execute(new Runnable() {
                public void run() {
                    try {
                        // 打印正在执行的缓存线程信息
                        System.out.println(Thread.currentThread().getName()
                                + "正在被执行");
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

}

pool-1-thread-1正在被执行
pool-1-thread-2正在被执行
pool-1-thread-3正在被执行
pool-1-thread-1正在被执行
pool-1-thread-2正在被执行
pool-1-thread-3正在被执行
pool-1-thread-1正在被执行
pool-1-thread-2正在被执行
pool-1-thread-3正在被执行
pool-1-thread-1正在被执行

因为线程池大小为3，每个任务输出打印结果后sleep 2秒，所以每两秒打印3个结果。
定长线程池的大小最好根据系统资源进行设置。如Runtime.getRuntime().availableProcessors()
```

###  Executors.newScheduledThreadPool(int n)：

创建一个定长线程池，支持定时及周期性任务执行 

```java
package com.mianshi.test;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class NewScheduledThreadPoolTest {

    public static void main(String[] args) {
                 //创建一个定长线程池，支持定时及周期性任务执行——延迟执行
                 ScheduledExecutorService scheduledThreadPool = Executors.newScheduledThreadPool(5);
                 //延迟1秒执行
                 /*scheduledThreadPool.schedule(new Runnable() {
                     public void run() {
                        System.out.println("延迟1秒执行");
                     }
                 }, 1, TimeUnit.SECONDS);*/
                 
                 
                 //延迟1秒后每3秒执行一次
                 scheduledThreadPool.scheduleAtFixedRate(new Runnable() {
                     public void run() {
                         System.out.println("延迟1秒后每3秒执行一次");
                     }
                }, 1, 3, TimeUnit.SECONDS);
                 
             }
    
}
输出结果：延迟1秒执行
    输出结果：

延迟1秒后每3秒执行一次
延迟1秒后每3秒执行一次
    .............
```

###   Executors.newSingleThreadExecutor()： 

 创建一个单线程化的线程池，它只会用唯一的工作线程来执行任务，保证所有任务按照指定顺序(FIFO, LIFO, 优先级)执行。 

```java
package com.mianshi.test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NewSingleThreadExecutorTest {


public static void main(String[] args) {
//创建一个单线程化的线程池
ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();
for (int i = 0; i < 10; i++) {
final int index = i;
singleThreadExecutor.execute(new Runnable() {
public void run() {
try {
//结果依次输出，相当于顺序执行各个任务
System.out.println(Thread.currentThread().getName()+"正在被执行,打印的值是:"+index);
Thread.sleep(5000);
} catch (InterruptedException e) {
e.printStackTrace();
}
}
});
}
}

}
pool-1-thread-1正在被执行,打印的值是:0
pool-1-thread-1正在被执行,打印的值是:1
pool-1-thread-1正在被执行,打印的值是:2
pool-1-thread-1正在被执行,打印的值是:3
pool-1-thread-1正在被执行,打印的值是:4
pool-1-thread-1正在被执行,打印的值是:5
pool-1-thread-1正在被执行,打印的值是:6
pool-1-thread-1正在被执行,打印的值是:7
pool-1-thread-1正在被执行,打印的值是:8
pool-1-thread-1正在被执行,打印的值是:9
```

#  缓冲队列BlockingQueue和自定义线程池ThreadPoolExecutor

\1. 缓冲队列BlockingQueue简介：

​     BlockingQueue是双缓冲队列。BlockingQueue内部使用两条队列，允许两个线程同时向队列一个存储，一个取出操作。在保证并发安全的同时，提高了队列的存取效率。

\2. 常用的几种BlockingQueue：

- ArrayBlockingQueue（int i）:规定大小的BlockingQueue，其构造必须指定大小。其所含的对象是FIFO顺序排序的。
- LinkedBlockingQueue（）或者（int i）:大小不固定的BlockingQueue，若其构造时指定大小，生成的BlockingQueue有大小限制，不指定大小，其大小有Integer.MAX_VALUE来决定。其所含的对象是FIFO顺序排序的。
- PriorityBlockingQueue（）或者（int i）:类似于LinkedBlockingQueue，但是其所含对象的排序不是FIFO，而是依据对象的自然顺序或者构造函数的Comparator决定。
- SynchronizedQueue（）:特殊的BlockingQueue，对其的操作必须是放和取交替完成。

\3. 自定义线程池（ThreadPoolExecutor和BlockingQueue连用）：

   *自定义线程池，可以用ThreadPoolExecutor类创建，它有多个构造方法来创建线程池。*

  *常见的构造函数：ThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue workQueue)*

```java
package com.mianshi.test;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ZiDingYiThreadPoolExecutor {
    
public static    class TempThread implements Runnable {
          
             @Override
             public void run() {
                 // 打印正在执行的缓存线程信息
                 System.out.println(Thread.currentThread().getName() + "正在被执行");
                 try {
                     // sleep一秒保证3个任务在分别在3个线程上执行
                    Thread.sleep(1000);
                 } catch (InterruptedException e) {
                     e.printStackTrace();
                 }
             }
         
         }
    
    
public static void main(String[] args) {
                 // 创建数组型缓冲等待队列
                 BlockingQueue<Runnable> bq = new ArrayBlockingQueue<Runnable>(10);
                 // ThreadPoolExecutor:创建自定义线程池，池中保存的线程数为3，允许最大的线程数为6
                 ThreadPoolExecutor tpe = new ThreadPoolExecutor(3, 6, 50, TimeUnit.MILLISECONDS, bq);
         
                 // 创建3个任务
                 Runnable t1 = new TempThread();
                 Runnable t2 = new TempThread();
                 Runnable t3 = new TempThread();
                  Runnable t4 = new TempThread();
                  Runnable t5 = new TempThread();
                  Runnable t6 = new TempThread();
         
                 // 3个任务在分别在3个线程上执行
                 tpe.execute(t1);
                 tpe.execute(t2);
                 tpe.execute(t3);
                  tpe.execute(t4);
                  tpe.execute(t5);
                  tpe.execute(t6);
         
                 // 关闭自定义线程池
                 tpe.shutdown();
             }
    
    

}
输出结果：

pool-1-thread-1正在被执行
pool-1-thread-2正在被执行
pool-1-thread-3正在被执行
```

# Java线程池七个参数详解

从源码中可以看出，线程池的构造函数有7个参数，分别是corePoolSize、maximumPoolSize、keepAliveTime、unit、workQueue、threadFactory、handler。下面会对这7个参数一一解释。

#### 一、corePoolSize 线程池核心线程大小

线程池中会维护一个最小的线程数量，即使这些线程处理空闲状态，他们也不会被销毁，除非设置了allowCoreThreadTimeOut。这里的最小线程数量即是corePoolSize。

#### 二、maximumPoolSize 线程池最大线程数量

一个任务被提交到线程池以后，首先会找有没有空闲存活线程，如果有则直接将任务交给这个空闲线程来执行，如果没有则会缓存到工作队列（后面会介绍）中，如果工作队列满了，才会创建一个新线程，然后从工作队列的头部取出一个任务交由新线程来处理，而将刚提交的任务放入工作队列尾部。线程池不会无限制的去创建新线程，它会有一个最大线程数量的限制，这个数量即由maximunPoolSize指定。

#### 三、keepAliveTime 空闲线程存活时间

一个线程如果处于空闲状态，并且当前的线程数量大于corePoolSize，那么在指定时间后，这个空闲线程会被销毁，这里的指定时间由keepAliveTime来设定

#### 四、unit 空闲线程存活时间单位

keepAliveTime的计量单位

#### 五、workQueue 工作队列

新任务被提交后，会先进入到此工作队列中，任务调度时再从队列中取出任务。jdk中提供了四种工作队列：

①ArrayBlockingQueue

基于数组的有界阻塞队列，按FIFO排序。新任务进来后，会放到该队列的队尾，有界的数组可以防止资源耗尽问题。当线程池中线程数量达到corePoolSize后，再有新任务进来，则会将任务放入该队列的队尾，等待被调度。如果队列已经是满的，则创建一个新线程，如果线程数量已经达到maxPoolSize，则会执行拒绝策略。

②LinkedBlockingQuene

基于链表的无界阻塞队列（其实最大容量为Interger.MAX），按照FIFO排序。由于该队列的近似无界性，当线程池中线程数量达到corePoolSize后，再有新任务进来，会一直存入该队列，而不会去创建新线程直到maxPoolSize，因此使用该工作队列时，参数maxPoolSize其实是不起作用的。

③SynchronousQuene

一个不缓存任务的阻塞队列，生产者放入一个任务必须等到消费者取出这个任务。也就是说新任务进来时，不会缓存，而是直接被调度执行该任务，如果没有可用线程，则创建新线程，如果线程数量达到maxPoolSize，则执行拒绝策略。

④PriorityBlockingQueue

具有优先级的无界阻塞队列，优先级通过参数Comparator实现。

#### 六、threadFactory 线程工厂

创建一个新线程时使用的工厂，可以用来设定线程名、是否为daemon线程等等

#### 七、handler 拒绝策略

当工作队列中的任务已到达最大限制，并且线程池中的线程数量也达到最大限制，这时如果有新任务提交进来，该如何处理呢。这里的拒绝策略，就是解决这个问题的，jdk中提供了4中拒绝策略：

①CallerRunsPolicy

该策略下，在调用者线程中直接执行被拒绝任务的run方法，除非线程池已经shutdown，则直接抛弃任务。

②AbortPolicy

该策略下，直接丢弃任务，并抛出RejectedExecutionException异常。

③DiscardPolicy

该策略下，直接丢弃任务，什么都不做。

④DiscardOldestPolicy

该策略下，抛弃进入队列最早的那个任务，然后尝试把这次拒绝的任务放入队列


