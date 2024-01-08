package com.itstep.coursework.services.utils;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;

public class RequestHelpers {
    public static Cookie getCookie(HttpServletRequest req, String cookieName) {
        Cookie[] cookies = req.getCookies();

        if(cookies == null) {
            return null;
        }

        for(Cookie cookie : cookies) {
            if(cookie.getName().equals(cookieName)) {
                return cookie;
            }
        }

        return null;
    }

    public static String getCookieValue(HttpServletRequest req, String cookieName) {
        Cookie cookie = getCookie(req, cookieName);

        if(cookie == null) {
            return null;
        }

        return cookie.getValue();
    }
}
