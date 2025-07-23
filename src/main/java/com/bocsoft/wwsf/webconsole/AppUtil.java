package com.bocsoft.wwsf.webconsole;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class AppUtil implements ApplicationContextAware {
    public static ApplicationContext applicationContext;

    public static void setAppContext(ApplicationContext applicationContext) {
        AppUtil.applicationContext = applicationContext;
    }

    public static <T> T getBean(String beanName,Class<T> requiredType) {
        return AppUtil.applicationContext.getBean(beanName, requiredType);
    }
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        AppUtil.setAppContext(applicationContext);
    }
}
