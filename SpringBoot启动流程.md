## **三、SpringBoot启动流程分析**

//1.创建SpringApplication对象
new SpringApplication(primarySources)
//1.1获取当前启动类型原理：判断当前classpath是否有加载我们的servlet类，返回启动方式，webApplicationType分为三种启动类型：REACTIVE，NONE，SERVLET，默认SERVLET类型启动：嵌入web server服务器启动
this.webApplicationType = WebApplicationType.deduceFromClasspath();
//1.2读取springboot包下的META-INF.spring.factories下的ApplicationContextInitializer装配到集合
this.setInitializers(this.getSpringFactoriesInstances(ApplicationContextInitializer.class));
//读取springboot包下的META-INF.spring.factories下的ApplicationListener装配到
this.setListeners(this.getSpringFactoriesInstances(ApplicationListener.class));
//2.调用SpringApplication run 实现启动同时返回当前容器的上下文
(new SpringApplication(primarySources)).run(args)
//3.记录springboot启动时间
StopWatch stopWatch = new StopWatch();
//4.读取META-INF/spring.factories下的ApplicationListener装配到集合
SpringApplicationRunListeners listeners = this.getRunListeners(args)
//5.循环调用监听starting方法(监听器初始化操作，做一些回调方法)
listeners.starting();
//6.对参数进赋值
ConfigurableEnvironment environment = this.prepareEnvironment(listeners, applicationArguments);
//6.1读取配置文件到我们的springboot容器中
listeners.environmentPrepared((ConfigurableEnvironment)environment)
//6.1.1
this.initialMulticaster.multicastEvent(new ApplicationEnvironmentPreparedEvent(this.application, this.args, environment));
//6.1.2
this.multicastEvent(event, this.resolveDefaultEventType(event));
//6.1.3
this.invokeListener(listener, event);
//6.1.4
this.doInvokeListener(listener, event);
//6.1.5
listener.onApplicationEvent(event);
this.addPropertySources(environment, application.getResourceLoader());
//7.读取到配置文件内容，放入springboot容器中
protected void addPropertySources(ConfigurableEnvironment environment, ResourceLoader resourceLoader) {
        RandomValuePropertySource.addToEnvironment(environment);
        (new ConfigFileApplicationListener.Loader(environment, resourceLoader)).load();
    }
this.load((ConfigFileApplicationListener.Profile)null, this::getNegativeProfileFilter, this.addToLoaded(MutablePropertySources::addFirst, true));
names.forEach((name) -> {    this.load(location, name, profile, filterFactory, consumer);});
this.load(loader, location, profile, filterFactory.getDocumentFilter(profile), consumer);
locations.addAll(this.asResolvedSet(ConfigFileApplicationListener.this.searchLocations, "classpath:/,classpath:/config/,file:./,file:./config/"));
//8.打印banner图
Banner printedBanner = this.printBanner(environment);
//9.创建SpringBoot上下文AnnotationConfigServletWebServerApplicationContext
context = this.createApplicationContext();
case SERVLET:contextClass = Class.forName("org.springframework.boot.web.servlet.context.AnnotationConfigServletWebServerApplicationContext");
this.refreshContext(context);
  ((AbstractApplicationContext)applicationContext).refresh();
//10.走spring的刷新方法
 public void refresh() throws BeansException, IllegalStateException {
        synchronized(this.startupShutdownMonitor) {
            this.prepareRefresh();
            ConfigurableListableBeanFactory beanFactory = this.obtainFreshBeanFactory();
            this.prepareBeanFactory(beanFactory);

```java
        try {
            this.postProcessBeanFactory(beanFactory);
            this.invokeBeanFactoryPostProcessors(beanFactory);
            this.registerBeanPostProcessors(beanFactory);
            this.initMessageSource();
            this.initApplicationEventMulticaster();
            this.onRefresh();//加载tomcat
            this.registerListeners();
            this.finishBeanFactoryInitialization(beanFactory);
            this.finishRefresh();
        } catch (BeansException var9) {
            if (this.logger.isWarnEnabled()) {
                this.logger.warn("Exception encountered during context initialization - cancelling refresh attempt: " + var9);
            }

            this.destroyBeans();
            this.cancelRefresh(var9);
            throw var9;
        } finally {
            this.resetCommonCaches();
        }

    }
}
```
//11.开始创建web server服务器
//12.加载springmvc
//13.空方法回调
this.afterRefresh(context, applicationArguments);
//14.开始使用广播和回调机制通知监听器SpringBoot容器启动成功
listeners.started(context);
//15.开始使用广播和回调机制开始运行项目
listeners.running(context);
//16.返回当前上下文
return context;