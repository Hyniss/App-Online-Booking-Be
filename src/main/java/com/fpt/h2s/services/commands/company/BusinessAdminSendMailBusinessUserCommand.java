package com.fpt.h2s.services.commands.company;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fpt.h2s.models.domains.ApiResponse;
import com.fpt.h2s.models.domains.BaseRequest;
import com.fpt.h2s.models.entities.EmailTemplate;
import com.fpt.h2s.models.entities.User;
import com.fpt.h2s.repositories.UserRepository;
import com.fpt.h2s.services.AmazonS3Service;
import com.fpt.h2s.services.commands.BaseCommand;
import com.fpt.h2s.workers.MailWorker;
import lombok.*;
import lombok.experimental.FieldNameConstants;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BusinessAdminSendMailBusinessUserCommand
        implements BaseCommand<BusinessAdminSendMailBusinessUserCommand.Request, Void> {

    private final MailWorker mailWorker;
    private final UserRepository userRepository;
    private final AmazonS3Service amazonS3Service;
    @Override
    public ApiResponse<Void> execute(@NonNull final Request request) {
        if(!request.getUrl().isEmpty()) this.amazonS3Service.deleteFile(request.getUrl());
        final List<User> originUsers = userRepository.findAllById(request.getUserIdRequests());
        originUsers.parallelStream().forEach(this::sendVerificationTo);
        return ApiResponse.success("Thêm thành viên thành công.");
    }

    private void sendVerificationTo(@NonNull final User savedUser) {
        this.mailWorker.sendMail(m -> m
                .sendTo(savedUser.getEmail())
                .withTemplate(EmailTemplate.Key.BUSINESS_EMAIL_VERIFICATION)
                .withProperty("username", savedUser.getUsername())
                .withSuccessMessage("Sent verification email to user with id {}", savedUser.getId()));
    }

    @Getter
    @Builder
    @FieldNameConstants
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, creatorVisibility = JsonAutoDetect.Visibility.ANY)
    public static class Request extends BaseRequest {

        private String url;
        private List<Integer> userIdRequests;
    }
}
