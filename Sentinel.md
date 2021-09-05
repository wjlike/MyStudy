## 简介

### 官网

[https://github.com/alibaba/Sentinel/wiki/%E4%B8%BB%E9%A1%B5](https://github.com/alibaba/Sentinel/wiki/主页)

### 与Hystrix区别

###### Hystrix

1. 需要自己搭建监控平台。
2. 没有一套web界面可以进行更加细粒度的配置，流控，速率控制，服务熔断，服务降级。

###### Sentinel

1. 单独一个组件，可以独立出来
2. 页面化的细粒度统一配置

### 作用：

1. 流量控制
2. 熔断降级
3. 系统自适应保护

# Sentinel 控制台

### 组件由两部分组成

1. 核心库，jar包，不依赖任何框架，能够运行于所有Java运行的环境。
2. 控制台，基于springboot开发，打包后直接运行，不需要额外的tomcat。

### 安装

1. https://github.com/alibaba/Sentinel/releases 选择sentinel-dashboard-1.7.2.jar
2. 命令行切换到jar包目录
3. `java -jar sentinel-dashboard-1.7.2.jar`
4. http://localhost:8080/
5. 账号密码 sentinel

# 使用

### 建模块cloud-alibaba-sentinel-service8401

1. pom

```
<!-- 后续做持久化用到 -->
<dependency>
    <groupId>com.alibaba.csp</groupId>
    <artifactId>sentinel-datasource-nacos</artifactId>
</dependency>
<dependency>
    <groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-starter-alibaba-sentinel</artifactId>
</dependency>
```

1. yml

```
server:
  port: 8401

spring:
  application:
    name: cloud-alibaba-sentinel-service
  cloud:
    nacos:
      discovery:
        # 服务注册中心地址
        server-addr: localhost:8848
    sentinel:
      transport:
        # 配置sentinel dashboard地址
        dashboard: localhost:8080
        # 默认 8719端口，假如被占用从8719开始+1扫描直到直到未被占用的端口
        port: 8719

management:
  endpoints:
    web:
      exposure:
        include: '*'
```

1. main @SpringBootApplication @EnableDiscoveryClient
2. controller

```
    @GetMapping("/testA")
    public String testA(){
        return "testA";
    }

    @GetMapping("/testB")
    public String testB(){
        return "testB";
    }
```

1. 启动 nacos，sentinel，启动模块
2. 访问模块，观察 sentinel里变化



# 流控规则

### 介绍

1. 资源名：唯一名称，默认请求路径
2. 针对来源：Sentinel可以针对调用者进行限流 ，填写微服务名，默认 default
3. 阈值类型
   - QPS（每秒请求数量）：当调用api的QPS达到阈值后进行限流
   - 线程数：调用该api的线程数达到阈值后进行限流
4. 是否集群：不需要集群
5. 流控模式：
   - 直接：api达到限流条件时直接限流
   - 关联：当关联的资源达到阈值时就限流自己
   - 链路：只记录指定链路上的流量（指定资源从入口资源进来的流量，如果达到阈值就进行限流）
6. 流控效果
   - 快速失败：直接失败，抛异常
   - Warm Up：根据codeFactor（冷加热因子，默认3）的值，从阈值 codeFactor，经过预热时长，才达到设定的QPS阈值。

### 流控模式

###### 直接

1. 流控设置QPS为1，然后访问 testA 接口 观察效果
2. 达到阈值快速失败
3. 结果：testA 只能一秒访问一次，其他时候 快速失败

###### 关联

1. testA  关联 testB

2. B达到阈值，限流A

3. 结果：并发访问 testB， 达到阈值后，访问testA 也快速失败

   

### 流控效果

 ##### 直接快速失败（默认） 

 ##### 预热 

1. 初始QPS = QPS/3
2. 阈值从初始QPS主键增加到 QPS
3. 访问 testB 观察随时间变化错误的数量变化

##### 排队等待 

# 熔断降级

### 基本介绍

熔断降级会在调用链路中某个资源出现不稳定状态时（例如调用超时或异常比例升高），对这个资源的调用进行限制，让请求快速失败，避免影响到其它的资源而导致级联错误。 ==没有半开状态==

### 触发降级的标准

1. 平均响应时间 (DEGRADE_GRADE_RT)：当 1s 内持续进入 N 个请求，对应时刻的平均响应时间（秒级）均超过阈值（count，以 ms 为单位），那么在接下的时间窗口（DegradeRule 中的 timeWindow，以 s 为单位）之内，对这个方法的调用都会自动地熔断（抛出 DegradeException）。注意 Sentinel 默认统计的 RT 上限是 4900 ms，超出此阈值的都会算作 4900 ms，若需要变更此上限可以通过启动配置项 -Dcsp.sentinel.statistic.max.rt=xxx 来配置。
2. 异常比例 (DEGRADE_GRADE_EXCEPTION_RATIO)：当资源的每秒请求量 >= N（可配置），并且每秒异常总数占通过量的比值超过阈值（DegradeRule 中的 count）之后，资源进入降级状态，即在接下的时间窗口（DegradeRule 中的 timeWindow，以 s 为单位）之内，对这个方法的调用都会自动地返回。异常比率的阈值范围是 [0.0, 1.0]，代表 0% - 100%。
3. 异常数 (DEGRADE_GRADE_EXCEPTION_COUNT)：当资源近 1 分钟的异常数目超过阈值之后会进行熔断。注意由于统计时间窗口是分钟级别的，若 timeWindow 小于 60s，则结束熔断状态后仍可能再进入熔断状态