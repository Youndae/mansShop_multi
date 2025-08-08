package com.example.modulecommon.fixture;

import com.example.modulecommon.model.entity.Member;
import com.example.modulecommon.model.entity.Notification;
import com.example.modulecommon.model.enumuration.NotificationType;

import java.util.ArrayList;
import java.util.List;

public class NotificationFixture {
    public static List<Notification> createNotification(List<Member> memberList) {
        List<Notification> notificationList = new ArrayList<>();
        for(Member member : memberList)
            notificationList.addAll(createMemberNotification(member));


        return notificationList;
    }

    private static List<Notification> createMemberNotification(Member member) {
        int allTypeSize = NotificationType.values().length;
        int loopCount = 30 / allTypeSize;
        if(loopCount == 0)
            loopCount = 1;
        List<Notification> resultList = new ArrayList<>();
        for(int i = 0; i < loopCount; i++) {
            for(NotificationType type : NotificationType.values()){
                resultList.add(
                        Notification.builder()
                                .member(member)
                                .type(type.getType())
                                .title(type.getTitle())
                                .relatedId(1L)
                                .isRead(false)
                                .build()
                );
            }
        }

        return resultList;
    }
}
