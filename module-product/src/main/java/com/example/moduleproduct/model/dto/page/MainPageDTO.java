package com.example.moduleproduct.model.dto.page;

import com.example.modulecommon.model.enumuration.PageAmount;
import lombok.Builder;

public record MainPageDTO(
        int pageNum,
        int amount,
        String keyword,
        String classification
) {

    @Builder
    public MainPageDTO(int pageNum,
                       String keyword,
                       String classification) {
        this(
                pageNum,
                12,
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
