package com.example.moduleorder.model.dto.admin.page;

import com.example.modulecommon.model.enumuration.PageAmount;

public record AdminOrderPageDTO(
        String keyword,
        String searchType,
        int page,
        int amount,
        long offset
) {
    public AdminOrderPageDTO(String keyword, String searchType, int page) {
        this(
                keyword,
                searchType,
                page,
                PageAmount.DEFAULT_AMOUNT.getAmount(),
                (long) (page - 1) * PageAmount.DEFAULT_AMOUNT.getAmount()
        );
    }

    public AdminOrderPageDTO(int page) {
        this(
                null,
                null,
                page,
                PageAmount.DEFAULT_AMOUNT.getAmount(),
                (long) (page - 1) * PageAmount.DEFAULT_AMOUNT.getAmount()
        );
    }
}
