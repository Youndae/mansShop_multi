package com.example.moduleproduct.service.productLike;

import com.example.modulecommon.model.entity.ProductLike;
import com.example.moduleproduct.model.dto.page.LikePageDTO;
import com.example.moduleproduct.model.dto.productLike.out.ProductLikeDTO;
import com.example.moduleproduct.repository.productLike.ProductLikeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductLikeDataService {

    private final ProductLikeRepository productLikeRepository;

    public boolean getProductLikeStatusByUser(String userId, String productId) {

        return productLikeRepository.countByUserIdAndProductId(productId, userId) == 1;
    }

    public void saveProductLike(ProductLike productLike) {
        productLikeRepository.save(productLike);
    }

    public void deleteProductLike(ProductLike productLike) {
        productLikeRepository.deleteByUserIdAndProductId(productLike);
    }

    public Page<ProductLikeDTO> findByUserIdPagination(String userId, LikePageDTO pageDTO) {
        Pageable pageable = PageRequest.of(pageDTO.pageNum() - 1,
                                                pageDTO.amount(),
                                                Sort.by("createdAt").descending()
                                        );

        return productLikeRepository.findListByUserId(userId, pageable);
    }
}
