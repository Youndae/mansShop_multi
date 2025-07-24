package com.example.moduleproduct.service.productQnA;

import com.example.modulecommon.model.entity.ProductQnA;
import com.example.moduleproduct.model.dto.product.business.ProductQnADTO;
import com.example.moduleproduct.model.dto.product.out.ProductDetailQnAReplyListDTO;
import com.example.moduleproduct.repository.productQnA.ProductQnARepository;
import com.example.moduleproduct.repository.productQnAReply.ProductQnAReplyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductQnADataService {

    private final ProductQnARepository productQnARepository;

    private final ProductQnAReplyRepository productQnAReplyRepository;

    public Page<ProductQnADTO> getProductDetailQnA(Pageable pageable, String productId) {
        return productQnARepository.findByProductId(productId, pageable);
    }

    public List<ProductDetailQnAReplyListDTO> getProductDetailQnAReplyList(List<Long> qnaIds) {
        return productQnAReplyRepository.getQnAReplyListByQnAIds(qnaIds);
    }

    public void saveProductQnA(ProductQnA productQnA) {
        productQnARepository.save(productQnA);
    }
}
