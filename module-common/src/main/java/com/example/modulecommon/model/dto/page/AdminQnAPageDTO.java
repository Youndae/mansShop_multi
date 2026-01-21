package com.example.modulecommon.model.dto.page;

import com.example.modulecommon.model.dto.request.ListRequestDTO;
import com.example.modulecommon.model.enumuration.PageAmount;
import com.example.modulecommon.utils.PaginationUtils;

public record AdminQnAPageDTO(
        String keyword,
        String listType,
        int page,
        int amount,
        long offset
) {

    private static final PageAmount pageAmount = PageAmount.DEFAULT_AMOUNT;

    public static AdminQnAPageDTO fromRequestDTO(ListRequestDTO requestDTO, String listType) {
        int page = PaginationUtils.getRequestPageValue(requestDTO.page());
        long offset = PaginationUtils.getOffsetOperation(page, pageAmount);

        return new AdminQnAPageDTO(
                requestDTO.keyword(),
                listType,
                page,
                pageAmount.getAmount(),
                offset
        );
    }

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
