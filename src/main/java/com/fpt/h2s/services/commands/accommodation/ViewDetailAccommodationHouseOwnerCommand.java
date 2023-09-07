package com.fpt.h2s.services.commands.accommodation;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fpt.h2s.models.domains.ApiResponse;
import com.fpt.h2s.models.domains.BaseRequest;
import com.fpt.h2s.models.domains.BaseValidator;
import com.fpt.h2s.models.entities.Accommodation;
import com.fpt.h2s.models.entities.Contract;
import com.fpt.h2s.models.exceptions.ApiException;
import com.fpt.h2s.repositories.AccommodationRepository;
import com.fpt.h2s.repositories.ContractRepository;
import com.fpt.h2s.services.commands.BaseCommand;
import com.fpt.h2s.services.commands.responses.DetailAccommodationHouseOwnerResponse;
import lombok.*;
import lombok.experimental.FieldNameConstants;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ViewDetailAccommodationHouseOwnerCommand
        implements BaseCommand<ViewDetailAccommodationHouseOwnerCommand.DetailAccommodationHouseOwnerRequest, DetailAccommodationHouseOwnerResponse> {

    private final AccommodationRepository accommodationRepository;
    private final ContractRepository contractRepository;
    @Override
    public ApiResponse<DetailAccommodationHouseOwnerResponse> execute (final DetailAccommodationHouseOwnerRequest request) {


        final Accommodation accommodation = this.accommodationRepository
                .findById(request.getId())
                .orElseThrow(() -> ApiException.badRequest("Không thể tìm thấy chỗ ở với id = {}.", request.getId()));
        Contract contract = this.contractRepository.findContractByCreatorId(accommodation.getOwnerId());
        if(contract == null || contract.is(Contract.Status.PENDING)) {
            return ApiResponse.badRequest("Không thể tìm thấy hợp đồng của chủ nhà này.");
        }
        return ApiResponse.success(DetailAccommodationHouseOwnerResponse.of(accommodation, contract));
    }

    @Getter
    @Builder
    @FieldNameConstants
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, creatorVisibility = JsonAutoDetect.Visibility.ANY)
    public static class DetailAccommodationHouseOwnerRequest extends BaseRequest {

        @NonNull
        private Integer id;

        @Component
        @RequiredArgsConstructor
        public static class Validator extends BaseValidator<DetailAccommodationHouseOwnerRequest> {
            private final AccommodationRepository accommodationRepository;

            @Override
            protected void validate() {
                this.rejectIfEmpty(ViewDetailAccommodationReviewCommand.Request.Fields.id, this::validateAccommodationId);
            }

            private String validateAccommodationId() {
                final Accommodation accommodation = this.accommodationRepository
                        .findById(this.request.id)
                        .orElseThrow(() -> ApiException.badRequest("Không thể tìm thấy chỗ ở với id = {}.", this.request.id));

                if (accommodation.getStatus() != Accommodation.Status.OPENING) {
                    return "Chỗ ở này hiện tại chưa được mở cho thuê phòng.";
                }
                return null;
            }
        }
    }
}
