package com.fpt.h2s.services.commands.user;

import com.fpt.h2s.models.domains.ApiResponse;
import com.fpt.h2s.models.entities.NotificationHistory;
import com.fpt.h2s.models.entities.User;
import com.fpt.h2s.repositories.NotificationHistoryRepository;
import com.fpt.h2s.services.commands.BaseCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MarkReadNotificationsCommand implements BaseCommand<Void, Void> {

    private final NotificationHistoryRepository notificationHistoryRepository;

    @Override
    public ApiResponse<Void> execute(final Void request) {
        final Integer userId = User.getCurrentId();
        List<NotificationHistory> notifications = notificationHistoryRepository.findAllByUserId(userId);
        List<NotificationHistory> notificationsToSave = notifications
            .stream()
            .filter(NotificationHistory::isUnread)
            .map(notification -> notification.withReadAt(Timestamp.from(Instant.now())))
            .toList();

        notificationHistoryRepository.saveAll(notificationsToSave);
        return ApiResponse.success();
    }

}
