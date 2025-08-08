package com.example.moduleproduct.service.productQnA;

import com.example.modulecommon.model.dto.admin.qna.out.AdminQnAListResponseDTO;
import com.example.modulecommon.model.dto.page.AdminQnAPageDTO;
import com.example.modulecommon.model.dto.page.MyPagePageDTO;
import com.example.modulecommon.model.dto.qna.out.QnADetailReplyDTO;
import com.example.modulecommon.model.entity.ProductQnA;
import com.example.modulecommon.model.entity.ProductQnAReply;
import com.example.moduleproduct.model.dto.product.business.ProductQnADTO;
import com.example.moduleproduct.model.dto.product.out.ProductDetailQnAReplyListDTO;
import com.example.moduleproduct.model.dto.productQnA.business.ProductQnADetailDTO;
import com.example.moduleproduct.model.dto.productQnA.out.ProductQnAListDTO;
import com.example.moduleproduct.repository.productQnA.ProductQnARepository;
import com.example.moduleproduct.repository.productQnAReply.ProductQnAReplyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

    public Page<ProductQnAListDTO> getProductQnAPaginationListByUserId(MyPagePageDTO pageDTO, String userId) {
        Pageable pageable = PageRequest.of(pageDTO.pageNum() - 1,
                                                    pageDTO.amount(),
                                                    Sort.by("id").descending()
                                            );

        return productQnARepository.findListByUserId(userId, pageable);
    }

    public ProductQnADetailDTO getProductQnADetail(long productQnAId) {
        return productQnARepository.findByQnAId(productQnAId);
    }

    public List<QnADetailReplyDTO> getProductQnADetailAllReplies(long productQnAId) {
        return productQnAReplyRepository.findAllByQnAId(productQnAId);
    }

    public ProductQnA findProductQnAByIdOrElseIllegal(long qnaId) {
        return productQnARepository.findById(qnaId).orElseThrow(IllegalArgumentException::new);
    }

    public void deleteProductQnAById(long qnaId) {
        productQnARepository.deleteById(qnaId);
    }

    public List<AdminQnAListResponseDTO> getAdminProductQnAList(AdminQnAPageDTO pageDTO) {
        return productQnARepository.findAllByAdminProductQnA(pageDTO);
    }

    public Long findAllAdminProductQnAListCount(AdminQnAPageDTO pageDTO) {
        return productQnARepository.countAdminProductQnA(pageDTO);
    }

    public void saveProductQnAReply(ProductQnAReply productQnAReply) {
        productQnAReplyRepository.save(productQnAReply);
    }

    public ProductQnAReply getProductQnAReplyByIdOrElseIllegal(long id) {
        return productQnAReplyRepository.findById(id).orElseThrow(IllegalArgumentException::new);
    }
}
