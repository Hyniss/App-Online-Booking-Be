package com.fpt.h2s.services;

import com.fpt.h2s.models.entities.EmailTemplate;
import lombok.*;
import org.jetbrains.annotations.Nullable;

import java.util.function.UnaryOperator;

/**
 * This service is responsible for sending email to users using Gmail.
 */
public interface MailService {

    /**
     * Prepare a mail to be sent to user.
     *
     * @param mailBuilder chaining methods which will be used to pass email values
     * @return a mail which can be sent using method {@link MailService#send(MailData)}.
     */
    MailData createMail(final UnaryOperator<MailBuilder> mailBuilder);

    /**
     * Send mail to the email
     *
     * @param mail contains data which will be used in email such as target email, subject, content.
     * @throws IllegalAccessException If send email failed. This might be due to poor connection, email not existed...
     */
    void send(@NonNull final MailData mail);

    interface MailBuilder {

        MailBuilder sendTo(final String email);

        MailBuilder withTemplate(final EmailTemplate.Key template);

        MailBuilder withProperty(final String key, final Object value);

        MailBuilder withSuccessMessage(final String message, @Nullable final Object... args);

        MailBuilder withAttachment(final byte[] pdfBytes);

    }

    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Builder
    @ToString
    final class MailData {

        private String email;

        private String subject;

        private String content;

        private String successMessage;

        private byte[] pdfBytes;

    }

}
