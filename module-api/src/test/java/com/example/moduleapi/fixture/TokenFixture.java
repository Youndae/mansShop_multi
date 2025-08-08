package com.example.moduleapi.fixture;


import com.example.moduleauthapi.service.JWTTokenProvider;
import com.example.modulecommon.model.entity.Member;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("#{jwt['token.access.secret']}")
    private String accessSecret;

    @Value("#{jwt['token.access.expiration']}")
    private Long accessExpiration;

    @Value("#{jwt['redis.accessExpiration']}")
    private Long redisAtExpiration;

    @Value("#{jwt['token.access.header']}")
    private String accessHeader;

    @Value("#{jwt['token.refresh.secret']}")
    private String refreshSecret;

    @Value("#{jwt['token.refresh.expiration']}")
    private Long refreshExpiration;

    @Value("#{jwt['redis.refreshExpiration']}")
    private Long redisRtExpiration;

    @Value("#{jwt['token.refresh.header']}")
    private String refreshHeader;

    @Value("#{jwt['cookie.ino.header']}")
    private String inoHeader;

    @Value("#{jwt['redis.accessPrefix']}")
    private String redisAccessPrefix;

    @Value("#{jwt['redis.refreshPrefix']}")
    private String redisRefreshPrefix;

    @Value("#{jwt['token.all.prefix']}")
    private String tokenPrefix;

    @Value("#{jwt['token.temporary.expiration']}")
    private Long temporaryExpiration;

    @Value("#{jwt['token.temporary.secret']}")
    private String temporarySecret;

    @Value("#{jwt['token.temporary.redis.expirationMinute']}")
    private Long temporaryRedisExpiration;

    private static final String ACCESS_KEY_NAME = "accessKey";

    private static final String REFRESH_KEY_NAME = "refreshKey";

    public String createAccessToken(Member member) {
        String token = tokenProvider.createToken(member.getUserId(), accessSecret, accessExpiration);

        return tokenPrefix + token;
    }

    public String createExpirationToken(Member member) {
        String token = tokenProvider.createToken(member.getUserId(), accessSecret, 1);

        return tokenPrefix + token;
    }

    public Map<String, String> createAndSaveAllToken(Member member) {
        Map<String, String> tokenMap = new HashMap<>();
        String ino = tokenProvider.createIno();
        String accessToken = tokenProvider.createToken(member.getUserId(), accessSecret, accessExpiration);
        String refreshToken = tokenProvider.createToken(member.getUserId(), refreshSecret, refreshExpiration);

        Map<String, String> keyMap = getRedisKeyMap(member, ino);

        String accessKey = keyMap.get(ACCESS_KEY_NAME);
        String refreshKey = keyMap.get(REFRESH_KEY_NAME);

        tokenProvider.saveTokenToRedis(accessKey, accessToken, Duration.ofHours(redisAtExpiration));
        tokenProvider.saveTokenToRedis(refreshKey, refreshToken, Duration.ofDays(redisRtExpiration));

        tokenMap.put(inoHeader, ino);
        tokenMap.put(accessHeader, tokenPrefix + accessToken);
        tokenMap.put(refreshHeader, tokenPrefix + refreshToken);
        tokenMap.put(ACCESS_KEY_NAME, accessKey);
        tokenMap.put(REFRESH_KEY_NAME, refreshKey);

        return tokenMap;
    }

    public Map<String, String> getRedisKeyMap(Member member, String ino) {
        Map<String, String> keyMap = new HashMap<>();
        String accessKey = tokenProvider.setRedisKey(redisAccessPrefix, ino, member.getUserId());
        String refreshKey = tokenProvider.setRedisKey(redisRefreshPrefix, ino, member.getUserId());

        keyMap.put(ACCESS_KEY_NAME, accessKey);
        keyMap.put(REFRESH_KEY_NAME, refreshKey);

        return keyMap;
    }

    public String createTemporaryToken(Member oAuthMember) {
        return tokenProvider.createToken(oAuthMember.getUserId(), temporarySecret, temporaryExpiration);
    }

    public String createAndRedisSaveTemporaryToken(Member oAuthMember) {
        String token = createTemporaryToken(oAuthMember);
        tokenProvider.saveTokenToRedis(oAuthMember.getUserId(), token, Duration.ofMinutes(temporaryRedisExpiration));

        return token;
    }

    public String createAndRedisSaveExpirationTemporaryToken(Member oAuthMember) {
        String token = tokenProvider.createToken(oAuthMember.getUserId(), temporarySecret, 1);
        tokenProvider.saveTokenToRedis(oAuthMember.getUserId(), token, Duration.ofMinutes(temporaryRedisExpiration));

        return token;
    }

    public String getResponseAuthorization(MvcResult result) {
        return result.getResponse().getHeader(accessHeader).substring(6);
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
