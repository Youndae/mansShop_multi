package com.example.modulemypage.service;

import com.example.modulecommon.model.dto.notification.NotificationSendDTO;
import com.example.modulecommon.model.entity.MemberQnA;
import com.example.modulecommon.model.enumuration.NotificationType;
import com.example.moduleconfig.config.rabbitMQ.RabbitMQPrefix;
import com.example.moduleconfig.properties.RabbitMQProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberQnAExternalService {

    private final RabbitMQProperties rabbitMQProperties;

    private final RabbitTemplate rabbitTemplate;

    public void sendMemberQnANotification(long qnaId, MemberQnA memberQnA) {
        String notificationTitle = memberQnA.getMemberQnATitle() + NotificationType.MEMBER_QNA_REPLY.getTitle();

        rabbitTemplate.convertAndSend(
                rabbitMQProperties.getExchange().get(RabbitMQPrefix.EXCHANGE_NOTIFICATION.getKey()).getName(),
                rabbitMQProperties.getQueue().get(RabbitMQPrefix.QUEUE_NOTIFICATION.getKey()).getRouting(),
                new NotificationSendDTO(
                        memberQnA.getMember().getUserId(),
                        NotificationType.MEMBER_QNA_REPLY,
                        notificationTitle,
                        qnaId
                )
        );
    }
}
