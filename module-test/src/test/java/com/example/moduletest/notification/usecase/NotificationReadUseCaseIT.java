package com.example.moduletest.notification.usecase;

import com.example.modulecommon.fixture.MemberAndAuthFixture;
import com.example.modulecommon.fixture.NotificationFixture;
import com.example.modulecommon.model.dto.MemberAndAuthFixtureDTO;
import com.example.modulecommon.model.dto.response.PagingListDTO;
import com.example.modulecommon.model.entity.Member;
import com.example.modulecommon.model.entity.Notification;
import com.example.modulecommon.utils.TestPaginationUtils;
import com.example.modulenotification.model.dto.out.NotificationListDTO;
import com.example.modulenotification.model.page.NotificationPageDTO;
import com.example.modulenotification.repository.NotificationRepository;
import com.example.modulenotification.usecase.NotificationReadUseCase;
import com.example.moduletest.ModuleTestApplication;
import com.example.moduleuser.repository.MemberRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = ModuleTestApplication.class)
@EntityScan(basePackages = "com.example.modulecommon")
@EnableJpaRepositories(basePackages = "com.example")
@ComponentScan(basePackages = {"com.example.moduleconfig", "com.example.modulecommon"})
@ActiveProfiles("test")
@Transactional
public class NotificationReadUseCaseIT {

    @Autowired
    private NotificationReadUseCase notificationReadUseCase;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private EntityManager em;

    private Member member;

    private List<Notification> memberNotifications;

    @BeforeEach
    void init() {
        MemberAndAuthFixtureDTO memberAndAuthFixture = MemberAndAuthFixture.createDefaultMember(10);
        List<Member> memberList = memberAndAuthFixture.memberList();
        memberRepository.saveAll(memberList);
        member = memberList.get(0);

        List<Notification> notificationList = NotificationFixture.createNotification(memberList);
        notificationRepository.saveAll(notificationList);

        memberNotifications = notificationList.stream()
                .filter(v -> v.getMember().getUserId().equals(member.getUserId()))
                .toList();

        em.flush();
        em.clear();
    }

    @Test
    @DisplayName(value = "회원의 알림 목록 조회")
    void getNotificationList() {
        NotificationPageDTO pageDTO = new NotificationPageDTO(1);
        int totalPages = TestPaginationUtils.getTotalPages(memberNotifications.size(), pageDTO.amount());
        int contentSize = Math.min(memberNotifications.size(), pageDTO.amount());

        PagingListDTO<NotificationListDTO> result = assertDoesNotThrow(
                () -> notificationReadUseCase.getNotificationList(pageDTO, member.getUserId())
        );

        assertNotNull(result);
        assertFalse(result.content().isEmpty());
        assertFalse(result.pagingData().isEmpty());
        assertEquals(contentSize, result.content().size());
        assertEquals(memberNotifications.size(), result.pagingData().getTotalElements());
        assertEquals(totalPages, result.pagingData().getTotalPages());

        List<Notification> checkPatchEntityData = notificationRepository.findAllByMember_UserId(member.getUserId());
        checkPatchEntityData.forEach(v -> assertTrue(v.isRead()));
    }

    @Test
    @DisplayName(value = "회원의 알림 목록 조회. 데이터가 없는 경우")
    void getNotificationListEmpty() {
        notificationRepository.deleteAll();
        NotificationPageDTO pageDTO = new NotificationPageDTO(1);

        PagingListDTO<NotificationListDTO> result = assertDoesNotThrow(
                () -> notificationReadUseCase.getNotificationList(pageDTO, member.getUserId())
        );

        assertNotNull(result);
        assertTrue(result.content().isEmpty());
        assertTrue(result.pagingData().isEmpty());
        assertEquals(0, result.pagingData().getTotalElements());
        assertEquals( 0, result.pagingData().getTotalPages());
    }
}
