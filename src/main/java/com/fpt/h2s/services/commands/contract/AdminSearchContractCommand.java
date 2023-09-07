package com.fpt.h2s.services.commands.contract;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fpt.h2s.models.domains.ApiResponse;
import com.fpt.h2s.models.domains.ListResult;
import com.fpt.h2s.models.entities.Contract;
import com.fpt.h2s.models.entities.converters.JPAEnumConverter;
import com.fpt.h2s.models.exceptions.ApiException;
import com.fpt.h2s.repositories.ContractRepository;
import com.fpt.h2s.services.commands.BaseCommand;
import com.fpt.h2s.services.commands.responses.AdminContractResponse;
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
public class AdminSearchContractCommand implements BaseCommand<AdminSearchContractCommand.AdminSearchContractCommandRequest, ListResult<AdminContractResponse>> {

    private final ContractRepository contractRepository;
    @Override
    public ApiResponse<ListResult<AdminContractResponse>> execute(final AdminSearchContractCommand.AdminSearchContractCommandRequest request) {

        final Specification<Contract> criteria = request.criteriaList
            .stream()
            .map(AdminSearchContractCommand.CriteriaRequest::toSpecification)
            .reduce(Specification::and)
            .orElse(null);

        final Page<Contract> contracts = this.contractRepository.findAll(criteria, request.toPageRequest());
        return ApiResponse.success(ListResult.of(contracts).map(AdminContractResponse::of));
    }

    @Getter
    @Builder(toBuilder = true)
    @FieldNameConstants
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, creatorVisibility = JsonAutoDetect.Visibility.ANY)
    public static class AdminSearchContractCommandRequest {
        private final List<AdminSearchContractCommand.CriteriaRequest> criteriaList;
        private final Integer size;

        private final Integer page;

        private final String orderBy;

        private final Boolean descending;

        public PageRequest toPageRequest() {
            final String orderBy = Optional.ofNullable(this.orderBy).orElse(Contract.Fields.id);
            final int page = Optional.ofNullable(this.page).orElse(1);
            final int size = Optional.ofNullable(this.size).orElse(10);
            final Boolean descending = Optional.ofNullable(this.descending).orElse(true);
            if (Objects.equals(orderBy, Contract.Fields.id)) {
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
        public static final Set<String> REQUIRED_FIELDS = Set.of(Contract.Fields.name, Contract.Fields.status, Contract.Fields.createdAt);
        private final String key;
        private final Criteria operation;
        private final Object[] value;

        private static Map<String, Class<? extends JPAEnumConverter<?>>> converterMap;

        static {
            AdminSearchContractCommand.CriteriaRequest.converterMap = Map.ofEntries(
                Map.entry(Contract.Fields.status, Contract.Status.Converter.class)
            );
        }

        public Specification<Contract> toSpecification() {
            if (!AdminSearchContractCommand.CriteriaRequest.REQUIRED_FIELDS.contains(this.key)) {
                throw ApiException.badRequest("Key must be one of {}", AdminSearchContractCommand.CriteriaRequest.REQUIRED_FIELDS);
            }
            final Object[] values = this.getValue();
            return this.operation.<Contract>value(values).apply(this.key);
        }

        public Object[] getValue() {
            if (!AdminSearchContractCommand.CriteriaRequest.converterMap.containsKey(this.key)) {
                return this.value;
            }
            final JPAEnumConverter<?> converter = SpringBeans.getBean(AdminSearchContractCommand.CriteriaRequest.converterMap.get(this.key));
            return Arrays.stream(this.value).map(v -> converter.convertToEntityAttribute((String) v)).toArray();
        }
    }
}
