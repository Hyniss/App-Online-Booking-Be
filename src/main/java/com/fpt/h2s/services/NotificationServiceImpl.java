package com.fpt.h2s.services;

import com.fpt.h2s.models.entities.NotificationHistory;
import com.fpt.h2s.repositories.NotificationHistoryRepository;
import com.fpt.h2s.repositories.UserDeviceTokenRepository;
import com.fpt.h2s.utilities.ExceptionPrinter;
import com.google.firebase.messaging.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service
@Log4j2
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final FirebaseMessaging firebaseMessaging;

    private final UserDeviceTokenRepository userDeviceTokenRepository;
    private final NotificationHistoryRepository notificationHistoryRepository;

    @Override
    public void send(final Function<Notification.Builder, Notification.Builder> notificationBuilder) {
        final Notification notification = notificationBuilder.apply(new Notification.Builder()).build();
        try {
            final MulticastMessage message = this.createMessage(notification);
            final BatchResponse batchResponse = this.firebaseMessaging.sendMulticast(message);

            final long totalSucceed = batchResponse.getResponses().stream().filter(SendResponse::isSuccessful).count();
            final long totalFailed = batchResponse.getResponses().stream().filter(Predicate.not(SendResponse::isSuccessful)).count();

            final List<NotificationHistory> notificationHistories = notification.getUserIds()
                .stream()
                .map(
                    userId -> NotificationHistory.builder().userId(userId).payload(notification.getContent()).build()
                )
                .collect(Collectors.toList());
            this.notificationHistoryRepository.saveAll(notificationHistories);

            log.info("Total notifications sent succeed: {}", totalSucceed);
            log.info("Total notifications sent failed: {}", totalFailed);
        } catch (final FirebaseMessagingException e) {
            log.info("Firebase error {}", e.getMessage());
        } catch (final Exception e) {
            ExceptionPrinter.print(e);
            log.warn("Send notification failed.");
        }
    }

    private MulticastMessage createMessage(final Notification notification) {
        final com.google.firebase.messaging.Notification notificationInformation = com.google.firebase.messaging.Notification
            .builder()
            .setBody(notification.getContent())
            .build();

        // for iOS
        final Aps aps = Aps.builder().setSound("default").build();
        final ApnsConfig apnsConfig = ApnsConfig.builder().setAps(aps).build();

        // for Android
        final AndroidNotification androidNotification = AndroidNotification.builder().setSound("default").build();
        final AndroidConfig androidConfig = AndroidConfig.builder().setNotification(androidNotification).build();

        final List<String> tokens = this.userDeviceTokenRepository.findAllByUserIds(notification.getUserIds()).stream().map(token -> token.getId().getToken()).toList();

        return MulticastMessage
            .builder()
            .addAllTokens(tokens)
            .setNotification(notificationInformation)
            .setApnsConfig(apnsConfig)
            .setAndroidConfig(androidConfig)
            .putAllData(Map.of("content", notification.getContent()))
            .build();
    }


}
