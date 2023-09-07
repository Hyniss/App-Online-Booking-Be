package com.fpt.h2s.services;

import ananta.utility.StringEx;
import com.fpt.h2s.configurations.ConsulConfiguration;
import com.fpt.h2s.models.exceptions.ApiException;
import com.fpt.h2s.utilities.ExceptionPrinter;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class SmsService {

    private PhoneNumber phoneNumber;

    private final ConsulConfiguration consul;

    @PostConstruct
    private void init() {
        Twilio.init(this.consul.get("service.sms.ACCOUNT"), this.consul.get("service.sms.PASSWORD"));
        this.phoneNumber = new PhoneNumber(this.consul.get("service.sms.PHONE"));
    }

    public void sendTo(final String phone, final String message, final Object... args) {
        if (Objects.equals(System.getenv("ENVIRONMENT"), "TEST")) {
            return;
        }
        try {
            String cleanPhone = phone.replace(" ", "");
            Message.creator(new PhoneNumber(cleanPhone), this.phoneNumber, StringEx.format(message, args)).create();
        } catch (Exception e) {
            ExceptionPrinter.print(e);
            throw ApiException.badRequest("Yêu cầu gửi SMS đến số điện thoại không thành công.");
        }
    }
}
