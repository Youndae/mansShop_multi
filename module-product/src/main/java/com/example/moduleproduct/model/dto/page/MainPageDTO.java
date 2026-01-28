package com.example.moduleproduct.model.dto.page;

import com.example.modulecommon.model.dto.request.ListRequestDTO;
import com.example.modulecommon.model.enumuration.PageAmount;
import com.example.modulecommon.utils.PaginationUtils;
import com.example.moduleproduct.model.dto.main.enumuration.SearchRequestDTO;

public record MainPageDTO(
        int pageNum,
        int amount,
        String keyword,
        String classification
) {

    private static final PageAmount pageAmount = PageAmount.MAIN_AMOUNT;

    public static MainPageDTO fromRequestDTO(SearchRequestDTO requestDTO, String classification) {
        int page = PaginationUtils.getRequestPageValue(requestDTO.page());

        return new MainPageDTO(
                page,
                pageAmount.getAmount(),
                requestDTO.keyword(),
                classification
        );
    }

    public MainPageDTO(int pageNum,
                       String keyword,
                       String classification) {
        this(
                pageNum,
                pageAmount.getAmount(),
                keyword,
                classification
        );
    }

    public MainPageDTO(String classification) {
        this(
                1,
                pageAmount.getAmount(),
                null,
                classification
        );
    }
}
