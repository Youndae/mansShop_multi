package com.example.moduleapi.controller.notification;

import com.example.moduleapi.service.PrincipalService;
import com.example.modulenotification.usecase.NotificationWriteUseCase;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api/notification")
@RequiredArgsConstructor
public class NotificationController {

    private final PrincipalService principalService;

    private final NotificationWriteUseCase notificationWriteUseCase;

    /**
     *
     * @param principal
     *
     * 클라이언트 온라인 상태 갱신
     * 주기적으로 클라이언트에서 요청을 보내고 Redis 데이터를 갱신하여 온라인 상태를 유지
     */
    @Operation(hidden = true)
    @GetMapping("/heartbeat")
    public ResponseEntity<Void> checkHeartbeat(Principal principal) {
        String userId = principalService.extractUserId(principal);
        notificationWriteUseCase.updateUserOnlineStatus(userId);

        return ResponseEntity.ok().build();
    }
}
