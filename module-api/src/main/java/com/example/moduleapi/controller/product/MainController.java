package com.example.moduleapi.controller.product;

import com.example.moduleapi.annotation.swagger.DefaultApiResponse;
import com.example.moduleapi.mapper.PagingResponseMapper;
import com.example.moduleapi.model.response.PagingResponseDTO;
import com.example.modulecommon.model.dto.response.PagingListDTO;
import com.example.modulecommon.model.enumuration.Role;
import com.example.modulefile.usecase.FileReadUseCase;
import com.example.moduleorder.model.dto.out.OrderListDTO;
import com.example.moduleorder.model.dto.page.OrderPageDTO;
import com.example.moduleorder.model.dto.in.MemberOrderDTO;
import com.example.moduleorder.usecase.OrderReadUseCase;
import com.example.moduleproduct.model.dto.main.out.MainListResponseDTO;
import com.example.moduleproduct.model.dto.page.MainPageDTO;
import com.example.moduleproduct.usecase.main.MainReadUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Main Controller")
@RestController
@RequestMapping("/api/main")
@RequiredArgsConstructor
@Slf4j
public class MainController {

    private final MainReadUseCase mainReadUseCase;

    private final FileReadUseCase fileReadUseCase;

    private final OrderReadUseCase orderReadUseCase;

    private final PagingResponseMapper pagingResponseMapper;

    /**
     *
     * 메인의 BEST 리스트 조회.
     */
    @Operation(summary = "메인 BEST 상품 조회")
    @DefaultApiResponse
    @GetMapping("/")
    public ResponseEntity<List<MainListResponseDTO>> mainList() {
        MainPageDTO mainPageDTO = new MainPageDTO("BEST");
        List<MainListResponseDTO> responseDTO = mainReadUseCase.getBestProductList(mainPageDTO);

        return ResponseEntity.ok(responseDTO);
    }

    @Operation(summary = "메인 NEW 상품 카테고리 조회")
    @DefaultApiResponse
    @GetMapping("/new")
    public ResponseEntity<List<MainListResponseDTO>> mainNewList() {
        MainPageDTO mainPageDTO = new MainPageDTO("NEW");
        List<MainListResponseDTO> responseDTO = mainReadUseCase.getNewProductList(mainPageDTO);

        return ResponseEntity.ok(responseDTO);
    }


    /**
     *
     * @param classification
     * @param page
     *
     * 메인의 상품 리스트 중 상품 카테고리 선택으로 인한 리스트 조회.
     */
    @Operation(summary = "상품 분류별 조회 요청")
    @DefaultApiResponse
    @Parameters({
            @Parameter(
                    name = "classification",
                    description = "상품 분류. OUTER, TOP, PANTS, SHOES, BAGS",
                    example = "OUTER",
                    required = true,
                    in = ParameterIn.PATH
            ),
            @Parameter(
                    name = "page",
                    description = "페이지 번호",
                    example = "1",
                    in = ParameterIn.QUERY
            )
    })
    @GetMapping("/{classification}")
    public ResponseEntity<PagingResponseDTO<MainListResponseDTO>> mainClassificationList(@PathVariable(name = "classification") String classification,
                                                                                         @RequestParam(name = "page", required = false, defaultValue = "1") int page){
        MainPageDTO mainPageDTO = new MainPageDTO(page, null, classification);

        PagingListDTO<MainListResponseDTO> responseDTO = mainReadUseCase.getClassificationOrSearchList(mainPageDTO);

        return pagingResponseMapper.toPagingResponse(responseDTO);
    }

    /**
     *
     * @param page
     * @param keyword
     *
     * Navbar의 상품 검색 기능
     */
    @Operation(summary = "상품 검색")
    @DefaultApiResponse
    @Parameters({
            @Parameter(name = "page",
                    description = "페이지 번호",
                    example = "1",
                    in = ParameterIn.QUERY
            ),
            @Parameter(name = "keyword",
                    description = "검색어",
                    example = "DummyOUTER",
                    required = true
            )
    })
    @GetMapping("/search")
    public ResponseEntity<PagingResponseDTO<MainListResponseDTO>> searchList(@RequestParam(name = "page", required = false, defaultValue = "1") int page,
                                                                             @RequestParam(name = "keyword") String keyword){

        MainPageDTO mainPageDTO = new MainPageDTO(page, keyword, null);
        PagingListDTO<MainListResponseDTO> responseDTO = mainReadUseCase.getClassificationOrSearchList(mainPageDTO);

        return pagingResponseMapper.toPagingResponse(responseDTO);
    }

    /**
     *
     * @param imageName
     *
     * 이미지 파일 조회
     */
    @Operation(summary = "이미지 파일 Binary Data 반환")
    @DefaultApiResponse
    @Parameter(name = "imageName",
            description = "이미지 파일명",
            example = "2149347511.jpg",
            required = true,
            in = ParameterIn.PATH
    )
    @GetMapping("/display/{imageName}")
    public ResponseEntity<?> displayByS3(@PathVariable(name = "imageName") String imageName) {

        return fileReadUseCase.getDisplayImage(imageName);
    }

    /**
     *
     * @param recipient
     * @param phone
     * @param term
     * @param page
     *
     * 비회원의 주문 내역 조회
     * 가능성은 별로 없다고 생각하지만 비회원도 같은 recipient와 연락처로 장기간 동안 주문한 경우
     * 해당 기간 동안의 내역을 출력할 수 있어야 한다고 생각해 사용자 주문내역과 마찬가지로 term을 받아 기간별 조회 처리.
     */
    @Operation(summary = "비회원의 주문내역 조회")
    @DefaultApiResponse
    @Parameters({
            @Parameter(name = "recipient",
                    description = "수령인",
                    example = "테스터1000",
                    required = true,
                    in = ParameterIn.QUERY
            ),
            @Parameter(name = "phone",
                    description = "수령인 연락처",
                    example = "01034568890",
                    required = true,
                    in = ParameterIn.QUERY
            ),
            @Parameter(name = "term",
                    description = "조회 기간. 페이지 최초 접근시에는 3으로 처리. 3, 6, 12, all로 구성",
                    example = "3",
                    required = true,
                    in = ParameterIn.PATH
            ),
            @Parameter(name = "page",
                    description = "페이지 번호. 최소값 1",
                    example = "1",
                    required = true,
                    in = ParameterIn.PATH
            )
    })
    @GetMapping("/order/{term}")
    public ResponseEntity<PagingResponseDTO<OrderListDTO>> nonMemberOrderList(@RequestParam(name = "recipient") String recipient,
                                                                                @RequestParam(name = "phone") String phone,
                                                                                @PathVariable(name = "term") String term,
                                                                                @RequestParam(name = "page", required = false, defaultValue = "1") int page){

        MemberOrderDTO memberOrderDTO = MemberOrderDTO.builder()
                .userId(Role.ANONYMOUS.getRole())
                .recipient(recipient)
                .phone(phone)
                .build();

        OrderPageDTO orderPageDTO = OrderPageDTO.builder()
                .pageNum(page)
                .term(term)
                .build();


        PagingListDTO<OrderListDTO> responseDTO = orderReadUseCase.getOrderList(orderPageDTO, memberOrderDTO);

        return pagingResponseMapper.toPagingResponse(responseDTO);
    }
}
