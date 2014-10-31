package com.github.quick4j.core.web.http.distributed.session;

import javax.servlet.http.HttpServletRequest;

/**
 * @author zhaojh.
 */
public interface SessionIDManager extends LifeCycle {
    String newSessionId(HttpServletRequest request,long created);
}
