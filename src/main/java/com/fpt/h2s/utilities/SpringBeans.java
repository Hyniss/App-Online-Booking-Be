package com.fpt.h2s.utilities;

import com.fpt.h2s.models.exceptions.ApiException;
import lombok.NonNull;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.ResolvableType;
import org.springframework.stereotype.Component;

import java.lang.reflect.Array;
import java.util.Map;

@Component
public class SpringBeans implements ApplicationContextAware {
    
    private static ConfigurableApplicationContext context;
    
    
    @Override
    public void setApplicationContext(@NonNull final ApplicationContext applicationContext) throws BeansException {
        SpringBeans.context = (ConfigurableApplicationContext) applicationContext;
    }
    
    /**
     * Get application bean based on its class.
     *
     * @param beanClass class of the bean that you want.
     * @return bean that has input class.
     */
    public static <T> T getBean(@NonNull final Class<T> beanClass) {
        return context.getBean(beanClass);
    }
    
    /**
     * Get application bean using its generic type.
     *
     * @param resolvableType type with generic.
     * @return bean that has the input generic type.
     */
    public static Object getBean(@NonNull final ResolvableType resolvableType) {
        final String[] foundBeanNames = BeanFactoryUtils.beanNamesForTypeIncludingAncestors(context, resolvableType);
        if (foundBeanNames.length == 0) {
            throw ApiException.failed("No bean found for type: {}", resolvableType);
        }
        if (foundBeanNames.length > 1) {
            throw ApiException.failed("Found many beans with same type {}: {}", resolvableType, foundBeanNames);
        }
        return context.getBean(foundBeanNames[0]);
    }
    
    /**
     * Get array of beans based on their class
     *
     * @param beanClass class of the bean that you want.
     * @return array of beans that have input class.
     */
    public static <T> T getArrayOfBean(@NonNull final Class<T> beanClass) {
        final Map<String, ?> beans = context.getBeansOfType(beanClass);
        final T[] array = (T[]) Array.newInstance(beanClass, beans.size());
        return (T) beans.values().toArray(array);
    }
    
    /**
     * Register new bean after application started successfully.
     *
     * @param beanName identification of the newly created bean. Must be not null.
     * @param bean     bean object that you want to register. Must be not null.
     */
    public static void registerBean(@NonNull final String beanName, @NonNull final Object bean) {
        final ConfigurableListableBeanFactory factory = context.getBeanFactory();
        final Object proxyBean = factory.initializeBean(bean, beanName);
        factory.registerSingleton(beanName, proxyBean);
    }
    
    /**
     * Register new bean after application started successfully.
     * @param bean bean object that you want to register. Must be not null.
     */
    public static void registerBean(@NonNull final Object bean) {
        final String beanName = bean.getClass().getSimpleName() + bean;
        registerBean(beanName, bean);
    }
}