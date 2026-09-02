package com.example.moduleauthapi.service;

import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.example.moduleauthapi.model.dto.TokenIssueResponse;
import com.example.moduleauthapi.model.dto.TokenReissueInfo;
import com.example.moduleauthapi.model.dto.TokenVerifyResult;
import com.example.modulecommon.customException.CustomBadCredentialsException;
import com.example.modulecommon.customException.CustomTokenStealingException;
import com.example.modulecommon.model.enumuration.ErrorCode;
import com.example.modulecommon.model.enumuration.Result;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class JWTTokenService {

    private final JWTTokenProvider jwtTokenProvider;

    /**
     *
     * @param response
     *
     * 토큰 검증 오류 또는 탈취로 인한 토큰 쿠키의 삭제 처리.
     * Redis 데이터는 삭제하지 않고 응답 쿠키로 0의 만료시간을 갖는 쿠키를 전달해
     * 클라이언트에서 쿠키가 삭제되도록 한다.
     * 탈취 응답 코드 반환.
     *
     * 대체로 모든 쿠키가 전달되지 않아 탈취라고 판단하는 경우.
     */
    public void deleteCookieAndThrowException(Result result, HttpServletResponse response) {
        jwtTokenProvider.deleteCookie(response);
        switch (result) {
            case WRONG_TOKEN -> setExceptionResponse(ErrorCode.TOKEN_INVALID, response);
            case TOKEN_STEALING -> setExceptionResponse(ErrorCode.TOKEN_STEALING, response);
        }

    }

    /**
     *
     * @param tokenClaim
     * @param ino
     * @param response
     *
     * 탈취로 인한 토큰 쿠키와 Redis 데이터 삭제 처리.
     * 토큰에서 Claim을 통해 사용자 아이디를 알아낼 수 있으며 ino가 존재하는 경우 처리.
     * Redis에서 해당 접근에 대한 토큰을 모두 삭제하고 쿠키 만료시간도 0으로 반환해 클라이언트에서 쿠키가 삭제되도록 처리.
     * 응답 코드로 탈취를 반환.
     */
    public void deleteTokenAndCookieAndThrowException(String tokenClaim, String ino, HttpServletResponse response) {
        jwtTokenProvider.deleteRedisDataAndCookie(tokenClaim, ino, response);
        setExceptionResponse(ErrorCode.TOKEN_STEALING, response);
    }

    /**
     *
     * @param response
     *
     * 토큰 만료 응답 설정
     */
    public void tokenExpirationResponse(HttpServletResponse response) {
        setExceptionResponse(ErrorCode.TOKEN_EXPIRED, response);
    }


    public void setExceptionResponse(ErrorCode errorCode, HttpServletResponse response) {
        response.setStatus(errorCode.getHttpStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("utf-8");

        Map<String, Object> body = Map.of(
                "code", errorCode.getHttpStatus().value(),
                "message", errorCode.getMessage()
        );

        try {
            new ObjectMapper().writeValue(response.getWriter(), body);
        }catch(IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     *
     * @param tokenDTO
     * @param response
     *
     * 토큰 재발급 요청 처리.
     * ino가 존재하지 않는다면 탈취로 판단.
     */
    public TokenIssueResponse reIssueToken(TokenReissueInfo tokenDTO, HttpServletResponse response) {
        /**
         * ino가 존재하지 않는다면 무조건 탈취로 판단.
         * controller 에서 검증하고 예외를 던지는 것도 괜찮다고 생각했지만
         * 탈취자의 쿠키를 강제로 제거하기 위해 Service에서 쿠키를 전체 초기화하고 예외를 던지도록 설계.
         * ino가 있음에도 토큰이 잘못된 경우 다른 처리 없이 예외 던짐
         * Redis 데이터를 제거하기에는 해당 ino가 정상이라는 보장도 없을 뿐더러
         * 애초에 ino에는 정보가 들어가지 않는 난수 조합이기 때문에 문제가 없다고 판단.
        **/
        if(tokenDTO.inoValue() == null) {
            jwtTokenProvider.deleteCookie(response);
            log.warn("JWTTokenService.reIssueToken :: ino Cookie is null");
            throw new CustomTokenStealingException(ErrorCode.TOKEN_STEALING, ErrorCode.TOKEN_STEALING.getMessage());
        }else {
            try {
                TokenVerifyResult result = jwtTokenProvider.verifyRefreshToken(tokenDTO.refreshTokenValue(), tokenDTO.inoValue());

                return jwtTokenProvider.issueTokens(result.userId(), result.role(), tokenDTO.inoValue(), response);
            } catch (TokenExpiredException | JWTDecodeException e) {
                throw new CustomBadCredentialsException(ErrorCode.UNAUTHORIZED, ErrorCode.UNAUTHORIZED.getMessage());
            } catch (CustomTokenStealingException e) {
                log.warn("JWTTokenService.reIssueToken :: token stealing");
                throw new CustomTokenStealingException(ErrorCode.TOKEN_STEALING, ErrorCode.TOKEN_STEALING.getMessage());
            }
        }
    }
}
