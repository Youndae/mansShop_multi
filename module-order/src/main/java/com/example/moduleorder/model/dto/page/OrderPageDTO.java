package com.example.moduleorder.model.dto.page;

import lombok.Builder;

public record OrderPageDTO(
        int pageNum,
        int orderAmount,
        String term
) {

    @Builder
    public OrderPageDTO(int pageNum, String term) {

        this(pageNum, 20, term);
    }
}