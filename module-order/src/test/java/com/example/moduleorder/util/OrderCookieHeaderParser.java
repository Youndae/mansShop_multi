package com.example.moduleorder.util;

import java.util.HashMap;
import java.util.Map;

public class OrderCookieHeaderParser {

    public static Map<String, String> parseSetCookieHeader(String setCookieHeader) {
        Map<String, String> cookieAttributes = new HashMap<>();

        String[] parts = setCookieHeader.split(";");

        for(String part : parts) {
            String[] keyValue = part.trim().split("=", 2);

            if(keyValue.length == 2)
                cookieAttributes.put(keyValue[0], keyValue[1]);
            else
                cookieAttributes.put(keyValue[0], null);
        }

        return cookieAttributes;
    }
}
