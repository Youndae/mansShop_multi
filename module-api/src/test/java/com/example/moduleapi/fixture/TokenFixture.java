package com.example.moduleapi.fixture;


import com.example.moduleauthapi.service.JWTTokenProvider;
import com.example.modulecommon.model.entity.Member;
import com.example.modulecommon.model.enumuration.Role;
import com.example.moduleconfig.properties.CookieProperties;
import com.example.moduleconfig.properties.JwtSecretProperties;
import com.example.moduleconfig.properties.TokenProperties;
import com.example.moduleconfig.properties.TokenRedisProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.test.web.servlet.MvcResult;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class TokenFixture {

    @Autowired
    private JWTTokenProvider tokenProvider;

    @Autowired
    private JwtSecretProperties jwtSecretProperties;

    @Autowired
    private TokenProperties tokenProperties;

    @Autowired
    private TokenRedisProperties tokenRedisProperties;

    @Autowired
    private CookieProperties cookieProperties;

    private static final String ACCESS_KEY_NAME = "accessKey";

    private static final String REFRESH_KEY_NAME = "refreshKey";

    private Role getUserRole(Member member) {
        return Role.fromKey(Role.getHighestRole(member.getAuths()));
    }

    public String createAccessToken(Member member) {
        Role userRole = getUserRole(member);
        String token = tokenProvider.createToken(member.getUserId(), userRole, jwtSecretProperties.getAccess(), tokenProperties.getAccess().getExpiration());

        return tokenProperties.getPrefix() + token;
    }

    public String createExpirationToken(Member member) {
        Role userRole = getUserRole(member);
        String token = tokenProvider.createToken(member.getUserId(), userRole, jwtSecretProperties.getAccess(), 1);

        return tokenProperties.getPrefix() + token;
    }

    public Map<String, String> createAndSaveAllToken(Member member) {
        Role userRole = getUserRole(member);
        Map<String, String> tokenMap = new HashMap<>();
        String ino = tokenProvider.createIno();
        String accessToken = tokenProvider.createToken(member.getUserId(), userRole, jwtSecretProperties.getAccess(), tokenProperties.getAccess().getExpiration());
        String refreshToken = tokenProvider.createToken(member.getUserId(), userRole, jwtSecretProperties.getRefresh(), tokenProperties.getRefresh().getExpiration());

        Map<String, String> keyMap = getRedisKeyMap(member, ino);

        String accessKey = keyMap.get(ACCESS_KEY_NAME);
        String refreshKey = keyMap.get(REFRESH_KEY_NAME);

        tokenProvider.saveTokenToRedis(refreshKey, refreshToken, Duration.ofDays(tokenRedisProperties.getRefresh().getExpiration()));

        tokenMap.put(cookieProperties.getIno().getHeader(), ino);
        tokenMap.put(tokenProperties.getAccess().getHeader(), tokenProperties.getPrefix() + accessToken);
        tokenMap.put(tokenProperties.getRefresh().getHeader(), tokenProperties.getPrefix() + refreshToken);
        tokenMap.put(ACCESS_KEY_NAME, accessKey);
        tokenMap.put(REFRESH_KEY_NAME, refreshKey);

        return tokenMap;
    }

    public Map<String, String> getRedisKeyMap(Member member, String ino) {
        Map<String, String> keyMap = new HashMap<>();
        String accessKey = tokenProvider.setRedisKey(tokenRedisProperties.getAccess().getPrefix(), ino, member.getUserId());
        String refreshKey = tokenProvider.setRedisKey(tokenRedisProperties.getRefresh().getPrefix(), ino, member.getUserId());

        keyMap.put(ACCESS_KEY_NAME, accessKey);
        keyMap.put(REFRESH_KEY_NAME, refreshKey);

        return keyMap;
    }

    public String createTemporaryToken(Member oAuthMember) {
        Role userRole = getUserRole(oAuthMember);
        return tokenProvider.createToken(oAuthMember.getUserId(), userRole, jwtSecretProperties.getTemporary(), tokenProperties.getTemporary().getExpiration());
    }

    public String createAndRedisSaveTemporaryToken(Member oAuthMember) {
        String token = createTemporaryToken(oAuthMember);
        tokenProvider.saveTokenToRedis(oAuthMember.getUserId(), token, Duration.ofMinutes(tokenRedisProperties.getTemporary().getExpiration()));

        return token;
    }

    public String createAndRedisSaveExpirationTemporaryToken(Member oAuthMember) {
        Role userRole = getUserRole(oAuthMember);
        String token = tokenProvider.createToken(oAuthMember.getUserId(), userRole, jwtSecretProperties.getTemporary(), 1);
        tokenProvider.saveTokenToRedis(oAuthMember.getUserId(), token, Duration.ofMinutes(tokenRedisProperties.getTemporary().getExpiration()));

        return token;
    }

    public String getResponseAuthorization(MvcResult result) {
        return result.getResponse().getHeader(tokenProperties.getAccess().getHeader()).substring(6);
    }

    public Map<String, String> getCookieMap(MvcResult result) {

        return result.getResponse()
                .getHeaders("Set-Cookie")
                .stream()
                .map(header -> header.split(";", 2)[0])
                .map(kv -> kv.split("=", 2))
                .filter(arr -> arr.length == 2)
                .collect(Collectors.toMap(arr -> arr[0], arr -> arr[1]));
    }
}
