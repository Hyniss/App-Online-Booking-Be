package com.fpt.h2s.services.commands.accommodation;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fpt.h2s.models.domains.ApiResponse;
import com.fpt.h2s.models.domains.ListResult;
import com.fpt.h2s.models.entities.Accommodation;
import com.fpt.h2s.models.entities.converters.JPAEnumConverter;
import com.fpt.h2s.models.exceptions.ApiException;
import com.fpt.h2s.repositories.AccommodationRepository;
import com.fpt.h2s.services.commands.BaseCommand;
import com.fpt.h2s.services.commands.responses.AdminAccommodationResponse;
import com.fpt.h2s.utilities.Criteria;
import com.fpt.h2s.utilities.SpringBeans;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldNameConstants;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AdminSearchAccommodationCommand implements BaseCommand<AdminSearchAccommodationCommand.AdminSearchAccommodationCommandRequest, ListResult<AdminAccommodationResponse>> {
    private final AccommodationRepository accommodationRepository;
    
    @Override
    public ApiResponse<ListResult<AdminAccommodationResponse>> execute(final AdminSearchAccommodationCommand.AdminSearchAccommodationCommandRequest request) {
        final Specification<Accommodation> criteria = request.criteriaList
            .stream()
            .map(AdminSearchAccommodationCommand.CriteriaRequest::toSpecification)
            .reduce(Specification::and)
            .orElse(null);
        
        final Page<Accommodation> accommodations = this.accommodationRepository.findAll(criteria, request.toPageRequest());
        return ApiResponse.success(ListResult.of(accommodations).map(AdminAccommodationResponse::of));
    }
    
    @Getter
    @Builder(toBuilder = true)
    @FieldNameConstants
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, creatorVisibility = JsonAutoDetect.Visibility.ANY)
    public static class AdminSearchAccommodationCommandRequest {
        private final List<AdminSearchAccommodationCommand.CriteriaRequest> criteriaList;
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
        public static final Set<String> REQUIRED_FIELDS = Set.of(Accommodation.Fields.name, Accommodation.Fields.address, Accommodation.Fields.type, Accommodation.Fields.status, Accommodation.Fields.createdAt);
        private final String key;
        private final Criteria operation;
        private final Object[] value;
        
        private static Map<String, Class<? extends JPAEnumConverter<?>>> converterMap;
        
        static {
            CriteriaRequest.converterMap = Map.ofEntries(
                Map.entry(Accommodation.Fields.status, Accommodation.Status.Converter.class),
                Map.entry(Accommodation.Fields.type, Accommodation.Type.Converter.class)
            );
        }
        
        public Specification<Accommodation> toSpecification() {
            if (!CriteriaRequest.REQUIRED_FIELDS.contains(this.key)) {
                throw ApiException.badRequest("Key must be one of {}", CriteriaRequest.REQUIRED_FIELDS);
            }
            final Object[] values = this.getValue();
            return this.operation.<Accommodation>value(values).apply(this.key);
        }
        
        public Object[] getValue() {
            if (!CriteriaRequest.converterMap.containsKey(this.key)) {
                return this.value;
            }
            final JPAEnumConverter<?> converter = SpringBeans.getBean(CriteriaRequest.converterMap.get(this.key));
            return Arrays.stream(this.value).map(v -> converter.convertToEntityAttribute((String) v)).toArray();
        }
    }
}
