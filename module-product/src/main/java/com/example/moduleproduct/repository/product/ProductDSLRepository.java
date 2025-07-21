package com.example.moduleproduct.repository.product;


import com.example.moduleproduct.model.dto.main.business.MainListDTO;
import com.example.moduleproduct.model.dto.page.MainPageDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProductDSLRepository {

    List<MainListDTO> findListDefault(MainPageDTO pageDTO);

    Page<MainListDTO> findListPageable(MainPageDTO pageDTO, Pageable pageable);
}
