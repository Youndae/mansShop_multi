package com.example.moduleauth.service;

import com.example.moduleauth.config.jwt.JWTTokenProvider;
import com.example.moduleauth.config.jwt.TokenDTO;
import com.example.modulecommon.model.enumuration.ErrorCode;
import com.example.modulecommon.model.enumuration.Result;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

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
    public void deleteCookieAndThrowException(HttpServletResponse response) {
        jwtTokenProvider.deleteCookie(response);
        tokenStealingExceptionResponse(response);
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
        tokenStealingExceptionResponse(response);
    }

    /**
     *
     * @param response
     *
     * 토큰 탈취 응답 설정
     */
    public void tokenStealingExceptionResponse(HttpServletResponse response) {
        response.setStatus(ErrorCode.TOKEN_STEALING.getHttpStatus());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("utf-8");
    }

    /**
     *
     * @param response
     *
     * 토큰 만료 응답 설정
     */
    public void tokenExpirationResponse(HttpServletResponse response) {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("utf-8");
    }

    /**
     *
     * @param tokenDTO
     * @param response
     *
     * 토큰 재발급 요청 처리.
     * AccessToken이 만료된 경우이기 때문에 AccessToken을 decode해서 Claim에 저장된 아이디를 알아내고
     * RefreshToken을 검증한 뒤 동일하게 Claim에 저장된 아이디를 반환받아 두 Claim이 일치하는 경우에 재발급을 처리.
     * ino가 존재하지 않는다면 탈취로 판단.
     */
    public String reIssueToken(TokenDTO tokenDTO, HttpServletResponse response) {
        String decodeAccessToken = jwtTokenProvider.decodeToken(tokenDTO.accessTokenValue());

        if(decodeAccessToken.equals(Result.WRONG_TOKEN.getResultKey())){
            String decodeRefreshToken = jwtTokenProvider.decodeToken(tokenDTO.refreshTokenValue());

            if(decodeRefreshToken.equals(Result.WRONG_TOKEN.getResultKey())){
                jwtTokenProvider.deleteCookie(response);

                return Result.FAIL.getResultKey();
            }
        }else {
            String claimByRefreshToken = jwtTokenProvider.verifyRefreshToken(
                    tokenDTO.refreshTokenValue()
                    , tokenDTO.inoValue()
                    , decodeAccessToken
            );

            if(claimByRefreshToken.equals(Result.TOKEN_STEALING.getResultKey())
                    || claimByRefreshToken.equals(Result.WRONG_TOKEN.getResultKey())) {
                deleteCookieAndThrowException(response);
                return Result.FAIL.getResultKey();
            }else
                jwtTokenProvider.issueTokens(decodeAccessToken, tokenDTO.inoValue(), response);
        }

        return Result.OK.getResultKey();
    }
}
