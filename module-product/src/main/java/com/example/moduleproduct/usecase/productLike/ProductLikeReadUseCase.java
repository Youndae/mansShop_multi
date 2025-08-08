package com.example.moduleproduct.usecase.productLike;

import com.example.moduleproduct.model.dto.page.LikePageDTO;
import com.example.moduleproduct.model.dto.productLike.out.ProductLikeDTO;
import com.example.moduleproduct.service.productLike.ProductLikeDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductLikeReadUseCase {

    private final ProductLikeDataService productLikeDataService;

    public Page<ProductLikeDTO> getLikeList(LikePageDTO pageDTO, String userId) {
        return productLikeDataService.findByUserIdPagination(userId, pageDTO);
    }
}
