package com.example.moduleapi.controller.admin;

import com.example.moduleapi.annotation.swagger.DefaultApiResponse;
import com.example.moduleapi.annotation.swagger.SwaggerAuthentication;
import com.example.moduleapi.mapper.PagingResponseMapper;
import com.example.moduleapi.model.response.PagingResponseDTO;
import com.example.moduleapi.service.PrincipalService;
import com.example.modulecache.model.cache.CacheRequest;
import com.example.modulecache.service.FullCountScanCachingService;
import com.example.modulecommon.model.dto.admin.qna.out.AdminQnAListResponseDTO;
import com.example.modulecommon.model.dto.page.AdminQnAPageDTO;
import com.example.modulecommon.model.dto.qna.in.QnAReplyInsertDTO;
import com.example.modulecommon.model.dto.qna.in.QnAReplyPatchDTO;
import com.example.modulecommon.model.dto.response.PagingListDTO;
import com.example.modulecommon.model.dto.response.ResponseMessageDTO;
import com.example.modulecommon.model.enumuration.AdminListType;
import com.example.modulecommon.model.enumuration.RedisCaching;
import com.example.modulemypage.model.dto.memberQnA.out.MemberQnADetailResponseDTO;
import com.example.modulemypage.model.dto.memberQnA.out.QnAClassificationDTO;
import com.example.modulemypage.usecase.MemberQnAReadUseCase;
import com.example.modulemypage.usecase.MemberQnAWriteUseCase;
import com.example.modulemypage.usecase.admin.AdminMemberQnAReadUseCase;
import com.example.modulemypage.usecase.admin.AdminMemberQnAWriteUseCase;
import com.example.moduleproduct.model.dto.productQnA.out.ProductQnADetailResponseDTO;
import com.example.moduleproduct.usecase.admin.productQnA.AdminProductQnAReadUseCase;
import com.example.moduleproduct.usecase.admin.productQnA.AdminProductQnAWriteUseCase;
import com.example.moduleproduct.usecase.productQnA.ProductQnAReadUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@Tag(name = "Admin QnA Controller")
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminQnAController {

    private final AdminProductQnAReadUseCase adminProductQnAReadUseCase;

    private final AdminProductQnAWriteUseCase adminProductQnAWriteUseCase;

    private final ProductQnAReadUseCase productQnAReadUseCase;

    private final AdminMemberQnAReadUseCase adminMemberQnAReadUseCase;

    private final AdminMemberQnAWriteUseCase adminMemberQnAWriteUseCase;

    private final MemberQnAReadUseCase memberQnAReadUseCase;

    private final MemberQnAWriteUseCase memberQnAWriteUseCase;

    private final PrincipalService principalService;

    private final PagingResponseMapper pagingResponseMapper;

    private final FullCountScanCachingService fullCountScanCachingService;

    /**
     *
     * @param keyword
     * @param page
     * @param listType
     *
     * 상품 문의 리스트
     */
    @Operation(summary = "상품 문의 리스트")
    @DefaultApiResponse
    @SwaggerAuthentication
    @Parameters({
            @Parameter(name = "page",
                    description = "페이지 번호",
                    example = "1",
                    in = ParameterIn.QUERY
            ),
            @Parameter(name = "type",
                    description = "조회 리스트 타입. new 또는 all",
                    example = "all",
                    required = true,
                    in = ParameterIn.QUERY
            ),
            @Parameter(name = "keyword",
                    description = "검색어(닉네임 또는 아이디)",
                    example = "tester1",
                    in = ParameterIn.QUERY
            )
    })
    @GetMapping("/qna/product")
    public ResponseEntity<PagingResponseDTO<AdminQnAListResponseDTO>> getProductQnA(@RequestParam(name = "keyword", required = false) String keyword,
                                                                                    @RequestParam(name = "page", required = false, defaultValue = "1") int page,
                                                                                    @RequestParam(name = "type") String listType) {

        AdminQnAPageDTO pageDTO = new AdminQnAPageDTO(keyword, listType, page);
        long totalElements = 0L;

        if(keyword == null && listType.equals(AdminListType.ALL.getType()))
            totalElements = fullCountScanCachingService.getFullScanCountCache(RedisCaching.ADMIN_PRODUCT_QNA_COUNT, new CacheRequest<>(pageDTO));

        PagingListDTO<AdminQnAListResponseDTO> responseDTO = adminProductQnAReadUseCase.getProductQnAList(pageDTO, totalElements);

        return pagingResponseMapper.toPagingResponse(responseDTO);
    }

    /**
     *
     * @param qnaId
     *
     * 상품 문의 상세 정보 조회
     */
    @Operation(summary = "상품 문의 상세 정보 조회")
    @DefaultApiResponse
    @SwaggerAuthentication
    @Parameter(name = "qnaId",
            description = "상품 문의 아이디",
            example = "1",
            required = true,
            in = ParameterIn.PATH
    )
    @GetMapping("/qna/product/{qnaId}")
    public ResponseEntity<ProductQnADetailResponseDTO> getProductQnADetail(@PathVariable(name = "qnaId") long qnaId) {


        ProductQnADetailResponseDTO responseDTO = productQnAReadUseCase.getProductQnADetailData(qnaId);

        return ResponseEntity.status(HttpStatus.OK)
                .body(responseDTO);
    }

    /**
     *
     * @param qnaId
     *
     * 상품 문의 답변 완료 상태로 수정
     */
    @Operation(summary = "상품 문의 답변 상태를 완료로 수정")
    @DefaultApiResponse
    @SwaggerAuthentication
    @Parameter(name = "qnaId",
            description = "상품 문의 아이디",
            example = "1",
            required = true,
            in = ParameterIn.PATH
    )
    @PatchMapping("/qna/product/{qnaId}")
    public ResponseEntity<ResponseMessageDTO> patchProductQnAComplete(@PathVariable(name = "qnaId") long qnaId) {

        String responseMessage = adminProductQnAWriteUseCase.patchProductQnAComplete(qnaId);

        return ResponseEntity.status(HttpStatus.OK)
                .body(new ResponseMessageDTO(responseMessage));
    }

    /**
     *
     * @param insertDTO
     * @param principal
     *
     * 관리자의 상품 문의 답변 작성
     */
    @Operation(summary = "상품 문의 답변 작성")
    @DefaultApiResponse
    @SwaggerAuthentication
    @PostMapping("/qna/product/reply")
    public ResponseEntity<ResponseMessageDTO> postProductQnAReply(@RequestBody QnAReplyInsertDTO insertDTO, Principal principal) {

        String userId = principalService.extractUserId(principal);
        String responseMessage = adminProductQnAWriteUseCase.postProductQnAReply(insertDTO, userId);

        return ResponseEntity.status(HttpStatus.OK)
                .body(new ResponseMessageDTO(responseMessage));
    }

    /**
     *
     * @param replyDTO
     * @param principal
     *
     * 관리자의 상품 문의 답변 수정
     */
    @Operation(summary = "상품 문의 답변 수정")
    @DefaultApiResponse
    @SwaggerAuthentication
    @PatchMapping("/qna/product/reply")
    public ResponseEntity<ResponseMessageDTO> patchProductQnAReply(@RequestBody QnAReplyPatchDTO replyDTO, Principal principal) {

        String userId = principalService.extractUserId(principal);
        String responseMessage = adminProductQnAWriteUseCase.patchProductQnAReply(replyDTO, userId);

        return ResponseEntity.status(HttpStatus.OK)
                .body(new ResponseMessageDTO(responseMessage));
    }

    /**
     *
     * @param keyword
     * @param page
     * @param listType
     *
     * 관리자의 회원 문의 리스트 조회
     */
    @Operation(summary = "회원 문의 리스트 조회")
    @DefaultApiResponse
    @SwaggerAuthentication
    @Parameters({
            @Parameter(name = "page",
                    description = "페이지 번호",
                    example = "1",
                    in = ParameterIn.QUERY
            ),
            @Parameter(name = "listType",
                    description = "조회 리스트 타입. new 또는 all",
                    example = "all",
                    required = true,
                    in = ParameterIn.QUERY
            ),
            @Parameter(name = "keyword",
                    description = "검색어(아이디 또는 닉네임)",
                    example = "tester1",
                    in = ParameterIn.QUERY
            )
    })
    @GetMapping("/qna/member")
    public ResponseEntity<PagingResponseDTO<AdminQnAListResponseDTO>> getMemberQnA(@RequestParam(name = "keyword", required = false) String keyword,
                                                                                   @RequestParam(name = "page", required = false, defaultValue = "1") int page,
                                                                                   @RequestParam(name = "type") String listType) {

        AdminQnAPageDTO pageDTO = new AdminQnAPageDTO(keyword, listType, page);
        long totalElements = 0L;

        if(keyword == null && listType.equals(AdminListType.ALL.getType()))
            totalElements = fullCountScanCachingService.getFullScanCountCache(RedisCaching.ADMIN_MEMBER_QNA_COUNT, new CacheRequest<>(pageDTO));

        PagingListDTO<AdminQnAListResponseDTO> responseDTO = adminMemberQnAReadUseCase.getMemberQnAList(pageDTO, totalElements);

        return pagingResponseMapper.toPagingResponse(responseDTO);
    }

    /**
     *
     * @param qnaId
     *
     * 관리자의 회원 문의 상세 조회
     */
    @Operation(summary = "회원 문의 상세 조회")
    @DefaultApiResponse
    @SwaggerAuthentication
    @Parameter(name = "qnaId",
            description = "회원 문의 아이디",
            example = "1",
            required = true,
            in = ParameterIn.PATH
    )
    @GetMapping("/qna/member/{qnaId}")
    public ResponseEntity<MemberQnADetailResponseDTO> getMemberQnADetail(@PathVariable(name = "qnaId") long qnaId) {

        MemberQnADetailResponseDTO responseDTO = memberQnAReadUseCase.getMemberQnADetailData(qnaId);

        return ResponseEntity.status(HttpStatus.OK)
                .body(responseDTO);
    }

    /**
     *
     * @param qnaId
     *
     * 관리자의 회원 문의 답변 완료 처리
     */
    @Operation(summary = "회원 문의 답변 상태 완료로 수정")
    @DefaultApiResponse
    @SwaggerAuthentication
    @Parameter(name = "qnaId",
            description = "회원 문의 아이디",
            example = "1",
            required = true,
            in = ParameterIn.PATH
    )
    @PatchMapping("/qna/member/{qnaId}")
    public ResponseEntity<ResponseMessageDTO> patchMemberQnAComplete(@PathVariable(name = "qnaId") long qnaId) {
        String responseMessage = adminMemberQnAWriteUseCase.patchMemberQnAComplete(qnaId);

        return ResponseEntity.status(HttpStatus.OK)
                .body(new ResponseMessageDTO(responseMessage));
    }

    /**
     *
     * @param insertDTO
     * @param principal
     *
     * 관리자의 회원 문의 답변 작성.
     */
    @Operation(summary = "회원 문의 답변 작성")
    @DefaultApiResponse
    @SwaggerAuthentication
    @PostMapping("/qna/member/reply")
    public ResponseEntity<ResponseMessageDTO> postMemberQnAReply(@RequestBody QnAReplyInsertDTO insertDTO, Principal principal) {

        String userId = principalService.extractUserId(principal);
        String responseMessage = adminMemberQnAWriteUseCase.postMemberQnAReply(insertDTO, userId);

        return ResponseEntity.status(HttpStatus.OK)
                .body(new ResponseMessageDTO(responseMessage));
    }

    /**
     *
     * @param replyDTO
     * @param principal
     *
     * 관리자의 회원 문의 답변 수정.
     */
    @Operation(summary = "회원 문의 답변 수정")
    @DefaultApiResponse
    @SwaggerAuthentication
    @PatchMapping("/qna/member/reply")
    public ResponseEntity<ResponseMessageDTO> patchMemberQnAReply(@RequestBody QnAReplyPatchDTO replyDTO, Principal principal) {

        String userId = principalService.extractUserId(principal);
        String responseMessage = memberQnAWriteUseCase.patchMemberQnAReply(replyDTO, userId);

        return ResponseEntity.status(HttpStatus.OK)
                .body(new ResponseMessageDTO(responseMessage));
    }

    /**
     *
     * 관리자의 회원 문의 분류 조회
     */
    @Operation(summary = "회원 문의 분류 조회",
            description = "회원 문의 분류 작성 기능 시 모든 분류 데이터를 출력하기 위해 사용"
    )
    @DefaultApiResponse
    @SwaggerAuthentication
    @GetMapping("/qna/classification")
    public ResponseEntity<List<QnAClassificationDTO>> getQnAClassification() {

        List<QnAClassificationDTO> responseDTO = memberQnAReadUseCase.getQnAClassification();

        return ResponseEntity.status(HttpStatus.OK)
                .body(responseDTO);
    }

    /**
     *
     * @param classification
     *
     * 관리자의 회원 문의 분류 추가
     */
    @Operation(summary = "회원 문의 분류 추가", description = "분류명만 담아 요청")
    @DefaultApiResponse
    @SwaggerAuthentication
    @PostMapping("/qna/classification")
    public ResponseEntity<ResponseMessageDTO> postQnAClassification(@RequestBody String classification) {
        String responseMessage = adminMemberQnAWriteUseCase.postQnAClassification(classification);

        return ResponseEntity.status(HttpStatus.OK)
                .body(new ResponseMessageDTO(responseMessage));
    }

    /**
     *
     * @param classificationId
     *
     * 관리자의 회원문의 분류 제거
     */
    @Operation(summary = "회원 문의 분류 삭제")
    @DefaultApiResponse
    @SwaggerAuthentication
    @Parameter(name = "qnaClassificationId",
            description = "삭제할 회원 문의 분류 아이디",
            example = "1",
            required = true,
            in = ParameterIn.PATH
    )
    @DeleteMapping("/qna/classification/{qnaClassificationId}")
    public ResponseEntity<ResponseMessageDTO> deleteQnAClassification(@PathVariable(name = "qnaClassificationId") Long classificationId) {

        String responseMessage = adminMemberQnAWriteUseCase.deleteQnAClassification(classificationId);

        return ResponseEntity.status(HttpStatus.OK)
                .body(new ResponseMessageDTO(responseMessage));
    }
}
