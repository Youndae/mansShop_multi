package com.example.moduleproduct.model.dto.page;

import com.example.modulecommon.model.enumuration.PageAmount;

public record AdminReviewPageDTO(
        String keyword,
        String searchType,
        int page,
        int amount,
        long offset
) {

    public AdminReviewPageDTO(String keyword, String searchType, int page) {
        this(
                keyword,
                searchType,
                page,
                PageAmount.DEFAULT_AMOUNT.getAmount(),
                (long) (page - 1) * PageAmount.DEFAULT_AMOUNT.getAmount()
        );
    }

    public AdminReviewPageDTO(int page) {
        this(
                null,
                null,
                page,
                PageAmount.DEFAULT_AMOUNT.getAmount(),
                (long) (page - 1) * PageAmount.DEFAULT_AMOUNT.getAmount()
        );
    }
}
