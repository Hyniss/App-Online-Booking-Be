package com.fpt.h2s.services.commands.accommodation;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fpt.h2s.models.domains.ApiResponse;
import com.fpt.h2s.models.domains.ListResult;
import com.fpt.h2s.models.entities.Accommodation;
import com.fpt.h2s.models.entities.Contract;
import com.fpt.h2s.models.entities.User;
import com.fpt.h2s.models.entities.converters.JPAEnumConverter;
import com.fpt.h2s.models.exceptions.ApiException;
import com.fpt.h2s.repositories.AccommodationRepository;
import com.fpt.h2s.repositories.ContractRepository;
import com.fpt.h2s.services.commands.BaseCommand;
import com.fpt.h2s.services.commands.responses.HouseOwnerAccommodationResponse;
import com.fpt.h2s.utilities.Criteria;
import com.fpt.h2s.utilities.SpringBeans;
import lombok.*;
import lombok.experimental.FieldNameConstants;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class HouseOwnerSearchAccommodationCommand implements
        BaseCommand<HouseOwnerSearchAccommodationCommand.HouseOwnerSearchAccommodationCommandRequest, ListResult<HouseOwnerAccommodationResponse>> {

    private final AccommodationRepository accommodationRepository;
    private final ContractRepository contractRepository;

    @Override
    public ApiResponse<ListResult<HouseOwnerAccommodationResponse>> execute(final HouseOwnerSearchAccommodationCommand
            .HouseOwnerSearchAccommodationCommandRequest request) {

        Integer currentUserId = User.currentUserId()
                .orElseThrow(() -> ApiException.badRequest("Phiên đăng nhập hết hạn. Xin vui lòng đăng nhập"));

        final Contract contract = this.contractRepository.findContractByCreatorId(currentUserId);

        if(contract == null) {
            return ApiResponse.badRequest("Xin vui lòng đăng kí làm người cho thuê chỗ ở trước khi quản lí chỗ ở. Người dùng thuộc tài khoản công ty không được phép quản lý chỗ ở.");
        }

        if(contract.is(Contract.Status.PENDING) || contract.is(Contract.Status.REJECTED)) {
            return ApiResponse.badRequest("Người dùng không thể quản lí chỗ ở do hợp đồng bị huỷ hoặc hợp đồng chưa được duyêt.");
        }

        Specification<Accommodation> ownerSpecification = (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("owner").get("id"), currentUserId);

        final Specification<Accommodation> criteria = request.criteriaList
                .stream()
                .map(HouseOwnerSearchAccommodationCommand.CriteriaRequest::toSpecification)
                .reduce(Specification::and)
                .orElse(null);

        Specification<Accommodation> finalSpecification = ownerSpecification.and(criteria);
        final Page<Accommodation> accommodations = this.accommodationRepository
                .findAll(finalSpecification, request.toPageRequest());
        return ApiResponse.success(ListResult.of(accommodations).map(HouseOwnerAccommodationResponse::of));
    }

    @Getter
    @Builder(toBuilder = true)
    @FieldNameConstants
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, creatorVisibility = JsonAutoDetect.Visibility.ANY)
    public static class HouseOwnerSearchAccommodationCommandRequest {
        private final List<HouseOwnerSearchAccommodationCommand.CriteriaRequest> criteriaList;
        private final Integer size;

        private final Integer page;

        private final String orderBy;

        private final Boolean descending;

        public PageRequest toPageRequest() {
            final String orderBy = Optional.ofNullable(this.orderBy).orElse(Accommodation.Fields.id);
            final int page = Optional.ofNullable(this.page).orElse(1);
            final int size = Optional.ofNullable(this.size).orElse(10);
            final Boolean descending = Optional.ofNullable(this.descending).orElse(true);
            if (Objects.equals(orderBy, Accommodation.Fields.id)) {
                return PageRequest.of(page - 1, size);
            }
            final Sort.Direction direction = descending ? Sort.Direction.DESC : Sort.Direction.ASC;
            return PageRequest.of(page - 1, size, direction, orderBy);
        }
    }


    @Getter
    @Builder
    @FieldNameConstants
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, creatorVisibility = JsonAutoDetect.Visibility.ANY)
    public static class CriteriaRequest {
        //input -fields
        public static final Set<String> REQUIRED_FIELDS =
                Set.of(Accommodation.Fields.name,
                Accommodation.Fields.address,
                Accommodation.Fields.type,
                Accommodation.Fields.status,
                Accommodation.Fields.createdAt);

        private final String key;
        private final Criteria operation;
        private final Object[] value;

        private static Map<String, Class<? extends JPAEnumConverter<?>>> converterMap;

        static {
            HouseOwnerSearchAccommodationCommand.CriteriaRequest.converterMap = Map.ofEntries(
                    Map.entry(Accommodation.Fields.status, Accommodation.Status.Converter.class),
                    Map.entry(Accommodation.Fields.type, Accommodation.Type.Converter.class)
            );
        }

        public Specification<Accommodation> toSpecification() {
            if (!HouseOwnerSearchAccommodationCommand.CriteriaRequest.REQUIRED_FIELDS.contains(this.key)) {
                throw ApiException.badRequest("Key must be one of {}", HouseOwnerSearchAccommodationCommand.CriteriaRequest.REQUIRED_FIELDS);
            }
            final Object[] values = this.getValue();
            return this.operation.<Accommodation>value(values).apply(this.key);
        }

        public Object[] getValue() {
            if (!HouseOwnerSearchAccommodationCommand.CriteriaRequest.converterMap.containsKey(this.key)) {
                return this.value;
            }
            final JPAEnumConverter<?> converter = SpringBeans.getBean(HouseOwnerSearchAccommodationCommand.CriteriaRequest.converterMap.get(this.key));
            return Arrays.stream(this.value).map(v -> converter.convertToEntityAttribute((String) v)).toArray();
        }
    }
}
