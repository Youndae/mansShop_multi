package com.example.modulenotification.usecase;

import com.example.modulecommon.model.dto.response.PagingListDTO;
import com.example.modulecommon.model.dto.response.PagingMappingDTO;
import com.example.modulenotification.model.dto.out.NotificationListDTO;
import com.example.modulenotification.model.page.NotificationPageDTO;
import com.example.modulenotification.service.NotificationDataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationReadUseCase {

    private final NotificationDataService notificationDataService;

    public PagingListDTO<NotificationListDTO> getNotificationList(NotificationPageDTO pageDTO, String userId) {
        Page<NotificationListDTO> notificationList = notificationDataService.getNotificationPaginationByUserId(pageDTO, userId);
        PagingMappingDTO pagingMappingDTO = new PagingMappingDTO(notificationList);

        if(!notificationList.isEmpty())
            notificationDataService.updateIsRead(userId);

        return new PagingListDTO<>(notificationList.getContent(), pagingMappingDTO);
    }
}
