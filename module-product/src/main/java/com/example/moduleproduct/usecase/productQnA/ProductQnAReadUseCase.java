package com.example.moduleproduct.usecase.productQnA;

import com.example.modulecommon.customException.CustomAccessDeniedException;
import com.example.modulecommon.customException.CustomNotFoundException;
import com.example.modulecommon.model.dto.page.MyPagePageDTO;
import com.example.modulecommon.model.dto.qna.out.QnADetailReplyDTO;
import com.example.modulecommon.model.enumuration.ErrorCode;
import com.example.moduleproduct.model.dto.productQnA.business.ProductQnADetailDTO;
import com.example.moduleproduct.model.dto.productQnA.out.ProductQnADetailResponseDTO;
import com.example.moduleproduct.model.dto.productQnA.out.ProductQnAListDTO;
import com.example.moduleproduct.service.productQnA.ProductQnADataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductQnAReadUseCase {

    private final ProductQnADataService productQnADataService;

    public Page<ProductQnAListDTO> getProductQnAList(MyPagePageDTO pageDTO, String userId) {
        return productQnADataService.getProductQnAPaginationListByUserId(pageDTO, userId);
    }

    public ProductQnADetailResponseDTO getProductQnADetail(long productQnAId, String nickname) {
        ProductQnADetailResponseDTO responseDTO = getProductQnADetailData(productQnAId);

        if(!responseDTO.writer().equals(nickname)) {
            log.info("ProductQnA Detail writer not match nickname. writer : {}, nickname : {}", responseDTO.writer(), nickname);
            throw new CustomAccessDeniedException(ErrorCode.ACCESS_DENIED, ErrorCode.ACCESS_DENIED.getMessage());
        }

        return responseDTO;
    }

    public ProductQnADetailResponseDTO getProductQnADetailData(long productQnAId) {
        ProductQnADetailDTO qnaDTO = productQnADataService.getProductQnADetail(productQnAId);

        if(qnaDTO == null) {
            log.info("ProductQnA Detail data is null. qnaId = {}", productQnAId);
            throw new CustomNotFoundException(ErrorCode.NOT_FOUND, ErrorCode.NOT_FOUND.getMessage());
        }

        List<QnADetailReplyDTO> replyDTOList = productQnADataService.getProductQnADetailAllReplies(productQnAId);

        return new ProductQnADetailResponseDTO(qnaDTO, replyDTOList);
    }
}
