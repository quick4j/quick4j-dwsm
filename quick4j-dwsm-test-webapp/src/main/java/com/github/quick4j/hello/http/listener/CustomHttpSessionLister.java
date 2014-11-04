package com.github.quick4j.hello.http.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

/**
 * @author zhaojh.
 */
public class CustomHttpSessionLister implements HttpSessionListener {
    private static final Logger logger = LoggerFactory.getLogger(CustomHttpSessionLister.class);
    @Override
    public void sessionCreated(HttpSessionEvent sessionEvent) {
        logger.info("Session[{}]被创建。", sessionEvent.getSession().getId());
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent sessionEvent) {
        logger.info("Session[{}]即将失效。", sessionEvent.getSession().getId());
    }
}
