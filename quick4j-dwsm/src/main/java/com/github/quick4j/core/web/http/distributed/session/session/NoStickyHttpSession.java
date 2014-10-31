package com.github.quick4j.core.web.http.distributed.session.session;

import com.github.quick4j.core.web.http.distributed.session.SessionManager;
import com.github.quick4j.core.web.http.distributed.session.session.metadata.SessionMetaData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionBindingListener;
import java.util.Collections;
import java.util.Enumeration;

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
        return sessionManager.getSessionStorage().getSessionAttribute(getId(), name);
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        access();
        return Collections.enumeration(sessionManager.getSessionStorage().getSessionAttributeNames(getId()));
    }

    @Override
    public void setAttribute(String name, Object value) {
        access();

        Object oldValue = getAttribute(name);
        if(value instanceof HttpSessionBindingListener){
            if(value != oldValue){
                try{
                    ((HttpSessionBindingListener)value).valueBound(new HttpSessionBindingEvent(this, name, value));
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
    }

    @Override
    public void removeAttribute(String name) {
        access();
        Object oldValue = getAttribute(name);
        sessionManager.getSessionStorage().removeSessionAttribute(getId(), name);
        if(null != oldValue && oldValue instanceof HttpSessionBindingListener){
            try{
                ((HttpSessionBindingListener)oldValue).valueUnbound(new HttpSessionBindingEvent(this, name));
            }catch (Exception e){
                logger.error("bingEvent error:", e);
                throw new RuntimeException(e);
            }
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
