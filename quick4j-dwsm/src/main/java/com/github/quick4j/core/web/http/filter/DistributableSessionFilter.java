package com.github.quick4j.core.web.http.filter;

import com.github.quick4j.core.web.http.distributed.session.Configuration;
import com.github.quick4j.core.web.http.distributed.session.SessionManager;
import com.github.quick4j.core.web.http.distributed.session.helper.CookieHelper;
import com.github.quick4j.core.web.http.distributed.session.manager.NoStickySessionManager;
import com.github.quick4j.core.web.http.distributed.session.manager.StickySessionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.ContextLoader;

import javax.annotation.Resource;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * @author zhaojh.
 */
public class DistributableSessionFilter implements Filter {
    private static Logger logger = LoggerFactory.getLogger(DistributableSessionFilter.class);
    private  SessionManager sessionManager;


    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        sessionManager = ContextLoader.getCurrentWebApplicationContext().getBean(SessionManager.class);
        sessionManager.setServletContext(filterConfig.getServletContext());
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        final HttpServletRequest httpRequest = (HttpServletRequest) request;
        final HttpServletResponse httpReponse = (HttpServletResponse) response;

        HttpServletRequest requestWrapper = new HttpServletRequestWrapper(httpRequest){
            private HttpSession session;

            @Override
            public HttpSession getSession(boolean create) {
                if(null != session) return session;

                logger.info("============================");
                String sessionid = getRequestedSessionId();

                if(sessionid != null){
                    logger.info("当前request与session[{}]关联.", sessionid);
                    session = sessionManager.getHttpSession(sessionid);

                    if(null == session){
                        logger.info("session[{}]已失效.", sessionid);
                    }

                    if(sessionManager.isValid(session)){
                        logger.info("session[{}]有效。", sessionid);
                        return session;
                    }else{
                        logger.info("session[{}]已失效。", sessionid);
                    }
                }

                logger.info("create: {}", create);
                if(!create){
                    logger.info("未要求创建新session. 返回null");
                    return null;
                }

                session = sessionManager.newHttpSession(this);
                logger.info("为当前request创建一个新session[{}].", session.getId());
                CookieHelper.writeSessionIdToCookie(session.getId(), httpRequest, httpReponse);
                return session;
            }

            @Override
            public HttpSession getSession() {
                return this.getSession(true);
            }

            @Override
            public String getRequestedSessionId() {
                String sessionid = httpRequest.getParameter(SessionManager.DISTRIBUTED_SESSION_ID);
                if(null == sessionid){
                    sessionid = CookieHelper.findSessionId(httpRequest);
                    if(null != sessionid){
                        logger.info("客户端Cookie中持有与当前request关联的session[{}]标识.", sessionid);
                    }
                }else{
                    logger.info("url中持有与当前request关联的session[{}]标识.", sessionid);
                }
                return sessionid;
            }
        };

        chain.doFilter(requestWrapper, httpReponse);
    }

    @Override
    public void destroy() {}
}
