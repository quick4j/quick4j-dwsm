package com.github.quick4j.core.web.http.distributed.session.manager;

import com.github.quick4j.core.web.http.distributed.session.Configuration;
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

    @Override
    protected HttpSession newHttpSession(String id, int maxInactiveInterval) {
        return new NoStickyHttpSession(id, maxInactiveInterval, this, getServletContext());
    }

    @Override
    protected HttpSession newHttpSession(SessionMetaData metaData) {
        return new NoStickyHttpSession(
                metaData.getId(),
                metaData.getCreationTime(),
                metaData.getLastAccessedTime(),
                metaData.getMaxInactiveInterval(),
                this,
                getServletContext()
        );
    }
}
