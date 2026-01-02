package com.example.moduleadmin.model.dto.failedData.out;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record FailedQueueDTO(
        @NotNull(message = "queueName is Not Null")
        @NotBlank(message = "queueName is Not Blank")
        @Schema(name = "queueName", description = "DLQ 이름")
        String queueName,

        @NotNull
        @Min(value = 1, message = "messageCount min Value is 1")
        @Schema(name = "messageCount", description = "실패한 메시지 개수")
        int messageCount
) {
}
