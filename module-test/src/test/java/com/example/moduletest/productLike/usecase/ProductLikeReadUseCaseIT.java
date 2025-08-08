package com.example.moduletest.productLike.usecase;

import com.example.modulecommon.fixture.ClassificationFixture;
import com.example.modulecommon.fixture.MemberAndAuthFixture;
import com.example.modulecommon.fixture.ProductFixture;
import com.example.modulecommon.fixture.ProductLikeFixture;
import com.example.modulecommon.model.dto.MemberAndAuthFixtureDTO;
import com.example.modulecommon.model.entity.*;
import com.example.modulecommon.utils.PaginationUtils;
import com.example.moduleproduct.model.dto.page.LikePageDTO;
import com.example.moduleproduct.model.dto.productLike.out.ProductLikeDTO;
import com.example.moduleproduct.repository.classification.ClassificationRepository;
import com.example.moduleproduct.repository.product.ProductRepository;
import com.example.moduleproduct.repository.productLike.ProductLikeRepository;
import com.example.moduleproduct.repository.productOption.ProductOptionRepository;
import com.example.moduleproduct.usecase.productLike.ProductLikeReadUseCase;
import com.example.moduletest.ModuleTestApplication;
import com.example.moduleuser.repository.MemberRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = ModuleTestApplication.class)
@EntityScan(basePackages = "com.example.modulecommon")
@EnableJpaRepositories(basePackages = "com.example")
@ComponentScan(basePackages = {"com.example.moduleconfig", "com.example.modulecommon"})
@ActiveProfiles("test")
@Transactional
public class ProductLikeReadUseCaseIT {

    @Autowired
    private ProductLikeReadUseCase productLikeReadUseCase;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ClassificationRepository classificationRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductOptionRepository productOptionRepository;

    @Autowired
    private ProductLikeRepository productLikeRepository;

    @Autowired
    private EntityManager em;

    private Member member;

    private List<Member> memberList;

    private List<Product> productList;

    private List<ProductLike> productLikeList;

    private List<ProductLike> getMemberProductLikeList(Member member, int limit) {
        int size = getLimitSize(productLikeList.size(), limit);

        return productLikeList.stream()
                .filter(v -> userIdEquals(v.getMember(), member))
                .limit(size)
                .toList();
    }

    private int getLimitSize(int listSize, int limit) {
        return limit == 0 ? listSize : limit;
    }

    private boolean userIdEquals(Member listMember, Member member) {
        return listMember.getUserId().equals(member.getUserId());
    }

    @BeforeEach
    void init() {
        MemberAndAuthFixtureDTO memberAndAuthFixture = MemberAndAuthFixture.createDefaultMember(10);
        memberList = memberAndAuthFixture.memberList();
        MemberAndAuthFixtureDTO adminFixture = MemberAndAuthFixture.createAdmin();
        Member admin = adminFixture.memberList().get(0);
        List<Member> saveMemberList = new ArrayList<>(memberList);
        saveMemberList.add(admin);
        memberRepository.saveAll(saveMemberList);
        member = memberList.get(0);

        List<Classification> classificationList = ClassificationFixture.createClassifications();
        classificationRepository.saveAll(classificationList);

        productList = ProductFixture.createProductFixtureList(50, classificationList.get(0));
        List<ProductOption> productOptionList = productList.stream().flatMap(v -> v.getProductOptions().stream()).toList();
        productRepository.saveAll(productList);
        productOptionRepository.saveAll(productOptionList);

        List<Product> likeProducts = productList.stream().limit(30).toList();
        productLikeList = ProductLikeFixture.createDefaultProductLike(memberList, likeProducts);
        productLikeRepository.saveAll(productLikeList);

        em.flush();
        em.clear();
    }

    @Test
    @DisplayName(value = "관심 상품 목록 조회")
    void getLikeList() {
        LikePageDTO pageDTO = new LikePageDTO(1);
        List<ProductLike> fixtureList = getMemberProductLikeList(member, 0);
        int totalPages = PaginationUtils.getTotalPages(fixtureList.size(), pageDTO.amount());
        int pageElements = Math.min(fixtureList.size(), pageDTO.amount());
        Page<ProductLikeDTO> result = assertDoesNotThrow(() -> productLikeReadUseCase.getLikeList(pageDTO, member.getUserId()));

        assertNotNull(result);
        assertFalse(result.getContent().isEmpty());
        assertFalse(result.isEmpty());
        assertEquals(fixtureList.size(), result.getTotalElements());
        assertEquals(totalPages, result.getTotalPages());
        assertEquals(pageElements, result.getContent().size());
    }

    @Test
    @DisplayName(value = "관심 상품 목록 조회. 데이터가 없는 경우")
    void getLikeListEmpty() {
        productLikeRepository.deleteAll();
        LikePageDTO pageDTO = new LikePageDTO(1);

        Page<ProductLikeDTO> result = assertDoesNotThrow(() -> productLikeReadUseCase.getLikeList(pageDTO, member.getUserId()));

        assertNotNull(result);
        assertTrue(result.getContent().isEmpty());
        assertTrue(result.isEmpty());
        assertEquals(0, result.getTotalElements());
        assertEquals(0, result.getTotalPages());
    }
}
