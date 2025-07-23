package com.example.moduleproduct.usecase.main;


import com.example.modulecommon.model.dto.response.PagingListDTO;
import com.example.modulecommon.model.dto.response.PagingMappingDTO;
import com.example.moduleproduct.model.dto.main.business.MainListDTO;
import com.example.moduleproduct.model.dto.main.out.MainListResponseDTO;
import com.example.moduleproduct.model.dto.page.MainPageDTO;
import com.example.moduleproduct.service.main.MainDataService;
import com.example.moduleproduct.service.main.MainDomainService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MainReadUseCase {

    private final MainDataService mainDataService;

    private final MainDomainService mainDomainService;

    public List<MainListResponseDTO> getBestProductList(MainPageDTO pageDTO) {

        List<MainListDTO> bestList = mainDataService.getMainBestAndNewList(pageDTO);

        return mainDomainService.mappingMainListData(bestList);
    }

    public List<MainListResponseDTO> getNewProductList(MainPageDTO pageDTO) {

        List<MainListDTO> newList = mainDataService.getMainBestAndNewList(pageDTO);

        return mainDomainService.mappingMainListData(newList);
    }

    public PagingListDTO<MainListResponseDTO> getClassificationOrSearchList(MainPageDTO pageDTO) {
        Page<MainListDTO> classificationOrSearchList = mainDataService.getClassificationOrSearchList(pageDTO);
        List<MainListResponseDTO> responseContent = mainDomainService.mappingMainListData(classificationOrSearchList.getContent());
        PagingMappingDTO mainPagingMappingDTO = mainDomainService.getMainPagingMappingDTO(classificationOrSearchList);

        return new PagingListDTO<>(responseContent, mainPagingMappingDTO);
    }
}
