package com.example.moduletest.admin.order.usecase;

import com.example.modulecommon.fixture.ClassificationFixture;
import com.example.modulecommon.fixture.MemberAndAuthFixture;
import com.example.modulecommon.fixture.ProductFixture;
import com.example.modulecommon.fixture.ProductOrderFixture;
import com.example.modulecommon.model.dto.MemberAndAuthFixtureDTO;
import com.example.modulecommon.model.dto.response.PagingListDTO;
import com.example.modulecommon.model.entity.*;
import com.example.modulecommon.utils.PaginationUtils;
import com.example.moduleorder.model.dto.admin.out.AdminOrderResponseDTO;
import com.example.moduleorder.model.dto.admin.page.AdminOrderPageDTO;
import com.example.moduleorder.repository.ProductOrderRepository;
import com.example.moduleorder.usecase.admin.AdminOrderReadUseCase;
import com.example.moduleproduct.repository.classification.ClassificationRepository;
import com.example.moduleproduct.repository.product.ProductRepository;
import com.example.moduleproduct.repository.productOption.ProductOptionRepository;
import com.example.moduletest.ModuleTestApplication;
import com.example.moduleuser.repository.AuthRepository;
import com.example.moduleuser.repository.MemberRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = ModuleTestApplication.class)
@EntityScan(basePackages = "com.example.modulecommon")
@EnableJpaRepositories(basePackages = "com.example")
@ComponentScan(basePackages = {"com.example.moduleconfig", "com.example.modulecommon"})
@ActiveProfiles("test")
@Transactional
public class AdminOrderReadUseCaseIT {

    @Autowired
    private AdminOrderReadUseCase adminOrderReadUseCase;

    @Autowired
    private ProductOrderRepository productOrderRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private AuthRepository authRepository;

    @Autowired
    private ClassificationRepository classificationRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductOptionRepository productOptionRepository;

    @Autowired
    private EntityManager em;

    private List<ProductOrder> productOrderList;

    @BeforeEach
    void init() {
        MemberAndAuthFixtureDTO memberFixture = MemberAndAuthFixture.createDefaultMember(40);
        List<Member> memberList = memberFixture.memberList();
        List<Auth> authList = memberFixture.authList();
        List<Classification> classificationFixture = ClassificationFixture.createClassifications();

        memberRepository.saveAll(memberList);
        authRepository.saveAll(authList);
        classificationRepository.saveAll(classificationFixture);

        List<Product> productFixture = ProductFixture.createProductFixtureList(5, classificationFixture.get(0));
        List<ProductOption> productOptionList = productFixture.stream()
                .flatMap(v -> v.getProductOptions().stream())
                .toList();
        productRepository.saveAll(productFixture);
        productOptionRepository.saveAll(productOptionList);

        productOrderList = ProductOrderFixture.createDefaultProductOrder(memberList, productOptionList);

        productOrderRepository.saveAll(productOrderList);

        em.flush();
        em.clear();
    }

    @Test
    @DisplayName(value = "전체 주문 목록 조회")
    void getAllOrderList() {
        AdminOrderPageDTO pageDTO = new AdminOrderPageDTO(1);
        int totalPages = PaginationUtils.getTotalPages(productOrderList.size(), pageDTO.amount());

        PagingListDTO<AdminOrderResponseDTO> result = assertDoesNotThrow(() -> adminOrderReadUseCase.getAdminAllOrderList(pageDTO, productOrderList.size()));

        assertNotNull(result);
        assertEquals(productOrderList.size(), result.pagingData().getTotalElements());
        assertEquals(totalPages, result.pagingData().getTotalPages());
        assertEquals(pageDTO.amount(), result.content().size());
        result.content().forEach(v -> assertFalse(v.detailList().isEmpty()));
    }

    @Test
    @DisplayName(value = "전체 주문 목록 조회. 데이터가 없는 경우")
    void getAllOrderListEmpty() {
        productOrderRepository.deleteAll();
        AdminOrderPageDTO pageDTO = new AdminOrderPageDTO(1);

        PagingListDTO<AdminOrderResponseDTO> result = assertDoesNotThrow(() -> adminOrderReadUseCase.getAdminAllOrderList(pageDTO, 0L));

        assertNotNull(result);
        assertTrue(result.content().isEmpty());
        assertEquals(0, result.pagingData().getTotalElements());
        assertEquals(0, result.pagingData().getTotalPages());
    }

