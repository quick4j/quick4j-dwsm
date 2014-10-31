package com.github.quick4j.core.web.http.distributed.session.helper;

import com.github.quick4j.core.web.http.distributed.session.SessionManager;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author zhaojh.
 */
public abstract class CookieHelper {
    public static void writeSessionIdToCookie(String id,
                                              HttpServletRequest request,
                                              HttpServletResponse response){

        Cookie cookie = new Cookie(SessionManager.DISTRIBUTED_SESSION_ID, id);
        response.addCookie(cookie);
    }

    public static String findSessionId(HttpServletRequest request){
        return findCookieValue(SessionManager.DISTRIBUTED_SESSION_ID, request);
    }

    private static Cookie findCookie(String name, HttpServletRequest request){
        Cookie[] cookies = request.getCookies();
        if(null == cookies) return null;

        for (Cookie cookie : cookies){
            if (cookie.getName().equals(name))
                return cookie;
        }

        return null;
    }

    private static String findCookieValue(String name, HttpServletRequest request) {
        Cookie cookie = findCookie(name, request);
        if (cookie != null) {
            return cookie.getValue();
        }
        return null;
    }
}
