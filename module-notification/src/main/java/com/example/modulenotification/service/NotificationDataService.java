package com.example.modulenotification.service;

import com.example.modulecommon.model.entity.Notification;
import com.example.modulenotification.model.dto.out.NotificationListDTO;
import com.example.modulenotification.model.page.NotificationPageDTO;
import com.example.modulenotification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class NotificationDataService {

    private final NotificationRepository notificationRepository;

    @Value("${notification.redis.prefix}")
    private String redisPrefix;

    @Value("${notification.redis.ttl}")
    private Long redisTtl;

    @Value("${notification.redis.status}")
    private String redisStatus;

    private final RedisTemplate<String, String> redisTemplate;

    public Page<NotificationListDTO> getNotificationPaginationByUserId(NotificationPageDTO pageDTO,
                                                                       String userId) {
        Pageable pageable = PageRequest.of(pageDTO.pageNum() - 1,
                                                pageDTO.amount(),
                                                Sort.by("createdAt").descending()
                                        );

        return notificationRepository.findAllByUserId(userId, pageable);
    }

    public void updateIsRead(String userId) {
        notificationRepository.updateIsRead(userId);
    }

    public void saveNotification(Notification notification) {
        notificationRepository.save(notification);
    }

    public boolean isUserOnline(String userId) {
        String key = redisPrefix + userId;
        String status = redisTemplate.opsForValue().get(key);

        return status != null && status.equals(redisStatus);
    }

    public void updateUserOnlineStatus(String userId) {
        String key = redisPrefix + userId;

        redisTemplate.opsForValue().set(key, redisStatus, redisTtl, TimeUnit.SECONDS);
    }
}
