package com.fpt.h2s.services.commands.requests;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fpt.h2s.models.domains.BaseRequest;
import com.fpt.h2s.models.domains.BaseValidator;
import com.fpt.h2s.models.entities.*;
import com.fpt.h2s.models.exceptions.ApiException;
import com.fpt.h2s.repositories.AccommodationRepository;
import com.fpt.h2s.repositories.ContractRepository;
import com.fpt.h2s.repositories.RoomRepository;
import com.fpt.h2s.utilities.MoreStrings;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldNameConstants;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Set;

import static com.fpt.h2s.models.entities.PriceHistory.Type.DISCOUNT;
import static com.fpt.h2s.models.entities.PriceHistory.Type.PRICE;

@Getter
@Builder
@FieldNameConstants
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, creatorVisibility = JsonAutoDetect.Visibility.ANY)
public class HouseOwnerUpdateRoomAmountRequest extends BaseRequest {

    @NotNull(message = "Xin vui lòng không để trống ID của phòng.")
    private Integer roomId;

    @NotNull(message = "Xin hãy chọn 1 trong 2 loại sau đây: PRICE (% giá) hoặc DISCOUNT(Phần trăm giá giảm).")
    private PriceHistory.Type type;

    private PriceHistory.DayType dayType;

    @NotNull(message = "Xin hãy nhập % giá hoặc phần trăm giảm giá cần cập nhật.")
    private Long amount;

    @NotNull(message = "Xin vui lòng không để trống ngày bắt đầu áp dụng thay đổi.")
    private LocalDate fromDate;

    @Nullable
    private LocalDate toDate;

    public PriceHistory toAmount(LocalDate from, LocalDate to) {
        return PriceHistory.builder()
                .roomId(this.roomId)
                .type(this.type)
                .dayType(PriceHistory.DayType.CUSTOM)
                .amount(this.amount)
                .fromDate(from == null ? this.fromDate : from)
                .toDate(to == null? this.toDate : to)
                .build();
    }

    public PriceHistory toFromDate(PriceHistory originPriceHistory) {
        return originPriceHistory
                .toBuilder()
                .fromDate(this.toDate.plusDays(1))
                .build();
    }

    public PriceHistory toToDate(PriceHistory originPriceHistory) {
        return originPriceHistory
                .toBuilder()
                .toDate(this.fromDate.minusDays(1))
                .build();
    }

    public PriceHistory toNewFromDate(PriceHistory originPriceHistory) {
        return PriceHistory.builder()
                .fromDate(this.toDate.plusDays(1))
                .toDate(originPriceHistory.getToDate())
                .amount(originPriceHistory.getAmount())
                .roomId(originPriceHistory.getRoomId())
                .type(originPriceHistory.getType())
                .dayType(originPriceHistory.getDayType())
                .build();
    }



    @Component
    @RequiredArgsConstructor
    public static class Validator extends BaseValidator<HouseOwnerUpdateRoomAmountRequest> {
        private final AccommodationRepository accommodationRepository;
        private final RoomRepository roomRepository;
        private  final ContractRepository contractRepository;
        @Override
        protected void validate() {
            this.rejectIfEmpty(Fields.roomId, this::validateId);
            this.rejectIfEmpty(Fields.fromDate, this::validateFromDate);
            this.rejectIfEmpty(Fields.type, this::validateAmountType);
            this.rejectIfEmpty(Fields.amount, this::validateAmount);
        }

        private String validateId() {

            Integer houseOwnerId =  User.currentUserId()
                    .orElseThrow(() -> ApiException.badRequest("Phiên đăng nhập hết hạn. Xin vui lòng đăng nhập."));

            final Room room = this.roomRepository.findById(this.request.roomId)
                    .orElseThrow(() -> ApiException.badRequest("Không thể tìm thấy phòng với id = {}.", this.request.roomId));

            final Contract contract = this.contractRepository.findContractByCreatorId(houseOwnerId);
            if(contract == null) {
                return "Xin vui lòng đăng kí làm người cho thuê chỗ ở trước khi quản lí chỗ ở. Người dùng thuộc tài khoản công ty không được phép quản lý chỗ ở.";
            }

            if(contract.is(Contract.Status.PENDING) || contract.is(Contract.Status.REJECTED)) {
                return "Người dùng không thể quản lí chỗ ở do hợp đồng bị huỷ hoặc hợp đồng chưa được duyêt.";
            }

            final Accommodation accommodation = this.accommodationRepository
                    .findById(room.getAccommodationId())
                    .orElseThrow(() -> ApiException.badRequest("Không thể tìm thấy chỗ ở với id = {}.", room.getAccommodationId()));

            if (!Objects.equals(accommodation.getOwnerId(), houseOwnerId)) {
                return "Chỗ ở này không thuộc quyền quản lí của bạn.";
            }

            return null;
        }

        private String validateAmount() {
            if (this.request.type == PriceHistory.Type.PRICE) {
                if(this.request.amount < 100 || this.request.amount > 1000) {
                    return "Xin vui lòng nhập % giá cần thay đổi trong khoảng từ 100 đến 1000.";
                }
            }

            if (this.request.type == PriceHistory.Type.DISCOUNT
                    && (this.request.amount < 0 || this.request.amount > 100)) {
                return "Phần trăm giá giảm phải trong khoảng từ 0% đến 100%.";
            }
            return null;
        }

        private String validateFromDate() {
            if (this.request.fromDate.isBefore(LocalDate.now().plusDays(1))) {
                return "Xin vui lòng chọn ngày bắt đầu áp dụng thay đổi sau ngày hiện tại.";
            }

            if (this.request.toDate != null) {
                if (this.request.toDate.isBefore(this.request.fromDate)) {
                    return "Xin vui lòng chọn ngày kết thúc sau ngày bắt đầu.";
                }
            }

            return null;
        }

        private String validateAmountType() {
            final Set<PriceHistory.Type> allowedType = Set.of(PRICE, DISCOUNT);
            if (!allowedType.contains(this.request.getType())) {
                return MoreStrings.format("Xin vui lòng chỉ chọn một trong hai loại sau đây: {}.", allowedType);
            }
            return null;
        }
    }
}
