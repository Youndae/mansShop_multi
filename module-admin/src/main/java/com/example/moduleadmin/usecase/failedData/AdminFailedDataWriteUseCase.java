package com.example.moduleadmin.usecase.failedData;

import com.example.moduleadmin.model.dto.failedData.out.FailedQueueDTO;
import com.example.moduleadmin.service.failedData.AdminFailedDataExternalService;
import com.example.moduleadmin.service.failedData.AdminFailedDataService;
import com.example.modulecommon.model.enumuration.FallbackMapKey;
import com.example.modulecommon.model.enumuration.Result;
import com.example.moduleorder.model.dto.business.FailedOrderDTO;
import com.example.moduleorder.usecase.OrderWriteUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AdminFailedDataWriteUseCase {

    private final AdminFailedDataExternalService adminFailedDataExternalService;

    private final AdminFailedDataService adminFailedDataService;

    private final OrderWriteUseCase orderWriteUseCase;

    public String retryDLQMessages(List<FailedQueueDTO> failedQueueDTO) {
        adminFailedDataExternalService.retryMessage(failedQueueDTO);

        return Result.OK.getResultKey();
    }


    public String retryFailedOrderByRedis() {
        Set<String> failedOrderKeys = adminFailedDataService.getFailedOrderRedisKeys(FallbackMapKey.ORDER);
        Set<String> failedMessageKeys = adminFailedDataService.getFailedOrderRedisKeys(FallbackMapKey.ORDER_MESSAGE);

        if(failedOrderKeys.isEmpty() && failedMessageKeys.isEmpty())
            return Result.EMPTY.getResultKey();

        if(!failedOrderKeys.isEmpty())
            retryFailedOrder(failedOrderKeys, FallbackMapKey.ORDER);

        if(!failedMessageKeys.isEmpty())
            retryFailedOrder(failedOrderKeys, FallbackMapKey.ORDER_MESSAGE);


        return Result.OK.getResultKey();
    }

    private void retryFailedOrder(Set<String> keys, FallbackMapKey fallbackMapKey) {
        List<String> keyList = keys.stream().toList();
        List<FailedOrderDTO> dataList = adminFailedDataService.getFailedOrderDataValue(keyList);

        for(int i = 0; i < dataList.size(); i++) {
            FailedOrderDTO data = dataList.get(i);

            String response = orderWriteUseCase.retryFailedOrder(data, fallbackMapKey);

            if(response.equals(Result.OK.getResultKey()))
                adminFailedDataService.deleteFailedOrderData(keyList.get(i));
        }
    }
}
