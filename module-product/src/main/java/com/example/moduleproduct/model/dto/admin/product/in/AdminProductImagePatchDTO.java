package com.example.moduleproduct.model.dto.admin.product.in;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "상품 추가 및 수정시 필요한 이미지 데이터")
public class AdminProductImagePatchDTO {

    @Schema(description = "상품 대표 썸네일")
    private MultipartFile firstThumbnail;

    @Schema(description = "삭제할 대표 썸네일 파일명")
    private String deleteFirstThumbnail;

    @Schema(description = "썸네일 리스트")
    private List<MultipartFile> thumbnail;

    @Schema(description = "삭제할 썸네일 파일명 리스트")
    private List<String> deleteThumbnail;

    @Schema(description = "상품 정보 이미지 리스트")
    private List<MultipartFile> infoImage;

    @Schema(description = "삭제할 상품 정보 이미지명 리스트")
    private List<String> deleteInfoImage;
}
