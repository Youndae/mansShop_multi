package com.example.moduleapi.utils;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.util.WebUtils;

@Component
public class OrderTokenUtils {

    public Cookie getOrderTokenCookie(HttpServletRequest request) {
        return WebUtils.getCookie(request, "order");
    }
}
