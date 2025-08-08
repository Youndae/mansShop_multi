package com.example.moduletest.notification.usecase;

import com.example.modulecommon.customException.CustomAccessDeniedException;
import com.example.modulecommon.fixture.MemberAndAuthFixture;
import com.example.modulecommon.model.dto.MemberAndAuthFixtureDTO;
import com.example.modulecommon.model.dto.notification.NotificationSendDTO;
import com.example.modulecommon.model.entity.Member;
import com.example.modulecommon.model.entity.Notification;
import com.example.modulecommon.model.enumuration.NotificationType;
import com.example.modulenotification.repository.NotificationRepository;
import com.example.modulenotification.usecase.NotificationWriteUseCase;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = ModuleTestApplication.class)
@EntityScan(basePackages = "com.example.modulecommon")
@EnableJpaRepositories(basePackages = "com.example")
@ComponentScan(basePackages = {"com.example.moduleconfig", "com.example.modulecommon"})
@ActiveProfiles("test")
@Transactional
public class NotificationWriteUseCaseIT {

    @Autowired
    private NotificationWriteUseCase notificationWriteUseCase;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

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
    @DisplayName(value = "알림 데이터 저장")
    void saveNotification() {
        NotificationSendDTO sendDTO = new NotificationSendDTO(
                member.getUserId(),
                NotificationType.MEMBER_QNA_REPLY,
                NotificationType.MEMBER_QNA_REPLY.getTitle(),
                1L
        );

        assertDoesNotThrow(() -> notificationWriteUseCase.saveNotificationData(sendDTO));

        List<Notification> notificationList = notificationRepository.findAll();

        assertFalse(notificationList.isEmpty());
        assertEquals(1, notificationList.size());

        Notification notification = notificationList.get(0);
        assertEquals(NotificationType.MEMBER_QNA_REPLY.getType(), notification.getType());
        assertEquals(NotificationType.MEMBER_QNA_REPLY.getTitle(), notification.getTitle());
        assertEquals(1L, notification.getRelatedId());
        assertEquals(member.getUserId(), notification.getMember().getUserId());
    }

    @Test
    @DisplayName(value = "알림 데이터 저장. 사용자 정보가 존재하지 않는 경우")
    void saveNotificationMemberIsNull() {
        NotificationSendDTO sendDTO = new NotificationSendDTO(
                "WrongUserId",
                NotificationType.MEMBER_QNA_REPLY,
                NotificationType.MEMBER_QNA_REPLY.getTitle(),
                1L
        );

        assertThrows(
                CustomAccessDeniedException.class,
                () -> notificationWriteUseCase.saveNotificationData(sendDTO)
        );

        List<Notification> notificationList = notificationRepository.findAll();

        assertTrue(notificationList.isEmpty());
    }

    @Test
    @DisplayName(value = "사용자 온라인 상태 확인")
    void isUserOnline() {
        redisTemplate.opsForValue().set(redisKey, redisStatus, redisTtl, TimeUnit.SECONDS);

        boolean result = assertDoesNotThrow(() -> notificationWriteUseCase.isUseOnline(member.getUserId()));

        assertTrue(result);
    }

    @Test
    @DisplayName(value = "사용자 온라인 상태 확인. 오프라인인 경우")
    void isUserOffline() {
        boolean result = assertDoesNotThrow(() -> notificationWriteUseCase.isUseOnline(member.getUserId()));

        assertFalse(result);
    }

    @Test
    @DisplayName(value = "사용자 온라인 상태 갱신")
    void updateUserOnlineStatus() {
        assertDoesNotThrow(() -> notificationWriteUseCase.updateUserOnlineStatus(member.getUserId()));

        String value = redisTemplate.opsForValue().get(redisKey);
        assertNotNull(value);
    }
}
