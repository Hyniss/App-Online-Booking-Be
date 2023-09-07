package com.fpt.h2s.services.commands.travelstatement;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fpt.h2s.models.domains.ApiResponse;
import com.fpt.h2s.models.domains.ListResult;
import com.fpt.h2s.models.entities.TravelStatement;
import com.fpt.h2s.models.entities.User;
import com.fpt.h2s.models.entities.converters.JPAEnumConverter;
import com.fpt.h2s.models.exceptions.ApiException;
import com.fpt.h2s.repositories.TravelStatementRepository;
import com.fpt.h2s.repositories.UserRepository;
import com.fpt.h2s.services.commands.BaseCommand;
import com.fpt.h2s.services.commands.responses.TravelStatementResponse;
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
public class BusinessAdminSearchTravelStatementCommand implements BaseCommand<BusinessAdminSearchTravelStatementCommand.BusinessAdminSearchTravelStatementCommandRequest, ListResult<TravelStatementResponse>> {
    private final TravelStatementRepository travelStatementRepository;

    private final UserRepository userRepository;

    @Override
    public ApiResponse<ListResult<TravelStatementResponse>> execute(final BusinessAdminSearchTravelStatementCommand.BusinessAdminSearchTravelStatementCommandRequest request) {

        Integer currentUserId = User.currentUserId()
                .orElseThrow();

        User currentUser = this.userRepository
                .findById(currentUserId)
                .orElseThrow(() -> ApiException.badRequest("Không thể tìm thấy người dùng với id = {}.", currentUserId));

        Specification<TravelStatement> companyCriteria = (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("user").get("companyId"), currentUser.getCompanyId());

        final Specification<TravelStatement> criteria = request.criteriaList
                .stream()
                .map(BusinessAdminSearchTravelStatementCommand.CriteriaRequest::toSpecification)
                .reduce(Specification::and)
                .orElse(null);

        Specification<TravelStatement> finalCriteria = companyCriteria.and(criteria);
        final Page<TravelStatement> travelStatements = this.travelStatementRepository.findAll(finalCriteria, request.toPageRequest());
        return ApiResponse.success(ListResult.of(travelStatements).map(TravelStatementResponse::of));
    }

    @Getter
    @Builder(toBuilder = true)
    @FieldNameConstants
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, creatorVisibility = JsonAutoDetect.Visibility.ANY)
    public static class BusinessAdminSearchTravelStatementCommandRequest {
        private final List<BusinessAdminSearchTravelStatementCommand.CriteriaRequest> criteriaList;
        private final Integer size;

        private final Integer page;

        private final String orderBy;

        private final Boolean descending;

        public PageRequest toPageRequest() {
            final String orderBy = Optional.ofNullable(this.orderBy).orElse(TravelStatement.Fields.id);
            final int page = Optional.ofNullable(this.page).orElse(1);
            final int size = Optional.ofNullable(this.size).orElse(10);
            final Boolean descending = Optional.ofNullable(this.descending).orElse(true);
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
        public static final Set<String> REQUIRED_FIELDS = Set.of(TravelStatement.Fields.name, TravelStatement.Fields.status, TravelStatement.Fields.numberOfPeople, TravelStatement.Fields.location, TravelStatement.Fields.createdAt);
        private final String key;
        private final Criteria operation;
        private final Object[] value;

        private static Map<String, Class<? extends JPAEnumConverter<?>>> converterMap;

        static {
            BusinessAdminSearchTravelStatementCommand.CriteriaRequest.converterMap = Map.ofEntries(
                    Map.entry(TravelStatement.Fields.status, TravelStatement.Status.Converter.class)
            );
        }

        public Specification<TravelStatement> toSpecification() {
            if (!BusinessAdminSearchTravelStatementCommand.CriteriaRequest.REQUIRED_FIELDS.contains(this.key)) {
                throw ApiException.badRequest("Key must be one of {}", BusinessAdminSearchTravelStatementCommand.CriteriaRequest.REQUIRED_FIELDS);
            }
            final Object[] values = this.getValue();
            return this.operation.<TravelStatement>value(values).apply(this.key);
        }

        public Object[] getValue() {
            if (!BusinessAdminSearchTravelStatementCommand.CriteriaRequest.converterMap.containsKey(this.key)) {
                return this.value;
            }
            final JPAEnumConverter<?> converter = SpringBeans.getBean(BusinessAdminSearchTravelStatementCommand.CriteriaRequest.converterMap.get(this.key));
            return Arrays.stream(this.value).map(v -> converter.convertToEntityAttribute((String) v)).toArray();
        }
    }
}
