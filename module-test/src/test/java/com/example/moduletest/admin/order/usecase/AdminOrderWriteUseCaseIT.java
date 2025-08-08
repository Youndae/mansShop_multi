package com.example.moduletest.admin.order.usecase;

import com.example.modulecommon.fixture.ClassificationFixture;
import com.example.modulecommon.fixture.MemberAndAuthFixture;
import com.example.modulecommon.fixture.ProductFixture;
import com.example.modulecommon.fixture.ProductOrderFixture;
import com.example.modulecommon.model.dto.MemberAndAuthFixtureDTO;
import com.example.modulecommon.model.entity.*;
import com.example.modulecommon.model.enumuration.NotificationType;
import com.example.modulecommon.model.enumuration.OrderStatus;
import com.example.modulecommon.model.enumuration.Result;
import com.example.modulenotification.repository.NotificationRepository;
import com.example.moduleorder.repository.ProductOrderDetailRepository;
import com.example.moduleorder.repository.ProductOrderRepository;
import com.example.moduleorder.service.OrderExternalService;
import com.example.moduleorder.usecase.admin.AdminOrderWriteUseCase;
import com.example.moduleproduct.repository.classification.ClassificationRepository;
import com.example.moduleproduct.repository.product.ProductRepository;
import com.example.moduleproduct.repository.productOption.ProductOptionRepository;
import com.example.moduletest.ModuleTestApplication;
import com.example.moduleuser.repository.AuthRepository;
import com.example.moduleuser.repository.MemberRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = ModuleTestApplication.class)
@EntityScan(basePackages = "com.example.modulecommon")
@EnableJpaRepositories(basePackages = "com.example")
@ComponentScan(basePackages = {"com.example.moduleconfig", "com.example.modulecommon"})
@ActiveProfiles("test")
public class AdminOrderWriteUseCaseIT {

    @Autowired
    private AdminOrderWriteUseCase adminOrderWriteUseCase;

    @Autowired
    private ProductOrderRepository productOrderRepository;

    @Autowired
    private ProductOrderDetailRepository productOrderDetailRepository;

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
    private NotificationRepository notificationRepository;

    @MockitoBean
    private OrderExternalService orderExternalService;

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
    }

    @AfterEach
    void cleanUp() {
        notificationRepository.deleteAll();
        productOrderDetailRepository.deleteAll();
        productOrderRepository.deleteAll();
        productOptionRepository.deleteAll();
        productRepository.deleteAll();
        classificationRepository.deleteAll();
        authRepository.deleteAll();
        memberRepository.deleteAll();
    }

    @Test
    @DisplayName(value = "주문 상태 상품 준비중으로 수정")
    void postOrderPreparation() {
        ProductOrder order = productOrderList.get(0);

        doNothing().when(orderExternalService).sendOrderNotification(any());

        String result = assertDoesNotThrow(() -> adminOrderWriteUseCase.orderPreparation(order.getId()));

        assertNotNull(result);
        assertEquals(Result.OK.getResultKey(), result);

        ProductOrder patchOrder = productOrderRepository.findById(order.getId()).orElse(null);
        assertNotNull(patchOrder);
        assertEquals(OrderStatus.PREPARATION.getStatusStr(), patchOrder.getOrderStat());
    }

    @Test
    @DisplayName(value = "주문 상태 상품 준비중으로 수정. 주문 데이터가 없는 경우")
    void postOrderPreparationNotFound() {

        assertThrows(
                IllegalArgumentException.class,
                () -> adminOrderWriteUseCase.orderPreparation(0L)
        );
    }
}
