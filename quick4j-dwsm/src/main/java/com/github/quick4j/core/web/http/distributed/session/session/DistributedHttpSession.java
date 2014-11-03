package com.github.quick4j.core.web.http.distributed.session.session;

import com.github.quick4j.core.web.http.distributed.session.SessionManager;
import com.github.quick4j.core.web.http.distributed.session.session.metadata.SessionMetaData;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionContext;
import java.util.Collections;

/**
 * @author zhaojh.
 */
public abstract class DistributedHttpSession implements HttpSession {
    private String id;
    private final long creationTime;
    private int maxInactiveInterval;
    private boolean newSession = false;
    private boolean persistent = false;
    private boolean validate = true;
    private ServletContext servletContext;

    protected SessionManager sessionManager;
    protected long lastAccessedTime;



    public DistributedHttpSession(String id,
                                  int maxInactiveInterval,
                                  SessionManager sessionManager,
                                  ServletContext servletContext) {
        this.id = id;
        this.creationTime = System.currentTimeMillis();
        this.lastAccessedTime = this.creationTime;
        this.maxInactiveInterval = maxInactiveInterval;
        this.newSession = true;
        this.sessionManager = sessionManager;
        this.servletContext = servletContext;
    }

    public DistributedHttpSession(String id,
                                  long creationTime,
                                  long lastAccessedTime,
                                  int maxInactiveInterval,
                                  SessionManager sessionManager,
                                  ServletContext servletContext) {
        this.id = id;
        this.creationTime = creationTime;
        this.lastAccessedTime = lastAccessedTime;
        this.maxInactiveInterval = maxInactiveInterval;
        this.persistent = true;
        this.sessionManager = sessionManager;
        this.servletContext = servletContext;
    }

    @Override
    public long getCreationTime() {
        return creationTime;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public long getLastAccessedTime() {
        return lastAccessedTime;
    }

    @Override
    public ServletContext getServletContext() {
        return servletContext;
    }

    @Override
    public void setMaxInactiveInterval(int interval) {
        maxInactiveInterval = interval;
    }

    @Override
    public int getMaxInactiveInterval() {
        return maxInactiveInterval;
    }

    @Override
    public HttpSessionContext getSessionContext() {
        return null;
    }

    @Override
    public Object getValue(String name) {
        return getAttribute(name);
    }

    @Override
    public String[] getValueNames() {
        return Collections.list(getAttributeNames()).toArray(new String[]{});
    }

    @Override
    public void putValue(String name, Object value) {
        setAttribute(name, value);
    }

    @Override
    public void removeValue(String name) {
        removeAttribute(name);
    }

    @Override
    public void invalidate() {
        sessionManager.removeHttpSession(this);
    }

    @Override
    public boolean isNew() {
        return newSession;
    }

    public boolean isValid(){
        if(maxInactiveInterval >0 && (lastAccessedTime + maxInactiveInterval * 1000) < System.currentTimeMillis()){
            validate = false;
        }
        return validate;
    }

    public void refresh(){
        reloadMetaData();
        reloadAttributes();
    }

    protected abstract void reloadAttributes();
    protected abstract void reloadMetaData();

    protected void access(){
        synchronized (this){
            if(!persistent || !sessionManager.getSessionStorage().isStored(this)){
                sessionManager.getSessionStorage().storeSessionMetaData(this);
                persistent = true;
            }

            newSession = false;
            updateLastAccessedTime();
        }
    }

    private void updateLastAccessedTime(){
        lastAccessedTime = System.currentTimeMillis();
        if(persistent){
            sessionManager.getSessionStorage().updateSessionMetaDataField(getId(), SessionMetaData.LAST_ACCESSED_TIME_KEY, lastAccessedTime);
        }
    }
}
