package com.example.moduleorder.model.dto.page;

import com.example.modulecommon.model.enumuration.PageAmount;
import lombok.Builder;

public record OrderPageDTO(
        int pageNum,
        int amount,
        String term
) {

    private static final PageAmount pageAmount = PageAmount.DEFAULT_AMOUNT;

    @Builder
    public OrderPageDTO(int pageNum, String term) {

        this(
                pageNum,
                pageAmount.getAmount(),
                term
        );
    }
}