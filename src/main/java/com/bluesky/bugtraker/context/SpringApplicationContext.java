package com.bluesky.bugtraker.context;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class SpringApplicationContext implements ApplicationContextAware {
    private static ApplicationContext CONTEX;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        CONTEX = applicationContext;
    }

    public static Object getBean(String beanName){
        return CONTEX.getBean(beanName);
    }
}
