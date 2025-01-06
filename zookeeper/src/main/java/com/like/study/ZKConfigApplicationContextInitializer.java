package com.like.study;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * @description: 项目启动获取zk配置信息
 * @author: like
 * @date: 2024-12-27 15:55
 */
public class ZKConfigApplicationContextInitializer implements ApplicationContextInitializer {

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        System.out.println("zk 初始化");
    }
}
