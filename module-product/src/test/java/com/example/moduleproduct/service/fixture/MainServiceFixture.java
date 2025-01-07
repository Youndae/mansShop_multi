package com.example.moduleproduct.service.fixture;

import com.example.moduleproduct.model.dto.main.business.MainListDTO;

public class MainServiceFixture {

    public static MainListDTO createMainListDTO(int i) {
        return new MainListDTO("productId" + i,
                            "productName" + i,
                            "productThumbnail" + i,
                            1000,
                            0,
                            100);
    }

}
