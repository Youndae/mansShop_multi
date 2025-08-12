package com.example.moduleapi.controller.user;

import com.example.moduleapi.annotation.swagger.DefaultApiResponse;
import com.example.moduleapi.annotation.swagger.SwaggerAuthentication;
import com.example.moduleapi.mapper.PagingResponseMapper;
import com.example.moduleapi.model.response.PagingResponseDTO;
import com.example.moduleapi.model.response.ResponseIdDTO;
import com.example.moduleapi.service.PrincipalService;
import com.example.moduleapi.usecase.ReviewWriteUseCase;
import com.example.modulecommon.model.dto.page.MyPagePageDTO;
import com.example.modulecommon.model.dto.qna.in.QnAReplyInsertDTO;
import com.example.modulecommon.model.dto.qna.in.QnAReplyPatchDTO;
import com.example.modulecommon.model.dto.response.PagingListDTO;
import com.example.modulecommon.model.dto.response.ResponseMessageDTO;
import com.example.modulemypage.model.dto.memberQnA.in.MemberQnAInsertDTO;
import com.example.modulemypage.model.dto.memberQnA.in.MemberQnAModifyDTO;
import com.example.modulemypage.model.dto.memberQnA.out.MemberQnADetailResponseDTO;
import com.example.modulemypage.model.dto.memberQnA.out.MemberQnAListDTO;
import com.example.modulemypage.model.dto.memberQnA.out.MemberQnAModifyDataDTO;
import com.example.modulemypage.model.dto.memberQnA.out.QnAClassificationDTO;
import com.example.modulemypage.usecase.MemberQnAReadUseCase;
import com.example.modulemypage.usecase.MemberQnAWriteUseCase;
import com.example.modulenotification.model.dto.out.NotificationListDTO;
import com.example.modulenotification.model.page.NotificationPageDTO;
import com.example.modulenotification.usecase.NotificationReadUseCase;
import com.example.moduleorder.model.dto.in.MemberOrderDTO;
import com.example.moduleorder.model.dto.out.OrderListDTO;
import com.example.moduleorder.model.dto.page.OrderPageDTO;
import com.example.moduleorder.usecase.OrderReadUseCase;
import com.example.moduleproduct.model.dto.page.LikePageDTO;
import com.example.moduleproduct.model.dto.productLike.out.ProductLikeDTO;
import com.example.moduleproduct.model.dto.productQnA.out.ProductQnADetailResponseDTO;
import com.example.moduleproduct.model.dto.productQnA.out.ProductQnAListDTO;
import com.example.moduleproduct.model.dto.productReview.in.MyPagePatchReviewDTO;
import com.example.moduleproduct.model.dto.productReview.in.MyPagePostReviewDTO;
import com.example.moduleproduct.model.dto.productReview.out.MyPagePatchReviewDataDTO;
import com.example.moduleproduct.model.dto.productReview.out.MyPageReviewDTO;
import com.example.moduleproduct.usecase.productLike.ProductLikeReadUseCase;
import com.example.moduleproduct.usecase.productQnA.ProductQnAReadUseCase;
import com.example.moduleproduct.usecase.productQnA.ProductQnAWriteUseCase;
import com.example.moduleproduct.usecase.productReview.ProductReviewReadUseCase;
import com.example.moduleproduct.usecase.productReview.ProductReviewWriteUseCase;
import com.example.moduleuser.model.dto.member.in.MyPageInfoPatchDTO;
import com.example.moduleuser.model.dto.member.out.MyPageInfoDTO;
import com.example.moduleuser.usecase.UserReadUseCase;
import com.example.moduleuser.usecase.UserWriteUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@Tag(name = "MyPage Controller")
@RestController
@RequestMapping("/api/my-page")
@RequiredArgsConstructor
@Slf4j
public class MyPageController {

    private final OrderReadUseCase orderReadUseCase;

    private final ProductLikeReadUseCase productLikeReadUseCase;

