package com.fpt.h2s.services.commands.responses;

import com.fpt.h2s.models.entities.NotificationHistory;
import com.fpt.h2s.utilities.Mappers;
import lombok.Builder;
import lombok.Getter;
import lombok.With;

import java.sql.Timestamp;

@Builder
@Getter
@With
public class NotificationResponse {

    private Integer id;

    private Integer userId;

    private String payload;

    private boolean unread;

    private Timestamp createdAt;

    private Timestamp updatedAt;

    public static NotificationResponse of(final NotificationHistory notificationHistory) {
        return Mappers.convertTo(NotificationResponse.class, notificationHistory).withUnread(notificationHistory.isUnread());
    }

}
