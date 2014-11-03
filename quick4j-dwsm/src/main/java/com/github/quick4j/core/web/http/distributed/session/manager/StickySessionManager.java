package com.github.quick4j.core.web.http.distributed.session.manager;

import com.github.quick4j.core.web.http.distributed.session.Configuration;
import com.github.quick4j.core.web.http.distributed.session.session.DistributedHttpSession;
import com.github.quick4j.core.web.http.distributed.session.session.StickyHttpSession;
import com.github.quick4j.core.web.http.distributed.session.session.metadata.SessionMetaData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;

/**
 * @author zhaojh.
 */
public class StickySessionManager extends AbstractSessionManager {

    public StickySessionManager(Configuration config) {
        super(config);
    }

    @Override
    protected HttpSession newHttpSession(String id, int maxInactiveInterval) {
        return new StickyHttpSession(id, maxInactiveInterval, this, getServletContext());
    }

    @Override
    protected HttpSession newHttpSession(SessionMetaData metaData) {
        HttpSession session = new StickyHttpSession(
                metaData.getId(),
                metaData.getCreationTime(),
                metaData.getLastAccessedTime(),
                metaData.getMaxInactiveInterval(),
                this,
                getServletContext()
        );

        ((DistributedHttpSession)session).refresh();
        return session;
    }
}
