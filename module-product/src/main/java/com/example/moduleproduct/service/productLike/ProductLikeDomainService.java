package com.example.moduleproduct.service.productLike;

import com.example.modulecommon.customException.CustomNotFoundException;
import com.example.modulecommon.model.entity.Member;
import com.example.modulecommon.model.entity.Product;
import com.example.modulecommon.model.entity.ProductLike;
import com.example.modulecommon.model.enumuration.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ProductLikeDomainService {

    public ProductLike buildLikeProduct(Member member, Product product) {
        if(member == null || product == null)
            throw new CustomNotFoundException(ErrorCode.BAD_REQUEST, ErrorCode.BAD_REQUEST.getMessage());

        return ProductLike.builder()
                .member(member)
                .product(product)
                .build();
    }
}
