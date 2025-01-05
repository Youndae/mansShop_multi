package com.example.moduleproduct.service.fixture;

import com.example.modulecommon.model.enumuration.PageAmount;
import com.example.moduleproduct.model.dto.main.business.MainListDTO;
import com.example.moduleproduct.model.dto.page.ProductPageDTO;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public class ProductServiceFixture {

    public static MainListDTO createMainListDTO(int i) {
        return new MainListDTO("productId" + i,
                            "productName" + i,
                            "productThumbnail" + i,
                            1000,
                            0,
                            100);
    }

    public static Pageable createProductListPageableObject() {
        return PageRequest.of(0, PageAmount.MAIN_AMOUNT.getAmount(), Sort.by("createdAt").descending());
    }
}
