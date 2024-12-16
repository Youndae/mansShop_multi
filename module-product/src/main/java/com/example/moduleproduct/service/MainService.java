package com.example.moduleproduct.service;

import com.example.modulecommon.model.dto.response.PagingListResponseDTO;
import com.example.modulecommon.model.dto.response.PagingMappingDTO;
import com.example.moduleproduct.model.dto.main.business.MainListDTO;
import com.example.moduleproduct.model.dto.main.out.MainListResponseDTO;
import com.example.moduleproduct.model.dto.page.ProductPageDTO;
import com.example.moduleproduct.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MainService {

    private final ProductRepository productRepository;

    public List<MainListResponseDTO> getBestAndNewProductList(ProductPageDTO pageDTO) {
        List<MainListDTO> list = productRepository.getProductDefaultList(pageDTO);

        return mappingMainListResponseDTO(list);
    }

    public PagingListResponseDTO<MainListResponseDTO> getClassificationAndSearchProductList(ProductPageDTO pageDTO) {
        Pageable pageable = PageRequest.of(pageDTO.pageNum() - 1,
                                            pageDTO.mainProductAmount(),
                                            Sort.by("createdAt").descending());
        Page<MainListDTO> list = productRepository.getProductClassificationAndSearchList(pageDTO, pageable);
        List<MainListResponseDTO> content = mappingMainListResponseDTO(list.getContent());
        PagingMappingDTO pagingMappingDTO = PagingMappingDTO.builder()
                                                            .totalElements(list.getTotalElements())
                                                            .totalPages(list.getTotalPages())
                                                            .empty(list.isEmpty())
                                                            .number(list.getNumber())
                                                            .build();

        return new PagingListResponseDTO<>(content, pagingMappingDTO);
    }

    private List<MainListResponseDTO> mappingMainListResponseDTO(List<MainListDTO> dto) {

        return dto.stream()
                .map(MainListResponseDTO::new)
                .toList();
    }
}
