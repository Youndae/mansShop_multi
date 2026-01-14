package com.example.modulecommon.utils;

public class TestPaginationUtils {
    public static int getTotalPages(int listSize, int amount) {

        return (int) Math.ceil((double) listSize / amount);
    }
}
