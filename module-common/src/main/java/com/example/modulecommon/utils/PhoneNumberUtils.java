package com.example.modulecommon.utils;

import java.util.regex.Pattern;

public class PhoneNumberUtils {

    private static final Pattern PHONE_PATTERN = Pattern.compile("(\\d{3})(\\d{3,4})(\\d{4})");

    public static String format(String phoneNumber) {
        if(phoneNumber == null)
            return null;

        return PHONE_PATTERN.matcher(phoneNumber).replaceAll("$1-$2-$3");
    }

    public static String unformat(String phoneNumber) {
        if(phoneNumber == null)
            return null;

        return phoneNumber.replaceAll("-", "");
    }
}
