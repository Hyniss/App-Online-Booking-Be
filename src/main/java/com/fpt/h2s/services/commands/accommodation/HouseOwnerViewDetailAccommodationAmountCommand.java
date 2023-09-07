package com.fpt.h2s.services.commands.accommodation;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fpt.h2s.models.domains.ApiResponse;
import com.fpt.h2s.models.domains.BaseRequest;
import com.fpt.h2s.models.domains.BaseValidator;
import com.fpt.h2s.models.entities.Accommodation;
import com.fpt.h2s.models.entities.Contract;
import com.fpt.h2s.models.entities.User;
import com.fpt.h2s.models.exceptions.ApiException;
import com.fpt.h2s.repositories.AccommodationRepository;
import com.fpt.h2s.repositories.ContractRepository;
import com.fpt.h2s.repositories.PriceHistoryRepository;
import com.fpt.h2s.services.commands.BaseCommand;
import com.fpt.h2s.services.commands.responses.HouseOwnerViewDetailAccommodationAmountResponse;
import lombok.*;
import lombok.experimental.FieldNameConstants;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HouseOwnerViewDetailAccommodationAmountCommand implements
        BaseCommand<HouseOwnerViewDetailAccommodationAmountCommand.Request, Map<Integer, List<HouseOwnerViewDetailAccommodationAmountResponse>>> {

    private final PriceHistoryRepository priceHistoryRepository;
    @Override
    public ApiResponse<Map<Integer, List<HouseOwnerViewDetailAccommodationAmountResponse>>> execute(final Request request) {

        Map<Integer, List<HouseOwnerViewDetailAccommodationAmountResponse>> results =
                this.priceHistoryRepository.getAmountByDate(request.getId(), request.getDate())
                        .stream()
                        .map(HouseOwnerViewDetailAccommodationAmountResponse::of)
                        .collect(Collectors.groupingBy(HouseOwnerViewDetailAccommodationAmountResponse::getRoomId));
        return ApiResponse.success(results);
    }

    @Getter
    @Builder
    @FieldNameConstants
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, creatorVisibility = JsonAutoDetect.Visibility.ANY)
    public static class Request extends BaseRequest {
        @NonNull
        private Integer id;

        @NonNull
        private LocalDate date;

        @Component
        @RequiredArgsConstructor
        public static class Validator extends BaseValidator<Request> {
            private final AccommodationRepository accommodationRepository;
            private final ContractRepository contractRepository;

            @Override
            protected void validate() {
                this.rejectIfEmpty(Fields.id, this::validateAccommodationId);
            }

            private String validateAccommodationId() {

                Integer houseOwnerId =  User.currentUserId()
                        .orElseThrow(() -> ApiException.badRequest("Phiên đăng nhập hết hạn. Xin vui lòng đăng nhập."));

                final Contract contract = this.contractRepository.findContractByCreatorId(houseOwnerId);
                if(contract == null) {
                    return "Xin vui lòng đăng kí làm người cho thuê chỗ ở trước khi quản lí chỗ ở. Người dùng thuộc tài khoản công ty không được phép quản lý chỗ ở.";
                }

                if(contract.is(Contract.Status.PENDING) || contract.is(Contract.Status.REJECTED)) {
                    return "Người dùng không thể quản lí chỗ ở do hợp đồng bị huỷ hoặc hợp đồng chưa được duyêt.";
                }

                final Accommodation accommodation = this.accommodationRepository
                        .findById(this.request.id)
                        .orElseThrow(() -> ApiException.badRequest("Không thể tìm thấy chỗ ở với id = {}.", this.request.id));

                if(!Objects.equals(accommodation.getOwnerId(), houseOwnerId)) {
                    return "Chỗ ở này không thuộc quyền quản lí của bạn.";
                }

                return null;
            }
        }
    }
}
