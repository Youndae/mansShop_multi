package com.example.moduleproduct.service.product;

import com.example.moduleproduct.model.dto.product.business.PatchOrderStockDTO;
import com.example.moduleproduct.repository.productOption.ProductOptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductOptionStore {

    private final ProductOptionRepository productOptionRepository;

    public void patchOrderStock(List<PatchOrderStockDTO> patchDTO) {
        productOptionRepository.patchOrderStock(patchDTO);
    }
}