    private final ProductQnAReadUseCase productQnAReadUseCase;

    private final ProductQnAWriteUseCase productQnaWriteUseCase;

    private final MemberQnAReadUseCase memberQnAReadUseCase;

    private final MemberQnAWriteUseCase memberQnaWriteUseCase;

    private final ProductReviewReadUseCase productReviewReadUseCase;

    private final ProductReviewWriteUseCase productReviewWriteUseCase;

    private final UserReadUseCase userReadUseCase;

    private final UserWriteUseCase userWriteUseCase;

    private final ReviewWriteUseCase reviewWriteUseCase;

    private final NotificationReadUseCase notificationReadUseCase;

    private final PagingResponseMapper pagingResponseMapper;

    private final PrincipalService principalService;

    /**
     *
     * @param term
     * @param page
     * @param principal
     *
     * 사용자의 주문 내역 조회
     * term으로는 3, 6, 12, all을 받는다.
     * 각 개월수를 의미.
     */
    @Operation(summary = "회원의 주문내역 조회")
    @DefaultApiResponse
    @SwaggerAuthentication
    @Parameters({
            @Parameter(name = "term",
                    description = "주문내역 조회 기간. 3, 6, 12, all",
                    example = "3",
                    required = true,
                    in = ParameterIn.PATH
            ),
            @Parameter(name = "page",
                    description = "페이지 번호",
                    example = "1",
                    required = true,
                    in = ParameterIn.PATH
            )
    })
    @GetMapping("/order/{term}")
    public ResponseEntity<PagingResponseDTO<OrderListDTO>> getOrderList(@PathVariable(name = "term") String term,
                                                                        @RequestParam(name = "page", required = false, defaultValue = "1") int page,
                                                                        Principal principal) {

        OrderPageDTO orderPageDTO = OrderPageDTO.builder()
                .term(term)
                .pageNum(page)
                .build();

        MemberOrderDTO memberOrderDTO = MemberOrderDTO.builder()
                .userId(principal.getName())
                .recipient(null)
                .phone(null)
                .build();

        PagingListDTO<OrderListDTO> responseDTO = orderReadUseCase.getOrderList(orderPageDTO, memberOrderDTO);
        return pagingResponseMapper.toPagingResponse(responseDTO);
    }

    /**
     *
     * @param page
     * @param principal
     *
     * 관심상품으로 등록된 상품의 리스트 조회
     */
    @Operation(summary = "회원의 관심상품 리스트 조회")
    @DefaultApiResponse
    @SwaggerAuthentication
    @Parameter(name = "page",
            description = "페이지 번호",
            example = "1",
            in = ParameterIn.QUERY
    )
    @GetMapping("/like")
    public ResponseEntity<PagingResponseDTO<ProductLikeDTO>> getLikeProduct(@RequestParam(name = "page", required = false, defaultValue = "1") int page,
                                                                            Principal principal) {
        LikePageDTO pageDTO = new LikePageDTO(page);
        String userId = principalService.extractUserId(principal);
        Page<ProductLikeDTO> responseDTO = productLikeReadUseCase.getLikeList(pageDTO, userId);

        return pagingResponseMapper.toPagingResponse(responseDTO);
    }

    /**
     *
     * @param page
     * @param principal
     *
     * 사용자의 상품 문의 리스트 조회.
     */
    @Operation(summary = "회원의 작성한 상품 문의 리스트 조회")
    @DefaultApiResponse
    @SwaggerAuthentication
    @Parameter(name = "page",
            description = "페이지 번호",
            example = "1",
            in = ParameterIn.QUERY
    )
    @GetMapping("/qna/product")
    public ResponseEntity<PagingResponseDTO<ProductQnAListDTO>> getProductQnA(@RequestParam(name = "page", required = false, defaultValue = "1") int page, Principal principal) {
        MyPagePageDTO pageDTO = new MyPagePageDTO(page);
        String userId = principalService.extractUserId(principal);
        Page<ProductQnAListDTO> responseDTO = productQnAReadUseCase.getProductQnAList(pageDTO, userId);

        return pagingResponseMapper.toPagingResponse(responseDTO);
    }

