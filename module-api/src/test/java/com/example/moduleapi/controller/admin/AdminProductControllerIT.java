package com.example.moduleapi.controller.admin;

import com.example.moduleapi.ModuleApiApplication;
import com.example.moduleapi.config.exception.ExceptionEntity;
import com.example.moduleapi.config.exception.ValidationError;
import com.example.moduleapi.config.exception.ValidationExceptionEntity;
import com.example.moduleapi.fixture.TokenFixture;
import com.example.moduleapi.model.response.PagingResponseDTO;
import com.example.moduleapi.model.response.ResponseIdDTO;
import com.example.modulecommon.fixture.ClassificationFixture;
import com.example.modulecommon.fixture.MemberAndAuthFixture;
import com.example.modulecommon.fixture.ProductFixture;
import com.example.modulecommon.model.dto.MemberAndAuthFixtureDTO;
import com.example.modulecommon.model.entity.*;
import com.example.modulecommon.model.enumuration.ErrorCode;
import com.example.modulecommon.utils.TestPaginationUtils;
import com.example.moduleconfig.properties.CookieProperties;
import com.example.moduleconfig.properties.TokenProperties;
import com.example.modulefile.service.FileDomainService;
import com.example.modulefile.service.FileService;
import com.example.moduleproduct.model.dto.admin.product.in.AdminDiscountPatchDTO;
import com.example.moduleproduct.model.dto.admin.product.in.AdminProductPatchDTO;
import com.example.moduleproduct.model.dto.admin.product.in.AdminProductPostDTO;
import com.example.moduleproduct.model.dto.admin.product.in.PatchOptionDTO;
import com.example.moduleproduct.model.dto.admin.product.out.*;
import com.example.moduleproduct.model.dto.page.AdminProductPageDTO;
import com.example.moduleproduct.repository.classification.ClassificationRepository;
import com.example.moduleproduct.repository.product.ProductRepository;
import com.example.moduleproduct.repository.productInfoImage.ProductInfoImageRepository;
import com.example.moduleproduct.repository.productOption.ProductOptionRepository;
import com.example.moduleproduct.repository.productThumbnail.ProductThumbnailRepository;
import com.example.moduleuser.repository.AuthRepository;
import com.example.moduleuser.repository.MemberRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.multipart.MultipartFile;


import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;


@SpringBootTest(classes = ModuleApiApplication.class)
@EntityScan(basePackages = "com.example.modulecommon")
@EnableJpaRepositories(basePackages = "com.example")
@ComponentScan(basePackages = {"com.example.moduleconfig", "com.example.modulecommon"})
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
public class AdminProductControllerIT {

    @MockitoBean
    private FileService fileService;

    @MockitoBean
    private FileDomainService fileDomainService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper om;

    @Autowired
    private EntityManager em;

    @Autowired
    private TokenFixture tokenFixture;

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
    private ProductThumbnailRepository productThumbnailRepository;

    @Autowired
    private ProductInfoImageRepository productInfoImageRepository;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private TokenProperties tokenProperties;

    @Autowired
    private CookieProperties cookieProperties;

    private Map<String, String> tokenMap;

    private String accessTokenValue;

    private String refreshTokenValue;

    private String inoValue;

    private List<Classification> classificationList;

    private List<Product> outerProductList;

    private List<Product> topProductList;

    private List<Product> allProductList;

    private static final String URL_PREFIX = "/api/admin/";

    @BeforeEach
    void init() {
        MemberAndAuthFixtureDTO adminFixture = MemberAndAuthFixture.createAdmin();
        Member admin = adminFixture.memberList().get(0);
        memberRepository.save(admin);
        authRepository.saveAll(adminFixture.authList());

        tokenMap = tokenFixture.createAndSaveAllToken(admin);
        accessTokenValue = tokenMap.get(tokenProperties.getAccess().getHeader());
        refreshTokenValue = tokenMap.get(tokenProperties.getRefresh().getHeader());
        inoValue = tokenMap.get(cookieProperties.getIno().getHeader());

        classificationList = ClassificationFixture.createClassifications();
        classificationRepository.saveAll(classificationList);

        int firstProductListSize = 30;

        outerProductList = ProductFixture.createProductFixtureList(firstProductListSize, classificationList.get(0));
        topProductList = ProductFixture.createAdditionalProduct(firstProductListSize + 1, 20, classificationList.get(1));
        allProductList = new ArrayList<>(outerProductList);
        allProductList.addAll(topProductList);
        List<ProductOption> productOptionList = allProductList.stream()
                .flatMap(v -> v.getProductOptions().stream())
                .toList();
        List<ProductThumbnail> productThumbnailList = allProductList.stream()
                .flatMap(v -> v.getProductThumbnails().stream())
                .toList();
        List<ProductInfoImage> productInfoImageList = allProductList.stream()
                .flatMap(v -> v.getProductInfoImages().stream())
                .toList();
        productRepository.saveAll(allProductList);
        productOptionRepository.saveAll(productOptionList);
        productThumbnailRepository.saveAll(productThumbnailList);
        productInfoImageRepository.saveAll(productInfoImageList);

        em.flush();
        em.clear();
    }

    @AfterEach
    void cleanUP() {
        String accessKey = tokenMap.get("accessKey");
        String refreshKey = tokenMap.get("refreshKey");

        redisTemplate.delete(accessKey);
        redisTemplate.delete(refreshKey);
    }

