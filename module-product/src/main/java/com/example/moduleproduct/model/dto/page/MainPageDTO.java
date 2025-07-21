package com.example.moduleproduct.model.dto.page;

import com.example.modulecommon.model.enumuration.PageAmount;

public record MainPageDTO(
        int pageNum,
        int amount,
        String keyword,
        String classification
) {

    public MainPageDTO(int pageNum,
                       String keyword,
                       String classification) {
        this(
                pageNum,
                PageAmount.MAIN_AMOUNT.getAmount(),
                keyword,
                classification
        );
    }

    public MainPageDTO(String classification) {
        this(
                1,
                PageAmount.MAIN_AMOUNT.getAmount(),
                null,
                classification
        );
    }
}
