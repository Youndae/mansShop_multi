package com.example.moduleapi.controller.admin;

import com.example.moduleapi.annotation.swagger.DefaultApiResponse;
import com.example.moduleapi.annotation.swagger.SwaggerAuthentication;
import com.example.moduleapi.mapper.PagingResponseMapper;
import com.example.moduleapi.model.response.PagingResponseDTO;
import com.example.moduleuser.model.dto.admin.in.AdminMemberListRequestDTO;
import com.example.moduleuser.model.dto.admin.in.AdminPostPointDTO;
import com.example.moduleuser.model.dto.admin.out.AdminMemberDTO;
import com.example.moduleuser.model.dto.admin.page.AdminMemberPageDTO;
import com.example.moduleuser.usecase.admin.AdminMemberReadUseCase;
import com.example.moduleuser.usecase.admin.AdminMemberWriteUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Admin Member Controller")
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminMemberController {

    private final PagingResponseMapper pagingResponseMapper;

    private final AdminMemberReadUseCase adminMemberReadUseCase;

    private final AdminMemberWriteUseCase adminMemberWriteUseCase;

    /**
     *
     * @param requestDTO
     *
     * 관리자의 회원 목록 조회
     */
    @Operation(summary = "회원 목록 조회",
            description = "검색 타입으로는 아이디, 사용자 이름, 닉네임이 존재"
    )
    @DefaultApiResponse
    @GetMapping("/member")
    public ResponseEntity<PagingResponseDTO<AdminMemberDTO>> getMember(
            @ParameterObject @Validated AdminMemberListRequestDTO requestDTO,
            @RequestParam(name = "searchType", required = false) @Size(min = 2, message = "searchType length min 2") String searchType
    ) {
        AdminMemberPageDTO pageDTO = AdminMemberPageDTO.fromRequestDTO(requestDTO, searchType);
        Page<AdminMemberDTO> responseDTO = adminMemberReadUseCase.getAdminMemberList(pageDTO);

        return pagingResponseMapper.toPagingResponse(responseDTO);
    }

    /**
     *
     * @param pointDTO
     *
     * 회원에게 포인트 직접 지급
     */
    @Operation(summary = "회원 포인트 지급")
    @DefaultApiResponse
    @SwaggerAuthentication
    @PatchMapping("/member/point")
    public ResponseEntity<Void> postPoint(@Validated @RequestBody AdminPostPointDTO pointDTO){

        adminMemberWriteUseCase.postPoint(pointDTO);

        return ResponseEntity.ok().build();
    }
}
