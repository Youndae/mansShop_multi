package com.example.moduleproduct.usecase.admin.productQnA;


import com.example.modulecommon.model.dto.admin.qna.out.AdminQnAListResponseDTO;
import com.example.modulecommon.model.dto.page.AdminQnAPageDTO;
import com.example.modulecommon.model.dto.response.PagingListDTO;
import com.example.modulecommon.model.dto.response.PagingMappingDTO;
import com.example.modulecommon.model.enumuration.AdminListType;
import com.example.moduleproduct.service.productQnA.ProductQnADataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminProductQnAReadUseCase {

    private final ProductQnADataService productQnADataService;


    public PagingListDTO<AdminQnAListResponseDTO> getProductQnAList(AdminQnAPageDTO pageDTO, long totalElements) {
        List<AdminQnAListResponseDTO> productQnAList = productQnADataService.getAdminProductQnAList(pageDTO);

        if(!productQnAList.isEmpty() && !(pageDTO.listType().equals(AdminListType.ALL.getType()) && pageDTO.keyword() == null))
            totalElements = productQnADataService.findAllAdminProductQnAListCount(pageDTO);

        PagingMappingDTO pagingMappingDTO = new PagingMappingDTO(totalElements, pageDTO.page(), pageDTO.amount());

        return new PagingListDTO<>(productQnAList, pagingMappingDTO);
    }
}
