package com.example.moduleproduct.usecase.admin.product;

import com.example.modulecommon.customException.CustomNotFoundException;
import com.example.modulecommon.model.dto.response.PagingListDTO;
import com.example.modulecommon.model.dto.response.PagingMappingDTO;
import com.example.modulecommon.model.entity.Classification;
import com.example.modulecommon.model.entity.Product;
import com.example.modulecommon.model.enumuration.ErrorCode;
import com.example.moduleproduct.model.dto.admin.product.business.AdminOptionStockDTO;
import com.example.moduleproduct.model.dto.admin.product.business.AdminProductStockDataDTO;
import com.example.moduleproduct.model.dto.admin.product.out.*;
import com.example.moduleproduct.model.dto.page.AdminProductPageDTO;
import com.example.moduleproduct.service.product.ProductDataService;
import com.example.moduleproduct.service.product.ProductDomainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminProductReadUseCase {

    private final ProductDataService productDataService;

    private final ProductDomainService productDomainService;

    public PagingListDTO<AdminProductListDTO> getProductList(AdminProductPageDTO pageDTO) {
        List<AdminProductListDTO> dto = productDataService.getAdminProductPageList(pageDTO);

        Long totalElements = 0L;

        if(!dto.isEmpty())
            totalElements = productDataService.getAdminProductListFullCount(pageDTO);

        PagingMappingDTO pagingMappingDTO = new PagingMappingDTO(totalElements, pageDTO.page(), pageDTO.amount());

        return new PagingListDTO<>(dto, pagingMappingDTO);
    }

    public List<String> getProductClassificationIdList() {
        List<Classification> classifications = productDataService.getAllClassification();

        return classifications.stream().map(Classification::getId).toList();
    }

    public AdminProductDetailDTO getProductDetailData(String productId) {
        Product product = productDataService.getProductByIdOrElseIllegal(productId);
        List<AdminProductOptionDTO> productOptionList = productDataService.findAllProductOptionByProductId(productId);
        List<String> thumbnailList = productDataService.getProductThumbnailImageNameList(productId);
        List<String> infoImageList = productDataService.getProductInfoImageNameList(productId);

        return new AdminProductDetailDTO(productId, product, thumbnailList, infoImageList, productOptionList);
    }

    public AdminProductPatchDataDTO getPatchProductData(String productId) {
        AdminProductDetailDTO dto = getProductDetailData(productId);
        List<String> classificationIdList = getProductClassificationIdList();

        return new AdminProductPatchDataDTO(dto, classificationIdList);
    }

    public PagingListDTO<AdminProductStockDTO> getProductStockList(AdminProductPageDTO pageDTO) {
        List<AdminProductStockDataDTO> dataList = productDataService.getAllProductStockData(pageDTO);
        Long totalElements = 0L;

        if(dataList.isEmpty()){
            PagingMappingDTO pagingMappingDTO = new PagingMappingDTO(totalElements, pageDTO.page(), pageDTO.amount());
            return new PagingListDTO<>(Collections.emptyList(), pagingMappingDTO);
        }

        totalElements = productDataService.getAllProductStockDataCount(pageDTO);
        PagingMappingDTO pagingMappingDTO = new PagingMappingDTO(totalElements, pageDTO.page(), pageDTO.amount());
        List<String> productIds = dataList.stream().map(AdminProductStockDataDTO::productId).toList();
        List<AdminOptionStockDTO> optionList = productDataService.getAllProductOptionStockByProductIds(productIds);
        List<AdminProductStockDTO> response = productDomainService.mapProductStockDTO(dataList, optionList);

        return new PagingListDTO<>(response, pagingMappingDTO);
    }

    public PagingListDTO<AdminDiscountResponseDTO> getDiscountProductList(AdminProductPageDTO pageDTO) {
        Page<Product> discountProductList = productDataService.getDiscountProductPageList(pageDTO);
        List<AdminDiscountResponseDTO> responseContent = discountProductList.getContent()
                .stream()
                .map(AdminDiscountResponseDTO::new)
                .toList();
        PagingMappingDTO pagingMappingDTO = new PagingMappingDTO(discountProductList.getTotalElements(), pageDTO.page(), pageDTO.amount());

        return new PagingListDTO<>(responseContent, pagingMappingDTO);
    }

    public List<AdminDiscountProductDTO> getSelectDiscountProductList(String classificationId) {
        List<AdminDiscountProductDTO> list = productDataService.getProductListByClassificationId(classificationId);

        if(list.isEmpty()){
            log.warn("AdminProductReadUseCase.getSelectDiscountProductList :: List result is empty. Request ClassificationId = {}", classificationId);
            throw new CustomNotFoundException(ErrorCode.BAD_REQUEST, "AdminProductReadUseCase.getSelectDiscountProductList :: NotFound");
        }

        return list;
    }
}
