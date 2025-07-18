package com.example.moduleproduct.service;

import com.example.modulecommon.model.dto.response.PagingListDTO;
import com.example.modulecommon.model.dto.response.PagingListResponseDTO;
import com.example.modulecommon.model.dto.response.PagingMappingDTO;
import com.example.moduleproduct.model.dto.main.business.MainListDTO;
import com.example.moduleproduct.model.dto.main.out.MainListResponseDTO;
import com.example.moduleproduct.model.dto.page.MainPageDTO;
import com.example.moduleproduct.model.dto.page.ProductPageDTO;
import com.example.moduleproduct.repository.product.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class MainService {

    private final ProductRepository productRepository;


    public List<MainListResponseDTO> getBestAndNewList(MainPageDTO pageDTO) {
        List<MainListDTO> listDTO = productRepository.findListDefault(pageDTO);

        return mainListDataMapping(listDTO);
    }

    public PagingListDTO<MainListResponseDTO> getClassificationAndSearchList(MainPageDTO pageDTO) {
        Pageable pageable = PageRequest.of(
                pageDTO.pageNum() - 1,
                pageDTO.amount(),
                Sort.by("createdAt").descending()
        );

        Page<MainListDTO> dto = productRepository.findListPageable(pageDTO, pageable);
        List<MainListResponseDTO> responseDTO = mainListDataMapping(dto.getContent());
        PagingMappingDTO pagingMappingDTO = PagingMappingDTO.builder()
                                                    .totalElements(dto.getTotalElements())
                                                    .totalPages(dto.getTotalPages())
                                                    .empty(dto.isEmpty())
                                                    .number(dto.getNumber())
                                                    .build();

        return new PagingListDTO<>(responseDTO, pagingMappingDTO);
    }

    private List<MainListResponseDTO> mainListDataMapping(List<MainListDTO> dto) {
        return dto.stream().map(MainListResponseDTO::new).toList();
    }
}
