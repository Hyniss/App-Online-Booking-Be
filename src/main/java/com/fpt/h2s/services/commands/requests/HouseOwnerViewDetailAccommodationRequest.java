package com.fpt.h2s.services.commands.requests;


import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fpt.h2s.models.domains.BaseRequest;
import com.fpt.h2s.models.domains.BaseValidator;
import com.fpt.h2s.models.entities.Accommodation;
import com.fpt.h2s.models.entities.Contract;
import com.fpt.h2s.models.entities.User;
import com.fpt.h2s.models.exceptions.ApiException;
import com.fpt.h2s.repositories.AccommodationRepository;
import com.fpt.h2s.repositories.ContractRepository;
import lombok.*;
import lombok.experimental.FieldNameConstants;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Getter
@Builder
@FieldNameConstants
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, creatorVisibility = JsonAutoDetect.Visibility.ANY)
public class HouseOwnerViewDetailAccommodationRequest extends BaseRequest {
    @NonNull
    private Integer id;

    @Component
    @RequiredArgsConstructor
    public static class Validator extends BaseValidator<HouseOwnerViewDetailAccommodationRequest> {
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
                    .orElseThrow(() -> ApiException.badRequest("Không tìm thấy chỗ ở với id = {}.", this.request.id));

            if(!Objects.equals(accommodation.getOwnerId(), houseOwnerId)) {
                return "Chỗ ở này không thuộc quyền quản lí của bạn.";
            }

            return null;
        }
    }
}
