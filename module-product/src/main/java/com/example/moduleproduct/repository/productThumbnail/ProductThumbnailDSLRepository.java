package com.example.moduleproduct.repository.productThumbnail;

import java.util.List;

public interface ProductThumbnailDSLRepository {

    List<String> findByProductId(String productId);
}