    @Test
    @DisplayName(value = "전체 상품 목록 조회")
    void getProductList() throws Exception {
        AdminProductPageDTO pageDTOFixture = new AdminProductPageDTO(null, 1);
        int contentSize = Math.min(allProductList.size(), pageDTOFixture.amount());
        int totalPages = TestPaginationUtils.getTotalPages(allProductList.size(), pageDTOFixture.amount());

        MvcResult result = mockMvc.perform(get(URL_PREFIX + "product")
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue)))
                .andExpect(status().isOk())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        PagingResponseDTO<AdminProductListDTO> response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        assertNotNull(response);
        assertFalse(response.content().isEmpty());
        assertFalse(response.empty());
        assertEquals(contentSize, response.content().size());
        assertEquals(totalPages, response.totalPages());
    }

    @Test
    @DisplayName(value = "전체 상품 목록 조회. 상품명 검색. 전체 데이터가 포함되는 'testProduct' 로 검색")
    void getProductListSearchMultipleResult() throws Exception {
        AdminProductPageDTO pageDTOFixture = new AdminProductPageDTO(null, 1);
        int contentSize = Math.min(allProductList.size(), pageDTOFixture.amount());
        int totalPages = TestPaginationUtils.getTotalPages(allProductList.size(), pageDTOFixture.amount());

        MvcResult result = mockMvc.perform(get(URL_PREFIX + "product")
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue))
                        .param("keyword", "testProduct"))
                .andExpect(status().isOk())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        PagingResponseDTO<AdminProductListDTO> response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        assertNotNull(response);
        assertFalse(response.content().isEmpty());
        assertFalse(response.empty());
        assertEquals(contentSize, response.content().size());
        assertEquals(totalPages, response.totalPages());
    }

    @Test
    @DisplayName(value = "전체 상품 목록 조회. 상품명 검색. 하나의 상품데이터 상품명으로 검색")
    void getProductListSearch() throws Exception {
        Product fixture = allProductList.get(0);
        int stock = fixture.getProductOptions()
                .stream()
                .mapToInt(ProductOption::getStock)
                .sum();
        Long optionCount = (long) fixture.getProductOptions().size();

        MvcResult result = mockMvc.perform(get(URL_PREFIX + "product")
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue))
                        .param("keyword", fixture.getProductName()))
                .andExpect(status().isOk())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        PagingResponseDTO<AdminProductListDTO> response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        assertNotNull(response);
        assertFalse(response.content().isEmpty());
        assertFalse(response.empty());
        assertEquals(1, response.content().size());
        assertEquals(1, response.totalPages());

        AdminProductListDTO responseData = response.content().get(0);
        assertEquals(fixture.getId(), responseData.productId());
        assertEquals(fixture.getClassification().getId(), responseData.classification());
        assertEquals(fixture.getProductName(), responseData.productName());
        assertEquals(stock, responseData.stock());
        assertEquals(optionCount, responseData.optionCount());
        assertEquals(fixture.getProductPrice(), responseData.price());
    }

    @Test
    @DisplayName(value = "전체 상품 목록 조회. 데이터가 없는 경우")
    void getProductListEmpty() throws Exception {
        productRepository.deleteAll();

        MvcResult result = mockMvc.perform(get(URL_PREFIX + "product")
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue)))
                .andExpect(status().isOk())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        PagingResponseDTO<AdminProductListDTO> response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        assertNotNull(response);
        assertTrue(response.content().isEmpty());
        assertTrue(response.empty());
        assertEquals(0, response.totalPages());
    }

    @Test
    @DisplayName(value = "전체 상품 목록 조회. 페이지 값이 0으로 전달된 경우")
    void getProductListValidationPageZero() throws Exception {
        MvcResult result = mockMvc.perform(get(URL_PREFIX + "product")
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue))
                        .param("page", "0")
                )
                .andExpect(status().isBadRequest())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        ValidationExceptionEntity response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        ErrorCode errorCode = ErrorCode.BAD_REQUEST;

        assertNotNull(response);
        assertEquals(errorCode.getHttpStatus().value(), response.errorCode());
        assertEquals(errorCode.getMessage(), response.errorMessage());
        assertFalse(response.errors().isEmpty());

        assertEquals(1, response.errors().size());

        ValidationError responseObj = response.errors().get(0);

        assertEquals("page", responseObj.field());
        assertEquals("Min", responseObj.constraint());
    }

    @Test
    @DisplayName(value = "전체 상품 목록 조회. keyword가 한글자인 경우")
    void getProductListValidationKeywordLength1() throws Exception {
        MvcResult result = mockMvc.perform(get(URL_PREFIX + "product")
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue))
                        .param("keyword", "a")
                )
                .andExpect(status().isBadRequest())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        ValidationExceptionEntity response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        ErrorCode errorCode = ErrorCode.BAD_REQUEST;

        assertNotNull(response);
        assertEquals(errorCode.getHttpStatus().value(), response.errorCode());
        assertEquals(errorCode.getMessage(), response.errorMessage());
        assertFalse(response.errors().isEmpty());

        assertEquals(1, response.errors().size());

        ValidationError responseObj = response.errors().get(0);

        assertEquals("keyword", responseObj.field());
        assertEquals("Size", responseObj.constraint());
    }

    @Test
    @DisplayName(value = "전체 상품 목록 조회. keyword가 한글자인 경우")
    void getProductListValidationPageZeroAndKeywordLength1() throws Exception {
        MvcResult result = mockMvc.perform(get(URL_PREFIX + "product")
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue))
                        .param("page", "0")
                        .param("keyword", "a")
                )
                .andExpect(status().isBadRequest())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        ValidationExceptionEntity response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        ErrorCode errorCode = ErrorCode.BAD_REQUEST;

        assertNotNull(response);
        assertEquals(errorCode.getHttpStatus().value(), response.errorCode());
        assertEquals(errorCode.getMessage(), response.errorMessage());
        assertFalse(response.errors().isEmpty());

        assertEquals(2, response.errors().size());

        Map<String, String> validationMap = new HashMap<>();
        validationMap.put("page", "Min");
        validationMap.put("keyword", "Size");

        response.errors().forEach(v -> {
            String constraint = validationMap.getOrDefault(v.field(), null);

            assertNotNull(constraint);
            assertEquals(constraint, v.constraint());
        });
    }

    @Test
    @DisplayName(value = "상품 분류 리스트 조회")
    void getClassificationList() throws Exception {
        MvcResult result = mockMvc.perform(get(URL_PREFIX + "product/classification")
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue)))
                .andExpect(status().isOk())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        List<String> response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        assertNotNull(response);
        assertFalse(response.isEmpty());
        assertEquals(classificationList.size(), response.size());
        classificationList.forEach(v -> assertTrue(response.contains(v.getId())));
    }

    @Test
    @DisplayName(value = "상품 분류 리스트 조회. 데이터가 없는 경우")
    void getClassificationListEmpty() throws Exception {
        productRepository.deleteAll();
        classificationRepository.deleteAll();
        MvcResult result = mockMvc.perform(get(URL_PREFIX + "product/classification")
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue)))
                .andExpect(status().isOk())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        List<String> response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        assertNotNull(response);
        assertTrue(response.isEmpty());
    }

    @Test
    @DisplayName(value = "상품 상세 정보 조회")
    void getProductDetail() throws Exception {
        Product fixture = allProductList.get(0);
        List<AdminProductOptionDTO> productOptionDTOFixture = fixture.getProductOptions()
                .stream()
                .map(v -> new AdminProductOptionDTO(
                        v.getId(),
                        v.getSize(),
                        v.getColor(),
                        v.getStock(),
                        v.isOpen()
                ))
                .toList();
        List<String> productThumbnailFixture = fixture.getProductThumbnails()
                .stream()
                .map(ProductThumbnail::getImageName)
                .toList();
        List<String> productInfoImageFixture = fixture.getProductInfoImages()
                .stream()
                .map(ProductInfoImage::getImageName)
                .toList();
        MvcResult result = mockMvc.perform(get(URL_PREFIX + "product/detail/" + fixture.getId())
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue)))
                .andExpect(status().isOk())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        AdminProductDetailDTO response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        assertNotNull(response);
        assertEquals(fixture.getId(), response.productId());
        assertEquals(fixture.getClassification().getId(), response.classification());
        assertEquals(fixture.getProductName(), response.productName());
        assertEquals(fixture.getThumbnail(), response.firstThumbnail());
        assertEquals(fixture.getProductPrice(), response.price());
        assertEquals(fixture.isOpen(), response.isOpen());
        assertEquals(fixture.getProductSalesQuantity(), response.sales());
        assertEquals(fixture.getProductDiscount(), response.discount());

        assertEquals(productThumbnailFixture.size(), response.thumbnailList().size());
        productThumbnailFixture.forEach(v -> assertTrue(response.thumbnailList().contains(v)));

        assertEquals(productInfoImageFixture.size(), response.infoImageList().size());
        productInfoImageFixture.forEach(v -> assertTrue(response.infoImageList().contains(v)));

        assertEquals(productOptionDTOFixture.size(), response.optionList().size());
        productOptionDTOFixture.forEach(v -> assertTrue(response.optionList().contains(v)));
    }

    @Test
    @DisplayName(value = "상품 상세 정보 조회. 상품 아이디가 잘못된 경우")
    void getProductDetailWrongProductId() throws Exception {
        MvcResult result = mockMvc.perform(get(URL_PREFIX + "product/detail/wrongProductId")
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue)))
                .andExpect(status().isBadRequest())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        ExceptionEntity response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        assertNotNull(response);
        assertEquals(ErrorCode.BAD_REQUEST.getMessage(), response.errorMessage());
    }

    @Test
    @DisplayName(value = "상품 상세 정보 조회. 상품 아이디 길이가 짧은 경우")
    void getProductDetailValidationProductIdSize() throws Exception {
        MvcResult result = mockMvc.perform(get(URL_PREFIX + "product/detail/productId")
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue)))
                .andExpect(status().isBadRequest())
                .andReturn();

        assertEquals(HandlerMethodValidationException.class.getSimpleName(), result.getResolvedException().getClass().getSimpleName());

        String content = result.getResponse().getContentAsString();
        ExceptionEntity response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        assertNotNull(response);
        assertEquals(ErrorCode.BAD_REQUEST.getHttpStatus().value(), response.errorCode());
        assertEquals(ErrorCode.BAD_REQUEST.getMessage(), response.errorMessage());
    }

    private MockMultipartFile createMockMultipartFile(String fieldName, String fileName) {
        return new MockMultipartFile(
                fieldName,
                fileName + ".jpg",
                "image/jpeg",
                fileName.getBytes()
        );
    }

    private List<MockMultipartFile> createMockMultipartFileList(String type, int count) {
        List<MockMultipartFile> result = new ArrayList<>();
        String fieldName = "thumbnail";
        String fileName = "thumb";

        if(type.equals("info")) {
            fieldName = "infoImage";
            fileName = "info";
        }

        for(int i = 0; i < count; i++) {
            fileName = fileName + i;
            result.add(createMockMultipartFile(fieldName, fileName));
        }

        return result;
    }

    private List<PatchOptionDTO> getPostPatchOptionDTOList() {
        return IntStream.range(0, 2)
                .mapToObj(v -> new PatchOptionDTO(
                        0L,
                        "postSize" + v,
                        "postColor" + v,
                        100,
                        true
                ))
                .toList();
    }

    private AdminProductPostDTO getAdminProductPostDTOFixture(List<PatchOptionDTO> optionList) {
        return new AdminProductPostDTO(
                "postProduct",
                "TOP",
                20000,
                true,
                0,
                optionList
        );
    }

    private AdminProductPatchDTO getAdminProductPatchDTOFixture(List<PatchOptionDTO> optionList) {

        return new AdminProductPatchDTO(
                "patchProduct",
                "TOP",
                20000,
                true,
                0,
                optionList
        );
    }

    private MockMultipartHttpServletRequestBuilder createPostProductMockMultipartRequestBuilder(List<PatchOptionDTO> optionList,
                                                                                     boolean imageListFlag) throws Exception {
        AdminProductPostDTO postDTO = getAdminProductPostDTOFixture(optionList);
        MockMultipartHttpServletRequestBuilder builder = createPostProductBuilder(postDTO);

        setOptionListFromBuilder(builder, optionList);
        setFirstThumbnailMockFileFromBuilder(builder);

        if(imageListFlag){
            setThumbnailMockFileFromBuilder(builder);
            setInfoImageMockFileFromBuilder(builder);
        }

        return builder;
    }

    private MockMultipartHttpServletRequestBuilder createPatchProductMockMultipartRequestBuilder(String productId,
                                                                                                 List<PatchOptionDTO> optionList,
                                                                                                 boolean imageListFlag) throws Exception {
        AdminProductPatchDTO patchDTO = getAdminProductPatchDTOFixture(optionList);
        MockMultipartHttpServletRequestBuilder builder = createPatchProductBuilder(productId, patchDTO);

        setOptionListFromBuilder(builder, optionList);
        setFirstThumbnailMockFileFromBuilder(builder);

        if(imageListFlag){
            setThumbnailMockFileFromBuilder(builder);
            setInfoImageMockFileFromBuilder(builder);
        }

        return builder;
    }

    private void setFirstThumbnailMockFileFromBuilder(MockMultipartHttpServletRequestBuilder builder) {
        MockMultipartFile firstThumbnail = createMockMultipartFile("firstThumbnail", "firstThumb");
        builder.file(firstThumbnail);
    }

    private void setThumbnailMockFileFromBuilder(MockMultipartHttpServletRequestBuilder builder) {
        List<MockMultipartFile> thumbnail = new ArrayList<>(createMockMultipartFileList("thumbnail", 3));

        thumbnail.forEach(builder::file);
    }

    private void setInfoImageMockFileFromBuilder(MockMultipartHttpServletRequestBuilder builder) {
        List<MockMultipartFile> infoImage = new ArrayList<>(createMockMultipartFileList("info", 3));

        infoImage.forEach(builder::file);
    }

    private MockMultipartHttpServletRequestBuilder createPostProductBuilder(AdminProductPostDTO requestDTO) {

        String requestURL = URL_PREFIX + "product";

        return (MockMultipartHttpServletRequestBuilder) multipart(requestURL)
                .param("productName", requestDTO.getProductName())
                .param("classification", requestDTO.getClassification())
                .param("price", String.valueOf(requestDTO.getPrice()))
                .param("isOpen", requestDTO.getIsOpen() == null ? null : String.valueOf(requestDTO.getIsOpen()))
                .param("discount", String.valueOf(requestDTO.getDiscount()));
    }

    private MockMultipartHttpServletRequestBuilder createPatchProductBuilder(String productId, AdminProductPatchDTO requestDTO) {

        String requestURL = URL_PREFIX + "product/" + productId;

        return (MockMultipartHttpServletRequestBuilder) multipart(requestURL)
                .param("productName", requestDTO.getProductName())
                .param("classification", requestDTO.getClassification())
                .param("price", String.valueOf(requestDTO.getPrice()))
                .param("isOpen", requestDTO.getIsOpen() == null ? null : String.valueOf(requestDTO.getIsOpen()))
                .param("discount", String.valueOf(requestDTO.getDiscount()))
                .with(req -> {
                    req.setMethod("PATCH");
                    return req;
                });
    }

    private void setOptionListFromBuilder(MockMultipartHttpServletRequestBuilder builder, List<PatchOptionDTO> optionList) {
        for(int i = 0; i < optionList.size(); i++) {
            PatchOptionDTO patchOptionDTO = optionList.get(i);

            builder
                    .param("optionList[" + i + "].optionId",
                            String.valueOf(patchOptionDTO.getOptionId()))
                    .param("optionList[" + i + "].size",
                            patchOptionDTO.getSize())
                    .param("optionList[" + i + "].color",
                            patchOptionDTO.getColor())
                    .param("optionList[" + i + "].optionStock",
                            String.valueOf(patchOptionDTO.getOptionStock()))
                    .param("optionList[" + i + "].optionIsOpen",
                            String.valueOf(patchOptionDTO.isOptionIsOpen()));
        }
    }

    @Test
    @DisplayName(value = "상품 추가")
    void postProduct() throws Exception {
        List<PatchOptionDTO> optionList = getPostPatchOptionDTOList();
        AdminProductPostDTO postFixture = getAdminProductPostDTOFixture(optionList);
        MockMultipartHttpServletRequestBuilder builder = createPostProductMockMultipartRequestBuilder(optionList, true);

        given(fileDomainService.setImageSaveName(any(MultipartFile.class)))
                .willReturn("saved-first-thumb.jpg");
        willDoNothing().given(fileService).imageInsert(any(MultipartFile.class), anyString());

        MvcResult result = mockMvc.perform(
                        builder
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                                .accept(MediaType.APPLICATION_JSON)
                                .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                                .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                                .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue))
                )
                .andExpect(status().isCreated())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        ResponseIdDTO<String> response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        assertNotNull(response);

        Product saveProduct = productRepository.findById(response.id()).orElse(null);
        assertNotNull(saveProduct);

        assertEquals(postFixture.getProductName(), saveProduct.getProductName());
        assertEquals(postFixture.getClassification(), saveProduct.getClassification().getId());
        assertEquals(postFixture.getPrice(), saveProduct.getProductPrice());
        assertEquals(postFixture.getIsOpen(), saveProduct.isOpen());
        assertEquals(postFixture.getDiscount(), saveProduct.getProductDiscount());
        assertEquals(postFixture.getOptionList().size(), saveProduct.getProductOptions().size());
    }

    @Test
    @DisplayName(value = "상품 추가. 옵션이 없는 상품인 경우")
    void postProductOptionIsEmpty() throws Exception {
        AdminProductPostDTO postFixture = getAdminProductPostDTOFixture(Collections.emptyList());
        MockMultipartHttpServletRequestBuilder builder = createPostProductBuilder(postFixture);
        setFirstThumbnailMockFileFromBuilder(builder);
        setInfoImageMockFileFromBuilder(builder);

        given(fileDomainService.setImageSaveName(any(MultipartFile.class)))
                .willReturn("saved-first-thumb.jpg");
        willDoNothing().given(fileService).imageInsert(any(MultipartFile.class), anyString());

        MvcResult result = mockMvc.perform(
                        builder
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                                .accept(MediaType.APPLICATION_JSON)
                                .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                                .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                                .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue))
                )
                .andExpect(status().isCreated())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        ResponseIdDTO<String> response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        assertNotNull(response);

        Product saveProduct = productRepository.findById(response.id()).orElse(null);
        assertNotNull(saveProduct);

        assertEquals(postFixture.getProductName(), saveProduct.getProductName());
        assertEquals(postFixture.getClassification(), saveProduct.getClassification().getId());
        assertEquals(postFixture.getPrice(), saveProduct.getProductPrice());
        assertEquals(postFixture.getIsOpen(), saveProduct.isOpen());
        assertEquals(postFixture.getDiscount(), saveProduct.getProductDiscount());
        assertTrue(saveProduct.getProductOptions().isEmpty());
    }

    @Test
    @DisplayName(value = "상품 추가. 상품명이 null인 경우")
    void postProductValidationProductNameIsNull() throws Exception {
        List<PatchOptionDTO> optionList = getPostPatchOptionDTOList();
        AdminProductPostDTO postDTO = new AdminProductPostDTO(
                null,
                "TOP",
                20000,
                true,
                0,
                optionList
        );;
        MockMultipartHttpServletRequestBuilder builder = createPostProductBuilder(postDTO);
        setFirstThumbnailMockFileFromBuilder(builder);
        setInfoImageMockFileFromBuilder(builder);

        MvcResult result = mockMvc.perform(
                        builder
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                                .accept(MediaType.APPLICATION_JSON)
                                .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                                .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                                .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue))
                )
                .andExpect(status().isBadRequest())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        ValidationExceptionEntity response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        ErrorCode errorCode = ErrorCode.BAD_REQUEST;

        verify(fileDomainService, never()).setImageSaveName(any(MultipartFile.class));
        verify(fileService, never()).imageInsert(any(MultipartFile.class), anyString());

        assertNotNull(response);
        assertEquals(errorCode.getHttpStatus().value(), response.errorCode());
        assertEquals(errorCode.getMessage(), response.errorMessage());
        assertFalse(response.errors().isEmpty());

        assertEquals(1, response.errors().size());

        ValidationError responseObj = response.errors().get(0);

        assertEquals("productName", responseObj.field());
        assertEquals("NotBlank", responseObj.constraint());
    }

    @Test
    @DisplayName(value = "상품 추가. 상품명이 Blank인 경우")
    void postProductValidationProductNameIsBlank() throws Exception {
        List<PatchOptionDTO> optionList = getPostPatchOptionDTOList();
        AdminProductPostDTO postDTO = new AdminProductPostDTO(
                "",
                "TOP",
                20000,
                true,
                0,
                optionList
        );;
        MockMultipartHttpServletRequestBuilder builder = createPostProductBuilder(postDTO);
        setFirstThumbnailMockFileFromBuilder(builder);
        setInfoImageMockFileFromBuilder(builder);

        MvcResult result = mockMvc.perform(
                        builder
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                                .accept(MediaType.APPLICATION_JSON)
                                .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                                .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                                .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue))
                )
                .andExpect(status().isBadRequest())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        ValidationExceptionEntity response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        ErrorCode errorCode = ErrorCode.BAD_REQUEST;

        verify(fileDomainService, never()).setImageSaveName(any(MultipartFile.class));
        verify(fileService, never()).imageInsert(any(MultipartFile.class), anyString());

        assertNotNull(response);
        assertEquals(errorCode.getHttpStatus().value(), response.errorCode());
        assertEquals(errorCode.getMessage(), response.errorMessage());
        assertFalse(response.errors().isEmpty());

        assertEquals(2, response.errors().size());

        List<String> validationConstraintList = List.of("Length", "NotBlank");

        response.errors().forEach(v -> {
            assertEquals("productName", v.field());
            assertTrue(validationConstraintList.contains(v.constraint()));
        });
    }

    @Test
    @DisplayName(value = "상품 추가. 상품명이 한글자인 경우")
    void postProductValidationProductNameLength1() throws Exception {
        List<PatchOptionDTO> optionList = getPostPatchOptionDTOList();
        AdminProductPostDTO postDTO = new AdminProductPostDTO(
                "a",
                "TOP",
                20000,
                true,
                0,
                optionList
        );;
        MockMultipartHttpServletRequestBuilder builder = createPostProductBuilder(postDTO);
        setFirstThumbnailMockFileFromBuilder(builder);
        setInfoImageMockFileFromBuilder(builder);

        MvcResult result = mockMvc.perform(
                        builder
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                                .accept(MediaType.APPLICATION_JSON)
                                .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                                .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                                .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue))
                )
                .andExpect(status().isBadRequest())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        ValidationExceptionEntity response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        ErrorCode errorCode = ErrorCode.BAD_REQUEST;

        verify(fileDomainService, never()).setImageSaveName(any(MultipartFile.class));
        verify(fileService, never()).imageInsert(any(MultipartFile.class), anyString());

        assertNotNull(response);
        assertEquals(errorCode.getHttpStatus().value(), response.errorCode());
        assertEquals(errorCode.getMessage(), response.errorMessage());
        assertFalse(response.errors().isEmpty());

        assertEquals(1, response.errors().size());

        ValidationError responseObj = response.errors().get(0);

        assertEquals("productName", responseObj.field());
        assertEquals("Length", responseObj.constraint());
    }

    @Test
    @DisplayName(value = "상품 추가. 상품 분류가 null인 경우")
    void postProductValidationClassificationIsNull() throws Exception {
        List<PatchOptionDTO> optionList = getPostPatchOptionDTOList();
        AdminProductPostDTO postDTO = new AdminProductPostDTO(
                "testName",
                null,
                20000,
                true,
                0,
                optionList
        );;
        MockMultipartHttpServletRequestBuilder builder = createPostProductBuilder(postDTO);
        setFirstThumbnailMockFileFromBuilder(builder);
        setInfoImageMockFileFromBuilder(builder);

        MvcResult result = mockMvc.perform(
                        builder
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                                .accept(MediaType.APPLICATION_JSON)
                                .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                                .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                                .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue))
                )
                .andExpect(status().isBadRequest())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        ValidationExceptionEntity response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        ErrorCode errorCode = ErrorCode.BAD_REQUEST;

        verify(fileDomainService, never()).setImageSaveName(any(MultipartFile.class));
        verify(fileService, never()).imageInsert(any(MultipartFile.class), anyString());

        assertNotNull(response);
        assertEquals(errorCode.getHttpStatus().value(), response.errorCode());
        assertEquals(errorCode.getMessage(), response.errorMessage());
        assertFalse(response.errors().isEmpty());

        assertEquals(1, response.errors().size());

        ValidationError responseObj = response.errors().get(0);

        assertEquals("classification", responseObj.field());
        assertEquals("NotBlank", responseObj.constraint());
    }

    @Test
    @DisplayName(value = "상품 추가. 상품 분류가 Blank인 경우")
    void postProductValidationClassificationIsBlank() throws Exception {
        List<PatchOptionDTO> optionList = getPostPatchOptionDTOList();
        AdminProductPostDTO postDTO = new AdminProductPostDTO(
                "testName",
                "",
                20000,
                true,
                0,
                optionList
        );;
        MockMultipartHttpServletRequestBuilder builder = createPostProductBuilder(postDTO);
        setFirstThumbnailMockFileFromBuilder(builder);
        setInfoImageMockFileFromBuilder(builder);

        MvcResult result = mockMvc.perform(
                        builder
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                                .accept(MediaType.APPLICATION_JSON)
                                .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                                .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                                .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue))
                )
                .andExpect(status().isBadRequest())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        ValidationExceptionEntity response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        ErrorCode errorCode = ErrorCode.BAD_REQUEST;

        verify(fileDomainService, never()).setImageSaveName(any(MultipartFile.class));
        verify(fileService, never()).imageInsert(any(MultipartFile.class), anyString());

        assertNotNull(response);
        assertEquals(errorCode.getHttpStatus().value(), response.errorCode());
        assertEquals(errorCode.getMessage(), response.errorMessage());
        assertFalse(response.errors().isEmpty());

        assertEquals(2, response.errors().size());
        List<String> validationConstraintList = List.of("Length", "NotBlank");

        response.errors().forEach(v -> {
            assertEquals("classification", v.field());
            assertTrue(validationConstraintList.contains(v.constraint()));
        });
    }

    @Test
    @DisplayName(value = "상품 추가. 상품 분류가 한글자인 경우")
    void postProductValidationClassificationLength1() throws Exception {
        List<PatchOptionDTO> optionList = getPostPatchOptionDTOList();
        AdminProductPostDTO postDTO = new AdminProductPostDTO(
                "testName",
                "T",
                20000,
                true,
                0,
                optionList
        );;
        MockMultipartHttpServletRequestBuilder builder = createPostProductBuilder(postDTO);
        setFirstThumbnailMockFileFromBuilder(builder);
        setInfoImageMockFileFromBuilder(builder);

        MvcResult result = mockMvc.perform(
                        builder
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                                .accept(MediaType.APPLICATION_JSON)
                                .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                                .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                                .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue))
                )
                .andExpect(status().isBadRequest())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        ValidationExceptionEntity response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        ErrorCode errorCode = ErrorCode.BAD_REQUEST;

        verify(fileDomainService, never()).setImageSaveName(any(MultipartFile.class));
        verify(fileService, never()).imageInsert(any(MultipartFile.class), anyString());

        assertNotNull(response);
        assertEquals(errorCode.getHttpStatus().value(), response.errorCode());
        assertEquals(errorCode.getMessage(), response.errorMessage());
        assertFalse(response.errors().isEmpty());

        assertEquals(1, response.errors().size());

        ValidationError responseObj = response.errors().get(0);

        assertEquals("classification", responseObj.field());
        assertEquals("Length", responseObj.constraint());
    }

    @Test
    @DisplayName(value = "상품 추가. 가격이 100원 미만인 경우")
    void postProductValidationPriceLT100() throws Exception {
        List<PatchOptionDTO> optionList = getPostPatchOptionDTOList();
        AdminProductPostDTO postDTO = new AdminProductPostDTO(
                "testName",
                "TOP",
                50,
                true,
                0,
                optionList
        );;
        MockMultipartHttpServletRequestBuilder builder = createPostProductBuilder(postDTO);
        setFirstThumbnailMockFileFromBuilder(builder);
        setInfoImageMockFileFromBuilder(builder);

        MvcResult result = mockMvc.perform(
                        builder
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                                .accept(MediaType.APPLICATION_JSON)
                                .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                                .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                                .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue))
                )
                .andExpect(status().isBadRequest())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        ValidationExceptionEntity response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        ErrorCode errorCode = ErrorCode.BAD_REQUEST;

        verify(fileDomainService, never()).setImageSaveName(any(MultipartFile.class));
        verify(fileService, never()).imageInsert(any(MultipartFile.class), anyString());

        assertNotNull(response);
        assertEquals(errorCode.getHttpStatus().value(), response.errorCode());
        assertEquals(errorCode.getMessage(), response.errorMessage());
        assertFalse(response.errors().isEmpty());

        assertEquals(1, response.errors().size());

        ValidationError responseObj = response.errors().get(0);

        assertEquals("price", responseObj.field());
        assertEquals("Min", responseObj.constraint());
    }

    @Test
    @DisplayName(value = "상품 추가. 공개 여부가 null인 경우")
    void postProductValidationIsOpenIsNull() throws Exception {
        List<PatchOptionDTO> optionList = getPostPatchOptionDTOList();
        AdminProductPostDTO postDTO = new AdminProductPostDTO(
                "testName",
                "TOP",
                20000,
                null,
                0,
                optionList
        );;
        MockMultipartHttpServletRequestBuilder builder = createPostProductBuilder(postDTO);
        setFirstThumbnailMockFileFromBuilder(builder);
        setInfoImageMockFileFromBuilder(builder);

        MvcResult result = mockMvc.perform(
                        builder
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                                .accept(MediaType.APPLICATION_JSON)
                                .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                                .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                                .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue))
                )
                .andExpect(status().isBadRequest())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        ValidationExceptionEntity response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        ErrorCode errorCode = ErrorCode.BAD_REQUEST;

        verify(fileDomainService, never()).setImageSaveName(any(MultipartFile.class));
        verify(fileService, never()).imageInsert(any(MultipartFile.class), anyString());

        assertNotNull(response);
        assertEquals(errorCode.getHttpStatus().value(), response.errorCode());
        assertEquals(errorCode.getMessage(), response.errorMessage());
        assertFalse(response.errors().isEmpty());

        assertEquals(1, response.errors().size());

        ValidationError responseObj = response.errors().get(0);

        assertEquals("isOpen", responseObj.field());
        assertEquals("NotNull", responseObj.constraint());
    }

    @Test
    @DisplayName(value = "상품 추가. discount값이 0보다 작은 경우")
    void postProductValidationDiscountLT0() throws Exception {
        List<PatchOptionDTO> optionList = getPostPatchOptionDTOList();
        AdminProductPostDTO postDTO = new AdminProductPostDTO(
                "testName",
                "TOP",
                20000,
                true,
                -1,
                optionList
        );;
        MockMultipartHttpServletRequestBuilder builder = createPostProductBuilder(postDTO);
        setFirstThumbnailMockFileFromBuilder(builder);
        setInfoImageMockFileFromBuilder(builder);

        MvcResult result = mockMvc.perform(
                        builder
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                                .accept(MediaType.APPLICATION_JSON)
                                .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                                .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                                .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue))
                )
                .andExpect(status().isBadRequest())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        ValidationExceptionEntity response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        ErrorCode errorCode = ErrorCode.BAD_REQUEST;

        verify(fileDomainService, never()).setImageSaveName(any(MultipartFile.class));
        verify(fileService, never()).imageInsert(any(MultipartFile.class), anyString());

        assertNotNull(response);
        assertEquals(errorCode.getHttpStatus().value(), response.errorCode());
        assertEquals(errorCode.getMessage(), response.errorMessage());
        assertFalse(response.errors().isEmpty());

        assertEquals(1, response.errors().size());

        ValidationError responseObj = response.errors().get(0);

        assertEquals("discount", responseObj.field());
        assertEquals("Min", responseObj.constraint());
    }

    @Test
    @DisplayName(value = "상품 추가. RequestDTO의 모든 필드 유효성 검사에 실패한 경우")
    void postProductValidationAllFieldValidationFail() throws Exception {
        List<PatchOptionDTO> optionList = getPostPatchOptionDTOList();
        AdminProductPostDTO postDTO = new AdminProductPostDTO(
                "t",
                "T",
                50,
                null,
                -1,
                optionList
        );;
        MockMultipartHttpServletRequestBuilder builder = createPostProductBuilder(postDTO);
        setFirstThumbnailMockFileFromBuilder(builder);
        setInfoImageMockFileFromBuilder(builder);

        MvcResult result = mockMvc.perform(
                        builder
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                                .accept(MediaType.APPLICATION_JSON)
                                .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                                .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                                .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue))
                )
                .andExpect(status().isBadRequest())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        ValidationExceptionEntity response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        ErrorCode errorCode = ErrorCode.BAD_REQUEST;

        verify(fileDomainService, never()).setImageSaveName(any(MultipartFile.class));
        verify(fileService, never()).imageInsert(any(MultipartFile.class), anyString());

        assertNotNull(response);
        assertEquals(errorCode.getHttpStatus().value(), response.errorCode());
        assertEquals(errorCode.getMessage(), response.errorMessage());
        assertFalse(response.errors().isEmpty());

        assertEquals(5, response.errors().size());

        Map<String, String> validationMap = new HashMap<>();
        validationMap.put("productName", "Length");
        validationMap.put("classification", "Length");
        validationMap.put("price", "Min");
        validationMap.put("isOpen", "NotNull");
        validationMap.put("discount", "Min");

        response.errors().forEach(v -> {
            String constraint = validationMap.getOrDefault(v.field(), null);

            assertNotNull(constraint);
            assertEquals(constraint, v.constraint());
        });
    }

    @Test
    @DisplayName(value = "상품 추가. 옵션 데이터의 옵션 아이디가 0보다 작은 경우")
    void postProductValidationOptionIdLT0() throws Exception {
        List<PatchOptionDTO> optionList = new ArrayList<>();
        optionList.add(
                new PatchOptionDTO(
                        -1L,
                        "postSize",
                        "postColor",
                        100,
                        true
                )
        );
        AdminProductPostDTO postDTO = getAdminProductPostDTOFixture(optionList);
        MockMultipartHttpServletRequestBuilder builder = createPostProductBuilder(postDTO);
        setOptionListFromBuilder(builder, optionList);
        setFirstThumbnailMockFileFromBuilder(builder);
        setInfoImageMockFileFromBuilder(builder);

        MvcResult result = mockMvc.perform(
                        builder
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                                .accept(MediaType.APPLICATION_JSON)
                                .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                                .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                                .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue))
                )
                .andExpect(status().isBadRequest())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        ExceptionEntity response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        ErrorCode errorCode = ErrorCode.BAD_REQUEST;

        verify(fileDomainService, never()).setImageSaveName(any(MultipartFile.class));
        verify(fileService, never()).imageInsert(any(MultipartFile.class), anyString());

        assertNotNull(response);
        assertEquals(errorCode.getHttpStatus().value(), response.errorCode());
        assertEquals(errorCode.getMessage(), response.errorMessage());
    }

    @Test
    @DisplayName(value = "상품 추가. 옵션 데이터의 옵션 재고가 0보다 작은 경우")
    void postProductValidationOptionStockLT0() throws Exception {
        List<PatchOptionDTO> optionList = new ArrayList<>();
        optionList.add(
                new PatchOptionDTO(
                        0L,
                        "postSize",
                        "postColor",
                        -1,
                        true
                )
        );
        AdminProductPostDTO postDTO = getAdminProductPostDTOFixture(optionList);
        MockMultipartHttpServletRequestBuilder builder = createPostProductBuilder(postDTO);
        setOptionListFromBuilder(builder, optionList);
        setFirstThumbnailMockFileFromBuilder(builder);
        setInfoImageMockFileFromBuilder(builder);

        MvcResult result = mockMvc.perform(
                        builder
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                                .accept(MediaType.APPLICATION_JSON)
                                .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                                .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                                .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue))
                )
                .andExpect(status().isBadRequest())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        ExceptionEntity response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        ErrorCode errorCode = ErrorCode.BAD_REQUEST;

        verify(fileDomainService, never()).setImageSaveName(any(MultipartFile.class));
        verify(fileService, never()).imageInsert(any(MultipartFile.class), anyString());

        assertNotNull(response);
        assertEquals(errorCode.getHttpStatus().value(), response.errorCode());
        assertEquals(errorCode.getMessage(), response.errorMessage());
    }

    @Test
    @DisplayName(value = "상품 추가. 대표 썸네일이 없는 경우")
    void postProductValidationFirstThumbnailIsNull() throws Exception {
        List<PatchOptionDTO> optionList = getPostPatchOptionDTOList();
        AdminProductPostDTO postDTO = getAdminProductPostDTOFixture(optionList);
        MockMultipartHttpServletRequestBuilder builder = createPostProductBuilder(postDTO);
        setOptionListFromBuilder(builder, optionList);
        setInfoImageMockFileFromBuilder(builder);

        MvcResult result = mockMvc.perform(
                        builder
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                                .accept(MediaType.APPLICATION_JSON)
                                .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                                .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                                .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue))
                )
                .andExpect(status().isBadRequest())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        ValidationExceptionEntity response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        ErrorCode errorCode = ErrorCode.BAD_REQUEST;

        verify(fileDomainService, never()).setImageSaveName(any(MultipartFile.class));
        verify(fileService, never()).imageInsert(any(MultipartFile.class), anyString());

        assertNotNull(response);
        assertEquals(errorCode.getHttpStatus().value(), response.errorCode());
        assertEquals(errorCode.getMessage(), response.errorMessage());
        assertFalse(response.errors().isEmpty());

        assertEquals(1, response.errors().size());
        ValidationError responseObj = response.errors().get(0);

        assertEquals("firstThumbnail", responseObj.field());
        assertEquals("NotNull", responseObj.constraint());
    }

    @Test
    @DisplayName(value = "상품 추가. 상품 정보 이미지가 없는 경우")
    void postProductValidationInfoImageIsNull() throws Exception {
        List<PatchOptionDTO> optionList = getPostPatchOptionDTOList();
        AdminProductPostDTO postDTO = getAdminProductPostDTOFixture(optionList);
        MockMultipartHttpServletRequestBuilder builder = createPostProductBuilder(postDTO);
        setOptionListFromBuilder(builder, optionList);
        setFirstThumbnailMockFileFromBuilder(builder);

        MvcResult result = mockMvc.perform(
                        builder
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                                .accept(MediaType.APPLICATION_JSON)
                                .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                                .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                                .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue))
                )
                .andExpect(status().isBadRequest())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        ValidationExceptionEntity response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        ErrorCode errorCode = ErrorCode.BAD_REQUEST;

        verify(fileDomainService, never()).setImageSaveName(any(MultipartFile.class));
        verify(fileService, never()).imageInsert(any(MultipartFile.class), anyString());

        assertNotNull(response);
        assertEquals(errorCode.getHttpStatus().value(), response.errorCode());
        assertEquals(errorCode.getMessage(), response.errorMessage());
        assertFalse(response.errors().isEmpty());

        assertEquals(1, response.errors().size());
        ValidationError responseObj = response.errors().get(0);

        assertEquals("infoImage", responseObj.field());
        assertEquals("NotEmpty", responseObj.constraint());
    }

    @Test
    @DisplayName(value = "수정할 상품 데이터 조회")
    void getPatchProductDetail() throws Exception {
        Product fixture = allProductList.get(0);
        List<AdminProductOptionDTO> productOptionDTOFixture = fixture.getProductOptions()
                .stream()
                .map(v -> new AdminProductOptionDTO(
                        v.getId(),
                        v.getSize(),
                        v.getColor(),
                        v.getStock(),
                        v.isOpen()
                ))
                .toList();
        List<String> productThumbnailFixture = fixture.getProductThumbnails()
                .stream()
                .map(ProductThumbnail::getImageName)
                .toList();
        List<String> productInfoImageFixture = fixture.getProductInfoImages()
                .stream()
                .map(ProductInfoImage::getImageName)
                .toList();

        MvcResult result = mockMvc.perform(get(URL_PREFIX + "product/patch/" + fixture.getId())
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue)))
                .andExpect(status().isOk())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        AdminProductPatchDataDTO response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        assertNotNull(response);
        assertEquals(fixture.getId(), response.productId());
        assertEquals(fixture.getProductName(), response.productName());
        assertEquals(fixture.getClassification().getId(), response.classificationId());
        assertEquals(fixture.getThumbnail(), response.firstThumbnail());
        assertEquals(fixture.getProductPrice(), response.price());
        assertEquals(fixture.isOpen(), response.isOpen());
        assertEquals(fixture.getProductDiscount(), response.discount());

        assertEquals(productOptionDTOFixture.size(), response.optionList().size());
        productOptionDTOFixture.forEach(v -> assertTrue(response.optionList().contains(v)));

        assertEquals(productThumbnailFixture.size(), response.thumbnailList().size());
        productThumbnailFixture.forEach(v -> assertTrue(response.thumbnailList().contains(v)));

        assertEquals(productInfoImageFixture.size(), response.infoImageList().size());
        productInfoImageFixture.forEach(v -> assertTrue(response.infoImageList().contains(v)));
    }

    @Test
    @DisplayName(value = "상품 상세 정보 조회. 상품 아이디가 잘못된 경우")
    void getPatchProductDetailWrongProductId() throws Exception {
        MvcResult result = mockMvc.perform(get(URL_PREFIX + "product/patch/wrongProductId")
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue)))
                .andExpect(status().isBadRequest())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        ExceptionEntity response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        assertNotNull(response);
        assertEquals(ErrorCode.BAD_REQUEST.getMessage(), response.errorMessage());
    }

    @Test
    @DisplayName(value = "상품 상세 정보 조회. 상품 아이디 길이가 짧은 경우")
    void getPatchProductDetailValidationProductIdSize() throws Exception {
        MvcResult result = mockMvc.perform(get(URL_PREFIX + "product/patch/productId")
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue)))
                .andExpect(status().isBadRequest())
                .andReturn();

        assertEquals(HandlerMethodValidationException.class.getSimpleName(), result.getResolvedException().getClass().getSimpleName());

        String content = result.getResponse().getContentAsString();
        ExceptionEntity response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        assertNotNull(response);
        assertEquals(ErrorCode.BAD_REQUEST.getHttpStatus().value(), response.errorCode());
        assertEquals(ErrorCode.BAD_REQUEST.getMessage(), response.errorMessage());
    }

    @Test
    @DisplayName(value = "상품 수정")
    void patchProduct() throws Exception {
        Product fixture = allProductList.get(0);
        List<ProductOption> optionFixture = fixture.getProductOptions();
        List<PatchOptionDTO> patchOptionList = new ArrayList<>();
        for(int i = 1; i < optionFixture.size(); i++) {
            ProductOption option = optionFixture.get(i);
            patchOptionList.add(
                    new PatchOptionDTO(
                            option.getId(),
                            "patchSize" + i,
                            "patchColor" + i,
                            option.getStock() + 100,
                            true
                    )
            );
        }
        patchOptionList.addAll(getPostPatchOptionDTOList());

        AdminProductPatchDTO patchFixture = getAdminProductPatchDTOFixture(patchOptionList);
        int optionCountFixture = fixture.getProductOptions().size() + 1;
        MockMultipartHttpServletRequestBuilder builder = createPatchProductMockMultipartRequestBuilder(fixture.getId(), patchOptionList, true);
        List<Long> deleteOptionIds = List.of(fixture.getProductOptions().get(0).getId());
        long deleteOptionId = deleteOptionIds.get(0);
        String deleteOptionIdsJson = om.writeValueAsString(deleteOptionIds);
        MockMultipartFile deleteOptionList = new MockMultipartFile(
                "deleteOptionList",
                "",
                "application/json",
                deleteOptionIdsJson.getBytes(StandardCharsets.UTF_8)
        );

        builder.file(deleteOptionList)
                .param("deleteFirstThumbnail", fixture.getThumbnail())
                .param("deleteThumbnail", fixture.getProductThumbnails().get(0).getImageName())
                .param("deleteInfoImage", fixture.getProductInfoImages().get(0).getImageName());

        given(fileDomainService.setImageSaveName(any(MultipartFile.class)))
                .willReturn("saved-first-thumb.jpg");
        willDoNothing().given(fileService).imageInsert(any(MultipartFile.class), anyString());
        willDoNothing().given(fileService).deleteImage(anyString());

        MvcResult result = mockMvc.perform(
                        builder
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                                .accept(MediaType.APPLICATION_JSON)
                                .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                                .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                                .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue))
                )
                .andExpect(status().isOk())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        ResponseIdDTO<String> response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        assertNotNull(response);

        em.flush();
        em.clear();

        Product saveProduct = productRepository.findById(response.id()).orElse(null);
        assertNotNull(saveProduct);

        assertEquals(patchFixture.getProductName(), saveProduct.getProductName());
        assertEquals(patchFixture.getClassification(), saveProduct.getClassification().getId());
        assertEquals(patchFixture.getPrice(), saveProduct.getProductPrice());
        assertEquals(patchFixture.getIsOpen(), saveProduct.isOpen());
        assertEquals(patchFixture.getDiscount(), saveProduct.getProductDiscount());
        assertEquals(optionCountFixture, saveProduct.getProductOptions().size());
        boolean flag = false;
        for(ProductOption saveOption : saveProduct.getProductOptions()) {
            if(deleteOptionId == saveOption.getId()){
                flag = true;
                break;
            }
        }

        if(flag)
            fail("Deleted deleteOptionId Exists");
    }

    @Test
    @DisplayName(value = "상품 수정. 대표 썸네일 추가 파일이 존재하지만, 삭제 파일명은 존재하지 않는 경우 400 발생")
    void patchProductWrongFirstThumb() throws Exception {
        Product fixture = allProductList.get(0);
        List<ProductOption> optionFixture = fixture.getProductOptions();
        List<PatchOptionDTO> patchOptionList = new ArrayList<>();
        for (int i = 1; i < optionFixture.size(); i++) {
            ProductOption option = optionFixture.get(i);
            patchOptionList.add(
                    new PatchOptionDTO(
                            option.getId(),
                            "patchSize" + i,
                            "patchColor" + i,
                            option.getStock() + 100,
                            true
                    )
            );
        }
        patchOptionList.addAll(getPostPatchOptionDTOList());

        int optionCountFixture = fixture.getProductOptions().size();
        MockMultipartHttpServletRequestBuilder builder = createPatchProductMockMultipartRequestBuilder(fixture.getId(), patchOptionList, true);
        List<Long> deleteOptionIds = List.of(fixture.getProductOptions().get(0).getId());
        long deleteOptionId = deleteOptionIds.get(0);
        String deleteOptionIdsJson = om.writeValueAsString(deleteOptionIds);
        MockMultipartFile deleteOptionList = new MockMultipartFile(
                "deleteOptionList",
                "",
                "application/json",
                deleteOptionIdsJson.getBytes(StandardCharsets.UTF_8)
        );

        builder.file(deleteOptionList)
                .param("deleteThumbnail", fixture.getProductThumbnails().get(0).getImageName())
                .param("deleteInfoImage", fixture.getProductInfoImages().get(0).getImageName());

        given(fileDomainService.setImageSaveName(any(MultipartFile.class)))
                .willReturn("saved-first-thumb.jpg");
        willDoNothing().given(fileService).imageInsert(any(MultipartFile.class), anyString());
        willDoNothing().given(fileService).deleteImage(anyString());

        MvcResult result = mockMvc.perform(
                        builder
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                                .accept(MediaType.APPLICATION_JSON)
                                .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                                .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                                .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue))
                )
                .andExpect(status().isBadRequest())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        ExceptionEntity response = om.readValue(
                content,
                new TypeReference<>() {
                }
        );

        em.flush();
        em.clear();

        assertNotNull(response);
        assertEquals(ErrorCode.BAD_REQUEST.getMessage(), response.errorMessage());

        Product saveProduct = productRepository.findById(fixture.getId()).orElse(null);
        assertNotNull(saveProduct);
        assertEquals(optionCountFixture, saveProduct.getProductOptions().size());
    }

    @Test
    @DisplayName(value = "상품 수정. 정보 이미지를 전부 삭제하기만 하고 추가하지 않는 경우")
    void patchProductAllInfoImageDelete() throws Exception {
        Product fixture = allProductList.get(0);
        List<ProductOption> optionFixture = fixture.getProductOptions();
        List<PatchOptionDTO> patchOptionList = new ArrayList<>();
        for(int i = 1; i < optionFixture.size(); i++) {
            ProductOption option = optionFixture.get(i);
            patchOptionList.add(
                    new PatchOptionDTO(
                            option.getId(),
                            "patchSize" + i,
                            "patchColor" + i,
                            option.getStock() + 100,
                            true
                    )
            );
        }
        patchOptionList.addAll(getPostPatchOptionDTOList());

        int optionCountFixture = fixture.getProductOptions().size() + 1;
        MockMultipartHttpServletRequestBuilder builder = createPatchProductMockMultipartRequestBuilder(fixture.getId(), patchOptionList, false);
        List<Long> deleteOptionIds = List.of(fixture.getProductOptions().get(0).getId());
        String deleteOptionIdsJson = om.writeValueAsString(deleteOptionIds);
        MockMultipartFile deleteOptionList = new MockMultipartFile(
                "deleteOptionList",
                "",
                "application/json",
                deleteOptionIdsJson.getBytes(StandardCharsets.UTF_8)
        );
        List<String> deleteInfoImageNames = fixture.getProductInfoImages().stream()
                                .map(ProductInfoImage::getImageName)
                                .toList();

        builder.file(deleteOptionList)
                .param("deleteFirstThumbnail", fixture.getThumbnail())
                .param("deleteThumbnail", fixture.getProductThumbnails().get(0).getImageName());

        deleteInfoImageNames.forEach(v -> {
            builder.param("deleteInfoImage", v);
        });

        MvcResult result = mockMvc.perform(
                        builder
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                                .accept(MediaType.APPLICATION_JSON)
                                .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                                .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                                .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue))
                )
                .andExpect(status().isBadRequest())
                .andReturn();

        verify(fileDomainService, never()).setImageSaveName(any(MultipartFile.class));
        verify(fileService, never()).imageInsert(any(MultipartFile.class), anyString());
        verify(fileService, never()).deleteImage(anyString());

        String content = result.getResponse().getContentAsString();
        ExceptionEntity response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        ErrorCode errorCode = ErrorCode.BAD_REQUEST;

        assertNotNull(response);
        assertEquals(errorCode.getHttpStatus().value(), response.errorCode());
        assertEquals(errorCode.getMessage(), response.errorMessage());
    }

    @Test
    @DisplayName(value = "상품 수정. 상품 아이디가 잘못된 경우")
    void patchProductWrongProductId() throws Exception {
        Product fixture = allProductList.get(0);
        List<ProductOption> optionFixture = fixture.getProductOptions();
        List<PatchOptionDTO> patchOptionList = new ArrayList<>();
        for(int i = 1; i < optionFixture.size(); i++) {
            ProductOption option = optionFixture.get(i);
            patchOptionList.add(
                    new PatchOptionDTO(
                            option.getId(),
                            "patchSize" + i,
                            "patchColor" + i,
                            option.getStock() + 100,
                            true
                    )
            );
        }
        patchOptionList.addAll(getPostPatchOptionDTOList());

        MockMultipartHttpServletRequestBuilder builder = createPatchProductMockMultipartRequestBuilder("wrongProductId", patchOptionList, true);
        List<Long> deleteOptionIds = List.of(fixture.getProductOptions().get(0).getId());
        String deleteOptionIdsJson = om.writeValueAsString(deleteOptionIds);
        MockMultipartFile deleteOptionList = new MockMultipartFile(
                "deleteOptionList",
                "",
                "application/json",
                deleteOptionIdsJson.getBytes(StandardCharsets.UTF_8)
        );

        builder.file(deleteOptionList)
                .param("deleteFirstThumbnail", fixture.getThumbnail())
                .param("deleteThumbnail", fixture.getProductThumbnails().get(0).getImageName())
                .param("deleteInfoImage", fixture.getProductInfoImages().get(0).getImageName());

        given(fileDomainService.setImageSaveName(any(MultipartFile.class)))
                .willReturn("saved-first-thumb.jpg");
        willDoNothing().given(fileService).imageInsert(any(MultipartFile.class), anyString());
        willDoNothing().given(fileService).deleteImage(anyString());

        MvcResult result = mockMvc.perform(
                        builder
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                                .accept(MediaType.APPLICATION_JSON)
                                .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                                .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                                .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue))
                )
                .andExpect(status().isBadRequest())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        ExceptionEntity response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        assertNotNull(response);
        assertEquals(ErrorCode.BAD_REQUEST.getMessage(), response.errorMessage());
    }

    @Test
    @DisplayName(value = "상품 수정. 상품 아이디가 짧은 경우")
    void patchProductValidationProductIdIsShort() throws Exception {
        Product fixture = allProductList.get(0);
        List<ProductOption> optionFixture = fixture.getProductOptions();
        List<PatchOptionDTO> patchOptionList = new ArrayList<>();
        for(int i = 1; i < optionFixture.size(); i++) {
            ProductOption option = optionFixture.get(i);
            patchOptionList.add(
                    new PatchOptionDTO(
                            option.getId(),
                            "patchSize" + i,
                            "patchColor" + i,
                            option.getStock() + 100,
                            true
                    )
            );
        }
        patchOptionList.addAll(getPostPatchOptionDTOList());

        MockMultipartHttpServletRequestBuilder builder = createPatchProductMockMultipartRequestBuilder("productId", patchOptionList, true);
        List<Long> deleteOptionIds = List.of(fixture.getProductOptions().get(0).getId());
        String deleteOptionIdsJson = om.writeValueAsString(deleteOptionIds);
        MockMultipartFile deleteOptionList = new MockMultipartFile(
                "deleteOptionList",
                "",
                "application/json",
                deleteOptionIdsJson.getBytes(StandardCharsets.UTF_8)
        );

        builder.file(deleteOptionList)
                .param("deleteFirstThumbnail", fixture.getThumbnail())
                .param("deleteThumbnail", fixture.getProductThumbnails().get(0).getImageName())
                .param("deleteInfoImage", fixture.getProductInfoImages().get(0).getImageName());

        MvcResult result = mockMvc.perform(
                        builder
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                                .accept(MediaType.APPLICATION_JSON)
                                .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                                .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                                .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue))
                )
                .andExpect(status().isBadRequest())
                .andReturn();

        assertEquals(HandlerMethodValidationException.class.getSimpleName(), result.getResolvedException().getClass().getSimpleName());
        verify(fileDomainService, never()).setImageSaveName(any(MultipartFile.class));
        verify(fileService, never()).imageInsert(any(MultipartFile.class), anyString());
        verify(fileService, never()).deleteImage(anyString());

        String content = result.getResponse().getContentAsString();
        ExceptionEntity response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        ErrorCode errorCode = ErrorCode.BAD_REQUEST;

        assertNotNull(response);
        assertEquals(errorCode.getHttpStatus().value(), response.errorCode());
        assertEquals(errorCode.getMessage(), response.errorMessage());
    }

    @Test
    @DisplayName(value = "상품 수정. 상품명이 null인 경우")
    void patchProductValidationProductNameIsNull() throws Exception {
        Product fixture = allProductList.get(0);
        List<ProductOption> optionFixture = fixture.getProductOptions();
        List<PatchOptionDTO> patchOptionList = new ArrayList<>();
        for(int i = 1; i < optionFixture.size(); i++) {
            ProductOption option = optionFixture.get(i);
            patchOptionList.add(
                    new PatchOptionDTO(
                            option.getId(),
                            "patchSize" + i,
                            "patchColor" + i,
                            option.getStock() + 100,
                            true
                    )
            );
        }
        patchOptionList.addAll(getPostPatchOptionDTOList());
        AdminProductPatchDTO patchDTO = new AdminProductPatchDTO(
                null,
                "TOP",
                20000,
                true,
                0,
                patchOptionList
        );

        MockMultipartHttpServletRequestBuilder builder = createPatchProductBuilder(fixture.getId(), patchDTO);
        setOptionListFromBuilder(builder, patchOptionList);

        MvcResult result = mockMvc.perform(
                        builder
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                                .accept(MediaType.APPLICATION_JSON)
                                .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                                .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                                .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue))
                )
                .andExpect(status().isBadRequest())
                .andReturn();

        verify(fileDomainService, never()).setImageSaveName(any(MultipartFile.class));
        verify(fileService, never()).imageInsert(any(MultipartFile.class), anyString());
        verify(fileService, never()).deleteImage(anyString());

        String content = result.getResponse().getContentAsString();
        ValidationExceptionEntity response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        ErrorCode errorCode = ErrorCode.BAD_REQUEST;

        assertNotNull(response);
        assertEquals(errorCode.getHttpStatus().value(), response.errorCode());
        assertEquals(errorCode.getMessage(), response.errorMessage());
        assertFalse(response.errors().isEmpty());

        assertEquals(1, response.errors().size());
        ValidationError responseObj = response.errors().get(0);

        assertEquals("productName", responseObj.field());
        assertEquals("NotBlank", responseObj.constraint());
    }

    @Test
    @DisplayName(value = "상품 수정. 상품명이 Blank인 경우")
    void patchProductValidationProductNameIsBlank() throws Exception {
        Product fixture = allProductList.get(0);
        List<ProductOption> optionFixture = fixture.getProductOptions();
        List<PatchOptionDTO> patchOptionList = new ArrayList<>();
        for(int i = 1; i < optionFixture.size(); i++) {
            ProductOption option = optionFixture.get(i);
            patchOptionList.add(
                    new PatchOptionDTO(
                            option.getId(),
                            "patchSize" + i,
                            "patchColor" + i,
                            option.getStock() + 100,
                            true
                    )
            );
        }
        patchOptionList.addAll(getPostPatchOptionDTOList());
        AdminProductPatchDTO patchDTO = new AdminProductPatchDTO(
                "",
                "TOP",
                20000,
                true,
                0,
                patchOptionList
        );

        MockMultipartHttpServletRequestBuilder builder = createPatchProductBuilder(fixture.getId(), patchDTO);
        setOptionListFromBuilder(builder, patchOptionList);

        MvcResult result = mockMvc.perform(
                        builder
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                                .accept(MediaType.APPLICATION_JSON)
                                .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                                .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                                .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue))
                )
                .andExpect(status().isBadRequest())
                .andReturn();

        verify(fileDomainService, never()).setImageSaveName(any(MultipartFile.class));
        verify(fileService, never()).imageInsert(any(MultipartFile.class), anyString());
        verify(fileService, never()).deleteImage(anyString());

        String content = result.getResponse().getContentAsString();
        ValidationExceptionEntity response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        ErrorCode errorCode = ErrorCode.BAD_REQUEST;

        assertNotNull(response);
        assertEquals(errorCode.getHttpStatus().value(), response.errorCode());
        assertEquals(errorCode.getMessage(), response.errorMessage());
        assertFalse(response.errors().isEmpty());

        assertEquals(2, response.errors().size());

        List<String> validationConstraintList = List.of("Length", "NotBlank");

        response.errors().forEach(v -> {
            assertEquals("productName", v.field());
            assertTrue(validationConstraintList.contains(v.constraint()));
        });
    }

    @Test
    @DisplayName(value = "상품 수정. 상품명이 한글자인 경우")
    void patchProductValidationProductNameLength1() throws Exception {
        Product fixture = allProductList.get(0);
        List<ProductOption> optionFixture = fixture.getProductOptions();
        List<PatchOptionDTO> patchOptionList = new ArrayList<>();
        for(int i = 1; i < optionFixture.size(); i++) {
            ProductOption option = optionFixture.get(i);
            patchOptionList.add(
                    new PatchOptionDTO(
                            option.getId(),
                            "patchSize" + i,
                            "patchColor" + i,
                            option.getStock() + 100,
                            true
                    )
            );
        }
        patchOptionList.addAll(getPostPatchOptionDTOList());
        AdminProductPatchDTO patchDTO = new AdminProductPatchDTO(
                "p",
                "TOP",
                20000,
                true,
                0,
                patchOptionList
        );

        MockMultipartHttpServletRequestBuilder builder = createPatchProductBuilder(fixture.getId(), patchDTO);
        setOptionListFromBuilder(builder, patchOptionList);

        MvcResult result = mockMvc.perform(
                        builder
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                                .accept(MediaType.APPLICATION_JSON)
                                .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                                .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                                .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue))
                )
                .andExpect(status().isBadRequest())
                .andReturn();

        verify(fileDomainService, never()).setImageSaveName(any(MultipartFile.class));
        verify(fileService, never()).imageInsert(any(MultipartFile.class), anyString());
        verify(fileService, never()).deleteImage(anyString());

        String content = result.getResponse().getContentAsString();
        ValidationExceptionEntity response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        ErrorCode errorCode = ErrorCode.BAD_REQUEST;

        assertNotNull(response);
        assertEquals(errorCode.getHttpStatus().value(), response.errorCode());
        assertEquals(errorCode.getMessage(), response.errorMessage());
        assertFalse(response.errors().isEmpty());

        assertEquals(1, response.errors().size());
        ValidationError responseObj = response.errors().get(0);

        assertEquals("productName", responseObj.field());
        assertEquals("Length", responseObj.constraint());
    }

    @Test
    @DisplayName(value = "상품 수정. 상품 분류가 null인 경우")
    void patchProductValidationClassificationIsNull() throws Exception {
        Product fixture = allProductList.get(0);
        List<ProductOption> optionFixture = fixture.getProductOptions();
        List<PatchOptionDTO> patchOptionList = new ArrayList<>();
        for(int i = 1; i < optionFixture.size(); i++) {
            ProductOption option = optionFixture.get(i);
            patchOptionList.add(
                    new PatchOptionDTO(
                            option.getId(),
                            "patchSize" + i,
                            "patchColor" + i,
                            option.getStock() + 100,
                            true
                    )
            );
        }
        patchOptionList.addAll(getPostPatchOptionDTOList());
        AdminProductPatchDTO patchDTO = new AdminProductPatchDTO(
                "patchProduct",
                null,
                20000,
                true,
                0,
                patchOptionList
        );

        MockMultipartHttpServletRequestBuilder builder = createPatchProductBuilder(fixture.getId(), patchDTO);
        setOptionListFromBuilder(builder, patchOptionList);

        MvcResult result = mockMvc.perform(
                        builder
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                                .accept(MediaType.APPLICATION_JSON)
                                .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                                .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                                .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue))
                )
                .andExpect(status().isBadRequest())
                .andReturn();

        verify(fileDomainService, never()).setImageSaveName(any(MultipartFile.class));
        verify(fileService, never()).imageInsert(any(MultipartFile.class), anyString());
        verify(fileService, never()).deleteImage(anyString());

        String content = result.getResponse().getContentAsString();
        ValidationExceptionEntity response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        ErrorCode errorCode = ErrorCode.BAD_REQUEST;

        assertNotNull(response);
        assertEquals(errorCode.getHttpStatus().value(), response.errorCode());
        assertEquals(errorCode.getMessage(), response.errorMessage());
        assertFalse(response.errors().isEmpty());

        assertEquals(1, response.errors().size());
        ValidationError responseObj = response.errors().get(0);

        assertEquals("classification", responseObj.field());
        assertEquals("NotBlank", responseObj.constraint());
    }

    @Test
    @DisplayName(value = "상품 수정. 상품 분류가 Blank인 경우")
    void patchProductValidationClassificationIsBlank() throws Exception {
        Product fixture = allProductList.get(0);
        List<ProductOption> optionFixture = fixture.getProductOptions();
        List<PatchOptionDTO> patchOptionList = new ArrayList<>();
        for(int i = 1; i < optionFixture.size(); i++) {
            ProductOption option = optionFixture.get(i);
            patchOptionList.add(
                    new PatchOptionDTO(
                            option.getId(),
                            "patchSize" + i,
                            "patchColor" + i,
                            option.getStock() + 100,
                            true
                    )
            );
        }
        patchOptionList.addAll(getPostPatchOptionDTOList());
        AdminProductPatchDTO patchDTO = new AdminProductPatchDTO(
                "patchProduct",
                "",
                20000,
                true,
                0,
                patchOptionList
        );

        MockMultipartHttpServletRequestBuilder builder = createPatchProductBuilder(fixture.getId(), patchDTO);
        setOptionListFromBuilder(builder, patchOptionList);

        MvcResult result = mockMvc.perform(
                        builder
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                                .accept(MediaType.APPLICATION_JSON)
                                .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                                .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                                .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue))
                )
                .andExpect(status().isBadRequest())
                .andReturn();

        verify(fileDomainService, never()).setImageSaveName(any(MultipartFile.class));
        verify(fileService, never()).imageInsert(any(MultipartFile.class), anyString());
        verify(fileService, never()).deleteImage(anyString());

        String content = result.getResponse().getContentAsString();
        ValidationExceptionEntity response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        ErrorCode errorCode = ErrorCode.BAD_REQUEST;

        assertNotNull(response);
        assertEquals(errorCode.getHttpStatus().value(), response.errorCode());
        assertEquals(errorCode.getMessage(), response.errorMessage());
        assertFalse(response.errors().isEmpty());

        assertEquals(2, response.errors().size());
        List<String> validationConstraintList = List.of("Length", "NotBlank");

        response.errors().forEach(v -> {
            assertEquals("classification", v.field());
            assertTrue(validationConstraintList.contains(v.constraint()));
        });
    }

    @Test
    @DisplayName(value = "상품 수정. 상품 분류가 한글자인 경우")
    void patchProductValidationClassificationLength1() throws Exception {
        Product fixture = allProductList.get(0);
        List<ProductOption> optionFixture = fixture.getProductOptions();
        List<PatchOptionDTO> patchOptionList = new ArrayList<>();
        for(int i = 1; i < optionFixture.size(); i++) {
            ProductOption option = optionFixture.get(i);
            patchOptionList.add(
                    new PatchOptionDTO(
                            option.getId(),
                            "patchSize" + i,
                            "patchColor" + i,
                            option.getStock() + 100,
                            true
                    )
            );
        }
        patchOptionList.addAll(getPostPatchOptionDTOList());
        AdminProductPatchDTO patchDTO = new AdminProductPatchDTO(
                "patchProduct",
                "a",
                20000,
                true,
                0,
                patchOptionList
        );

        MockMultipartHttpServletRequestBuilder builder = createPatchProductBuilder(fixture.getId(), patchDTO);
        setOptionListFromBuilder(builder, patchOptionList);

        MvcResult result = mockMvc.perform(
                        builder
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                                .accept(MediaType.APPLICATION_JSON)
                                .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                                .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                                .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue))
                )
                .andExpect(status().isBadRequest())
                .andReturn();

        verify(fileDomainService, never()).setImageSaveName(any(MultipartFile.class));
        verify(fileService, never()).imageInsert(any(MultipartFile.class), anyString());
        verify(fileService, never()).deleteImage(anyString());

        String content = result.getResponse().getContentAsString();
        ValidationExceptionEntity response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        ErrorCode errorCode = ErrorCode.BAD_REQUEST;

        assertNotNull(response);
        assertEquals(errorCode.getHttpStatus().value(), response.errorCode());
        assertEquals(errorCode.getMessage(), response.errorMessage());
        assertFalse(response.errors().isEmpty());

        assertEquals(1, response.errors().size());
        ValidationError responseObj = response.errors().get(0);

        assertEquals("classification", responseObj.field());
        assertEquals("Length", responseObj.constraint());
    }

    @Test
    @DisplayName(value = "상품 수정. 가격이 100원 미만인 경우")
    void patchProductValidationPriceLT100() throws Exception {
        Product fixture = allProductList.get(0);
        List<ProductOption> optionFixture = fixture.getProductOptions();
        List<PatchOptionDTO> patchOptionList = new ArrayList<>();
        for(int i = 1; i < optionFixture.size(); i++) {
            ProductOption option = optionFixture.get(i);
            patchOptionList.add(
                    new PatchOptionDTO(
                            option.getId(),
                            "patchSize" + i,
                            "patchColor" + i,
                            option.getStock() + 100,
                            true
                    )
            );
        }
        patchOptionList.addAll(getPostPatchOptionDTOList());
        AdminProductPatchDTO patchDTO = new AdminProductPatchDTO(
                "patchProduct",
                "TOP",
                50,
                true,
                0,
                patchOptionList
        );

        MockMultipartHttpServletRequestBuilder builder = createPatchProductBuilder(fixture.getId(), patchDTO);
        setOptionListFromBuilder(builder, patchOptionList);

        MvcResult result = mockMvc.perform(
                        builder
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                                .accept(MediaType.APPLICATION_JSON)
                                .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                                .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                                .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue))
                )
                .andExpect(status().isBadRequest())
                .andReturn();

        verify(fileDomainService, never()).setImageSaveName(any(MultipartFile.class));
        verify(fileService, never()).imageInsert(any(MultipartFile.class), anyString());
        verify(fileService, never()).deleteImage(anyString());

        String content = result.getResponse().getContentAsString();
        ValidationExceptionEntity response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        ErrorCode errorCode = ErrorCode.BAD_REQUEST;

        assertNotNull(response);
        assertEquals(errorCode.getHttpStatus().value(), response.errorCode());
        assertEquals(errorCode.getMessage(), response.errorMessage());
        assertFalse(response.errors().isEmpty());

        assertEquals(1, response.errors().size());
        ValidationError responseObj = response.errors().get(0);

        assertEquals("price", responseObj.field());
        assertEquals("Min", responseObj.constraint());
    }

    @Test
    @DisplayName(value = "상품 수정. 공개 여부가 null인 경우")
    void patchProductValidationIsOpenIsNull() throws Exception {
        Product fixture = allProductList.get(0);
        List<ProductOption> optionFixture = fixture.getProductOptions();
        List<PatchOptionDTO> patchOptionList = new ArrayList<>();
        for(int i = 1; i < optionFixture.size(); i++) {
            ProductOption option = optionFixture.get(i);
            patchOptionList.add(
                    new PatchOptionDTO(
                            option.getId(),
                            "patchSize" + i,
                            "patchColor" + i,
                            option.getStock() + 100,
                            true
                    )
            );
        }
        patchOptionList.addAll(getPostPatchOptionDTOList());
        AdminProductPatchDTO patchDTO = new AdminProductPatchDTO(
                "patchProduct",
                "TOP",
                20000,
                null,
                0,
                patchOptionList
        );

        MockMultipartHttpServletRequestBuilder builder = createPatchProductBuilder(fixture.getId(), patchDTO);
        setOptionListFromBuilder(builder, patchOptionList);

        MvcResult result = mockMvc.perform(
                        builder
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                                .accept(MediaType.APPLICATION_JSON)
                                .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                                .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                                .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue))
                )
                .andExpect(status().isBadRequest())
                .andReturn();

        verify(fileDomainService, never()).setImageSaveName(any(MultipartFile.class));
        verify(fileService, never()).imageInsert(any(MultipartFile.class), anyString());
        verify(fileService, never()).deleteImage(anyString());

        String content = result.getResponse().getContentAsString();
        ValidationExceptionEntity response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        ErrorCode errorCode = ErrorCode.BAD_REQUEST;

        assertNotNull(response);
        assertEquals(errorCode.getHttpStatus().value(), response.errorCode());
        assertEquals(errorCode.getMessage(), response.errorMessage());
        assertFalse(response.errors().isEmpty());

        assertEquals(1, response.errors().size());
        ValidationError responseObj = response.errors().get(0);

        assertEquals("isOpen", responseObj.field());
        assertEquals("NotNull", responseObj.constraint());
    }

    @Test
    @DisplayName(value = "상품 수정. discount값이 0보다 작은 경우")
    void patchProductValidationDiscountLT0() throws Exception {
        Product fixture = allProductList.get(0);
        List<ProductOption> optionFixture = fixture.getProductOptions();
        List<PatchOptionDTO> patchOptionList = new ArrayList<>();
        for(int i = 1; i < optionFixture.size(); i++) {
            ProductOption option = optionFixture.get(i);
            patchOptionList.add(
                    new PatchOptionDTO(
                            option.getId(),
                            "patchSize" + i,
                            "patchColor" + i,
                            option.getStock() + 100,
                            true
                    )
            );
        }
        patchOptionList.addAll(getPostPatchOptionDTOList());
        AdminProductPatchDTO patchDTO = new AdminProductPatchDTO(
                "patchProduct",
                "TOP",
                20000,
                true,
                -1,
                patchOptionList
        );

        MockMultipartHttpServletRequestBuilder builder = createPatchProductBuilder(fixture.getId(), patchDTO);
        setOptionListFromBuilder(builder, patchOptionList);

        MvcResult result = mockMvc.perform(
                        builder
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                                .accept(MediaType.APPLICATION_JSON)
                                .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                                .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                                .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue))
                )
                .andExpect(status().isBadRequest())
                .andReturn();

        verify(fileDomainService, never()).setImageSaveName(any(MultipartFile.class));
        verify(fileService, never()).imageInsert(any(MultipartFile.class), anyString());
        verify(fileService, never()).deleteImage(anyString());

        String content = result.getResponse().getContentAsString();
        ValidationExceptionEntity response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        ErrorCode errorCode = ErrorCode.BAD_REQUEST;

        assertNotNull(response);
        assertEquals(errorCode.getHttpStatus().value(), response.errorCode());
        assertEquals(errorCode.getMessage(), response.errorMessage());
        assertFalse(response.errors().isEmpty());

        assertEquals(1, response.errors().size());
        ValidationError responseObj = response.errors().get(0);

        assertEquals("discount", responseObj.field());
        assertEquals("Min", responseObj.constraint());
    }

    @Test
    @DisplayName(value = "상품 수정. RequestDTO의 모든 필드 유효성 검사에 실패한 경우")
    void patchProductValidationAllFieldValidationFail() throws Exception {
        Product fixture = allProductList.get(0);
        List<ProductOption> optionFixture = fixture.getProductOptions();
        List<PatchOptionDTO> patchOptionList = new ArrayList<>();
        for(int i = 1; i < optionFixture.size(); i++) {
            ProductOption option = optionFixture.get(i);
            patchOptionList.add(
                    new PatchOptionDTO(
                            option.getId(),
                            "patchSize" + i,
                            "patchColor" + i,
                            option.getStock() + 100,
                            true
                    )
            );
        }
        patchOptionList.addAll(getPostPatchOptionDTOList());
        AdminProductPatchDTO patchDTO = new AdminProductPatchDTO(
                "p",
                "T",
                50,
                null,
                -1,
                patchOptionList
        );

        MockMultipartHttpServletRequestBuilder builder = createPatchProductBuilder(fixture.getId(), patchDTO);
        setOptionListFromBuilder(builder, patchOptionList);

        MvcResult result = mockMvc.perform(
                        builder
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                                .accept(MediaType.APPLICATION_JSON)
                                .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                                .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                                .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue))
                )
                .andExpect(status().isBadRequest())
                .andReturn();

        verify(fileDomainService, never()).setImageSaveName(any(MultipartFile.class));
        verify(fileService, never()).imageInsert(any(MultipartFile.class), anyString());
        verify(fileService, never()).deleteImage(anyString());

        String content = result.getResponse().getContentAsString();
        ValidationExceptionEntity response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        ErrorCode errorCode = ErrorCode.BAD_REQUEST;

        assertNotNull(response);
        assertEquals(errorCode.getHttpStatus().value(), response.errorCode());
        assertEquals(errorCode.getMessage(), response.errorMessage());
        assertFalse(response.errors().isEmpty());

        assertEquals(5, response.errors().size());

        Map<String, String> validationMap = new HashMap<>();
        validationMap.put("productName", "Length");
        validationMap.put("classification", "Length");
        validationMap.put("price", "Min");
        validationMap.put("isOpen", "NotNull");
        validationMap.put("discount", "Min");

        response.errors().forEach(v -> {
            String constraint = validationMap.getOrDefault(v.field(), null);

            assertNotNull(constraint);
            assertEquals(constraint, v.constraint());
        });
    }

    @Test
    @DisplayName(value = "상품 수정. 옵션 데이터의 옵션 아이디가 0보다 작은 경우")
    void patchProductValidationOptionIdLT0() throws Exception {
        Product fixture = allProductList.get(0);
        List<PatchOptionDTO> patchOptionList = new ArrayList<>();
        patchOptionList.add(
                new PatchOptionDTO(
                        -1L,
                        "patchSize",
                        "patchColor",
                        100,
                        true
                )
        );
        patchOptionList.addAll(getPostPatchOptionDTOList());
        AdminProductPatchDTO patchDTO = getAdminProductPatchDTOFixture(patchOptionList);

        MockMultipartHttpServletRequestBuilder builder = createPatchProductBuilder(fixture.getId(), patchDTO);
        setOptionListFromBuilder(builder, patchOptionList);

        MvcResult result = mockMvc.perform(
                        builder
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                                .accept(MediaType.APPLICATION_JSON)
                                .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                                .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                                .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue))
                )
                .andExpect(status().isBadRequest())
                .andReturn();

        verify(fileDomainService, never()).setImageSaveName(any(MultipartFile.class));
        verify(fileService, never()).imageInsert(any(MultipartFile.class), anyString());
        verify(fileService, never()).deleteImage(anyString());

        String content = result.getResponse().getContentAsString();
        ExceptionEntity response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        ErrorCode errorCode = ErrorCode.BAD_REQUEST;

        verify(fileDomainService, never()).setImageSaveName(any(MultipartFile.class));
        verify(fileService, never()).imageInsert(any(MultipartFile.class), anyString());

        assertNotNull(response);
        assertEquals(errorCode.getHttpStatus().value(), response.errorCode());
        assertEquals(errorCode.getMessage(), response.errorMessage());
    }

    @Test
    @DisplayName(value = "상품 수정. 옵션 데이터의 옵션 재고가 0보다 작은 경우")
    void patchProductValidationOptionStockLT0() throws Exception {
        Product fixture = allProductList.get(0);
        List<PatchOptionDTO> patchOptionList = new ArrayList<>();
        patchOptionList.add(
                new PatchOptionDTO(
                        0L,
                        "patchSize",
                        "patchColor",
                        -1,
                        true
                )
        );
        patchOptionList.addAll(getPostPatchOptionDTOList());
        AdminProductPatchDTO patchDTO = getAdminProductPatchDTOFixture(patchOptionList);

        MockMultipartHttpServletRequestBuilder builder = createPatchProductBuilder(fixture.getId(), patchDTO);
        setOptionListFromBuilder(builder, patchOptionList);

        MvcResult result = mockMvc.perform(
                        builder
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                                .accept(MediaType.APPLICATION_JSON)
                                .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                                .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                                .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue))
                )
                .andExpect(status().isBadRequest())
                .andReturn();

        verify(fileDomainService, never()).setImageSaveName(any(MultipartFile.class));
        verify(fileService, never()).imageInsert(any(MultipartFile.class), anyString());
        verify(fileService, never()).deleteImage(anyString());

        String content = result.getResponse().getContentAsString();
        ExceptionEntity response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        ErrorCode errorCode = ErrorCode.BAD_REQUEST;

        verify(fileDomainService, never()).setImageSaveName(any(MultipartFile.class));
        verify(fileService, never()).imageInsert(any(MultipartFile.class), anyString());

        assertNotNull(response);
        assertEquals(errorCode.getHttpStatus().value(), response.errorCode());
        assertEquals(errorCode.getMessage(), response.errorMessage());
    }

    @Test
    @DisplayName(value = "상품 재고 리스트 조회")
    void getProductStockList() throws Exception {
        AdminProductPageDTO pageDTOFixture = new AdminProductPageDTO(null, 1);
        int totalPages = TestPaginationUtils.getTotalPages(allProductList.size(), pageDTOFixture.amount());
        List<Product> fixture = allProductList.stream()
                .sorted(
                        Comparator.comparingInt(product ->
                                product.getProductOptions().stream()
                                        .mapToInt(ProductOption::getStock)
                                        .sum()
                        )
                )
                .limit(pageDTOFixture.amount())
                .toList();

        MvcResult result = mockMvc.perform(get(URL_PREFIX + "product/stock")
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue)))
                .andExpect(status().isOk())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        PagingResponseDTO<AdminProductStockDTO> response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        assertNotNull(response);
        assertFalse(response.content().isEmpty());
        assertFalse(response.empty());
        assertEquals(fixture.size(), response.content().size());
        assertEquals(totalPages, response.totalPages());
    }

    @Test
    @DisplayName(value = "상품 재고 리스트 조회. 데이터가 없는 경우")
    void getProductStockListEmpty() throws Exception {
        productRepository.deleteAll();

        MvcResult result = mockMvc.perform(get(URL_PREFIX + "product/stock")
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue)))
                .andExpect(status().isOk())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        PagingResponseDTO<AdminProductStockDTO> response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        assertNotNull(response);
        assertTrue(response.content().isEmpty());
        assertTrue(response.empty());
        assertEquals(0, response.totalPages());
    }

    @Test
    @DisplayName(value = "상품 재고 리스트 조회. 상품명 기반 검색")
    void getProductStockListSearchProductName() throws Exception {
        Product fixture = allProductList.get(0);
        int totalStock = fixture.getProductOptions()
                .stream()
                .mapToInt(ProductOption::getStock)
                .sum();

        MvcResult result = mockMvc.perform(get(URL_PREFIX + "product/stock")
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue))
                        .param("keyword", fixture.getProductName()))
                .andExpect(status().isOk())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        PagingResponseDTO<AdminProductStockDTO> response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        assertNotNull(response);
        assertFalse(response.content().isEmpty());
        assertFalse(response.empty());
        assertEquals(1, response.content().size());
        assertEquals(1, response.totalPages());
        AdminProductStockDTO responseDTO = response.content().get(0);
        assertEquals(fixture.getId(), responseDTO.productId());
        assertEquals(fixture.getClassification().getId(), responseDTO.classification());
        assertEquals(fixture.getProductName(), responseDTO.productName());
        assertEquals(totalStock, responseDTO.totalStock());
        assertEquals(fixture.isOpen(), responseDTO.isOpen());
        assertEquals(fixture.getProductOptions().size(), responseDTO.optionList().size());
    }

    @Test
    @DisplayName(value = "상품 재고 리스트 조회. 페이지가 0으로 전달된 경우")
    void getProductStockListValidationPageZero() throws Exception {
        MvcResult result = mockMvc.perform(get(URL_PREFIX + "product/stock")
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue))
                        .param("page", "0"))
                .andExpect(status().isBadRequest())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        ValidationExceptionEntity response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        ErrorCode errorCode = ErrorCode.BAD_REQUEST;

        assertNotNull(response);
        assertEquals(errorCode.getHttpStatus().value(), response.errorCode());
        assertEquals(errorCode.getMessage(), response.errorMessage());
        assertFalse(response.errors().isEmpty());

        assertEquals(1, response.errors().size());
        ValidationError responseObj = response.errors().get(0);

        assertEquals("page", responseObj.field());
        assertEquals("Min", responseObj.constraint());
    }

    @Test
    @DisplayName(value = "상품 재고 리스트 조회. 검색어가 한글자인 경우")
    void getProductStockListValidationKeywordSize1() throws Exception {
        MvcResult result = mockMvc.perform(get(URL_PREFIX + "product/stock")
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue))
                        .param("keyword", "a"))
                .andExpect(status().isBadRequest())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        ValidationExceptionEntity response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        ErrorCode errorCode = ErrorCode.BAD_REQUEST;

        assertNotNull(response);
        assertEquals(errorCode.getHttpStatus().value(), response.errorCode());
        assertEquals(errorCode.getMessage(), response.errorMessage());
        assertFalse(response.errors().isEmpty());

        assertEquals(1, response.errors().size());
        ValidationError responseObj = response.errors().get(0);

        assertEquals("keyword", responseObj.field());
        assertEquals("Size", responseObj.constraint());
    }

    @Test
    @DisplayName(value = "상품 재고 리스트 조회. 페이지가 0, 검색어가 한글자인 경우")
    void getProductStockListValidationPageZeroAndKeywordSize1() throws Exception {
        MvcResult result = mockMvc.perform(get(URL_PREFIX + "product/stock")
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue))
                        .param("page", "0")
                        .param("keyword", "a")
                )
                .andExpect(status().isBadRequest())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        ValidationExceptionEntity response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        ErrorCode errorCode = ErrorCode.BAD_REQUEST;

        assertNotNull(response);
        assertEquals(errorCode.getHttpStatus().value(), response.errorCode());
        assertEquals(errorCode.getMessage(), response.errorMessage());
        assertFalse(response.errors().isEmpty());

        assertEquals(2, response.errors().size());
        Map<String, String> validationMap = new HashMap<>();
        validationMap.put("page", "Min");
        validationMap.put("keyword", "Size");

        response.errors().forEach(v -> {
            String constraint = validationMap.getOrDefault(v.field(), null);

            assertNotNull(constraint);
            assertEquals(constraint, v.constraint());
        });
    }

    @Test
    @DisplayName(value = "할인중인 상품 목록 조회")
    void getDiscountList() throws Exception {
        AdminProductPageDTO pageDTOFixture = new AdminProductPageDTO(null, 1);
        List<Product> fixtureList = allProductList.stream()
                .filter(v -> v.getProductDiscount() > 0)
                .toList();
        int contentSize = Math.min(fixtureList.size(), pageDTOFixture.amount());
        int totalPages = TestPaginationUtils.getTotalPages(fixtureList.size(), pageDTOFixture.amount());

        MvcResult result = mockMvc.perform(get(URL_PREFIX + "product/discount")
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue)))
                .andExpect(status().isOk())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        PagingResponseDTO<AdminDiscountResponseDTO> response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        assertNotNull(response);
        assertFalse(response.content().isEmpty());
        assertFalse(response.empty());
        assertEquals(contentSize, response.content().size());
        assertEquals(totalPages, response.totalPages());
    }

    @Test
    @DisplayName(value = "할인중인 상품 목록 조회. 데이터가 없는 경우")
    void getDiscountListEmpty() throws Exception {
        productRepository.deleteAll();

        MvcResult result = mockMvc.perform(get(URL_PREFIX + "product/discount")
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue)))
                .andExpect(status().isOk())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        PagingResponseDTO<AdminDiscountResponseDTO> response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        assertNotNull(response);
        assertTrue(response.content().isEmpty());
        assertTrue(response.empty());
        assertEquals(0, response.totalPages());
    }

    @Test
    @DisplayName(value = "할인중인 상품 목록 조회. 상품명 기반 검색")
    void getDiscountListSearchProductName() throws Exception {
        Product fixture = allProductList.stream()
                .filter(v -> v.getProductDiscount() > 0)
                .toList()
                .get(0);
        int discountPrice = (int) (fixture.getProductPrice() * (1 - ((double)fixture.getProductDiscount() / 100)));

        MvcResult result = mockMvc.perform(get(URL_PREFIX + "product/discount")
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue))
                        .param("keyword", fixture.getProductName()))
                .andExpect(status().isOk())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        PagingResponseDTO<AdminDiscountResponseDTO> response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        assertNotNull(response);
        assertFalse(response.content().isEmpty());
        assertFalse(response.empty());
        assertEquals(1, response.content().size());
        assertEquals(1, response.totalPages());

        AdminDiscountResponseDTO responseDTO = response.content().get(0);
        assertEquals(fixture.getId(), responseDTO.productId());
        assertEquals(fixture.getClassification().getId(), responseDTO.classification());
        assertEquals(fixture.getProductName(), responseDTO.productName());
        assertEquals(fixture.getProductPrice(), responseDTO.price());
        assertEquals(fixture.getProductDiscount(), responseDTO.discount());
        assertEquals(discountPrice, responseDTO.totalPrice());
    }

    @Test
    @DisplayName(value = "할인중인 상품 목록 조회. 페이지가 0으로 전달된 경우")
    void getDiscountListValidationPageZero() throws Exception {
        MvcResult result = mockMvc.perform(get(URL_PREFIX + "product/discount")
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue))
                        .param("page", "0")
                )
                .andExpect(status().isBadRequest())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        ValidationExceptionEntity response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        ErrorCode errorCode = ErrorCode.BAD_REQUEST;

        assertNotNull(response);
        assertEquals(errorCode.getHttpStatus().value(), response.errorCode());
        assertEquals(errorCode.getMessage(), response.errorMessage());
        assertFalse(response.errors().isEmpty());

        assertEquals(1, response.errors().size());
        ValidationError responseObj = response.errors().get(0);

        assertEquals("page", responseObj.field());
        assertEquals("Min", responseObj.constraint());
    }

    @Test
    @DisplayName(value = "할인중인 상품 목록 조회. 검색어가 한글자인 경우")
    void getDiscountListValidationKeywordSize1() throws Exception {
        MvcResult result = mockMvc.perform(get(URL_PREFIX + "product/discount")
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue))
                        .param("keyword", "a")
                )
                .andExpect(status().isBadRequest())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        ValidationExceptionEntity response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        ErrorCode errorCode = ErrorCode.BAD_REQUEST;

        assertNotNull(response);
        assertEquals(errorCode.getHttpStatus().value(), response.errorCode());
        assertEquals(errorCode.getMessage(), response.errorMessage());
        assertFalse(response.errors().isEmpty());

        assertEquals(1, response.errors().size());
        ValidationError responseObj = response.errors().get(0);

        assertEquals("keyword", responseObj.field());
        assertEquals("Size", responseObj.constraint());
    }

    @Test
    @DisplayName(value = "할인중인 상품 목록 조회. 페이지가 0, 검색어가 한글자인 경우")
    void getDiscountListValidationPageZeroAndKeywordSize1() throws Exception {
        MvcResult result = mockMvc.perform(get(URL_PREFIX + "product/discount")
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue))
                        .param("page", "0")
                        .param("keyword", "a")
                )
                .andExpect(status().isBadRequest())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        ValidationExceptionEntity response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        ErrorCode errorCode = ErrorCode.BAD_REQUEST;

        assertNotNull(response);
        assertEquals(errorCode.getHttpStatus().value(), response.errorCode());
        assertEquals(errorCode.getMessage(), response.errorMessage());
        assertFalse(response.errors().isEmpty());

        assertEquals(2, response.errors().size());
        Map<String, String> validationMap = new HashMap<>();
        validationMap.put("page", "Min");
        validationMap.put("keyword", "Size");

        response.errors().forEach(v -> {
            String constraint = validationMap.getOrDefault(v.field(), null);

            assertNotNull(constraint);
            assertEquals(constraint, v.constraint());
        });
    }

    @Test
    @DisplayName(value = "할인 설정에서 선택한 상품 분류에 해당하는 상품 목록 조회")
    void getDiscountProductSelectList() throws Exception {
        List<Product> fixtureList = outerProductList.stream()
                .sorted(Comparator.comparing(Product::getProductName))
                .toList();
        MvcResult result = mockMvc.perform(get(URL_PREFIX + "product/discount/select/" + classificationList.get(0).getId())
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue)))
                .andExpect(status().isOk())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        List<AdminDiscountProductDTO> response = om.readValue(
                content,
                new TypeReference<>() {}
        );
        assertNotNull(response);
        assertFalse(response.isEmpty());
        assertEquals(fixtureList.size(), response.size());

        for(int i = 0; i < fixtureList.size(); i++) {
            Product fixture = fixtureList.get(i);
            AdminDiscountProductDTO responseDTO = response.get(i);

            assertEquals(fixture.getId(), responseDTO.productId());
            assertEquals(fixture.getProductName(), responseDTO.productName());
            assertEquals(fixture.getProductPrice(), responseDTO.productPrice());
        }
    }

    @Test
    @DisplayName(value = "할인 설정에서 선택한 상품 분류에 해당하는 상품 목록 조회. 상품 분류가 한글자인 경우")
    void getDiscountProductSelectListValidationLength1() throws Exception {
        MvcResult result = mockMvc.perform(get(URL_PREFIX + "product/discount/select/a")
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue)))
                .andExpect(status().isBadRequest())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        ExceptionEntity response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        ErrorCode errorCode = ErrorCode.BAD_REQUEST;

        assertNotNull(response);
        assertEquals(errorCode.getHttpStatus().value(), response.errorCode());
        assertEquals(errorCode.getMessage(), response.errorMessage());
    }

    @Test
    @DisplayName(value = "할인 설정에서 선택한 상품 분류에 해당하는 상품 목록 조회. 잘못된 상품 분류 아이디가 전달된 경우")
    void getDiscountProductSelectListWrongClassificationId() throws Exception {
        MvcResult result = mockMvc.perform(get(URL_PREFIX + "product/discount/select/abc")
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue)))
                .andExpect(status().isBadRequest())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        ExceptionEntity response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        ErrorCode errorCode = ErrorCode.BAD_REQUEST;

        assertNotNull(response);
        assertEquals(errorCode.getHttpStatus().value(), response.errorCode());
        assertEquals(errorCode.getMessage(), response.errorMessage());
    }

    @Test
    @DisplayName(value = "상품 할인 설정. 단일 상품만 수정하는 경우")
    void patchDiscountProductOne() throws Exception {
        Product fixture = allProductList.stream()
                .filter(v -> v.getProductDiscount() == 0)
                .findFirst()
                .get();
        AdminDiscountPatchDTO discountDTO = new AdminDiscountPatchDTO(List.of(fixture.getId()), 30);

        String requestDTO = om.writeValueAsString(discountDTO);

        mockMvc.perform(patch(URL_PREFIX + "product/discount")
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestDTO))
                .andExpect(status().isNoContent())
                .andReturn();

        em.flush();
        em.clear();

        Product patchProduct = productRepository.findById(fixture.getId()).orElse(null);
        assertNotNull(patchProduct);
        assertEquals(discountDTO.discount(), patchProduct.getProductDiscount());
    }

    @Test
    @DisplayName(value = "상품 할인 설정. 여러 상품을 수정하는 경우")
    void patchDiscountProductMultiple() throws Exception {
        List<String> patchProductIds = allProductList.stream()
                .filter(v -> v.getProductDiscount() == 0)
                .limit(2)
                .map(Product::getId)
                .toList();

        AdminDiscountPatchDTO discountDTO = new AdminDiscountPatchDTO(patchProductIds, 30);

        String requestDTO = om.writeValueAsString(discountDTO);

        mockMvc.perform(patch(URL_PREFIX + "product/discount")
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestDTO))
                .andExpect(status().isNoContent())
                .andReturn();

        em.flush();
        em.clear();

        List<Product> patchProductList = productRepository.findAllById(patchProductIds);

        assertFalse(patchProductList.isEmpty());
        patchProductList.forEach(v -> assertEquals(discountDTO.discount(), v.getProductDiscount()));
    }


    @Test
    @DisplayName(value = "상품 할인 설정. 상품 아이디가 잘못된 경우")
    void patchDiscountProductWrongProductId() throws Exception {
        AdminDiscountPatchDTO discountDTO = new AdminDiscountPatchDTO(
                List.of("noneProductId1","noneProductId2"), 30);

        String requestDTO = om.writeValueAsString(discountDTO);

        MvcResult result = mockMvc.perform(patch(URL_PREFIX + "product/discount")
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestDTO))
                .andExpect(status().isBadRequest())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        ExceptionEntity response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        assertNotNull(response);
        assertEquals(ErrorCode.BAD_REQUEST.getMessage(), response.errorMessage());
    }
}
