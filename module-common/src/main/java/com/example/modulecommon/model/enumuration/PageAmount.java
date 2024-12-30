package com.example.modulecommon.model.enumuration;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PageAmount {
    MAIN_AMOUNT(12)
    , DEFAULT_AMOUNT(20)
    , PRODUCT_REVIEW_AMOUNT(10)
    , PRODUCT_QNA_AMOUNT(10);

    private final int amount;
}
