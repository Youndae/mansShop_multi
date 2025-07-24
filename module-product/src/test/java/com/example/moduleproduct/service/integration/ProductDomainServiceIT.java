package com.example.moduleproduct.service.integration;

import com.example.modulecommon.customException.CustomNotFoundException;
import com.example.modulecommon.model.entity.Member;
import com.example.modulecommon.model.entity.Product;
import com.example.modulecommon.model.entity.ProductLike;
import com.example.moduleproduct.ModuleProductApplication;
import com.example.moduleproduct.model.dto.product.business.ProductQnADTO;
import com.example.moduleproduct.model.dto.product.business.ProductQnAReplyDTO;
import com.example.moduleproduct.model.dto.product.business.ProductQnAResponseDTO;
import com.example.moduleproduct.model.dto.product.out.ProductDetailQnAReplyListDTO;
import com.example.moduleproduct.service.product.ProductDomainService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = ModuleProductApplication.class)
@EntityScan(basePackages = "com.example.modulecommon")
@EnableJpaRepositories(basePackages = "com.example")
@ComponentScan(basePackages = {"com.example.moduleconfig", "com.example.modulecommon"})
@ActiveProfiles("test")
@Transactional
public class ProductDomainServiceIT {

    @Autowired
    private ProductDomainService productDomainService;

    private List<ProductQnADTO> createProductQnADTOList() {
        List<ProductQnADTO> result = new ArrayList<>();

        for(int i = 0; i < 20; i++) {
            result.add(
                    new ProductQnADTO(
                            (long) i,
                            "writer" + i,
                            "content" + i,
                            LocalDate.now(),
                            i % 2 == 0
                    )
            );
        }

        return result;
    }

    private List<ProductDetailQnAReplyListDTO> createProductDetailQnAReplyListDTOList() {
        List<ProductDetailQnAReplyListDTO> result = new ArrayList<>();

        for(int i = 0; i < 20; i++) {
            result.add(
                    new ProductDetailQnAReplyListDTO(
                            "admin",
                            "replyContent" + i,
                            (long) i,
                            LocalDateTime.now()
                    )
            );
        }

        return result;
    }

    @Test
    @DisplayName(value = "ProductQnADTO 리스트와 ProductDetailQnAReplyListDTO 매핑")
    void mapToProductQnAResponseDTO() {
        Page<ProductQnADTO> productQnADTOPage = new PageImpl<>(createProductQnADTOList());
        List<ProductDetailQnAReplyListDTO> replyListDTO = createProductDetailQnAReplyListDTOList();

        List<ProductQnAResponseDTO> result = assertDoesNotThrow(() -> productDomainService.mapToProductQnAResponseDTO(productQnADTOPage, replyListDTO));

        assertNotNull(result);
        assertEquals(productQnADTOPage.getContent().size(), result.size());

        for(int i = 0; i < productQnADTOPage.getContent().size(); i++) {
            ProductQnADTO fixtureDTO = productQnADTOPage.getContent().get(i);
            ProductQnAResponseDTO resultDTO = result.get(i);

            assertEquals(fixtureDTO.qnaId(), resultDTO.qnaId());
            assertEquals(fixtureDTO.writer(), resultDTO.writer());
            assertEquals(fixtureDTO.qnaContent(), resultDTO.qnaContent());
            assertEquals(fixtureDTO.productQnAStat(), resultDTO.productQnAStat());
            assertEquals(1, resultDTO.replyList().size());

            ProductDetailQnAReplyListDTO fixtureReplyDTO = replyListDTO.get(i);
            ProductQnAReplyDTO resultReplyDTO = resultDTO.replyList().get(0);

            assertEquals(fixtureReplyDTO.writer(), resultReplyDTO.writer());
            assertEquals(fixtureReplyDTO.replyContent(), resultReplyDTO.content());
        }
    }
}
