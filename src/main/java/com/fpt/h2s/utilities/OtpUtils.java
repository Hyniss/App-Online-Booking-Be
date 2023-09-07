package com.fpt.h2s.utilities;

import com.fpt.h2s.models.entities.OtpDetail;
import com.fpt.h2s.repositories.OtpRepository;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.StandardException;
import lombok.experimental.UtilityClass;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

@UtilityClass
public class OtpUtils {

    public Optional<OtpDetail> findOtpByKey(final String key) {
        final String ipAddress = MoreRequests.getIPAddress();
        return SpringBeans.getBean(OtpRepository.class)
            .findAllByOtpKey(key)
            .stream()
            .filter(otp_ -> otp_.getIpAddress().equals(ipAddress))
            .filter(Predicate.not(OtpDetail::isVerified).and(Predicate.not(OtpDetail::isExpired)))
            .findFirst();
    }

    public void validateOtp(final OtpDetail otp, final String requestOtp, final int maxTry) {
        final Integer totalTried = otp.getTotalTried();
        if (totalTried >= maxTry) {
            throw new OtpDisabledException();
        }

        if (Objects.equals(requestOtp, otp.getValue())) {
            return;
        }

        final int newTotalTried = totalTried + 1;
        final OtpDetail otpToSave = otp.withTotalTried(newTotalTried);
        SpringBeans.getBean(OtpRepository.class).save(otpToSave);
        final int tryLeft = maxTry - newTotalTried;

        if (tryLeft == 0) {
            throw new OtpDisabledException();
        }

        throw new WrongOtpException(tryLeft);
    }


    @StandardException
    public static class OtpDisabledException extends RuntimeException {

    }

    @Getter
    @AllArgsConstructor
    @StandardException
    public static class WrongOtpException extends RuntimeException {
        private Integer tryLeft;
    }
}