    /**
     *
     * @param qnaId
     * @param principal
     *
     * 사용자의 상품 문의 상세 페이지 데이터 조회
     */
    @Operation(summary = "회원의 상품 문의 상세 데이터")
    @DefaultApiResponse
    @SwaggerAuthentication
    @Parameter(name = "qnaId",
            description = "상품 문의 아이디",
            example = "1",
            required = true,
            in = ParameterIn.PATH
    )
    @GetMapping("/qna/product/detail/{qnaId}")
    public ResponseEntity<ProductQnADetailResponseDTO> getProductQnADetail(@PathVariable(name = "qnaId") long qnaId, Principal principal) {
        String nickname = principalService.getNicknameOrUsername(principal);
        ProductQnADetailResponseDTO responseDTO = productQnAReadUseCase.getProductQnADetail(qnaId, nickname);

        return ResponseEntity.status(HttpStatus.OK)
                .body(responseDTO);
    }

    /**
     *
     * @param qnaId
     * @param principal
     *
     * 사용자의 상품 문의 삭제 요청
     */
    @Operation(summary = "회원의 상품 문의 삭제")
    @DefaultApiResponse
    @SwaggerAuthentication
    @Parameter(name = "qnaId",
            description = "상품 문의 아이디",
            example = "1",
            required = true,
            in = ParameterIn.PATH
    )
    @DeleteMapping("/qna/product/{qnaId}")
    public ResponseEntity<ResponseMessageDTO> deleteProductQnA(@PathVariable(name = "qnaId") long qnaId, Principal principal) {

        String userId = principalService.extractUserId(principal);
        String responseMessage = productQnaWriteUseCase.deleteProductQnA(qnaId, userId);

        return ResponseEntity.status(HttpStatus.OK)
                .body(new ResponseMessageDTO(responseMessage));
    }

    /**
     *
     * @param page
     * @param principal
     *
     * 사용자의 회원 문의 내역 리스트 조회
     */
    @Operation(summary = "회원의 문의내역 조회")
    @DefaultApiResponse
    @SwaggerAuthentication
    @Parameter(name = "page",
            description = "페이지 번호",
            example = "1",
            required = true,
            in = ParameterIn.QUERY
    )
    @GetMapping("/qna/member")
    public ResponseEntity<PagingResponseDTO<MemberQnAListDTO>> getMemberQnA(@RequestParam(name = "page", required = false, defaultValue = "1") int page,
                                                                            Principal principal) {
        MyPagePageDTO pageDTO = new MyPagePageDTO(page);
        String userId = principalService.extractUserId(principal);
        Page<MemberQnAListDTO> responseDTO = memberQnAReadUseCase.getMemberQnAList(pageDTO, userId);

        return pagingResponseMapper.toPagingResponse(responseDTO);
    }

    /**
     *
     * @param insertDTO
     * @param principal
     *
     * 사용자의 회원 문의 내역 작성
     */
    @Operation(summary = "회원의 문의 작성 요청")
    @DefaultApiResponse
    @SwaggerAuthentication
    @PostMapping("/qna/member")
    public ResponseEntity<ResponseIdDTO<Long>> memberQnAInsert(@RequestBody MemberQnAInsertDTO insertDTO, Principal principal) {
        String userId = principalService.extractUserId(principal);

        Long responseId = memberQnaWriteUseCase.postMemberQnA(insertDTO, userId);

        return ResponseEntity.status(HttpStatus.OK)
                .body(new ResponseIdDTO<>(responseId));
    }

