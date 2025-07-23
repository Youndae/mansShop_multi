package com.example.moduleproduct.service.main;

import com.example.moduleproduct.model.dto.main.business.MainListDTO;
import com.example.moduleproduct.model.dto.page.MainPageDTO;
import com.example.moduleproduct.repository.product.ProductRepository;
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
public class MainDataService {

    private final ProductRepository productRepository;

    public List<MainListDTO> getMainBestAndNewList(MainPageDTO pageDTO) {
        return productRepository.findListDefault(pageDTO);
    }

    public Page<MainListDTO> getClassificationOrSearchList(MainPageDTO pageDTO) {
        Pageable pageable = PageRequest.of(
                pageDTO.pageNum() - 1,
                pageDTO.amount(),
                Sort.by("createdAt").descending()
        );

        return productRepository.findListPageable(pageDTO, pageable);
    }
}
