package com.github.quick4j.hello.http.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpSessionAttributeListener;
import javax.servlet.http.HttpSessionBindingEvent;

/**
 * @author zhaojh.
 */
public class CustomHttpSessionAttributeListener implements HttpSessionAttributeListener {
    private static final Logger logger = LoggerFactory.getLogger(CustomHttpSessionAttributeListener.class);

    @Override
    public void attributeAdded(HttpSessionBindingEvent event) {
        logger.info("Session[{}]增加了一个新的属性[{}],其值为[{}].", event.getSession().getId(), event.getName(), event.getValue());
    }

    @Override
    public void attributeRemoved(HttpSessionBindingEvent event) {
        logger.info("Session[{}]的[{}]属性被移除.", event.getSession().getId(), event.getName());
    }

    @Override
    public void attributeReplaced(HttpSessionBindingEvent event) {
        logger.info("Session[{}]的[{}]属性值被替换为了[{}].", event.getSession().getId(), event.getName(), event.getValue());
    }
}
