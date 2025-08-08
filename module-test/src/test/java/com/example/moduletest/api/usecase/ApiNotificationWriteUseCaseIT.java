package com.example.moduletest.api.usecase;

import com.example.moduleapi.usecase.ApiNotificationWriteUseCase;
import com.example.modulecommon.fixture.MemberAndAuthFixture;
import com.example.modulecommon.model.dto.MemberAndAuthFixtureDTO;
import com.example.modulecommon.model.dto.notification.NotificationSendDTO;
import com.example.modulecommon.model.entity.Member;
import com.example.modulecommon.model.entity.Notification;
import com.example.modulecommon.model.enumuration.NotificationType;
import com.example.modulenotification.model.dto.out.NotificationDTO;
import com.example.modulenotification.repository.NotificationRepository;
import com.example.moduletest.ModuleTestApplication;
import com.example.moduleuser.repository.MemberRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = ModuleTestApplication.class)
@EntityScan(basePackages = "com.example.modulecommon")
@EnableJpaRepositories(basePackages = "com.example")
@ComponentScan(basePackages = {"com.example.moduleconfig", "com.example.modulecommon"})
@ActiveProfiles("test")
@Transactional
public class ApiNotificationWriteUseCaseIT {

    @Autowired
    private ApiNotificationWriteUseCase apiNotificationWriteUseCase;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @MockitoBean
    private SimpMessagingTemplate simpMessagingTemplate;

    private Member member;

    @Value("${notification.redis.prefix}")
    private String redisPrefix;

    @Value("${notification.redis.ttl}")
    private Long redisTtl;

    @Value("${notification.redis.status}")
    private String redisStatus;

    private String redisKey;

    @BeforeEach
    void init() {
        MemberAndAuthFixtureDTO memberAndAuthFixture = MemberAndAuthFixture.createDefaultMember(10);
        List<Member> memberList = memberAndAuthFixture.memberList();
        memberRepository.saveAll(memberList);
        member = memberList.get(0);

        redisKey = redisPrefix + member.getUserId();

        em.flush();
        em.clear();
    }

    @AfterEach
    void cleanUp() {
        redisTemplate.delete(redisKey);
    }

    @Test
    @DisplayName(value = "알림 발생. 사용자가 온라인이라 알림이 전달되는 경우")
    void sendNotification() {
        NotificationSendDTO sendDTO = new NotificationSendDTO(
                member.getUserId(),
                NotificationType.MEMBER_QNA_REPLY,
                NotificationType.MEMBER_QNA_REPLY.getTitle(),
                1L
        );
        redisTemplate.opsForValue().set(redisKey, redisStatus, redisTtl, TimeUnit.SECONDS);

        doNothing().when(simpMessagingTemplate).convertAndSendToUser(any(), any(), any(NotificationDTO.class));

        assertDoesNotThrow(() -> apiNotificationWriteUseCase.sendNotification(sendDTO));

        verify(simpMessagingTemplate).convertAndSendToUser(any(), any(), any(NotificationDTO.class));

        List<Notification> saveNotifications = notificationRepository.findAll();

        assertFalse(saveNotifications.isEmpty());
        assertEquals(1, saveNotifications.size());

        Notification notification = saveNotifications.get(0);

        assertEquals(NotificationType.MEMBER_QNA_REPLY.getType(), notification.getType());
        assertEquals(NotificationType.MEMBER_QNA_REPLY.getTitle(), notification.getTitle());
        assertEquals(1L, notification.getRelatedId());
        assertEquals(member.getUserId(), notification.getMember().getUserId());
    }

    @Test
    @DisplayName(value = "알림 발생. 사용자가 오프인이라 알림이 전달되지 않는 경우")
    void sendNotificationUserOffline() {
        NotificationSendDTO sendDTO = new NotificationSendDTO(
                member.getUserId(),
                NotificationType.MEMBER_QNA_REPLY,
                NotificationType.MEMBER_QNA_REPLY.getTitle(),
                1L
        );

        assertDoesNotThrow(() -> apiNotificationWriteUseCase.sendNotification(sendDTO));

        verify(simpMessagingTemplate, never()).convertAndSendToUser(any(), any(), any(NotificationDTO.class));

        List<Notification> saveNotifications = notificationRepository.findAll();

        assertFalse(saveNotifications.isEmpty());
        assertEquals(1, saveNotifications.size());

        Notification notification = saveNotifications.get(0);

        assertEquals(NotificationType.MEMBER_QNA_REPLY.getType(), notification.getType());
        assertEquals(NotificationType.MEMBER_QNA_REPLY.getTitle(), notification.getTitle());
        assertEquals(1L, notification.getRelatedId());
        assertEquals(member.getUserId(), notification.getMember().getUserId());
    }
}
