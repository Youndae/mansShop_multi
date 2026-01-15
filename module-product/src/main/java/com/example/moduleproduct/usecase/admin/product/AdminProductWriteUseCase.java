package com.example.moduleproduct.usecase.admin.product;

import com.example.modulecommon.model.entity.Classification;
import com.example.modulecommon.model.entity.Product;
import com.example.moduleproduct.model.dto.admin.product.in.*;
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
    public String postProduct(AdminProductPostDTO patchDTO, AdminProductImageDTO imageDTO) {
        Product product = patchDTO.toPostEntity();
        String resultId;
        List<String> saveImages = new ArrayList<>();

        // optionList Validation
        if(patchDTO.getOptionList() != null && !patchDTO.getOptionList().isEmpty())
            patchDTO.getProductOptionList(product).forEach(v -> {

                //getProductOptionList에서 호출되는 toEntity에 의해 정상적으로 0이 들어왔다면
                //null이 되기 때문에 null로 체크
                //null 사용 이유는 GeneratedValue IDENTITY 전략이기 때문.
                if(v.getId() != null)
                    throw new IllegalArgumentException("AdminProductWriteUseCase.postProduct :: addOptionId is Not Zero");

                if(v.getStock() < 0)
                    throw new IllegalArgumentException("AdminProductWriteUseCase.postProduct :: addOptionStock is Less Then Zero");

                product.addProductOption(v);
            });

        if(imageDTO.getFirstThumbnail() == null || imageDTO.getFirstThumbnail().isEmpty())
            throw new IllegalArgumentException("AdminProductWriteUseCase.postProduct :: firstThumbnail is null or Empty");

        if(imageDTO.getInfoImage() == null || imageDTO.getInfoImage().isEmpty())
            throw new IllegalArgumentException("AdminProductWriteUseCase.postProduct :: InfoImage is null or Empty");

        try {
            String firstThumbnail = productDomainService.setProductFirstThumbnail(product, imageDTO.getFirstThumbnail());
            saveImages = productDomainService.saveProductImage(product, imageDTO.getThumbnail(), imageDTO.getInfoImage());
            saveImages.add(firstThumbnail);

            resultId = productDataService.saveProductAndReturnId(product);
            productDataService.saveProductOptions(product.getProductOptions());
            productDataService.saveProductThumbnails(product.getProductThumbnails());
            productDataService.saveProductInfoImages(product.getProductInfoImages());
        }catch (Exception e) {
            log.warn("Filed admin postProduct");
            e.printStackTrace();
            productDomainService.deleteImages(saveImages);

            throw new IllegalArgumentException("AdminProductWriteUseCase.postProduct :: Failed postProduct", e);
        }

        return resultId;
    }

    @Transactional(rollbackFor = Exception.class)
    public String patchProduct(String productId,
                               List<Long> deleteOptionList,
                               AdminProductPatchDTO patchDTO,
                               AdminProductImagePatchDTO imageDTO) {
        Product product = productDataService.getProductByIdOrElseIllegal(productId);
        Classification classification = product.getClassification();
        List<String> productInfoImageNames = productDataService.getProductInfoImageNameList(productId);

        //FirstThumbnail 컬럼은 NotNull 이므로 삭제 데이터가 있는 경우 새로운 Thumbnail이 필요.
        if((imageDTO.getDeleteFirstThumbnail() == null && imageDTO.getFirstThumbnail() != null)
                || (imageDTO.getDeleteFirstThumbnail() != null && imageDTO.getFirstThumbnail() == null)){
            log.debug("AdminProductWriteUseCase.patchProduct :: FirstThumbnail Validation Error. deleteFirstThumbnail = {}, firstThumbnail = {}", imageDTO.getDeleteFirstThumbnail(), imageDTO.getFirstThumbnail());
            throw new IllegalArgumentException("AdminProductWriteUseCase.patchProduct :: FirstThumbnail Field Error");
        }

        // InfoImage는 최소 1장이 필요하므로 삭제하고자 하는 데이터 크기와 존재하는 크기의 데이터가 일치하는 경우
        // 새로운 InfoImage가 필수로 존재해야 함.
        if(imageDTO.getDeleteInfoImage() != null) {
            if(imageDTO.getDeleteInfoImage().size() == productInfoImageNames.size() && (imageDTO.getInfoImage() == null || imageDTO.getInfoImage().isEmpty())){
                log.debug("AdminProductWriteUseCase.patchProduct :: infoImage Validation Error. deleteInfoImageSize = {}, saveInfoImageNameSize = {}, newInfoImageSize = {}",
                        imageDTO.getDeleteInfoImage().size(),
                        productInfoImageNames.size(),
                        imageDTO.getInfoImage() == null ? "null" : 0
                );
                throw new IllegalArgumentException("AdminProductWriteUseCase.patchProduct :: InfoImage Field Error");
            }
        }

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
            saveImages = productDomainService.saveProductImage(product, imageDTO.getThumbnail(), imageDTO.getInfoImage());

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

            throw new IllegalArgumentException("AdminProductWriteUseCase.patchProduct :: Failed patchProduct", e);
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
