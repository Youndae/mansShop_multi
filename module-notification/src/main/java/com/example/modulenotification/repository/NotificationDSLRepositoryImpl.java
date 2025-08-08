package com.example.modulenotification.repository;

import com.example.modulenotification.model.dto.out.NotificationListDTO;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.example.modulecommon.model.entity.QNotification.notification;

@Repository
@RequiredArgsConstructor
public class NotificationDSLRepositoryImpl implements NotificationDSLRepository {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Page<NotificationListDTO> findAllByUserId(String userId, Pageable pageable) {
        List<NotificationListDTO> list = jpaQueryFactory.select(
                                            Projections.constructor(
                                                    NotificationListDTO.class,
                                                    notification.title,
                                                    notification.relatedId,
                                                    notification.type
                                            )
                                    )
                                    .from(notification)
                                    .where(notification.member.userId.eq(userId))
                                    .orderBy(notification.createdAt.desc())
                                    .offset(pageable.getOffset())
                                    .limit(pageable.getPageSize())
                                    .fetch();

        JPAQuery<Long> count = jpaQueryFactory.select(notification.countDistinct())
                .from(notification)
                .where(notification.member.userId.eq(userId));


        return PageableExecutionUtils.getPage(list, pageable, count::fetchOne);
    }

    @Override
    @Transactional
    public void updateIsRead(String userId) {
        jpaQueryFactory.update(notification)
                .set(notification.isRead, true)
                .where(notification.member.userId.eq(userId))
                .execute();
    }
}
