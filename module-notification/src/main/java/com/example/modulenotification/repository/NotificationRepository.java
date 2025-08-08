package com.example.modulenotification.repository;

import com.example.modulecommon.model.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long>, NotificationDSLRepository {

    //NotificationReadUseCase Integration Test용
    List<Notification> findAllByMember_UserId(String memberUserId);
}
