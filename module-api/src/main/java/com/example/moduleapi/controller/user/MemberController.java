package com.example.moduleapi.controller.user;

import com.example.moduleapi.annotation.swagger.DefaultApiResponse;
import com.example.moduleapi.annotation.swagger.SwaggerAuthentication;
import com.example.moduleapi.config.exception.ExceptionEntity;
import com.example.moduleauth.model.dto.member.UserSearchDTO;
import com.example.moduleauth.model.dto.member.UserSearchPwDTO;
import com.example.modulecommon.customException.CustomAccessDeniedException;
import com.example.modulecommon.model.dto.response.ResponseMessageDTO;
import com.example.modulecommon.model.enumuration.ErrorCode;
import com.example.moduleuser.model.dto.member.in.*;
import com.example.moduleuser.model.dto.member.out.UserSearchIdResponseDTO;
import com.example.moduleuser.model.dto.member.out.UserStatusResponseDTO;
import com.example.moduleuser.usecase.UserReadUseCase;
import com.example.moduleuser.usecase.UserWriteUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.WebUtils;

import java.security.Principal;

@RestController
@RequestMapping("/api/member")
@RequiredArgsConstructor
@Slf4j
public class MemberController {

    @Value("#{jwt['token.access.header']}")
    private String authorizationHeader;

    @Value("#{jwt['cookie.ino.header']}")
    private String inoHeader;

    private final UserReadUseCase userReadUseCase;

    private final UserWriteUseCase userWriteUseCase;

