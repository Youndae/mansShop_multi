package com.example.moduleproduct.service.product;

import com.example.modulecommon.model.entity.*;
import com.example.modulefile.facade.FileFacade;
import com.example.moduleproduct.model.dto.admin.product.business.AdminOptionStockDTO;
import com.example.moduleproduct.model.dto.admin.product.business.AdminProductStockDataDTO;
import com.example.moduleproduct.model.dto.admin.product.in.AdminProductImageDTO;
import com.example.moduleproduct.model.dto.admin.product.in.AdminProductPatchDTO;
import com.example.moduleproduct.model.dto.admin.product.in.PatchOptionDTO;
import com.example.moduleproduct.model.dto.admin.product.out.AdminProductOptionStockDTO;
import com.example.moduleproduct.model.dto.admin.product.out.AdminProductStockDTO;
import com.example.moduleproduct.model.dto.product.business.ProductQnADTO;
import com.example.moduleproduct.model.dto.product.business.ProductQnAReplyDTO;
import com.example.moduleproduct.model.dto.product.business.ProductQnAResponseDTO;
import com.example.moduleproduct.model.dto.product.out.ProductDetailQnAReplyListDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductDomainService {

    private final FileFacade fileFacade;

    public List<ProductQnAResponseDTO> mapToProductQnAResponseDTO(Page<ProductQnADTO> productQnA,
                                                                  List<ProductDetailQnAReplyListDTO> productQnAReplyList) {
        List<ProductQnAResponseDTO> resultList = new ArrayList<>();

        for(int i = 0; i < productQnA.getContent().size(); i++) {
            List<ProductQnAReplyDTO> replyList = new ArrayList<>();
            ProductQnADTO dto = productQnA.getContent().get(i);

            for(int j = 0; j < productQnAReplyList.size(); j++) {
                if(dto.qnaId().equals(productQnAReplyList.get(j).qnaId())){
                    replyList.add(
                            new ProductQnAReplyDTO(productQnAReplyList.get(j))
                    );
                }
            }

            resultList.add(new ProductQnAResponseDTO(dto, replyList));
        }

        return resultList;
    }

    public String setProductFirstThumbnail(Product product, MultipartFile firstThumbnail) throws Exception {
        String thumbnail = null;

        if (firstThumbnail != null) {
            thumbnail = fileFacade.imageInsert(firstThumbnail);

            product.setThumbnail(thumbnail);
        }

        return thumbnail;
    }

    public List<String> saveProductImage(Product product, AdminProductImageDTO imageDTO) throws Exception {
        List<String> thumbnails = saveThumbnail(product, imageDTO.getThumbnail());
        List<String> infoImages = saveInfoImage(product, imageDTO.getInfoImage());

        thumbnails.addAll(infoImages);

        return thumbnails;
    }

    /**
     *
     * @param product
     * @param imageList
     * @throws Exception
     *
     * 상품 썸네일 이미지 저장 처리 및 저장 파일명 리스트 반환, Product Entity 내부 필드에 추가
     * 예외는 최상위 메서드에서 제어하기 위해 throws
     */
    private List<String> saveThumbnail(Product product, List<MultipartFile> imageList) throws Exception{
        List<String> thumbnailList = new ArrayList<>();

        if(imageList != null){
            for(MultipartFile image : imageList){
                String saveName = fileFacade.imageInsert(image);
                thumbnailList.add(saveName);
                product.addProductThumbnail(
                        ProductThumbnail.builder()
                                .product(product)
                                .imageName(saveName)
                                .build()
                );
            }
        }

        return thumbnailList;
    }

    /**
     *
     * @param product
     * @param imageList
     * @throws Exception
     *
     * 상품 정보 이미지 저장 처리 및 저장 파일명 리스트 반환, Product Entity 내부 필드에 추가
     * 예외는 최상위 메서드에서 제어하기 위해 throws
     */
    private List<String> saveInfoImage(Product product, List<MultipartFile> imageList) throws Exception{
        List<String> infoImages = new ArrayList<>();

        if(imageList != null) {
            for(MultipartFile image : imageList) {
                String saveName = fileFacade.imageInsert(image);
                infoImages.add(saveName);
                product.addProductInfoImage(
                        ProductInfoImage.builder()
                                .product(product)
                                .imageName(saveName)
                                .build()
                );
            }
        }

        return infoImages;
    }

    public void deleteImages(List<String> saveImages) {
        saveImages.forEach(this::deleteImage);
    }

    public void setPatchProductOptionData(Product product, AdminProductPatchDTO patchDTO) {
        List<PatchOptionDTO> optionDTOList = patchDTO.getOptionList();
        List<ProductOption> optionEntities = product.getProductOptions();

        for(int i = 0; i < optionDTOList.size(); i++) {
            PatchOptionDTO dto = optionDTOList.get(i);
            long dtoOptionId = dto.getOptionId();
            boolean patchStatus = true;

            for(int j = 0; j < optionEntities.size(); j++) {
                ProductOption option = optionEntities.get(j);

                if(option.getId() != null && dtoOptionId == option.getId()){
                    option.patchOptionData(
                            dto.getSize(),
                            dto.getColor(),
                            dto.getOptionStock(),
                            dto.isOptionIsOpen()
                    );
                    patchStatus = false;
                    break;
                }
            }

            if(patchStatus)
                product.addProductOption(dto.toEntity());
        }
    }

    public void deleteImage(String imageName) {
        fileFacade.deleteImage(imageName);
    }

    public List<AdminProductStockDTO> mapProductStockDTO(List<AdminProductStockDataDTO> dataList, List<AdminOptionStockDTO> optionList) {
        List<AdminProductStockDTO> responseContent = new ArrayList<>();

        for(int i = 0; i < dataList.size(); i++) {
            AdminProductStockDataDTO stockDTO = dataList.get(i);
            String productId = stockDTO.productId();

            List<AdminProductOptionStockDTO> responseOptionList = optionList.stream()
                    .filter(option ->
                            productId.equals(option.productId()))
                    .map(AdminProductOptionStockDTO::new)
                    .toList();

            responseContent.add(new AdminProductStockDTO(productId, stockDTO, responseOptionList));
        }

        return responseContent;
    }
}
