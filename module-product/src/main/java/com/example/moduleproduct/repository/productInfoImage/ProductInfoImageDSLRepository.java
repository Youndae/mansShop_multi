package com.example.moduleproduct.repository.productInfoImage;

import java.util.List;

public interface ProductInfoImageDSLRepository {

    List<String> findByProductId(String productId);
}
