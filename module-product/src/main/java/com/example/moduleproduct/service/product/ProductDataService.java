package com.example.moduleproduct.service.product;

import com.example.modulecommon.model.entity.*;
import com.example.moduleproduct.model.dto.admin.product.business.AdminOptionStockDTO;
import com.example.moduleproduct.model.dto.admin.product.business.AdminProductStockDataDTO;
import com.example.moduleproduct.model.dto.admin.product.in.AdminDiscountPatchDTO;
import com.example.moduleproduct.model.dto.admin.product.out.AdminDiscountProductDTO;
import com.example.moduleproduct.model.dto.admin.product.out.AdminProductListDTO;
import com.example.moduleproduct.model.dto.admin.product.out.AdminProductOptionDTO;
import com.example.moduleproduct.model.dto.page.AdminProductPageDTO;
import com.example.moduleproduct.model.dto.product.business.OrderProductInfoDTO;
import com.example.moduleproduct.model.dto.product.business.PatchOrderStockDTO;
import com.example.moduleproduct.model.dto.product.business.ProductIdClassificationDTO;
import com.example.moduleproduct.model.dto.product.business.ProductOptionDTO;
import com.example.moduleproduct.repository.classification.ClassificationRepository;
import com.example.moduleproduct.repository.product.ProductRepository;
import com.example.moduleproduct.repository.productInfoImage.ProductInfoImageRepository;
import com.example.moduleproduct.repository.productOption.ProductOptionRepository;
import com.example.moduleproduct.repository.productThumbnail.ProductThumbnailRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductDataService {

    private final ClassificationRepository classificationRepository;

    private final ProductRepository productRepository;

    private final ProductOptionRepository productOptionRepository;

    private final ProductThumbnailRepository productThumbnailRepository;

    private final ProductInfoImageRepository productInfoImageRepository;

    public Product getProductByIdOrElseIllegal(String productId) {
        return productRepository.findById(productId)
                .orElseThrow(IllegalArgumentException::new);
    }

    public List<ProductOptionDTO> getProductOptionDTOListByProductId(String productId) {
        return productOptionRepository.findByDetailOption(productId);
    }

    public List<String> getProductThumbnailImageNameList(String productId) {
        return productThumbnailRepository.findByProductId(productId);
    }

    public List<String> getProductInfoImageNameList(String productId) {
        return productInfoImageRepository.findByProductId(productId);
    }

    public List<ProductOption> getProductOptionListByIds(List<Long> ids) {
        return productOptionRepository.findAllById(ids);
    }

    public List<OrderProductInfoDTO> getOrderInfoDTOListByOptionIds(List<Long> optionIds) {
        return productOptionRepository.findOrderData(optionIds);
    }

    public ProductOption getProductOptionByIdOrElseIllegal(long optionId) {
        return productOptionRepository.findById(optionId).orElseThrow(IllegalArgumentException::new);
    }

    public List<AdminProductListDTO> getAdminProductPageList(AdminProductPageDTO pageDTO) {
        return productRepository.findAdminProductPageList(pageDTO);
    }

    public Long getAdminProductListFullCount(AdminProductPageDTO pageDTO) {
        return productRepository.findAdminProductListCount(pageDTO);
    }

    public List<Classification> getAllClassification() {
        return classificationRepository.findAll(Sort.by("classificationStep").ascending());
    }

    public List<AdminProductOptionDTO> findAllProductOptionByProductId(String productId) {
        return productOptionRepository.findAllAdminOptionDTOByProductId(productId);
    }

    public String saveProductAndReturnId(Product product) {
        return productRepository.save(product).getId();
    }

    public void saveProductOptions(List<ProductOption> productOptions) {
        productOptionRepository.saveAll(productOptions);
    }

    public void saveProductThumbnails(List<ProductThumbnail> productThumbnails) {
        productThumbnailRepository.saveAll(productThumbnails);
    }

    public void saveProductInfoImages(List<ProductInfoImage> productInfoImages) {
        productInfoImageRepository.saveAll(productInfoImages);
    }

    public Classification getClassificationByIdOrElseIllegal(String classification) {
        return classificationRepository.findById(classification).orElseThrow(IllegalArgumentException::new);
    }

    public void deleteAllProductOptionByIds(List<Long> deleteOptionList) {
        productOptionRepository.deleteAllById(deleteOptionList);
    }

    public void deleteAllProductThumbnailByImageNames(List<String> deleteThumbnails) {
        productThumbnailRepository.deleteAllByImageNames(deleteThumbnails);
    }

    public void deleteAllProductInfoImageByImageNames(List<String> deleteInfoImages) {
        productInfoImageRepository.deleteAllByImageNames(deleteInfoImages);
    }

    public List<AdminProductStockDataDTO> getAllProductStockData(AdminProductPageDTO pageDTO) {
        return productRepository.findAllProductStockData(pageDTO);
    }

    public Long getAllProductStockDataCount(AdminProductPageDTO pageDTO) {
        return productRepository.findAllProductStockDataCount(pageDTO);
    }

    public List<AdminOptionStockDTO> getAllProductOptionStockByProductIds(List<String> productIds) {
        return productOptionRepository.findAllProductOptionStockByProductIds(productIds);
    }

    public Page<Product> getDiscountProductPageList(AdminProductPageDTO pageDTO) {
        Pageable pageable = PageRequest.of(pageDTO.page() - 1,
                                                pageDTO.amount(),
                                                Sort.by("updatedAt").descending()
                                        );

        return productRepository.getDiscountProductList(pageDTO, pageable);
    }

    public List<AdminDiscountProductDTO> getProductListByClassificationId(String classificationId) {
        return productRepository.findAllProductByClassificationId(classificationId);
    }

    public List<Product> getProductListByIds(List<String> productIds) {
        return productRepository.findAllById(productIds);
    }

    public void patchProductDiscount(AdminDiscountPatchDTO patchDTO) {
        productRepository.patchProductDiscount(patchDTO);
    }

    public List<ProductIdClassificationDTO> getClassificationIdAndProductIdByProductIds(List<String> productIds) {
        return productRepository.findClassificationAllByProductIds(productIds);
    }

    public void patchProductSalesQuantity(Map<String, Integer> productPatchMap) {
        productRepository.patchProductSalesQuantity(productPatchMap);
    }

    public void patchOrderStock(List<PatchOrderStockDTO> patchDTO) {
        productOptionRepository.patchOrderStock(patchDTO);
    }
}
