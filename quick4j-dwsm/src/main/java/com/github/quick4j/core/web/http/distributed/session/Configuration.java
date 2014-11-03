package com.github.quick4j.core.web.http.distributed.session;

import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import java.util.Enumeration;
import java.util.Properties;

/**
 * @author zhaojh.
 */
public class Configuration {
    private Properties properties;
    private ServletContext servletContext;

    public Configuration(FilterConfig filterConfig) {
        servletContext = filterConfig.getServletContext();

        Enumeration<String> parameterNames = filterConfig.getInitParameterNames();
        properties = new Properties();
        while (parameterNames.hasMoreElements()){
            String parameterName = parameterNames.nextElement();
            properties.put(parameterName, filterConfig.getInitParameter(parameterName));
        }
    }

    public String getProperty(String key, String defaultValue){
        return properties.getProperty(key, defaultValue);
    }

    public String getProperty(String key){
        return properties.getProperty(key);
    }

    public ServletContext getServletContext() {
        return servletContext;
    }
}
