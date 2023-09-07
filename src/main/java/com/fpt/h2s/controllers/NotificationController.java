package com.fpt.h2s.controllers;

import com.fpt.h2s.models.annotations.RequiredRoles;
import com.fpt.h2s.models.domains.ApiResponse;
import com.fpt.h2s.services.commands.responses.NotificationResponse;
import com.fpt.h2s.services.commands.user.AddNotificationTokenDeviceForUserCommand;
import com.fpt.h2s.services.commands.user.FindAllNotificationsOfUserCommand;
import com.fpt.h2s.services.commands.user.MarkReadNotificationsCommand;
import com.fpt.h2s.services.commands.user.RemoveNotificationTokenDeviceForUserCommand;
import com.fpt.h2s.utilities.SpringBeans;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/notification")
@SuppressWarnings("unused")
public class NotificationController {

    @GetMapping("")
    @RequiredRoles
    @Operation(summary = "Find all notifications of user")
    public ApiResponse<List<NotificationResponse>> findAllNotificationsOfUser(final FindAllNotificationsOfUserCommand.Request request) {
        return SpringBeans.getBean(FindAllNotificationsOfUserCommand.class).execute(request);
    }

    @PostMapping("/token")
    @RequiredRoles
    @Operation(summary = "Add device token for user to receive notification.")
    public static ApiResponse<Void> addNotificationTokenDeviceForUser(@Valid @RequestBody final AddNotificationTokenDeviceForUserCommand.Request request) {
        return SpringBeans.getBean(AddNotificationTokenDeviceForUserCommand.class).execute(request);
    }

    @PostMapping("/token/remove")
    @RequiredRoles
    @Operation(summary = "Remove device token for user")
    public static ApiResponse<Void> removeTokenForUser(@Valid @RequestBody final RemoveNotificationTokenDeviceForUserCommand.Request request) {
        return SpringBeans.getBean(RemoveNotificationTokenDeviceForUserCommand.class).execute(request);
    }

    @PutMapping("/read")
    @RequiredRoles
    @Operation(summary = "Mark read for notifications.")
    public static ApiResponse<Void> markReadForNotifications() {
        return SpringBeans.getBean(MarkReadNotificationsCommand.class).execute(null);
    }
}