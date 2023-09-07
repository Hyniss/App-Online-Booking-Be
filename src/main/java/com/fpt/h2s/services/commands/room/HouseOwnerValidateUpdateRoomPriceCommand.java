package com.fpt.h2s.services.commands.room;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fpt.h2s.models.domains.ApiResponse;
import com.fpt.h2s.models.domains.BaseRequest;
import com.fpt.h2s.models.domains.BaseValidator;
import com.fpt.h2s.models.entities.*;
import com.fpt.h2s.models.exceptions.ApiException;
import com.fpt.h2s.repositories.AccommodationRepository;
import com.fpt.h2s.repositories.ContractRepository;
import com.fpt.h2s.repositories.PriceHistoryRepository;
import com.fpt.h2s.repositories.RoomRepository;
import com.fpt.h2s.services.commands.BaseCommand;
import com.fpt.h2s.utilities.MoreStrings;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldNameConstants;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static com.fpt.h2s.models.entities.PriceHistory.Type.DISCOUNT;
import static com.fpt.h2s.models.entities.PriceHistory.Type.PRICE;

@Service
@RequiredArgsConstructor
public class HouseOwnerValidateUpdateRoomPriceCommand
        implements BaseCommand<HouseOwnerValidateUpdateRoomPriceCommand.Request, Void> {

    private final PriceHistoryRepository priceHistoryRepository;

    @Override
    public ApiResponse<Void> execute(Request request) {
        if(updatePriceHistoryWhenFromDateEqual(request) != null) {
            return ApiResponse.badRequest(updatePriceHistoryWhenFromDateEqual(request));
        }
        if(updatePriceHistoryWhenToDateEqual(request) != null) {
            return ApiResponse.badRequest(updatePriceHistoryWhenToDateEqual(request));
        }
        if(updatePriceHistoryWhenFromDateToDateInBound(request) != null) {
            return ApiResponse.badRequest(updatePriceHistoryWhenFromDateToDateInBound(request));
        }
        if(updatePriceHistoryWhenFromDateToDateOutBound(request) != null) {
            return ApiResponse.badRequest(updatePriceHistoryWhenFromDateToDateOutBound(request));
        }
        return ApiResponse.success("Không tìm thấy thời gian bị trùng trong lịch. Thông tin hợp lệ.");
    }

    private String updatePriceHistoryWhenFromDateToDateOutBound(Request request) {
        final List<PriceHistory> priceHistories = this.priceHistoryRepository
                .findByRoomIdAndTypeAndDayType(request.getRoomId(), request.getType(), PriceHistory.DayType.CUSTOM)
                .stream()
                .filter(priceHistory -> priceHistory.getToDate() != null)
                .filter(priceHistory -> priceHistory.getFromDate().isAfter(request.getFromDate())
                        && priceHistory.getToDate().isBefore(request.getToDate()))
                .toList();

        if (priceHistories.size() > 0) {
            LocalDate minFromDate = priceHistories.stream()
                    .map(PriceHistory::getFromDate)
                    .min(LocalDate::compareTo)
                    .orElse(null);

            LocalDate maxToDate = priceHistories.stream()
                    .map(PriceHistory::getToDate)
                    .max(LocalDate::compareTo)
                    .orElse(null);
            return minFromDate + "/" + maxToDate;
        }
        return null;
    }

    private String updatePriceHistoryWhenFromDateToDateInBound(final Request request) {
        final List<PriceHistory> priceHistories = this.priceHistoryRepository
                .findByRoomIdAndTypeAndDayType(request.getRoomId(), request.getType(), PriceHistory.DayType.CUSTOM)
                .stream()
                .filter(priceHistory -> priceHistory.getToDate() != null)
                .filter(priceHistory -> priceHistory.getFromDate().isBefore(request.getFromDate())
                        && priceHistory.getToDate().isAfter(request.getToDate()))
                .toList();

        if (priceHistories.size() > 0) {
            LocalDate maxFromDate = priceHistories.stream()
                    .map(PriceHistory::getFromDate)
                    .max(LocalDate::compareTo)
                    .orElse(null);

            LocalDate minToDate = priceHistories.stream()
                    .map(PriceHistory::getToDate)
                    .min(LocalDate::compareTo)
                    .orElse(null);
            return maxFromDate + "/" + minToDate;
        }
        return null;
    }

    private String updatePriceHistoryWhenFromDateEqual(final Request request) {
        final List<PriceHistory> priceHistories = this.priceHistoryRepository
                .findByRoomIdAndTypeAndDayTypeAndFromDate(request.getRoomId(), request.getType(), PriceHistory.DayType.CUSTOM, request.getFromDate());
        if(priceHistories.size() > 0) {
            LocalDate minFromDate = priceHistories.stream()
                    .map(PriceHistory::getFromDate)
                    .findFirst()
                    .orElse(null);

            LocalDate maxToDate = priceHistories.stream()
                    .map(PriceHistory::getToDate)
                    .findFirst()
                    .orElse(null);
            return minFromDate + "/" + maxToDate;
        }
        return null;
    }

    private String updatePriceHistoryWhenToDateEqual(final Request request) {
        final List<PriceHistory> priceHistories = this.priceHistoryRepository
                .findByRoomIdAndTypeAndDayTypeAndToDate(request.getRoomId(), request.getType(), PriceHistory.DayType.CUSTOM, request.getToDate());
        if(priceHistories.size() > 0) {
            LocalDate minFromDate = priceHistories.stream()
                    .map(PriceHistory::getFromDate)
                    .findFirst()
                    .orElse(null);

            LocalDate maxToDate = priceHistories.stream()
                    .map(PriceHistory::getToDate)
                    .findFirst()
                    .orElse(null);
            return minFromDate + "/" + maxToDate;
        }
        return null;
    }

    @Getter
    @Builder
    @FieldNameConstants
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, creatorVisibility = JsonAutoDetect.Visibility.ANY)
    public static class Request extends BaseRequest {

        @NotNull(message = "Xin vui lòng không để trống ID của phòng.")
        private Integer roomId;

        @NotNull(message = "Xin hãy chọn 1 trong 2 loại sau đây: PRICE (% giá) hoặc DISCOUNT(Phần trăm giá giảm).")
        private PriceHistory.Type type;

        private PriceHistory.DayType dayType;

        @NotBlank(message = "Xin hãy nhập % giá hoặc phần trăm giảm giá cần cập nhật.")
        private String amount;

        @NotNull(message = "Xin vui lòng không để trống ngày bắt đầu áp dụng thay đổi.")
        private LocalDate fromDate;

        @Nullable
        private LocalDate toDate;

        
        @Component
        @RequiredArgsConstructor
        public static class Validator extends BaseValidator<Request> {
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
                if(!this.request.amount.matches("^[0-9]+$")) {
                    return "Xin vui lòng chỉ nhập số nguyên.";
                }
                long amount = Long.parseLong(this.request.amount);
                if (this.request.type == PriceHistory.Type.PRICE) {
                    if(amount < 100 || amount > 1000) {
                        return "Xin vui lòng nhập % giá cần thay đổi trong khoảng từ 100 đến 1000.";
                    }
                }
                if (this.request.type == PriceHistory.Type.DISCOUNT
                        && (amount < 0 || amount > 100)) {
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
}
