package com.github.quick4j.core.web.http.distributed.session.filter;

import com.github.quick4j.core.web.http.distributed.session.SessionManager;
import com.github.quick4j.core.web.http.distributed.session.helper.CookieHelper;
import com.github.quick4j.core.web.http.distributed.session.manager.NoStickySessionManager;
import com.github.quick4j.core.web.http.distributed.session.manager.StickySessionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private SessionManager sessionManager;
    private int maxInactiveInterval = 60 * 30;
    private String loadBalancingStrategy = "sticky";

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        String sessionTimeout = filterConfig.getInitParameter("sessionTimeout");
        //默认最大有效期30分钟
        try{
            maxInactiveInterval = 60 * Integer.parseInt(sessionTimeout);
        }catch (NumberFormatException e){}


        loadBalancingStrategy = filterConfig.getInitParameter("loadBalancingStrategy");
        if(loadBalancingStrategy.equalsIgnoreCase("sticky")){
            logger.info("应用负载均衡策略为： Sticky Session");
            sessionManager = new StickySessionManager(filterConfig.getServletContext(), maxInactiveInterval);
        }else{
            logger.info("应用负载均衡策略为： 非Sticky Session");
            sessionManager = new NoStickySessionManager(filterConfig.getServletContext(), maxInactiveInterval);
        }
        sessionManager.start();
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

                if(null == sessionid){
                    logger.info("当前request还未与Session关联.");
                }

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
    public void destroy() {
        sessionManager.stop();
    }
}
