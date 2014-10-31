package com.github.quick4j.core.web.http.distributed.session.manager;

import com.github.quick4j.core.web.http.distributed.session.SessionIDManager;
import com.github.quick4j.core.web.http.distributed.session.SessionManager;
import com.github.quick4j.core.web.http.distributed.session.SessionStorage;
import com.github.quick4j.core.web.http.distributed.session.manager.task.ClearInvalidSessionScheduledTask;
import com.github.quick4j.core.web.http.distributed.session.session.DistributedHttpSession;
import com.github.quick4j.core.web.http.distributed.session.session.metadata.SessionMetaData;
import com.github.quick4j.core.web.http.distributed.session.storage.RedisSessionStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author zhaojh.
 */
public abstract class AbstractSessionManager implements SessionManager{
    private final Logger logger = LoggerFactory.getLogger(AbstractSessionManager.class);

    private int maxInactiveInterval;
    private ServletContext servletContext;

    private Map<String, HttpSession> localSessionContainer = new ConcurrentHashMap<String, HttpSession>();
    private SessionStorage sessionStorage;
    private SessionIDManager sessionIdManager;
    private ScheduledExecutorService scheduledExecutorService;


    public AbstractSessionManager(ServletContext servletContext, int maxInactiveInterval) {
        this.servletContext = servletContext;
        this.maxInactiveInterval = maxInactiveInterval;
    }

    protected abstract HttpSession newHttpSession(String id, int maxInactiveInterval, ServletContext servletContext);
    protected abstract HttpSession findHttpSession(String id);

    @Override
    public HttpSession newHttpSession(HttpServletRequest request) {
        String id = newSessionId(request);
        HttpSession session = newHttpSession(id, maxInactiveInterval, servletContext);
        addSessionToLocal(session);
        return session;
    }

    @Override
    public HttpSession getHttpSession(String id) {
        HttpSession session = findHttpSession(id);
        addSessionToLocal(session);
        return session;
    }

    @Override
    public void removeHttpSession(HttpSession session) {
        removeSessionFromStorage(session);
        removeSessionFromLocal(session);
    }

    @Override
    public boolean isValid(HttpSession session) {
        if(null == session) return false;

        return ((DistributedHttpSession)session).isValid();
    }

    @Override
    public void start() {
        logger.info("启动Session Manager.");
        sessionIdManager = new DefaultSessionIDManager();
        sessionIdManager.start();

        sessionStorage = new RedisSessionStorage();
        sessionStorage.start();

        scheduledExecutorService = Executors.newScheduledThreadPool(1);
        scheduledExecutorService.scheduleWithFixedDelay(
                new ClearInvalidSessionScheduledTask(localSessionContainer),
                10, 10, TimeUnit.SECONDS);
    }

    @Override
    public void stop() {
        scheduledExecutorService.shutdown();
        sessionIdManager.stop();
        sessionStorage.stop();
    }

    @Override
    public SessionStorage getSessionStorage() {
        return sessionStorage;
    }

    protected ServletContext getServletContext(){
        return servletContext;
    }

    protected boolean isStaleSession(HttpSession session){
        Object lastAccessedTime = session.getLastAccessedTime();
        if(sessionStorage.isStored(session)){
            lastAccessedTime = sessionStorage.getSessionMetaDataField(session.getId(), SessionMetaData.LAST_ACCESSED_TIME_KEY);
        }
        return  session.getLastAccessedTime() != Long.valueOf(String.valueOf(lastAccessedTime)).longValue();
    }

    protected SessionMetaData getSessionMetaDataFromStorage(String id){
        return sessionStorage.getSessionMetaData(id);
    }

    protected HttpSession findSessionFromLocal(String id){
        HttpSession session = localSessionContainer.get(id);
        if(null != session){
            logger.info("本地持有session[{}]的信息.", id);
            logger.debug("检查是否需要刷新session.");
            if(isStaleSession(session)){
                logger.debug("更新本地session内容与Session Storage中内容一致.");
                ((DistributedHttpSession)session).refresh();
            }else{
                logger.debug("无需刷新.");
            }
        }
        return session;
    }

    private String newSessionId(HttpServletRequest request){
        return sessionIdManager.newSessionId(request, System.currentTimeMillis());
    }

    private void addSessionToLocal(HttpSession session){
        if (null == session) return;
        localSessionContainer.put(session.getId(), session);
    }

    private void removeSessionFromLocal(HttpSession session){
        localSessionContainer.remove(session.getId());
    }

    private void removeSessionFromStorage(HttpSession session){
        sessionStorage.removeSession(session.getId());
    }
}
