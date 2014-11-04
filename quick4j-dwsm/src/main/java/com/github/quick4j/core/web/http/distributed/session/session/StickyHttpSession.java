package com.github.quick4j.core.web.http.distributed.session.session;

import com.github.quick4j.core.web.http.distributed.session.SessionManager;
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
public class StickyHttpSession extends DistributedHttpSession {
    private static final Logger logger = LoggerFactory.getLogger(StickyHttpSession.class);
    private final Map<String, Object> attributes = new ConcurrentHashMap<String, Object>();

    public StickyHttpSession(String id,
                             int maxInactiveInterval,
                             SessionManager sessionManager,
                             ServletContext servletContext) {
        super(id, maxInactiveInterval, sessionManager, servletContext);
    }

    public StickyHttpSession(String id,
                             long creationTime,
                             long lastAccessedTime,
                             int maxInactiveInterval,
                             SessionManager sessionManager,
                             ServletContext servletContext) {
        super(id, creationTime, lastAccessedTime,
                maxInactiveInterval, sessionManager, servletContext);
    }

    @Override
    public Object getAttribute(String name) {
        access();
        return attributes.get(name);
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        access();
        return Collections.enumeration(attributes.keySet());
    }

    @Override
    public void setAttribute(String name, Object value) {
        access();

        if(null == value){
            removeAttribute(name);
            return;
        }

        HttpSessionBindingEvent event = null;

        if(value instanceof HttpSessionBindingListener){
            Object oldValue = attributes.get(name);
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

        Object unbound = attributes.put(name, value);
        sessionManager.getSessionStorage().storeSessionAttribute(getId(), name, value);

        if(null != unbound && unbound != value && unbound instanceof HttpSessionBindingListener){
            try{
                ((HttpSessionBindingListener)unbound).valueUnbound(new HttpSessionBindingEvent(this, name));
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
            if(unbound != null){
                if(null == event){
                    event = new HttpSessionBindingEvent(this, name, unbound);
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
        sessionManager.getSessionStorage().removeSessionAttribute(getId(), name);
        Object value = attributes.remove(name);

        if(null == value){
            return;
        }

        HttpSessionBindingEvent event = null;
        if(null != value && value instanceof HttpSessionBindingListener){
            event = new HttpSessionBindingEvent(this, name, value);
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

    /**
     * 注意：将Session Storage上的内容刷新到本地时，
     * 不再触发HttpSessionBindingListener的valueBound和valueUnbound事件。
     */
    @Override
    protected void reloadAttributes() {
        attributes.clear();
        attributes.putAll(sessionManager.getSessionStorage().getSessionAttributes(getId()));
    }

    @Override
    public void reloadMetaData() {
        //do nothing
    }
}
