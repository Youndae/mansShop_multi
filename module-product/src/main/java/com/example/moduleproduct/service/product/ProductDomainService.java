package com.example.moduleproduct.service.product;



import com.example.modulecommon.customException.CustomNotFoundException;
import com.example.modulecommon.model.entity.Member;
import com.example.modulecommon.model.entity.Product;
import com.example.modulecommon.model.entity.ProductLike;
import com.example.modulecommon.model.enumuration.ErrorCode;
import com.example.moduleproduct.model.dto.product.business.ProductQnADTO;
import com.example.moduleproduct.model.dto.product.business.ProductQnAReplyDTO;
import com.example.moduleproduct.model.dto.product.business.ProductQnAResponseDTO;
import com.example.moduleproduct.model.dto.product.out.ProductDetailQnAReplyListDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class ProductDomainService {

    public List<ProductQnAResponseDTO> mapToProductQnAResponseDTO(Page<ProductQnADTO> productQnA,
                                                                  List<ProductDetailQnAReplyListDTO> productQnAReplyList) {
        List<ProductQnAResponseDTO> resultList = new ArrayList<>();

        for(int i = 0; i < productQnA.getContent().size(); i++) {
            List<ProductQnAReplyDTO> replyList = new ArrayList<>();
            ProductQnADTO dto = productQnA.getContent().get(i);

            for(int j = 0; j < productQnAReplyList.size(); j++) {
                if(dto.qnaId().equals(productQnAReplyList.get(j).qnaId())){
                    replyList.add(
                            new ProductQnAReplyDTO(productQnAReplyList.get(j))
                    );
                }
            }

            resultList.add(new ProductQnAResponseDTO(dto, replyList));
        }

        return resultList;
    }
}