    /**
     *
     * @param qnaId
     * @param principal
     *
     * 사용자의 회원 문의 상세 데이터 조회
     */
    @Operation(summary = "회원의 문의 상세 데이터 조회")
    @DefaultApiResponse
    @SwaggerAuthentication
    @Parameter(name = "qnaId",
            description = "회원 문의 아이디",
            example = "1",
            required = true,
            in = ParameterIn.PATH
    )
    @GetMapping("/qna/member/detail/{qnaId}")
    public ResponseEntity<MemberQnADetailResponseDTO> getMemberQnADetail(@PathVariable(name = "qnaId") long qnaId, Principal principal) {
        String userId = principalService.getNicknameOrUsername(principal);
        MemberQnADetailResponseDTO responseDTO = memberQnAReadUseCase.getMemberQnADetail(qnaId, userId);

        return ResponseEntity.status(HttpStatus.OK)
                .body(responseDTO);
    }

    /**
     *
     * @param insertDTO
     * @param principal
     *
     * 사용자의 회원 문의 답변 작성
     */
    @Operation(summary = "회원의 문의 답변 작성")
    @DefaultApiResponse
    @SwaggerAuthentication
    @PostMapping("/qna/member/reply")
    public ResponseEntity<ResponseMessageDTO> postMemberQnAReply(@RequestBody QnAReplyInsertDTO insertDTO, Principal principal) {
        String userId = principalService.extractUserId(principal);

        String responseMessage = memberQnaWriteUseCase.postMemberQnAReply(insertDTO, userId);

        return ResponseEntity.status(HttpStatus.OK)
                .body(new ResponseMessageDTO(responseMessage));
    }

    /**
     *
     * @param replyDTO
     * @param principal
     *
     * 사용자의 회원 문의 답변 수정
     */
    @Operation(summary = "회원의 문의 답변 수정")
    @DefaultApiResponse
    @SwaggerAuthentication
    @PatchMapping("/qna/member/reply")
    public ResponseEntity<ResponseMessageDTO> patchMemberQnAReply(@RequestBody QnAReplyPatchDTO replyDTO, Principal principal) {
        String userId = principalService.extractUserId(principal);

        String responseMessage = memberQnaWriteUseCase.patchMemberQnAReply(replyDTO, userId);

        return ResponseEntity.status(HttpStatus.OK)
                .body(new ResponseMessageDTO(responseMessage));
    }

    /**
     *
     * @param qnaId
     * @param principal
     *
     * 사용자의 회원 문의 수정시 필요한 데이터 요청
     */
    @Operation(summary = "회원의 문의 수정 기능 데이터 조회")
    @DefaultApiResponse
    @SwaggerAuthentication
    @Parameter(name = "qnaId",
            description = "회원 문의 아이디",
            example = "1",
            required = true,
            in = ParameterIn.PATH
    )
    @GetMapping("/qna/member/modify/{qnaId}")
    public ResponseEntity<MemberQnAModifyDataDTO> getModifyData(@PathVariable(name = "qnaId") long qnaId, Principal principal) {
        String userId = principalService.extractUserId(principal);

        MemberQnAModifyDataDTO responseDTO = memberQnAReadUseCase.getModifyData(qnaId, userId);

        return ResponseEntity.status(HttpStatus.OK)
                .body(responseDTO);
    }


    /**
     *
     * @param modifyDTO
     * @param principal
     *
     * 사용자의 회원 문의 수정
     */
    @Operation(summary = "회원의 문의 수정 요청")
    @DefaultApiResponse
    @SwaggerAuthentication
    @PatchMapping("/qna/member")
    public ResponseEntity<ResponseMessageDTO> patchMemberQnA(@RequestBody MemberQnAModifyDTO modifyDTO, Principal principal) {
        String userId = principalService.extractUserId(principal);

        String responseMessage = memberQnaWriteUseCase.patchMemberQnA(modifyDTO, userId);

        return ResponseEntity.status(HttpStatus.OK)
                .body(new ResponseMessageDTO(responseMessage));
    }

