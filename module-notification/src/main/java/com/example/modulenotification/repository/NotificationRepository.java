package com.example.modulenotification.repository;

import com.example.modulecommon.model.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long>, NotificationDSLRepository {
}
