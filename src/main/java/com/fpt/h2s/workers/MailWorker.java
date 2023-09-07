package com.fpt.h2s.workers;

import com.fpt.h2s.services.MailService;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.function.UnaryOperator;

@Log4j2
@Service
@AllArgsConstructor
public class MailWorker extends BaseWorker<MailService.MailData> {

    private final MailService mailService;

    @Override
    protected void execute(final MailService.MailData mail) {
        try {
            this.mailService.send(mail);
        } catch (final Exception e) {
            log.warn("Send message failed for {}", mail);
        }
    }

    @Override
    protected String getQueueName() {
        return "mail_queue";
    }

    @Override
    protected String getExchangeName() {
        return "mail_exchange";
    }

    @Override
    protected String getRoutingKey() {
        return "mail_routingKey";
    }

    public void sendMail(final UnaryOperator<MailService.MailBuilder> mailBuilder) {
        final MailService.MailData mail = this.mailService.createMail(mailBuilder);
        this.send(mail);
    }

}