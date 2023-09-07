package com.fpt.h2s.services.commands.requests;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fpt.h2s.models.domains.BaseRequest;
import com.fpt.h2s.models.domains.BaseValidator;
import com.fpt.h2s.models.entities.Accommodation;
import com.fpt.h2s.models.exceptions.ApiException;
import com.fpt.h2s.repositories.AccommodationRepository;
import lombok.*;
import lombok.experimental.FieldNameConstants;
import org.springframework.stereotype.Component;

@Getter
@Builder
@FieldNameConstants
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, creatorVisibility = JsonAutoDetect.Visibility.ANY)
public class ChangeStatusAccommodationRequest extends BaseRequest {
    @NonNull
    private final Integer id;
    @NonNull
    private final Accommodation.Status status;

    @Component
    @RequiredArgsConstructor
    public static class Validator extends BaseValidator<ChangeStatusAccommodationRequest> {
        private final AccommodationRepository accommodationRepository;

        @Override
        protected void validate() {
            this.rejectIfEmpty(ChangeStatusAccommodationRequest.Fields.id, this::validateAccommodationId);
        }

        private String validateAccommodationId() {

            Accommodation accommodationToChangeStatus = this.accommodationRepository
                    .findById(this.request.id)
                    .orElseThrow(() -> ApiException.badRequest("Không tìm thấy chỗ ở với id = {}.", this.request.id));

            return null;
        }

    }
}
