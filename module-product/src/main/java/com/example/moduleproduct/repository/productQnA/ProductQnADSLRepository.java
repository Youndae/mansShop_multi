package com.example.moduleproduct.repository.productQnA;

import com.example.modulecommon.model.dto.admin.qna.out.AdminQnAListResponseDTO;
import com.example.modulecommon.model.dto.page.AdminQnAPageDTO;
import com.example.moduleproduct.model.dto.product.business.ProductQnADTO;
import com.example.moduleproduct.model.dto.productQnA.business.ProductQnADetailDTO;
import com.example.moduleproduct.model.dto.productQnA.out.ProductQnAListDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProductQnADSLRepository {

    Page<ProductQnADTO> findByProductId(String productId, Pageable pageable);

    Page<ProductQnAListDTO> findListByUserId(String userId, Pageable pageable);

    ProductQnADetailDTO findByQnAId(long qnaId);

    List<AdminQnAListResponseDTO> findAllByAdminProductQnA(AdminQnAPageDTO pageDTO);

    Long countAdminProductQnA(AdminQnAPageDTO pageDTO);
}
