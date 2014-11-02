package com.github.quick4j.hello.entity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionBindingListener;
import java.io.Serializable;

/**
 * @author zhaojh.
 */
public class Person implements HttpSessionBindingListener, Serializable {
    private static final Logger logger = LoggerFactory.getLogger(Person.class);
    private String name;

    public Person(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public void valueBound(HttpSessionBindingEvent event) {
        logger.info("Bean Person[{}] 被绑定到Session中.", name);
    }

    @Override
    public void valueUnbound(HttpSessionBindingEvent event) {
        logger.info("Bean Person[{}] 已被从Session中移除.", name);
    }
}
