package com.example.moduleorder.service;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

@Service
public class OrderCookieWriter {
    public void deleteOrderTokenCookie(HttpServletResponse response) {
        String responseCookie = createOrderTokenCookie("", Duration.ZERO);
        setCookieFromResponseHeader(responseCookie, response);
    }

    public String createAndSetOrderTokenCookie(HttpServletResponse response) {
        String orderToken = createOrderToken();
        String responseCookie = createOrderTokenCookie(orderToken, Duration.ofMinutes(10));
        setCookieFromResponseHeader(responseCookie, response);

        return orderToken;
    }

    private void setCookieFromResponseHeader(String responseCookie, HttpServletResponse response) {
        response.addHeader("Set-Cookie", responseCookie);
    }

    private String createOrderToken() {
        return UUID.randomUUID().toString();
    }

    private String createOrderTokenCookie(String value, Duration maxAge) {
        return ResponseCookie
                .from("order", value)
                .path("/")
                .maxAge(maxAge)
                .secure(true)
                .httpOnly(true)
                .sameSite("Strict")
                .build()
                .toString();
    }
}
