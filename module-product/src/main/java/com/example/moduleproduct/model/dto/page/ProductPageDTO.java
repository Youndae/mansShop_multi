package com.example.moduleproduct.model.dto.page;

import com.example.modulecommon.model.enumuration.PageAmount;
import lombok.Builder;

public record ProductPageDTO(
        int pageNum,
        int mainProductAmount,
        String keyword,
        String classification
) {

    @Builder
    public ProductPageDTO(int pageNum,
                          String keyword,
                          String classification) {
        this(
                pageNum,
                PageAmount.MAIN_AMOUNT.getAmount(),
                keyword,
                classification
        );
    }
}
