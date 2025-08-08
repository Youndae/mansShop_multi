package com.example.modulecommon.model.dto.page;

import com.example.modulecommon.model.enumuration.PageAmount;

public record AdminQnAPageDTO(
        String keyword,
        String listType,
        int page,
        int amount,
        long offset
) {

    public AdminQnAPageDTO(String keyword, String listType, int page) {
        this(
                keyword,
                listType,
                page,
                PageAmount.DEFAULT_AMOUNT.getAmount(),
                (long) (page - 1) * PageAmount.DEFAULT_AMOUNT.getAmount()
        );
    }

    public AdminQnAPageDTO(String listType, int page) {
        this(
                null,
                listType,
                page,
                PageAmount.DEFAULT_AMOUNT.getAmount(),
                (long) (page - 1) * PageAmount.DEFAULT_AMOUNT.getAmount()
        );
    }
}
