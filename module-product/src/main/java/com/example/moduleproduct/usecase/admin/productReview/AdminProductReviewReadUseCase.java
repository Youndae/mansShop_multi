package com.example.moduleproduct.usecase.admin.productReview;

import com.example.modulecommon.model.dto.response.PagingListDTO;
import com.example.modulecommon.model.dto.response.PagingMappingDTO;
import com.example.modulecommon.model.enumuration.AdminListType;
import com.example.moduleproduct.model.dto.admin.review.out.AdminReviewDTO;
import com.example.moduleproduct.model.dto.admin.review.out.AdminReviewDetailDTO;
import com.example.moduleproduct.model.dto.page.AdminReviewPageDTO;
import com.example.moduleproduct.service.productReview.ProductReviewDataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminProductReviewReadUseCase {

    private final ProductReviewDataService productReviewDataService;

    public PagingListDTO<AdminReviewDTO> getReviewList(AdminReviewPageDTO pageDTO, AdminListType listType, long totalElements) {
        List<AdminReviewDTO> responseContent = productReviewDataService.getAdminProductReviewList(pageDTO, listType.getType());

        if(!responseContent.isEmpty() && !(listType.equals(AdminListType.ALL) && pageDTO.keyword() == null))
            totalElements = productReviewDataService.countByAdminReviewList(pageDTO, listType.getType());

        PagingMappingDTO pagingMappingDTO = new PagingMappingDTO(totalElements, pageDTO.page(), pageDTO.amount());

        return new PagingListDTO<>(responseContent, pagingMappingDTO);
    }

    public AdminReviewDetailDTO getReviewDetail(long reviewId) {
        AdminReviewDetailDTO responseDTO = productReviewDataService.getAdminReviewDetailById(reviewId);

        if(responseDTO == null)
            throw new IllegalArgumentException("review Detail Data is null");

        return responseDTO;
    }
}
