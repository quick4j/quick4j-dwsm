package com.github.quick4j.core.web.http.distributed.session;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.EventListener;
import java.util.List;

/**
 * @author zhaojh.
 */
public interface SessionManager extends LifeCycle {
    String DISTRIBUTED_SESSION_ID = "JSESSIONID";

    HttpSession newHttpSession(HttpServletRequest request);
    HttpSession getHttpSession(String id);
    boolean isValid(HttpSession session);
    void removeHttpSession(HttpSession session);
    SessionStorage getSessionStorage();
    void setServletContext(ServletContext servletContext);
    List<EventListener> getEventListeners();
}
