package com.github.quick4j.core.web.http.distributed.session.session;

import com.github.quick4j.core.web.http.distributed.session.SessionManager;
import com.github.quick4j.core.web.http.distributed.session.session.metadata.SessionMetaData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSessionAttributeListener;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionBindingListener;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author zhaojh.
 */
public class NoStickyHttpSession extends DistributedHttpSession {
    private static final Logger logger = LoggerFactory.getLogger(NoStickyHttpSession.class);

    public NoStickyHttpSession(String id,
                               int maxInactiveInterval,
                               SessionManager sessionManager,
                               ServletContext servletContext) {
        super(id, maxInactiveInterval, sessionManager, servletContext);
    }

    public NoStickyHttpSession(String id,
                               long creationTime,
                               long lastAccessedTime,
                               int maxInactiveInterval,
                               SessionManager sessionManager,
                               ServletContext servletContext) {
        super(id, creationTime, lastAccessedTime, maxInactiveInterval, sessionManager, servletContext);
    }

    @Override
    public Object getAttribute(String name) {
        access();
        Object value = sessionManager.getSessionStorage().getSessionAttribute(getId(), name);
        return value;
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        access();
        return Collections.enumeration(sessionManager.getSessionStorage().getSessionAttributeNames(getId()));
    }

    @Override
    public void setAttribute(String name, Object value) {
        access();

        if(null == value){
            removeAttribute(name);
            return;
        }

        HttpSessionBindingEvent event = null;

        Object oldValue = getAttribute(name);
        if(value instanceof HttpSessionBindingListener){
            if(value != oldValue){
                event = new HttpSessionBindingEvent(this, name, value);
                try{
                    ((HttpSessionBindingListener)value).valueBound(event);
                }catch (Exception e){
                    logger.error("bingEvent error:", e);
                    throw new RuntimeException(e);
                }
            }
        }

        sessionManager.getSessionStorage().storeSessionAttribute(getId(), name, value);

        if(null != oldValue && oldValue != value && oldValue instanceof HttpSessionBindingListener){
            try{
                ((HttpSessionBindingListener)oldValue).valueUnbound(new HttpSessionBindingEvent(this, name));
            }catch (Exception e){
                logger.error("bingEvent error:", e);
                throw new RuntimeException(e);
            }
        }

        List<EventListener> listeners = sessionManager.getEventListeners();
        for(EventListener eventListener : listeners){
            if(!(eventListener instanceof HttpSessionAttributeListener)){
                continue;
            }

            HttpSessionAttributeListener listener = (HttpSessionAttributeListener) eventListener;
            if(null != oldValue){
                if(null == event){
                    event = new HttpSessionBindingEvent(this, name, oldValue);
                }
                listener.attributeReplaced(event);
            }else{
                if(null == event){
                    event = new HttpSessionBindingEvent(this, name, value);
                }
                listener.attributeAdded(event);
            }
        }
    }

    @Override
    public void removeAttribute(String name) {
        access();
        Object value = getAttribute(name);

        if(null == value){
            return;
        }

        HttpSessionBindingEvent event = null;
        sessionManager.getSessionStorage().removeSessionAttribute(getId(), name);
        if(null != value && value instanceof HttpSessionBindingListener){
            event = new HttpSessionBindingEvent(this, name);
            try{
                ((HttpSessionBindingListener)value).valueUnbound(event);
            }catch (Exception e){
                logger.error("bingEvent error:", e);
                throw new RuntimeException(e);
            }
        }


        List<EventListener> listeners = sessionManager.getEventListeners();
        for(EventListener eventListener : listeners){
            if(!(eventListener instanceof HttpSessionAttributeListener)){
                continue;
            }

            HttpSessionAttributeListener listener = (HttpSessionAttributeListener) eventListener;
            if(event == null){
                event = new HttpSessionBindingEvent(this, name, value);
            }
            listener.attributeRemoved(event);
        }
    }

    @Override
    protected void reloadAttributes() {
        //do nothing
    }

    @Override
    protected void reloadMetaData() {
        SessionMetaData metaData = sessionManager.getSessionStorage().getSessionMetaData(getId());
        lastAccessedTime = metaData.getLastAccessedTime();
    }
}
