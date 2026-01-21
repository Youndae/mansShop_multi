package com.example.moduleproduct.model.dto.page;

import com.example.modulecommon.model.dto.request.ListRequestDTO;
import com.example.modulecommon.model.enumuration.PageAmount;
import com.example.modulecommon.utils.PaginationUtils;
import com.example.moduleproduct.model.dto.admin.review.enumuration.AdminReviewSearchType;

public record AdminReviewPageDTO(
        String keyword,
        String searchType,
        int page,
        int amount,
        long offset
) {

    private static final PageAmount pageAmount = PageAmount.DEFAULT_AMOUNT;

    public static AdminReviewPageDTO fromRequestDTO(ListRequestDTO requestDTO, String searchType) {
        int page = PaginationUtils.getRequestPageValue(requestDTO.page());
        long offset = PaginationUtils.getOffsetOperation(page, pageAmount);
        String searchTypeValue = searchType == null ? null : AdminReviewSearchType.from(searchType).value();

        return new AdminReviewPageDTO(
                requestDTO.keyword(),
                searchTypeValue,
                page,
                pageAmount.getAmount(),
                offset
        );
    }

    public AdminReviewPageDTO(String keyword, String searchType, int page) {
        this(
                keyword,
                searchType,
                page,
                pageAmount.getAmount(),
                PaginationUtils.getOffsetOperation(page, pageAmount)
        );
    }

    public AdminReviewPageDTO(int page) {
        this(
                null,
                null,
                page,
                pageAmount.getAmount(),
                PaginationUtils.getOffsetOperation(page, pageAmount)
        );
    }
}
