package com.fpt.h2s.services.commands.requests;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fpt.h2s.models.domains.BaseRequest;
import com.fpt.h2s.models.domains.BaseValidator;
import com.fpt.h2s.models.entities.TravelStatement;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldNameConstants;
import org.hibernate.validator.constraints.Length;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@Getter
@Builder
@FieldNameConstants
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, creatorVisibility = JsonAutoDetect.Visibility.ANY)
public class BusinessMemberCreateTravelStatementRequest extends BaseRequest {

    @NotBlank(message = "Xin vui lòng điền tên của tờ trình.")
    @Length( max = 255, message = "Xin vui lòng để tên của tờ trình trong khoảng 255 kí tự.")
    private String name;

    @NotBlank(message = "Xin vui lòng điền số lượng người cần đặt phòng.")
    private String numberOfPeople;

    @NotBlank(message = "Xin vui lòng nhập khu vực mà người dùng có dự định ở vào tờ trình.")
    @Length(max = 2048, message = "Xin vui lòng để khu vực mà người dùng có dự định ở vào tờ trình trong khoảng 2048 kí tự.")
    private String location;

    private String note;

    @NotNull(message = "Xin vui lòng chọn ngày nhận phòng.")
    private Timestamp fromDate;

    @NotNull(message = "Xin vui lòng chọn ngày trả phòng.")
    private Timestamp toDate;

    public TravelStatement toTravelStatement() {
        return TravelStatement.builder()
                .name(this.name)
                .numberOfPeople(Integer.parseInt(this.numberOfPeople))
                .location(this.location)
                .note(this.note)
                .fromDate(this.fromDate)
                .toDate(this.toDate)
                .status(TravelStatement.Status.PENDING)
                .build();
    }

    @Component
    @RequiredArgsConstructor
    public static class Validator extends BaseValidator<BusinessMemberCreateTravelStatementRequest> {
        @Override
        protected void validate() {
            this.rejectIfEmpty(Fields.numberOfPeople, this::validateNumberOfPeople);
            this.rejectIfEmpty(Fields.note, this::validateNote);
            this.rejectIfEmpty(Fields.fromDate, this::validateFromDate);
            if(this.request.fromDate == null) return;
            this.rejectIfEmpty(Fields.toDate, this::validateToDate);
        }

        private String validateNumberOfPeople() {
            if(!this.request.numberOfPeople.matches("^[0-9]+$")) {
                return "Xin vui lòng chỉ nhập số nguyên dương!";
            }

            int number = Integer.parseInt(this.request.numberOfPeople);
            if(number < 1 || number > 5000) {
                return "Xin vui lòng nhập số lượng người trong khoảng từ 0 đến 5000.";
            }
            return null;
        }

        private String validateNote() {
            if(this.request.note != null) {
                if(this.request.note.length() > 255) {
                    return "Xin vui lòng để ghi chú của tờ trình trong khoảng 255 kí tự.";
                }
            }
            return null;
        }

        private String validateFromDate() {
            if (this.request.fromDate.before(Timestamp.valueOf(LocalDateTime.now()))) {
                return "Xin vui lòng chọn ngày nhận phòng sau ngày hiện tại.";
            }
            return null;
        }

        private String validateToDate() {
            if (this.request.toDate.before(Timestamp.valueOf(this.request.fromDate.toLocalDateTime().plusDays(1)))) {
                return "Xin vui lòng chọn ngày trả phòng sau ngày nhận phòng.";
            }
            return null;
        }
    }
}
