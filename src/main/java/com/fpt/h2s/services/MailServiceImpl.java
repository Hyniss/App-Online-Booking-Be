package com.fpt.h2s.services;

import ananta.utility.StringEx;
import com.fpt.h2s.models.entities.EmailTemplate;
import com.fpt.h2s.models.exceptions.ApiException;
import com.fpt.h2s.repositories.EmailTemplateRepository;
import jakarta.activation.DataHandler;
import jakarta.mail.BodyPart;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import jakarta.mail.util.ByteArrayDataSource;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.Nullable;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.UnaryOperator;

@Log4j2
@Service
@RequiredArgsConstructor
public class MailServiceImpl implements MailService {

    private final JavaMailSender mailSender;

    private final EmailTemplateRepository emailTemplateRepository;

    @Override
    public MailData createMail(final UnaryOperator<MailBuilder> mailBuilder) {
        final MailBuilderImpl mailData = (MailBuilderImpl) mailBuilder.apply(new MailBuilderImpl());
        assert Strings.isNotBlank(mailData.email) : "Email should not be blank.";
        assert mailData.template != null : "Template should not be null.";

        final EmailTemplate template = this.emailTemplateRepository
                .findById(mailData.template)
                .orElseThrow(() -> ApiException.failed("Key {} not found", mailData.template));

        String content = template.getContent();
        for (final Map.Entry<String, Object> entry : mailData.keyValueMap.entrySet()) {
            content = content.replace("{{" + entry.getKey() + "}}", entry.getValue().toString());
        }

        if(mailData.pdfBytes != null) {
            return  MailData.builder()
                    .email(mailData.email)
                    .subject(template.getSubject())
                    .content(content)
                    .successMessage(mailData.successMessage)
                    .pdfBytes(mailData.pdfBytes)
                    .build();
        }

        return MailData.builder()
                .email(mailData.email)
                .subject(template.getSubject())
                .content(content)
                .successMessage(mailData.successMessage)
                .build();
    }

    @Override
    public void send(@NonNull final MailData mail) {
        try {
            if (Objects.equals(System.getenv("ENVIRONMENT"), "TEST")) {
                return;
            }
            final MimeMessage mimeMessage = this.mailSender.createMimeMessage();
            final MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true,  "utf-8");
            mimeMessage.setContent(mail.getContent(), "text/html; charset=utf-8");
            helper.setTo(mail.getEmail());
            helper.setSubject(mail.getSubject());
            if (mail.getPdfBytes() != null) {
                MimeMultipart multipart = new MimeMultipart();

                BodyPart contentPart = new MimeBodyPart();
                contentPart.setContent(mail.getContent(), "text/html; charset=utf-8");
                multipart.addBodyPart(contentPart);

                BodyPart attachmentPart = new MimeBodyPart();
                ByteArrayDataSource dataSource = new ByteArrayDataSource(mail.getPdfBytes(), "application/pdf");
                attachmentPart.setDataHandler(new DataHandler(dataSource));
                attachmentPart.setFileName("Contract.pdf");
                multipart.addBodyPart(attachmentPart);
                mimeMessage.setContent(multipart);
            }
            this.mailSender.send(mimeMessage);
            if (mail.getSuccessMessage() != null) {
                MailServiceImpl.log.info(mail.getSuccessMessage());
            }
        } catch (final NullPointerException | MessagingException e) {
            throw ApiException.failed(e, "There are some errors while sending mail.");
        }
    }

    private static class MailBuilderImpl implements MailBuilder {

        private String email;

        private EmailTemplate.Key template;

        private final Map<String, Object> keyValueMap = new HashMap<>();

        private String successMessage;

        private byte[] pdfBytes;

        @Override
        public MailBuilder sendTo(final String email) {
            if (Strings.isBlank(email)) {
                throw new IllegalArgumentException("Email should not blank.");
            }
            this.email = email;
            return this;
        }

        @Override
        public MailBuilder withTemplate(final EmailTemplate.Key template) {
            if (template == null) {
                throw new IllegalArgumentException("Template should not null.");
            }
            this.template = template;
            return this;
        }

        @Override
        public MailBuilder withProperty(final String key, final Object value) {
            if (Strings.isNotBlank(key) && value != null) {
                this.keyValueMap.put(key, value);
            }
            return this;
        }

        @Override
        public MailBuilder withSuccessMessage(final String message, @Nullable final Object... args) {
            this.successMessage = StringEx.format(message, args);
            return this;
        }

        @Override
        public MailBuilder withAttachment(byte[] pdfBytes) {
            this.pdfBytes = pdfBytes;
            return this;
        }

    }

}