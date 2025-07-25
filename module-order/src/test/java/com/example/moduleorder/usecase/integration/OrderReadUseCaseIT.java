package com.example.moduleorder.usecase.integration;

import com.example.modulecommon.fixture.ClassificationFixture;
import com.example.modulecommon.fixture.MemberAndAuthFixture;
import com.example.modulecommon.fixture.ProductFixture;
import com.example.modulecommon.fixture.ProductOrderFixture;
import com.example.modulecommon.model.dto.MemberAndAuthFixtureDTO;
import com.example.modulecommon.model.dto.response.PagingListDTO;
import com.example.modulecommon.model.entity.*;
import com.example.modulecommon.utils.PaginationUtils;
import com.example.moduleorder.ModuleOrderApplication;
import com.example.moduleorder.model.dto.in.MemberOrderDTO;
import com.example.moduleorder.model.dto.out.OrderListDTO;
import com.example.moduleorder.model.dto.page.OrderPageDTO;
import com.example.moduleorder.repository.ProductOrderRepository;
import com.example.moduleorder.usecase.OrderReadUseCase;
import com.example.moduleproduct.repository.classification.ClassificationRepository;
import com.example.moduleproduct.repository.product.ProductRepository;
import com.example.moduleproduct.repository.productOption.ProductOptionRepository;
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

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = ModuleOrderApplication.class)
@EntityScan(basePackages = "com.example.modulecommon")
@EnableJpaRepositories(basePackages = "com.example")
@ComponentScan(basePackages = {"com.example.moduleconfig", "com.example.modulecommon"})
@ActiveProfiles("test")
@Transactional
public class OrderReadUseCaseIT {

    @Autowired
    private OrderReadUseCase orderReadUseCase;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ClassificationRepository classificationRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductOptionRepository productOptionRepository;

    @Autowired
    private ProductOrderRepository productOrderRepository;

    @Autowired
    private EntityManager em;

    private List<Member> memberList;

    private Member member;

    private List<ProductOrder> productOrderList;

    @BeforeEach
    void init() {
        MemberAndAuthFixtureDTO memberAndAuthFixture = MemberAndAuthFixture.createDefaultMember(2);
        MemberAndAuthFixtureDTO adminFixture = MemberAndAuthFixture.createAdmin();
        Member admin = adminFixture.memberList().get(0);
        memberList = memberAndAuthFixture.memberList();
        member = memberList.get(0);
        List<Member> saveMemberList = new ArrayList<>(memberList);
        saveMemberList.addAll(adminFixture.memberList());
        memberRepository.saveAll(saveMemberList);

        List<Classification> classificationList = ClassificationFixture.createClassifications();
        classificationRepository.saveAll(classificationList);

        List<Product> productList = ProductFixture.createProductFixtureList(50, classificationList.get(0));
        List<ProductOption> productOptionList = productList.stream().flatMap(v -> v.getProductOptions().stream()).toList();
        productRepository.saveAll(productList);
        productOptionRepository.saveAll(productOptionList);

        productOrderList = ProductOrderFixture.createDefaultProductOrder(memberList, productOptionList);
        productOrderRepository.saveAll(productOrderList);

        em.flush();
        em.clear();
    }

    private int getLimitSize(int listSize, int limit) {
        return limit == 0 ? listSize : limit;
    }

    private boolean userIdEquals(Member listMember, Member member) {
        return listMember.getUserId().equals(member.getUserId());
    }

    private List<ProductOrder> getMemberProductOrderList(Member member, int limit) {
        int size = getLimitSize(productOrderList.size(), limit);

        return productOrderList.stream()
                .filter(v -> userIdEquals(v.getMember(), member))
                .limit(size)
                .toList();
    }

    @Test
    @DisplayName(value = "주문 목록 조회")
    void getOrderList() {
        OrderPageDTO pageDTO = new OrderPageDTO(1, "3");
        MemberOrderDTO memberOrderDTO = new MemberOrderDTO(member.getUserId(), null, null);
        List<ProductOrder> orderFixture = getMemberProductOrderList(member, 0);
        int totalPages = PaginationUtils.getTotalPages(orderFixture.size(), pageDTO.amount());
        PagingListDTO<OrderListDTO> result = assertDoesNotThrow(() -> orderReadUseCase.getOrderList(pageDTO, memberOrderDTO));

        assertNotNull(result);
        assertFalse(result.content().isEmpty());
        assertFalse(result.pagingData().isEmpty());
        assertEquals(orderFixture.size(), result.pagingData().getTotalElements());
        assertEquals(totalPages, result.pagingData().getTotalPages());
    }

    @Test
    @DisplayName(value = "주문 목록 조회. 데이터가 없는 경우")
    void getOrderListEmpty() {
        OrderPageDTO pageDTO = new OrderPageDTO(1, "3");
        MemberOrderDTO memberOrderDTO = new MemberOrderDTO(member.getUserId(), null, null);
        productOrderRepository.deleteAll();

        PagingListDTO<OrderListDTO> result = assertDoesNotThrow(() -> orderReadUseCase.getOrderList(pageDTO, memberOrderDTO));

        assertNotNull(result);
        assertTrue(result.content().isEmpty());
        assertTrue(result.pagingData().isEmpty());
        assertEquals(0, result.pagingData().getTotalElements());
        assertEquals(0, result.pagingData().getTotalPages());
    }
}
