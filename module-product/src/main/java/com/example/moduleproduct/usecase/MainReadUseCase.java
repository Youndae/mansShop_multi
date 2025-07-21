package com.example.moduleproduct.usecase;


import com.example.modulecommon.model.dto.response.PagingListDTO;
import com.example.moduleproduct.model.dto.main.out.MainListResponseDTO;
import com.example.moduleproduct.model.dto.page.MainPageDTO;
import com.example.moduleproduct.service.MainService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MainReadUseCase {

    private final MainService mainService;

    public List<MainListResponseDTO> getBestProductList(MainPageDTO pageDTO) {

        return mainService.getBestAndNewList(pageDTO);
    }

    public List<MainListResponseDTO> getNewProductList(MainPageDTO pageDTO) {

        return mainService.getBestAndNewList(pageDTO);
    }

    public PagingListDTO<MainListResponseDTO> getClassificationOrSearchList(MainPageDTO pageDTO) {

        return mainService.getClassificationAndSearchList(pageDTO);
    }
}
