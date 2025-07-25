package com.example.moduleproduct.service.product;

import com.example.modulecommon.model.entity.ProductOption;
import com.example.moduleproduct.model.dto.product.business.OrderProductInfoDTO;
import com.example.moduleproduct.repository.productOption.ProductOptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductOptionReader {

    private final ProductDataService productDataService;

    private final ProductOptionRepository productOptionRepository;

    public List<ProductOption> getListByIds(List<Long> ids) {
        return productDataService.getListByIds(ids);
    }

    public List<OrderProductInfoDTO> findOrderData(List<Long> ids) {
        return productOptionRepository.findOrderData(ids);
    }
}
