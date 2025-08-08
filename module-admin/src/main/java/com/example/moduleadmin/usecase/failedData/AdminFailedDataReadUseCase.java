package com.example.moduleadmin.usecase.failedData;

import com.example.moduleadmin.model.dto.failedData.out.FailedQueueDTO;
import com.example.moduleadmin.service.failedData.AdminFailedDataExternalService;
import com.example.moduleadmin.service.failedData.AdminFailedDataService;
import com.example.modulecommon.model.enumuration.FallbackMapKey;
import com.example.moduleconfig.properties.RabbitMQProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminFailedDataReadUseCase {

    private final RabbitMQProperties rabbitMQProperties;

    private final AdminFailedDataExternalService adminFailedDataExternalService;

    private final AdminFailedDataService adminFailedDataService;

    public List<FailedQueueDTO> getFailedMessageList() {
        List<String> dlqNames = rabbitMQProperties.getQueue()
                                    .values()
                                    .stream()
                                    .map(RabbitMQProperties.Queue::getDlq)
                                    .toList();
        List<FailedQueueDTO> response = new ArrayList<>();

        for(String name : dlqNames) {
            int messageCount = adminFailedDataExternalService.getDLQMessageCount(name);

            if(messageCount > 0)
                response.add(new FailedQueueDTO(name, messageCount));
        }

        return response;
    }

    public long getFailedOrderDataByRedis() {
        Set<String> failedOrderKeys = adminFailedDataService.getFailedOrderRedisKeys(FallbackMapKey.ORDER);
        Set<String> failedMessageKeys = adminFailedDataService.getFailedOrderRedisKeys(FallbackMapKey.ORDER_MESSAGE);

        return failedOrderKeys.size() + failedMessageKeys.size();
    }
}
