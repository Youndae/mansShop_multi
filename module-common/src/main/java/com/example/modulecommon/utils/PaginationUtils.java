package com.example.modulecommon.utils;

import com.example.modulecommon.model.enumuration.PageAmount;

public class PaginationUtils {

    public static int getRequestPageValue(Integer page) {
        return page == null ? 1 : page;
    }

    public static long getOffsetOperation(int page, PageAmount amount) {
        return (long) (page - 1) * amount.getAmount();
    }

    public static void checkKeywordAndSearchTypeExist(String keyword, String searchType) {
        // keyword가 없는데 SearchType이 있거나
        // keyword는 있는데 SearchType이 없는 경우
        // 정상적인 요청이 아니므로 IllegalArgumentException으로 단순 BAD_REQUEST를 반환
        if((keyword == null && searchType != null)
                || (keyword != null && searchType == null)) {
            throw new IllegalArgumentException("keyword and searchType must both be null or both must exist. keyword=" + keyword + ", searchType=" + searchType);
        }
    }
}