    /**
     *
     * @param loginDTO
     * @param request
     * @param response
     *
     * 로컬 로그인 요청
     */
    @Operation(summary = "로그인 요청")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공"),
            @ApiResponse(responseCode = "403", description = "권한 정보 불일치"
                    , content = @Content(schema = @Schema(implementation = ExceptionEntity.class))
            ),
            @ApiResponse(responseCode = "400", description = "존재하지 않는 데이터"
                    , content = @Content(schema = @Schema(implementation = ExceptionEntity.class))
            ),
            @ApiResponse(responseCode = "800", description = "토큰 탈취"
                    , content = @Content(schema = @Schema(implementation = ExceptionEntity.class))
            )
    })
    @PostMapping("/login")
    public ResponseEntity<UserStatusResponseDTO> loginProc(@RequestBody LoginDTO loginDTO,
                                                           HttpServletRequest request,
                                                           HttpServletResponse response){

        UserStatusResponseDTO result = userWriteUseCase.loginProc(loginDTO, request, response);

        return ResponseEntity.status(HttpStatus.OK)
                .body(result);
    }

    /**
     *
     * @param request
     * @param response
     * @param principal
     *
     * 로그아웃 요청
     */
    @Operation(summary = "로그아웃 요청")
    @DefaultApiResponse
    @SwaggerAuthentication
    @PostMapping("/logout")
    @PreAuthorize("hasAnyRole('ROLE_MEMBER', 'ROLE_MANAGER', 'ROLE_ADMIN')")
    public ResponseEntity<ResponseMessageDTO> logoutProc(HttpServletRequest request,
                                                         HttpServletResponse response,
                                                         Principal principal) {

        try{
            LogoutDTO dto = LogoutDTO.builder()
                    .authorizationToken(request.getHeader(authorizationHeader))
                    .inoValue(WebUtils.getCookie(request, inoHeader).getValue())
                    .userId(principal.getName())
                    .build();

            String responseMessage = userWriteUseCase.logoutProc(dto, response);

            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ResponseMessageDTO(responseMessage));
        }catch (Exception e) {
            log.info("logout createDTO Exception");
            e.printStackTrace();
            throw new CustomAccessDeniedException(ErrorCode.ACCESS_DENIED, ErrorCode.ACCESS_DENIED.getMessage());
        }
    }

    /**
     *
     * @param joinDTO
     *
     * 회원 가입 요청
     */
    @Operation(summary = "회원가입 요청")
    @ApiResponse(responseCode = "200", description = "성공")
    @PostMapping("/join")
    public ResponseEntity<ResponseMessageDTO> joinProc(@RequestBody JoinDTO joinDTO) {

        String responseMessage = userWriteUseCase.joinProc(joinDTO);

        return ResponseEntity.status(HttpStatus.OK)
                .body(new ResponseMessageDTO(responseMessage));
    }


    /**
     *
     * @param request
     * @param response
     *
     * OAuth2 로그인 사용자의 토큰 발급 요청.
     * OAuth2 로그인의 경우 href 요청으로 처리되기 때문에 쿠키 반환은 가능하나 응답 헤더에 Authorization을 담을 수 없다.
     * 그렇기 때문에 OAuth2 로그인 사용자에 대해서는 임시 토큰을 응답 쿠키에 담아 먼저 발급한 뒤
     * 특정 컴포넌트로 Redirect 되도록 처리했고 그 컴포넌트에서는 임시 토큰을 통해 정상적인 토큰의 발급을 요청한다.
     * 해당 요청이 여기로 오는 것.
     */
    @Operation(summary = "oAuth 사용자의 토큰 발급 요청",
            description = "Swagger 테스트 불가. oAuth2 로그인 사용자는 임시 토큰이 발급되기 때문에 임시 토큰 응답 이후 바로 해당 요청을 보내 정식 토큰을 발급."
    )
    @DefaultApiResponse
    @GetMapping("/oAuth/token")
    public ResponseEntity<ResponseMessageDTO> oAuthIssueToken(HttpServletRequest request, HttpServletResponse response) {
        String responseMessage = userWriteUseCase.oAuthUserIssueToken(request, response);

        return ResponseEntity.status(HttpStatus.OK)
                .body(new ResponseMessageDTO(responseMessage));
    }

    /**
     *
     * @param userId
     *
     * 회원 가입 중 아이디 중복 체크
     */
    @Operation(summary = "회원가입 과정 중 아이디 중복 체크 요청")
    @ApiResponse(responseCode = "200", description = "사용 가능한 경우 No Duplicated, 중복인 경우 Duplicated 반환")
    @Parameter(name = "userId",
            example = "tester1",
            required = true,
            in = ParameterIn.QUERY
    )
    @GetMapping("/check-id")
    public ResponseEntity<ResponseMessageDTO> checkJoinId(@RequestParam("userId") String userId) {

        String responseMessage = userReadUseCase.checkJoinUserId(userId);

        return ResponseEntity.status(HttpStatus.OK)
                .body(new ResponseMessageDTO(responseMessage));
    }

    /**
     *
     * @param nickname
     * @param principal
     *
     * 닉네임 중복 체크
     * 회원 가입 시 또는 회원 정보 수정 시.
     */
    @Operation(summary = "닉네임 중복 체크 요청")
    @ApiResponse(responseCode = "200", description = "사용 가능한 경우 No Duplicated, 중복인 경우 Duplicated 반환")
    @Parameter(name = "nickname",
            example = "테스터1",
            required = true,
            in = ParameterIn.QUERY
    )
    @GetMapping("/check-nickname")
    public ResponseEntity<ResponseMessageDTO> checkNickname(@RequestParam("nickname") String nickname, Principal principal) {

        String responseMessage = userReadUseCase.checkNickname(nickname, principal);

        return ResponseEntity.status(HttpStatus.OK)
                .body(new ResponseMessageDTO(responseMessage));
    }

    /**
     *
     * @param authentication
     *
     * 클라이언트에서 로그인 상태 체크 요청
     * 새로고침에 대한 Redux의 처리를 위함.
     */
    @Operation(hidden = true)
    @GetMapping("/status")
    @PreAuthorize("hasAnyRole('ROLE_MEMBER', 'ROLE_MANAGER', 'ROLE_ADMIN')")
    public ResponseEntity<UserStatusResponseDTO> checkLoginStatus(Authentication authentication) {

        return ResponseEntity.status(HttpStatus.OK)
                .body(new UserStatusResponseDTO(authentication));
    }

    /**
     *
     * @param userName
     * @param userPhone
     * @param userEmail
     *
     * 아이디 찾기
     */
    @Operation(summary = "아이디 찾기 요청",
            description = "사용자 이름은 필수, 연락처와 이메일 둘 중 하나를 선택해서 조회 가능"
    )
    @ApiResponse(responseCode = "200", description = "성공. 일치하는 데이터가 있다면 OK, 없다면 not found 반환")
    @Parameters({
            @Parameter(name = "userName",
                    description = "사용자 이름",
                    example = "코코",
                    required = true,
                    in = ParameterIn.QUERY
            ),
            @Parameter(name = "userPhone",
                    description = "사용자 연락처",
                    example = "01012345678",
                    in = ParameterIn.QUERY
            ),
            @Parameter(name = "userEmail",
                    description = "사용자 이메일",
                    example = "tester1@tester1.com",
                    in = ParameterIn.QUERY
            )
    })
    @GetMapping("/search-id")
    public ResponseEntity<UserSearchIdResponseDTO> searchId(@RequestParam(name = "userName") String userName,
                                                            @RequestParam(name = "userPhone", required = false) String userPhone,
                                                            @RequestParam(name = "userEmail", required = false) String userEmail) {

        UserSearchDTO searchDTO = new UserSearchDTO(userName, userPhone, userEmail);

        UserSearchIdResponseDTO responseDTO = userReadUseCase.searchId(searchDTO);

        return ResponseEntity.status(HttpStatus.OK)
                .body(responseDTO);
    }

    /**
     *
     * @param userId
     * @param userName
     * @param userEmail
     *
     * 비밀번호 찾기
     * 사용자가 전달한 정보를 통해 사용자 검증을 한 뒤
     * 데이터베이스에 저장된 이메일로 인증번호를 보낸다.
     * 정상적으로 처리되면 메세지를 담은 응답 전달.
     */
    @Operation(summary = "비밀번호 찾기 요청")
    @ApiResponse(responseCode = "200", description = "성공. 정상인 경우 OK, 일치하는 데이터가 없는 경우 not found, 오류 발생 시 FAIL 반환")
    @Parameters({
            @Parameter(name = "userId",
                    description = "사용자 아이디",
                    example = "coco",
                    required = true,
                    in = ParameterIn.QUERY
            ),
            @Parameter(name = "userName",
                    description = "사용자 이름",
                    example = "코코",
                    required = true,
                    in = ParameterIn.QUERY
            ),
            @Parameter(name = "userEmail",
                    description = "사용자 이메일",
                    example = "tester1@tester1.com",
                    required = true,
                    in = ParameterIn.QUERY
            )
    })
    @GetMapping("/search-pw")
    public ResponseEntity<ResponseMessageDTO> searchPw(@RequestParam(name = "id") String userId,
                                                       @RequestParam(name = "name") String userName,
                                                       @RequestParam(name = "email") String userEmail) {

        UserSearchPwDTO searchDTO = new UserSearchPwDTO(userId, userName, userEmail);

        String responseMessage = userWriteUseCase.searchPassword(searchDTO);

        return ResponseEntity.status(HttpStatus.OK)
                .body(new ResponseMessageDTO(responseMessage));
    }

    /**
     *
     * @param certificationDTO
     *
     * 인증번호 확인.
     * 사용자가 메일을 확인하고 해당 인증번호를 입력해 확인 요청.
     */
    @Operation(summary = "비밀번호 찾기 인증번호 확인 요청")
    @ApiResponse(responseCode = "200", description = "성공. 정상인 경우 OK, 일치하는 데이터가 없는 경우 FAIL, 오류 발생 시 ERROR 반환")
    @PostMapping("/certification")
    public ResponseEntity<ResponseMessageDTO> checkCertification(@RequestBody UserCertificationDTO certificationDTO) {

        String responseMessage = userWriteUseCase.checkCertificationNo(certificationDTO);

        return ResponseEntity.status(HttpStatus.OK)
                .body(new ResponseMessageDTO(responseMessage));
    }

    /**
     *
     * @param resetDTO
     *
     * 비밀번호 수정 요청
     */
    @Operation(summary = "인증번호 확인 이후 비밀번호 수정 요청")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공. 정상인 경우 OK, 인증번호 재확인 시 불일치하는 경우 FAIL 반환"),
            @ApiResponse(responseCode = "500", description = "일치하는 사용자 데이터가 없는 경우"
                    , content = @Content(schema = @Schema(implementation = ExceptionEntity.class))
            )
    })
    @PatchMapping("/reset-pw")
    public ResponseEntity<ResponseMessageDTO> resetPassword(@RequestBody UserResetPwDTO resetDTO) {

        String responseMessage = userWriteUseCase.resetPw(resetDTO);

        return ResponseEntity.status(HttpStatus.OK)
                .body(new ResponseMessageDTO(responseMessage));
    }
}
