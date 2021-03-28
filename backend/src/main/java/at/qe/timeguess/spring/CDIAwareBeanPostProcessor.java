package at.qe.timeguess.spring;

import java.lang.reflect.Field;

import javax.enterprise.inject.spi.CDI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import at.qe.timeguess.ui.websockets.WebSocketManager;
import at.qe.timeguess.utils.CDIAutowired;
import at.qe.timeguess.utils.CDIContextRelated;

/**
 * This beanPostProcessor is used to manually "autowire" CDI-managed beans (see
 * {@link WebSocketManager}) within the spring-context. This happens after a
 * beans' initialization is finished.
 *
 * This class is part of the skeleton project provided for students of the
 * courses "Software Architecture" and "Software Engineering" offered by the
 * University of Innsbruck.
 *
 */
@Component
/*
 * CDI-injection for pushContext does not makes sense in test-mode (presumably
 * highly interlinked with the FacesContext which is not available in test-mode)
 * => only perform the custom CDI-injection in production-mode (where the
 * FacesContext is available)
 */
@Profile("!test")
public class CDIAwareBeanPostProcessor implements BeanPostProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(CDIAwareBeanPostProcessor.class);

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        // only if controller has a webSocketManager
        Class<? extends Object> beanClass = bean.getClass();
        /*
         * proceed only if bean uses websockets (i.e. in our case cdi-managed
         * webSocket-infrastructure)
         */
        if (beanClass.isAnnotationPresent(CDIContextRelated.class)) {
            // check for @CDIAutowired on fields to find the websocket-managing field
            for (Field field : beanClass.getDeclaredFields()) {
                field.setAccessible(true);
                // when annotation is present, perform a manual "autowiring"
                if (field.isAnnotationPresent(CDIAutowired.class)) {
                    Class<?> fieldType = field.getType();
                    Object cdiManagedBean = CDI.current().select(fieldType).get();
                    LOGGER.info("Field '{}' of '{}' successfully autowired", field.getName(), bean.getClass());
                    try {
                        field.set(bean, cdiManagedBean);
                    } catch (IllegalArgumentException | IllegalAccessException e) {
                        LOGGER.error("Manual CDI-injection failed", e);
                    }
                }
            }
        }
        // simply returns bean
        return BeanPostProcessor.super.postProcessAfterInitialization(bean, beanName);
    }

}
