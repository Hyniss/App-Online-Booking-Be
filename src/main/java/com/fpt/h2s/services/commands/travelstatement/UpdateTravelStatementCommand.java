package com.fpt.h2s.services.commands.travelstatement;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fpt.h2s.models.domains.ApiResponse;
import com.fpt.h2s.models.domains.BaseRequest;
import com.fpt.h2s.models.domains.BaseValidator;
import com.fpt.h2s.models.entities.TravelStatement;
import com.fpt.h2s.models.entities.User;
import com.fpt.h2s.models.exceptions.ApiException;
import com.fpt.h2s.repositories.TravelStatementRepository;
import com.fpt.h2s.repositories.UserRepository;
import com.fpt.h2s.services.commands.BaseCommand;
import com.fpt.h2s.services.commands.responses.TravelStatementResponse;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldNameConstants;
import org.hibernate.validator.constraints.Length;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Objects;

@Service
@Transactional
@RequiredArgsConstructor
public class UpdateTravelStatementCommand implements BaseCommand<UpdateTravelStatementCommand.UpdateTravelStatementRequest, TravelStatementResponse> {
    private final TravelStatementRepository travelStatementRepository;
    private final UserRepository userRepository;

    @Override
    public ApiResponse<TravelStatementResponse> execute(final UpdateTravelStatementRequest request) {
        final TravelStatement travelStatement = this.travelStatementRepository
                .findById(request.getId())
                .orElseThrow(() -> ApiException.badRequest("Không thể tìm thấy tờ trình với id = {}.", request.getId()));

        if (!travelStatement.is(TravelStatement.Status.PENDING)) {
            throw ApiException.badRequest("Chỉ có thể cập nhật tờ trình khi ở trạng thái chưa giải quyết (PENDING).");
        }

        final Integer currentUserId = User.currentUserId()
                .orElseThrow();

        this.userRepository
                .findById(currentUserId)
                .orElseThrow();

        if (!Objects.equals(travelStatement.getCreatorId(), currentUserId)) {
            throw ApiException.badRequest("Tờ trình này không thuộc quyền quản lí của bạn.");
        }

        final TravelStatement travelStatementToUpdate = travelStatement.toBuilder()
                .name(request.name)
                .numberOfPeople(Integer.parseInt(request.numberOfPeople))
                .location(request.location)
                .note(request.note)
                .fromDate(request.fromDate)
                .toDate(request.toDate)
                .build();
        this.travelStatementRepository.save(travelStatementToUpdate);
        return ApiResponse.success("Cập nhật tờ trình thành công.", TravelStatementResponse.of(travelStatementToUpdate));
    }

    @Getter
    @Builder
    @FieldNameConstants
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, creatorVisibility = JsonAutoDetect.Visibility.ANY)
    public static class UpdateTravelStatementRequest extends BaseRequest {
        @NotNull(message = "Xin vui lòng không để trống ID của tờ trình.")
        private Integer id;

        @NotBlank(message = "Xin vui lòng điền tên của tờ trình.")
        @Length(max = 255, message = "Xin vui lòng để tên của tờ trình trong khoảng 255 kí tự.")
        private String name;

        @NotBlank(message = "Xin vui lòng điền số lượng người cần đặt phòng.")
        private String numberOfPeople;

        @NotBlank(message = "Xin vui lòng nhập khu vực mà người dùng có dự định ở vào tờ trình.")
        @Length(max = 2048, message = "Xin vui lòng để khu vực mà người dùng có dự định ở vào tờ trình trong khoảng 2048 kí tự.")
        private String location;

        @Length(max = 255, message = "Xin vui lòng để ghi chú của tờ trình trong khoảng 255 kí tự.")
        private String note;

        @NotNull(message = "Xin vui lòng chọn ngày nhận phòng.")
        private Timestamp fromDate;

        @NotNull(message = "Xin vui lòng chọn ngày trả phòng.")
        private Timestamp toDate;

        @Component
        @RequiredArgsConstructor
        public static class Validator extends BaseValidator<UpdateTravelStatementCommand.UpdateTravelStatementRequest> {
            @Override
            protected void validate() {
                this.rejectIfEmpty(Fields.numberOfPeople, this::validateNumberOfPeople);
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

            public String validateFromDate() {
                if (this.request.fromDate.before(Timestamp.valueOf(LocalDateTime.now()))) {
                    return "Xin vui lòng chọn ngày nhận phòng sau ngày hiện tại.";
                }
                return null;
            }
            public String validateToDate() {
                if (this.request.toDate.before(Timestamp.valueOf(this.request.fromDate.toLocalDateTime().plusDays(1)))) {
                    return "Xin vui lòng chọn ngày trả phòng sau ngày nhận phòng.";
                }
                return null;
            }
        }
    }
}
