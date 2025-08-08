package com.example.moduleadmin.service.failedData;

import com.example.modulecommon.model.enumuration.FallbackMapKey;
import com.example.moduleconfig.properties.FallbackProperties;
import com.example.moduleorder.model.dto.business.FailedOrderDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AdminFailedDataService {

    private final RedisTemplate<String, FailedOrderDTO> failedOrderRedisTemplate;

    private final FallbackProperties fallbackProperties;

    public Set<String> getFailedOrderRedisKeys(FallbackMapKey fallbackMapKey) {
        String keyPrefix = fallbackProperties.getRedis().get(fallbackMapKey.getKey()).getPrefix();

        return failedOrderRedisTemplate.keys(keyPrefix + "*");
    }

    public List<FailedOrderDTO> getFailedOrderDataValue(List<String> keyList) {
        return failedOrderRedisTemplate.opsForValue().multiGet(keyList);
    }

    public void deleteFailedOrderData(String key) {
        failedOrderRedisTemplate.delete(key);
    }
}
