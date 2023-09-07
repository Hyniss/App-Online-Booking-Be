package com.fpt.h2s.services.commands.user;

import com.fpt.h2s.models.domains.ApiResponse;
import com.fpt.h2s.models.domains.SearchRequest;
import com.fpt.h2s.models.entities.NotificationHistory;
import com.fpt.h2s.models.entities.User;
import com.fpt.h2s.repositories.NotificationHistoryRepository;
import com.fpt.h2s.services.commands.BaseCommand;
import com.fpt.h2s.services.commands.responses.NotificationResponse;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldNameConstants;
import lombok.extern.jackson.Jacksonized;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FindAllNotificationsOfUserCommand implements BaseCommand<FindAllNotificationsOfUserCommand.Request, List<NotificationResponse>> {

    private final NotificationHistoryRepository notificationHistoryRepository;

    @Override
    public ApiResponse<List<NotificationResponse>> execute(final Request request) {
        final Integer userId = User.currentUserId().orElseThrow();
        final List<NotificationHistory> notificationHistories = this.notificationHistoryRepository.findAllByUserId(userId);
        List<NotificationResponse> response = notificationHistories.stream().map(NotificationResponse::of).toList();
        return ApiResponse.success(response);
    }

    @Getter
    @Builder(toBuilder = true)
    @FieldNameConstants
    @Jacksonized
    public static class Request extends SearchRequest {

        private final Integer size;

        private final Integer page;

        private final String orderBy;

        private final Boolean isDescending;
    }

}
