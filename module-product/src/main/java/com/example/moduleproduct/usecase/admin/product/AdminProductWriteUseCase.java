package com.example.moduleproduct.usecase.admin.product;

import com.example.modulecommon.model.entity.Classification;
import com.example.modulecommon.model.entity.Product;
import com.example.moduleproduct.model.dto.admin.product.in.AdminDiscountPatchDTO;
import com.example.moduleproduct.model.dto.admin.product.in.AdminProductImageDTO;
import com.example.moduleproduct.model.dto.admin.product.in.AdminProductPatchDTO;
import com.example.moduleproduct.service.product.ProductDataService;
import com.example.moduleproduct.service.product.ProductDomainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminProductWriteUseCase {

    private final ProductDomainService productDomainService;

    private final ProductDataService productDataService;

    @Transactional(rollbackFor = Exception.class)
    public String postProduct(AdminProductPatchDTO patchDTO, AdminProductImageDTO imageDTO) {
        Product product = patchDTO.toPostEntity();
        String resultId;
        List<String> saveImages = new ArrayList<>();

        try {
            String firstThumbnail = productDomainService.setProductFirstThumbnail(product, imageDTO.getFirstThumbnail());
            if(firstThumbnail != null) {
                saveImages = productDomainService.saveProductImage(product, imageDTO);
                saveImages.add(firstThumbnail);
            }else
                throw new IllegalArgumentException("Failed postProduct. firstThumbnail is null");

            patchDTO.getProductOptionList(product).forEach(product::addProductOption);

            resultId = productDataService.saveProductAndReturnId(product);
            productDataService.saveProductOptions(product.getProductOptions());
            productDataService.saveProductThumbnails(product.getProductThumbnails());
            productDataService.saveProductInfoImages(product.getProductInfoImages());
        }catch (Exception e) {
            log.warn("Filed admin postProduct");
            e.printStackTrace();
            productDomainService.deleteImages(saveImages);

            throw new IllegalArgumentException("Failed postProduct", e);
        }

        return resultId;
    }

    @Transactional(rollbackFor = Exception.class)
    public String patchProduct(String productId,
                               List<Long> deleteOptionList,
                               AdminProductPatchDTO patchDTO,
                               AdminProductImageDTO imageDTO) {
        Product product = productDataService.getProductByIdOrElseIllegal(productId);
        Classification classification = product.getClassification();

        if(!product.getClassification().getId().equals(patchDTO.getClassification()))
            classification = productDataService.getClassificationByIdOrElseIllegal(patchDTO.getClassification());

        product.setPatchData(
                patchDTO.getProductName(),
                patchDTO.getPrice(),
                patchDTO.getIsOpen(),
                patchDTO.getDiscount(),
                classification
        );

        List<String> saveImages = new ArrayList<>();

        try {
            productDomainService.setPatchProductOptionData(product, patchDTO);
            saveImages = productDomainService.saveProductImage(product, imageDTO);

            if((imageDTO.getDeleteFirstThumbnail() == null && imageDTO.getFirstThumbnail() != null) ||
                    (imageDTO.getDeleteFirstThumbnail() != null && imageDTO.getFirstThumbnail() == null))
                throw new IllegalArgumentException();

            String firstThumbnail = productDomainService.setProductFirstThumbnail(product, imageDTO.getFirstThumbnail());

            if(firstThumbnail != null)
                saveImages.add(firstThumbnail);

            productDataService.saveProductOptions(product.getProductOptions());
            productDataService.saveProductThumbnails(product.getProductThumbnails());
            productDataService.saveProductInfoImages(product.getProductInfoImages());
            productDataService.saveProductAndReturnId(product);

            if(deleteOptionList != null)
                productDataService.deleteAllProductOptionByIds(deleteOptionList);
        }catch (Exception e) {
            log.warn("Failed admin patchProduct");
            e.printStackTrace();
            productDomainService.deleteImages(saveImages);

            throw new IllegalArgumentException("Failed patchProduct", e);
        }

        String deleteFirstThumbnail = imageDTO.getDeleteFirstThumbnail();
        List<String> deleteThumbnails = imageDTO.getDeleteThumbnail();
        List<String> deleteInfoImages = imageDTO.getDeleteInfoImage();

        if(deleteFirstThumbnail != null)
            productDomainService.deleteImage(deleteFirstThumbnail);

        if(deleteThumbnails != null && !deleteThumbnails.isEmpty()){
            productDataService.deleteAllProductThumbnailByImageNames(deleteThumbnails);
            productDomainService.deleteImages(deleteThumbnails);
        }

        if(deleteInfoImages != null && !deleteInfoImages.isEmpty()){
            productDataService.deleteAllProductInfoImageByImageNames(deleteInfoImages);
            productDomainService.deleteImages(deleteInfoImages);
        }

        return productId;
    }

    public void patchDiscountProduct(AdminDiscountPatchDTO patchDTO) {
        List<Product> patchProductList = productDataService.getProductListByIds(patchDTO.productIdList());

        if(patchProductList.isEmpty() || patchProductList.size() != patchDTO.productIdList().size())
            throw new IllegalArgumentException("patchDiscountProduct IllegalArgumentException");

        productDataService.patchProductDiscount(patchDTO);
    }
}
