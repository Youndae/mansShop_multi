package com.example.moduleadmin.model.dto.page;

import com.example.modulecommon.model.enumuration.PageAmount;

public record AdminSalesPageDTO(
        String keyword,
        int page,
        int amount,
        long offset
) {

    public AdminSalesPageDTO(String keyword, int page) {
        this(
                keyword == null ? null : "%" + keyword + "%",
                page,
                PageAmount.DEFAULT_AMOUNT.getAmount(),
                (long) (page - 1) * PageAmount.DEFAULT_AMOUNT.getAmount()
        );
    }
}
