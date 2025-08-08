package com.example.modulecache.service;

import com.example.modulecache.model.cache.CacheRequest;
import com.example.modulecommon.model.dto.page.AdminQnAPageDTO;
import com.example.modulecommon.model.enumuration.RedisCaching;
import com.example.moduleconfig.properties.CacheProperties;
import com.example.modulemypage.service.MemberQnADataService;
import com.example.moduleorder.model.dto.admin.page.AdminOrderPageDTO;
import com.example.moduleorder.service.OrderDataService;
import com.example.moduleproduct.model.dto.page.AdminReviewPageDTO;
import com.example.moduleproduct.service.productQnA.ProductQnADataService;
import com.example.moduleproduct.service.productReview.ProductReviewDataService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
@Slf4j
public class FullCountScanCachingService {


    private final OrderDataService orderDataService;

    private final ProductQnADataService productQnADataService;

    private final ProductReviewDataService productReviewDataService;

    private final MemberQnADataService memberQnADataService;

    private final CacheProperties cacheProperties;

    private final RedisTemplate<String, Long> redisTemplate;

    private Map<String, Function<CacheRequest<?>, Long>> KEY_ACTION_MAP;

    @PostConstruct
    void init() {
        KEY_ACTION_MAP = Map.of(
                RedisCaching.ADMIN_PRODUCT_QNA_COUNT.getKey(),
                req -> productQnADataService.findAllAdminProductQnAListCount((AdminQnAPageDTO) req.getPageDTO()),
                RedisCaching.ADMIN_REVIEW_COUNT.getKey(),
                req -> productReviewDataService.countByAdminReviewList((AdminReviewPageDTO) req.getPageDTO(), req.getListType()),
                RedisCaching.ADMIN_MEMBER_QNA_COUNT.getKey(),
                req -> memberQnADataService.findAllByAdminMemberQnACount((AdminQnAPageDTO) req.getPageDTO()),
                RedisCaching.ADMIN_ORDER_COUNT.getKey(),
                req -> orderDataService.findAllAdminOrderListCount((AdminOrderPageDTO) req.getPageDTO())
        );
    }

    /**
     *
     * @param cachingKey
     * @return
     *
     * Double-check 전략.
     * 많이 사용되는 캐싱이라면 @Scheduled 를 통한 주기적인 동기화를 시행하는 것이 옳겠으나
     * 관리자 기능인만큼 주기적인 갱신은 필요하지 않을 것이라고 생각해 Doudle-check로 처리.
     */
    public <T> long getFullScanCountCache(RedisCaching cachingKey, CacheRequest<T> request) {
        String key = cachingKey.getKey();

        Long result = redisTemplate.opsForValue().get(key);
        if(result == null){
            synchronized (this) {
                result = redisTemplate.opsForValue().get(key);
                if(result == null) {
                    Function<CacheRequest<?>, Long> action = KEY_ACTION_MAP.get(key);

                    if(action == null)
                        throw new IllegalArgumentException("caching Key is Abnormal");

                    result = action.apply(request);
                    long ttl = cacheProperties.getCount().get(key).getTtl();
                    redisTemplate.opsForValue().set(key, result, Duration.ofMinutes(ttl));
                }
            }
        }

        return result;
    }
}
