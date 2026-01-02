package com.example.moduleapi.controller.admin;

import com.example.moduleadmin.model.dto.failedData.out.FailedQueueDTO;
import com.example.moduleadmin.usecase.failedData.AdminFailedDataReadUseCase;
import com.example.moduleadmin.usecase.failedData.AdminFailedDataWriteUseCase;
import com.example.moduleapi.annotation.swagger.DefaultApiResponse;
import com.example.moduleapi.annotation.swagger.SwaggerAuthentication;
import com.example.moduleapi.model.response.ResponseIdDTO;
import com.example.modulecommon.model.enumuration.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Admin FailedData Controller")
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminFailedDataController {

    private final AdminFailedDataReadUseCase adminFailedDataReadUseCase;

    private final AdminFailedDataWriteUseCase adminFailedDataWriteUseCase;

    /**
     *
     * 각 DLQ에 담긴 실패한 메시지 수량 반환.
     */
    @Operation(summary = "RabbitMQ 처리 중 실패한 메시지를 담고 있는 각 DLQ의 메시지 개수 반환. 실패 메시지가 존재하는 DLQ만 반환")
    @DefaultApiResponse
    @SwaggerAuthentication
    @GetMapping("/message")
    public ResponseEntity<List<FailedQueueDTO>> getFailedQueueCount() {
        List<FailedQueueDTO> responseDTO = adminFailedDataReadUseCase.getFailedMessageList();

        return ResponseEntity.ok(responseDTO);
    }

    /**
     *
     * @param failedQueueDTO
     *
     * DLQ 메시지 재시도 요청
     * 반환된 데이터를 그대로 받아서 처리
     */
    @Operation(summary = "DLQ 데이터 재시도 요청")
    @DefaultApiResponse
    @SwaggerAuthentication
    @PostMapping("/message")
    public ResponseEntity<Void> retryDLQMessages(@RequestBody List<@Valid FailedQueueDTO> failedQueueDTO) {
        adminFailedDataWriteUseCase.retryDLQMessages(failedQueueDTO);

        return ResponseEntity.ok().build();
    }

    /**
     *
     * Redis에 저장된 실패한 주문 데이터 개수 조회
     */
    @GetMapping("/message/order")
    public ResponseEntity<ResponseIdDTO<Long>> getFailedOrderDataByRedis() {
        long response = adminFailedDataReadUseCase.getFailedOrderDataByRedis();

        return ResponseEntity.ok(new ResponseIdDTO<>(response));
    }

    /**
     *
     * redis에 저장된 실패한 주문 데이터 재처리
     */
    @PostMapping("/message/order")
    public ResponseEntity<Void> retryRedisOrderMessage() {
        String responseMessage = adminFailedDataWriteUseCase.retryFailedOrderByRedis();

        HttpStatus status = HttpStatus.OK;

        if(responseMessage.equals(Result.EMPTY.getResultKey()))
            status = HttpStatus.NO_CONTENT;

        return ResponseEntity.status(status).build();
    }
}
