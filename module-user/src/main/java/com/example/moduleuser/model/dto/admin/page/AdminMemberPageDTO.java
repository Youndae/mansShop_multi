package com.example.moduleuser.model.dto.admin.page;

import com.example.modulecommon.model.enumuration.PageAmount;

public record AdminMemberPageDTO(
        String keyword,
        String searchType,
        int page,
        int amount,
        long offset
) {

    public AdminMemberPageDTO(String keyword, String searchType, int page) {
        this(
                keyword,
                searchType,
                page,
                PageAmount.DEFAULT_AMOUNT.getAmount(),
                (long) (page - 1) * PageAmount.DEFAULT_AMOUNT.getAmount()
        );
    }

    public AdminMemberPageDTO(int page) {
        this(
                null,
                null,
                page,
                PageAmount.DEFAULT_AMOUNT.getAmount(),
                (long) (page - 1) * PageAmount.DEFAULT_AMOUNT.getAmount()
        );
    }
}
