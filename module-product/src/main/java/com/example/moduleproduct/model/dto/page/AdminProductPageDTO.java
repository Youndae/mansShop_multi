package com.example.moduleproduct.model.dto.page;

import com.example.modulecommon.model.enumuration.PageAmount;

public record AdminProductPageDTO(
        String keyword,
        int page,
        int amount,
        long offset
) {

    public AdminProductPageDTO(String keyword, int page) {
        this(
                keyword == null ? null : "%" + keyword + "%",
                page,
                PageAmount.DEFAULT_AMOUNT.getAmount(),
                (long) (page - 1) * PageAmount.DEFAULT_AMOUNT.getAmount()
        );
    }

    public AdminProductPageDTO(int page) {
        this(
                null,
                page,
                PageAmount.DEFAULT_AMOUNT.getAmount(),
                (long) (page - 1) * PageAmount.DEFAULT_AMOUNT.getAmount()
        );
    }
}