    /**
     *
     * @param qnaId
     * @param principal
     *
     * 사용자의 회원 문의 삭제
     */
    @Operation(summary = "회원의 문의 삭제 요청")
    @DefaultApiResponse
    @SwaggerAuthentication
    @Parameter(name = "qnaId",
            description = "문의 아이디",
            example = "1",
            required = true,
            in = ParameterIn.PATH
    )
    @DeleteMapping("/qna/member/{qnaId}")
    public ResponseEntity<ResponseMessageDTO> deleteMemberQnA(@PathVariable(name = "qnaId") long qnaId, Principal principal) {
        String userId = principalService.extractUserId(principal);
        String responseMessage = memberQnaWriteUseCase.deleteMemberQnA(qnaId, userId);

        return ResponseEntity.status(HttpStatus.OK)
                .body(new ResponseMessageDTO(responseMessage));
    }

    /**
     *
     *
     * 회원 문의 작성 또는 수정 시 문의 카테고리 설정을 위한 카테고리 리스트 조회.
     */
    @Operation(summary = "회원의 문의 작성 시 필요한 문의 분류 리스트 조회",
            description = "관리자에도 동일한 기능이 있으나, 거기에서는 모든 분류를 조회하고 여기에서는 노출되어야 할 분류만 조회"
    )
    @DefaultApiResponse
    @SwaggerAuthentication
    @GetMapping("/qna/classification")
    public ResponseEntity<List<QnAClassificationDTO>> getQnAClassification() {
        List<QnAClassificationDTO> responseDTO = memberQnAReadUseCase.getQnAClassification();

        responseDTO.forEach(v -> System.out.println("responseDTO : " + v));

        return ResponseEntity.status(HttpStatus.OK)
                .body(responseDTO);
    }

    /**
     *
     * @param page
     * @param principal
     *
     * 사용자의 작성한 리뷰 리스트 조회
     */
    @Operation(summary = "회원의 작성한 리뷰 목록 조회")
    @DefaultApiResponse
    @SwaggerAuthentication
    @Parameter(name = "page",
            description = "페이지 번호",
            example = "1",
            required = true,
            in = ParameterIn.QUERY
    )
    @GetMapping("/review")
    public ResponseEntity<PagingResponseDTO<MyPageReviewDTO>> getReview(@RequestParam(name = "page", required = false, defaultValue = "1") int page,
                                                                        Principal principal) {
        MyPagePageDTO pageDTO = new MyPagePageDTO(page);
        String userId = principalService.extractUserId(principal);
        Page<MyPageReviewDTO> responseDTO = productReviewReadUseCase.getMyPageReviewList(pageDTO, userId);

        return pagingResponseMapper.toPagingResponse(responseDTO);
    }

    /**
     *
     * @param reviewId
     * @param principal
     *
     * 사용자의 작성한 리뷰 수정 데이터 요청
     */
    @Operation(summary = "회원의 리뷰 수정 기능 데이터 조회")
    @DefaultApiResponse
    @SwaggerAuthentication
    @Parameter(name = "reviewId",
            description = "리뷰 아이디",
            example = "1",
            required = true,
            in = ParameterIn.PATH
    )
    @GetMapping("/review/modify/{reviewId}")
    public ResponseEntity<MyPagePatchReviewDataDTO> getPatchReviewData(@PathVariable(name = "reviewId") long reviewId, Principal principal) {
        String userId = principalService.extractUserId(principal);

        MyPagePatchReviewDataDTO responseDTO = productReviewReadUseCase.getPatchReview(reviewId, userId);

        return ResponseEntity.status(HttpStatus.OK)
                .body(responseDTO);
    }

