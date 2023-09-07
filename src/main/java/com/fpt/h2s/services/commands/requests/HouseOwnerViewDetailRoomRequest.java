package com.fpt.h2s.services.commands.requests;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fpt.h2s.interceptors.models.MessageResolver;
import com.fpt.h2s.models.domains.BaseRequest;
import com.fpt.h2s.models.domains.BaseValidator;
import com.fpt.h2s.models.entities.Accommodation;
import com.fpt.h2s.models.entities.Contract;
import com.fpt.h2s.models.entities.Room;
import com.fpt.h2s.models.entities.User;
import com.fpt.h2s.models.exceptions.ApiException;
import com.fpt.h2s.repositories.AccommodationRepository;
import com.fpt.h2s.repositories.ContractRepository;
import com.fpt.h2s.repositories.RoomRepository;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldNameConstants;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Getter
@Builder
@FieldNameConstants
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, creatorVisibility = JsonAutoDetect.Visibility.ANY)
public class HouseOwnerViewDetailRoomRequest extends BaseRequest {

    @NotNull(message = "Xin vui lòng không để trống ID của phòng.")
    private Integer id;

    @NotNull(message = "Xin vui lòng không để trống ID của chỗ ở.")
    private Integer accommodationId;

    @Component
    @RequiredArgsConstructor
    public static class Validator extends BaseValidator<HouseOwnerViewDetailRoomRequest> {
        private final AccommodationRepository accommodationRepository;
        private final RoomRepository roomRepository;
        private final ContractRepository contractRepository;
        private final MessageResolver messageResolver;

        @Override
        protected void validate() {
            this.rejectIfEmpty(Fields.accommodationId, this::validateAccommodationId);
            this.rejectIfEmpty(Fields.id, this::validateRoomId);
        }

        private String validateRoomId() {

            final Room room = this.roomRepository
                    .findById(this.request.id)
                    .orElseThrow(() -> ApiException.badRequest("Không thể tìm thấy phòng với id = {}.", this.request.id));

            if(!Objects.equals(room.getAccommodationId(), this.request.accommodationId)) {
                return "Phòng này không thuộc về chỗ ở hiện tại.";
            }

            return null;
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
                    .findById(this.request.accommodationId)
                    .orElseThrow(() -> ApiException.badRequest("Không thể tìm thấy chỗ ở với id = {}.", this.request.accommodationId));

            if(!Objects.equals(accommodation.getOwnerId(), houseOwnerId)) {
                return "Chỗ ở này không thuộc quyền quản lí của bạn.";
            }

            return null;
        }
    }
}
