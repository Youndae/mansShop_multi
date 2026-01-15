package com.example.moduleproduct.model.dto.page;

import com.example.modulecommon.model.dto.request.ListRequestDTO;
import com.example.modulecommon.model.enumuration.PageAmount;
import com.example.modulecommon.utils.PaginationUtils;

public record AdminProductPageDTO(
        String keyword,
        int page,
        int amount,
        long offset
) {

    private static final PageAmount pageAmount = PageAmount.DEFAULT_AMOUNT;

    public static AdminProductPageDTO fromRequestDTO(ListRequestDTO requestDTO) {
        int page = PaginationUtils.getRequestPageValue(requestDTO.page());
        long offset = PaginationUtils.getOffsetOperation(page, pageAmount);

        return new AdminProductPageDTO(
                requestDTO.keyword() == null ? null : "%" + requestDTO.keyword() + "%",
                page,
                pageAmount.getAmount(),
                offset
        );
    }

    public AdminProductPageDTO(String keyword, int page) {
        this(
                keyword == null ? null : "%" + keyword + "%",
                page,
                pageAmount.getAmount(),
                PaginationUtils.getOffsetOperation(page, pageAmount)
        );
    }

    public AdminProductPageDTO(int page) {
        this(
                null,
                page,
                pageAmount.getAmount(),
                PaginationUtils.getOffsetOperation(page, pageAmount)
        );
    }
}