    @Test
    @DisplayName(value = "전체 주문 목록 조회. 주문자명으로 조회")
    void getAllOrderListSearchRecipient() {
        ProductOrder order = productOrderList.get(0);
        AdminOrderPageDTO pageDTO = new AdminOrderPageDTO(order.getRecipient(), "recipient", 1);

        PagingListDTO<AdminOrderResponseDTO> result = assertDoesNotThrow(() -> adminOrderReadUseCase.getAdminAllOrderList(pageDTO, 0L));

        assertNotNull(result);
        assertFalse(result.content().isEmpty());
        assertEquals(1, result.pagingData().getTotalElements());
        assertEquals(1, result.pagingData().getTotalPages());

        AdminOrderResponseDTO responseDTO = result.content().get(0);

        assertEquals(order.getId(), responseDTO.orderId());
        assertEquals(order.getRecipient(), responseDTO.recipient());
        assertEquals(order.getMember().getUserId(), responseDTO.userId());
    }

    @Test
    @DisplayName(value = "전체 주문 목록 조회. 사용자 아이디로 조회")
    void getAllOrderListSearchUserId() {
        ProductOrder order = productOrderList.get(0);
        AdminOrderPageDTO pageDTO = new AdminOrderPageDTO(order.getMember().getUserId(), "userId", 1);

        PagingListDTO<AdminOrderResponseDTO> result = assertDoesNotThrow(() -> adminOrderReadUseCase.getAdminAllOrderList(pageDTO, 0L));

        assertNotNull(result);
        assertFalse(result.content().isEmpty());
        assertEquals(1, result.pagingData().getTotalElements());
        assertEquals(1, result.pagingData().getTotalPages());

        AdminOrderResponseDTO responseDTO = result.content().get(0);

        assertEquals(order.getId(), responseDTO.orderId());
        assertEquals(order.getRecipient(), responseDTO.recipient());
        assertEquals(order.getMember().getUserId(), responseDTO.userId());
    }

    @Test
    @DisplayName(value = "미처리 주문 목록 조회")
    void getNewOrderList() {
        AdminOrderPageDTO pageDTO = new AdminOrderPageDTO(1);
        int totalPages = PaginationUtils.getTotalPages(productOrderList.size(), pageDTO.amount());

        PagingListDTO<AdminOrderResponseDTO> result = assertDoesNotThrow(
                () -> adminOrderReadUseCase.getAdminNewOrderList(pageDTO)
        );

        assertNotNull(result);
        assertEquals(productOrderList.size(), result.pagingData().getTotalElements());
        assertEquals(totalPages, result.pagingData().getTotalPages());
        assertEquals(pageDTO.amount(), result.content().size());
        result.content().forEach(v -> assertFalse(v.detailList().isEmpty()));
    }

    @Test
    @DisplayName(value = "미처리 주문 목록 조회. 데이터가 없는 경우")
    void getNewOrderListEmpty() {
        productOrderRepository.deleteAll();
        AdminOrderPageDTO pageDTO = new AdminOrderPageDTO(1);

        PagingListDTO<AdminOrderResponseDTO> result = assertDoesNotThrow(
                () -> adminOrderReadUseCase.getAdminNewOrderList(pageDTO)
        );

        assertNotNull(result);
        assertTrue(result.content().isEmpty());
        assertEquals(0, result.pagingData().getTotalElements());
        assertEquals(0, result.pagingData().getTotalPages());
    }

    @Test
    @DisplayName(value = "미처리 주문 목록 조회. 주문자명으로 조회")
    void getNewOrderListSearchRecipient() {
        ProductOrder order = productOrderList.get(0);
        AdminOrderPageDTO pageDTO = new AdminOrderPageDTO(order.getRecipient(), "recipient", 1);

        PagingListDTO<AdminOrderResponseDTO> result = assertDoesNotThrow(() -> adminOrderReadUseCase.getAdminNewOrderList(pageDTO));

        assertNotNull(result);
        assertFalse(result.content().isEmpty());
        assertEquals(1, result.pagingData().getTotalElements());
        assertEquals(1, result.pagingData().getTotalPages());

        AdminOrderResponseDTO responseDTO = result.content().get(0);

        assertEquals(order.getId(), responseDTO.orderId());
        assertEquals(order.getRecipient(), responseDTO.recipient());
        assertEquals(order.getMember().getUserId(), responseDTO.userId());
    }

    @Test
    @DisplayName(value = "미처리 주문 목록 조회. 사용자 아이디로 조회")
    void getNewOrderListSearchUserId() {
        ProductOrder order = productOrderList.get(0);
        AdminOrderPageDTO pageDTO = new AdminOrderPageDTO(order.getMember().getUserId(), "userId", 1);

        PagingListDTO<AdminOrderResponseDTO> result = assertDoesNotThrow(() -> adminOrderReadUseCase.getAdminNewOrderList(pageDTO));

        assertNotNull(result);
        assertFalse(result.content().isEmpty());
        assertEquals(1, result.pagingData().getTotalElements());
        assertEquals(1, result.pagingData().getTotalPages());

        AdminOrderResponseDTO responseDTO = result.content().get(0);

        assertEquals(order.getId(), responseDTO.orderId());
        assertEquals(order.getRecipient(), responseDTO.recipient());
        assertEquals(order.getMember().getUserId(), responseDTO.userId());
    }
}
