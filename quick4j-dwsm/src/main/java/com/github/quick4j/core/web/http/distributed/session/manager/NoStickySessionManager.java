package com.github.quick4j.core.web.http.distributed.session.manager;

import com.github.quick4j.core.web.http.distributed.session.session.NoStickyHttpSession;
import com.github.quick4j.core.web.http.distributed.session.session.metadata.SessionMetaData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;

/**
 * @author zhaojh.
 */
public class NoStickySessionManager extends AbstractSessionManager {
    private static final Logger logger = LoggerFactory.getLogger(NoStickySessionManager.class);

    public NoStickySessionManager(ServletContext servletContext, int maxInactiveInterval) {
        super(servletContext, maxInactiveInterval);
    }

    @Override
    protected HttpSession newHttpSession(String id, int maxInactiveInterval, ServletContext servletContext) {
        return new NoStickyHttpSession(id, maxInactiveInterval, this, servletContext);
    }

    @Override
    protected HttpSession findHttpSession(String id) {
        //local
        HttpSession session = findSessionFromLocal(id);
        if(null != session){
            return session;
        }else{
            logger.info("本地没有session[{}]的信息。", id);
        }

        SessionMetaData metaData = getSessionMetaDataFromStorage(id);
        if(null != metaData){
            logger.debug("Session Storage中持有session[{}]的信息.", id);
            session = new NoStickyHttpSession(
                    id,
                    metaData.getCreationTime(),
                    metaData.getLastAccessedTime(),
                    metaData.getMaxInactiveInterval(),
                    this,
                    getServletContext()
            );
        }
        return session;
    }
}
