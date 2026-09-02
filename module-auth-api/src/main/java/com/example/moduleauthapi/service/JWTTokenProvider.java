package com.example.moduleauthapi.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.moduleauthapi.model.dto.TokenIssueResponse;
import com.example.moduleauthapi.model.dto.TokenVerifyResult;
import com.example.modulecommon.customException.CustomBadCredentialsException;
import com.example.modulecommon.customException.CustomTokenStealingException;
import com.example.modulecommon.model.enumuration.ErrorCode;
import com.example.modulecommon.model.enumuration.Result;
import com.example.modulecommon.model.enumuration.Role;
import com.example.moduleconfig.properties.CookieProperties;
import com.example.moduleconfig.properties.JwtSecretProperties;
import com.example.moduleconfig.properties.TokenProperties;
import com.example.moduleconfig.properties.TokenRedisProperties;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Date;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class JWTTokenProvider {

    private final TokenProperties tokenProperties;

    private final JwtSecretProperties jwtSecretProperties;

    private final CookieProperties cookieProperties;

    private final TokenRedisProperties tokenRedisProperties;

    private final StringRedisTemplate redisTemplate;

    /**
     *
     * @param userId
     * @param secretKey
     * @param expiration
     * @return AccessToken || RefreshToken
     *
     * 토큰 생성
     */
    public String createToken(String userId, Role role, String secretKey, long expiration) {
        return JWT.create()
                .withIssuer("Man's shop")
                .withSubject(userId)
                .withExpiresAt(new Date(System.currentTimeMillis() + expiration))
                .withClaim("role", role.getRole())
                .sign(Algorithm.HMAC512(secretKey));
    }

    public String createIno() {

        return UUID.randomUUID()
                .toString()
                .replace("-", "");
    }

    /**
     *
     * @param token
     * @return
     *
     * 토큰 prefix 확인
     */
    public boolean checkTokenPrefix(String token) {

        return token.startsWith(tokenProperties.getPrefix());
    }

    /**
     *
     * @param accessTokenValue
     * @return TokenVerifyResult( userId, Role )
     *
     * AccessToken 검증
     *
     */
    public TokenVerifyResult verifyAccessToken(String accessTokenValue) {
        return verifyToken(accessTokenValue, jwtSecretProperties.getAccess());
    }

    /**
     *
     * @param refreshTokenValue
     * @param inoValue
     * @return userId || WRONG_TOKEN || TOKEN_EXPIRATION || TOKEN_STEALING
     *
     * RefreshToken을 검증하는 경우는 AccessToken의 만료로 인한 재발급 요청시에만 수행.
     * AccessToken과 RefreshToken을 같이 보내야만 재발급을 수행할 수 있도록 처리할 것이기 때문에
     * 해당 메소드를 호출하기 이전 AccessToken을 decode해서 claim을 꺼낸 뒤 보내야 함.
     *
     * 두 토큰은 같은 Claim을 갖기 때문에 두 Claim이 다른 경우 탈취로 판단.
     * 두 토큰의 Claim에 해당하는 redis 데이터를 삭제 후 탈취 응답.
     *
     * 일치한다면 Redis 데이터와 비교.
     * Redis 데이터와 일치한다면 아이디 반환
     * 일치하지 않는다면 TOKEN_STEALING 반환
     */
    public TokenVerifyResult verifyRefreshToken(String refreshTokenValue, String inoValue) {

        TokenVerifyResult result = verifyToken(refreshTokenValue, jwtSecretProperties.getRefresh());

        String redisKey = setRedisKey(tokenRedisProperties.getRefresh().getPrefix(), inoValue, result.userId());
        String redisValue = getTokenValueToRedis(redisKey);

        if(refreshTokenValue.equals(redisValue))
            return result;
        else {
            deleteRefreshTokenByRedis(result.userId(), inoValue);
            log.warn("token stealing!!");
            throw new CustomTokenStealingException(ErrorCode.TOKEN_STEALING, ErrorCode.TOKEN_STEALING.getMessage());
        }
    }

    /**
     *
     * @param tokenValue
     * @param secret
     * @return userId || WRONG_TOKEN || TOKEN_EXPIRATION
     *
     * 토큰 검증.
     * null에 대한 처리를 확실하게 하기 위해 null인 경우 WRONG_TOKEN 반환.
     *
     * 만료와 DocodeException은 catch부분에서 잡아 각 오류 응답을 반환.
     */
    public TokenVerifyResult verifyToken(String tokenValue, String secret) {
        DecodedJWT decodedToken = JWT.require(Algorithm.HMAC512(secret))
                .build()
                .verify(tokenValue);

        String userId = decodedToken.getSubject();

        if(userId == null || userId.isBlank())
            throw new JWTDecodeException("Subject is missing");

        String role = decodedToken.getClaim("role").asString();

        return new TokenVerifyResult(userId, role);
    }

    /**
     *
     * @param tokenValue
     * @return userId || WRONG_TOKEN
     *
     * 재발급 요청 시 AccessToken decode를 위한 메소드
     * 잘못된 토큰이 전달되어 JWTDecodeException이 발생되는 경우 WRONG_TOKEN을 반환.
     * 정상이라면 claim 반환
     *
     */
    public String decodeToken(String tokenValue) {

        try{
            return JWT.decode(tokenValue)
                    .getClaim("userId")
                    .asString();
        }catch (JWTDecodeException e) {
            return Result.WRONG_TOKEN.getResultKey();
        }

    }

    /**
     *
     * @param key
     * @param value
     *
     * Redis에 토큰 데이터 저장
     */
    public void saveTokenToRedis(String key, String value, Duration redisExpiration) {
        ValueOperations<String, String> stringValueOperations = redisTemplate.opsForValue();

        stringValueOperations.set(key, value, redisExpiration);
    }

    /**
     *
     * @param tokenKey
     * @return null || tokenValue
     *
     * Redis에 저장되어있는 Token 데이터 조회.
     * -2로 존재하지 않는 데이터인 경우 null을 반환.
     *
     * 데이터가 존재하는 경우 value를 반환
     */
    public String getTokenValueToRedis(String tokenKey) {
        long keyExpire = redisTemplate.getExpire(tokenKey);

        if(keyExpire == -2) {
            return null;
        }

        return redisTemplate.opsForValue().get(tokenKey);
    }

    /**
     *
     * @param userId
     * @param inoValue
     *
     * 탈취 또는 로그아웃 시 Redis에서 모든 토큰 데이터 삭제
     * AccessToken과 RefreshToken 모두 삭제.
     *
     */
    public void deleteRefreshTokenByRedis(String userId, String inoValue) {
        String refreshKey = setRedisKey(tokenRedisProperties.getRefresh().getPrefix(), inoValue, userId);

        redisTemplate.delete(refreshKey);
    }

    /**
     *
     * @param ino
     * @param userId
     * @return redisKey
     *
     * RedisKey 구조를 생성해 반환.
     * 구조 변환 시 누락되는 실수를 대비하기 위해 메소드로 처리
     */
    public String setRedisKey(String ino, String userId){
        return userId + ":" + ino;
    }

    /**
     *
     * @param name
     * @param value
     * @param expires
     * @return ResponseCookie
     *
     * ResponseCookie 생성 후 반환.
     * RefreshToken cookie와 ino cookie, cartCookie 생성에 사용
     */
    public String createCookie(String name, String value, Duration expires) {

        return ResponseCookie
                .from(name, value)
                .path("/")
                .maxAge(expires)
                .secure(true)
                .httpOnly(true)
                .sameSite("Strict")
                .build()
                .toString();
    }


    /**
     *
     * @param tokenHeader
     * @param tokenValue
     * @param tokenCookieAge
     * @param response
     *
     * Token Cookie 생성
     */
    public void setTokenCookie(String tokenHeader, String tokenValue, Duration tokenCookieAge, HttpServletResponse response) {

        response.addHeader("Set-Cookie",
                createCookie(
                        tokenHeader,
                        tokenValue,
                        tokenCookieAge
                ));
    }

    public void setAccessTokenToResponseHeader(String accessToken, HttpServletResponse response){

        response.addHeader(tokenProperties.getAccess().getHeader(), accessToken);
    }

    /**
     *
     * @param response
     *
     * 탈취 또는 로그아웃 시 쿠키 제거.
     * 추후 기능 추가하면서 추가적으로 처리해야 하는 쿠키가 발생하는 경우를 대비해 배열을 생성하고
     * 배열 값들에 대한 쿠키를 삭제하는 방법으로 처리.
     *
     */
    public void deleteCookie(HttpServletResponse response) {
        String[] cookieNameArr = {
                tokenProperties.getRefresh().getHeader(),
                cookieProperties.getIno().getHeader()
        };

        for(String name : cookieNameArr){
            Cookie cookie = new Cookie(name, null);
            cookie.setMaxAge(0);
            cookie.setPath("/");
            response.addCookie(cookie);
        }
    }

    /**
     *
     * @param userId
     * @param ino
     * @param response
     *
     * 탈취 또는 로그아웃 시 Redis 데이터와 Cookie 삭제를 처리해주는 메소드
     * 개별적으로 처리해야하는 경우를 제외하고는 이 메소드 호출로 처리.
     */
    public void deleteRedisDataAndCookie(String userId, String ino, HttpServletResponse response){
        deleteRefreshTokenByRedis(userId, ino);
        deleteCookie(response);
    }

    /**
     *
     * @param userId
     * @param response
     *
     * issue ino, AccessToken, RefreshToken
     */
    public TokenIssueResponse issueAllTokens(String userId, Role role, HttpServletResponse response) {
        String ino = createIno();
        TokenIssueResponse tokenIssueResponse = issueTokens(userId, role, ino, response);

        setTokenCookie(cookieProperties.getIno().getHeader(), ino, Duration.ofDays(cookieProperties.getIno().getAge()), response);

        return tokenIssueResponse;
    }

    /**
     *
     * @param userId
     * @param ino
     * @param response
     *
     * issue AccessToken, RefreshToken
     */
    public TokenIssueResponse issueTokens(String userId, Role role, String ino, HttpServletResponse response) {
        String accessToken = createToken(userId, role, jwtSecretProperties.getAccess(), tokenProperties.getAccess().getExpiration());
        String refreshToken = createToken(userId, role, jwtSecretProperties.getRefresh(), tokenProperties.getRefresh().getExpiration());
        String refreshKey = setRedisKey(tokenRedisProperties.getRefresh().getPrefix(), ino, userId);
        Duration tokenExpiration = Duration.ofDays(tokenRedisProperties.getRefresh().getExpiration());
        saveTokenToRedis(refreshKey, refreshToken, tokenExpiration);

        accessToken = tokenProperties.getPrefix() + accessToken;
        refreshToken = tokenProperties.getPrefix() + refreshToken;

        setTokenCookie(tokenProperties.getRefresh().getHeader(), refreshToken, Duration.ofDays(tokenRedisProperties.getRefresh().getExpiration()), response);

        return new TokenIssueResponse(accessToken, userId, role.getRole());

    }

    /**
     *
     * @param userId
     * @param response
     *
     * 임시 토큰 발행 및 Redis에 저장, 응답 쿠키에 저장 처리
     * OAuth2 로그인 사용자에 대한 토큰 발급 대응.
     * response.sendRedirect로 처리되기 때문에 AccessToken 저장 처리를 Client에서 수행할 수 없기 때문에 임시 토큰 발행.
     */
    public void createTemporaryToken(String userId, HttpServletResponse response) {
        String temporaryToken = createToken(userId, Role.MEMBER, jwtSecretProperties.getTemporary(), tokenProperties.getTemporary().getExpiration());

        saveTokenToRedis(userId, temporaryToken, Duration.ofMinutes(tokenRedisProperties.getTemporary().getExpiration()));
        setTokenCookie(tokenProperties.getTemporary().getHeader(), temporaryToken, Duration.ofMinutes(tokenRedisProperties.getTemporary().getExpiration()), response);
    }

    /**
     *
     * @param temporaryTokenValue
     * @return
     *
     * 임시토큰 검증
     * 임시 토큰 검증이 정상적으로 수행되어 Claim을 반환받았으나 Redis 데이터와 일치하지 않는다면 위장 토큰으로 의심할 수 있다.
     * 위장 토큰이 생성되었고 해당 토큰이 정상적으로 Claim을 반환한다는 것은 SecretKey 유출로 볼 수 있다고 생각해 로그를 남겨 확인할 수 있도록 처리.
     *
     */
    public TokenVerifyResult verifyTemporaryToken(String temporaryTokenValue) {
        try {
            TokenVerifyResult result = verifyToken(temporaryTokenValue, jwtSecretProperties.getTemporary());

            String redisValue = getTokenValueToRedis(result.userId());

            if(temporaryTokenValue.equals(redisValue))
                return result;
            else{
                log.warn("Temporary token claim is not the same as Redis data");
                throw new CustomTokenStealingException(ErrorCode.TOKEN_STEALING, ErrorCode.TOKEN_STEALING.getMessage());
            }
        } catch(TokenExpiredException | JWTDecodeException ex) {
            throw new CustomBadCredentialsException(ErrorCode.UNAUTHORIZED, ErrorCode.UNAUTHORIZED.getMessage());
        } catch(CustomTokenStealingException e) {
          throw e;
        } catch(Exception e) {
            log.warn("JWTTokenProvider.verifyTemporaryToken :: verifyToken Exception. error: {}", e);
            throw new IllegalArgumentException();
        }
    }

    /**
     *
     * @param userId
     * @param response
     *
     * OAuth 사용자의 임시 토큰 발급 이후 토큰 발급 요청으로 발급 처리 된 이후
     * 임시 토큰의 쿠키 및 Redis 데이터 삭제
     */
    public void deleteTemporaryTokenAndCookie(String userId, HttpServletResponse response) {
        redisTemplate.delete(userId);

        Cookie cookie = new Cookie(tokenProperties.getTemporary().getHeader(), null);
        cookie.setMaxAge(0);
        cookie.setPath("/");
        response.addCookie(cookie);
    }
}
