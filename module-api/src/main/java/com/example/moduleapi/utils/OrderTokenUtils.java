package com.example.moduleapi.utils;

import com.example.moduleconfig.properties.TokenProperties;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.util.WebUtils;

@Component
@RequiredArgsConstructor
public class OrderTokenUtils {

    private final TokenProperties tokenProperties;

    public Cookie getOrderTokenCookie(HttpServletRequest request) {
        return WebUtils.getCookie(request, tokenProperties.getOrder().getHeader());
    }
}
