package com.example.modulenotification.repository;

import com.example.modulenotification.model.dto.out.NotificationListDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NotificationDSLRepository {

    Page<NotificationListDTO> findAllByUserId(String userId, Pageable pageable);

    void updateIsRead(String userId);
}