    /**
     *
     * @param reviewDTO
     * @param principal
     *
     * 사용자의 리뷰 작성
     */
    @Operation(summary = "회원의 리뷰 작성")
    @DefaultApiResponse
    @SwaggerAuthentication
    @PostMapping("/review")
    public ResponseEntity<ResponseMessageDTO> postReview(@RequestBody MyPagePostReviewDTO reviewDTO, Principal principal) {
        String userId = principalService.extractUserId(principal);
        String responseMessage = reviewWriteUseCase.postReview(reviewDTO, userId);

        return ResponseEntity.status(HttpStatus.OK)
                .body(new ResponseMessageDTO(responseMessage));
    }

    /**
     *
     * @param reviewDTO
     * @param principal
     *
     * 사용자의 리뷰 수정
     */
    @Operation(summary = "회원의 리뷰 수정")
    @DefaultApiResponse
    @SwaggerAuthentication
    @PatchMapping("/review")
    public ResponseEntity<ResponseMessageDTO> patchReview(@RequestBody MyPagePatchReviewDTO reviewDTO, Principal principal) {

        String userId = principalService.extractUserId(principal);
        String responseMessage = productReviewWriteUseCase.patchReview(reviewDTO, userId);

        return ResponseEntity.status(HttpStatus.OK)
                .body(new ResponseMessageDTO(responseMessage));
    }

    /**
     *
     * @param reviewId
     * @param principal
     *
     * 사용자의 리뷰 삭제
     */
    @Operation(summary = "회원의 리뷰 삭제")
    @DefaultApiResponse
    @SwaggerAuthentication
    @Parameter(name = "reviewId",
            description = "리뷰 아이디",
            example = "1",
            required = true,
            in = ParameterIn.PATH
    )
    @DeleteMapping("/review/{reviewId}")
    public ResponseEntity<ResponseMessageDTO> deleteReview(@PathVariable(name = "reviewId") long reviewId, Principal principal) {
        String userId = principalService.extractUserId(principal);
        String responseMessage = productReviewWriteUseCase.deleteReview(reviewId, userId);

        return ResponseEntity.status(HttpStatus.OK)
                .body(new ResponseMessageDTO(responseMessage));
    }

    /**
     *
     * @param principal
     *
     * 사용자의 정보 수정 데이터 요청
     */
    @Operation(summary = "회원의 정보 수정 기능 데이터 조회")
    @DefaultApiResponse
    @SwaggerAuthentication
    @GetMapping("/info")
    public ResponseEntity<MyPageInfoDTO> getInfo(Principal principal) {
        String userId = principalService.extractUserId(principal);
        MyPageInfoDTO responseDTO = userReadUseCase.getMyPageUserInfo(userId);

        return ResponseEntity.status(HttpStatus.OK)
                .body(responseDTO);
    }

    /**
     *
     * @param infoDTO
     * @param principal
     *
     * 사용자의 정보 수정 요청
     */
    @Operation(summary = "회원의 정보 수정 요청")
    @DefaultApiResponse
    @SwaggerAuthentication
    @PatchMapping("/info")
    public ResponseEntity<ResponseMessageDTO> patchInfo(@RequestBody MyPageInfoPatchDTO infoDTO, Principal principal) {

        String userId = principalService.extractUserId(principal);
        String responseMessage = userWriteUseCase.patchMyPageUserInfo(infoDTO, userId);

        return ResponseEntity.status(HttpStatus.OK)
                .body(new ResponseMessageDTO(responseMessage));
    }


    @Operation(summary = "회원의 알림 리스트 조회")
    @DefaultApiResponse
    @SwaggerAuthentication
    @GetMapping("/notification")
    public ResponseEntity<PagingResponseDTO<NotificationListDTO>> getNotification(@RequestParam(name = "page", required = false, defaultValue = "1") int page,
                                                                                  Principal principal) {
        NotificationPageDTO pageDTO = new NotificationPageDTO(page);
        String userId = principalService.extractUserId(principal);
        PagingListDTO<NotificationListDTO> responseDTO = notificationReadUseCase.getNotificationList(pageDTO, userId);

        return pagingResponseMapper.toPagingResponse(responseDTO);
    }
}
