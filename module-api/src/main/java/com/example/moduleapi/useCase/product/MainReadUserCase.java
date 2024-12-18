package com.example.moduleapi.useCase.product;

import com.example.modulecommon.model.dto.response.PagingListResponseDTO;
import com.example.moduleproduct.model.dto.main.out.MainListResponseDTO;
import com.example.moduleproduct.model.dto.page.ProductPageDTO;
import com.example.moduleproduct.service.MainService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MainReadUserCase {

    private final MainService mainService;


    public List<MainListResponseDTO> getMainProduct(ProductPageDTO pageDTO) {

        return mainService.getBestAndNewProductList(pageDTO);
    }

    public PagingListResponseDTO<MainListResponseDTO> getClassificationProduct(ProductPageDTO pageDTO) {

        return mainService.getClassificationAndSearchProductList(pageDTO);
    }
}
