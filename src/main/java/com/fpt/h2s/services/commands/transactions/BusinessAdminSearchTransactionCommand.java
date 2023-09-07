package com.fpt.h2s.services.commands.transactions;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fpt.h2s.models.domains.ApiResponse;
import com.fpt.h2s.models.domains.ListResult;
import com.fpt.h2s.models.entities.Transaction;
import com.fpt.h2s.models.entities.User;
import com.fpt.h2s.models.entities.converters.JPAEnumConverter;
import com.fpt.h2s.models.exceptions.ApiException;
import com.fpt.h2s.repositories.TransactionRepository;
import com.fpt.h2s.repositories.UserRepository;
import com.fpt.h2s.services.commands.BaseCommand;
import com.fpt.h2s.services.commands.responses.BusinessAdminTransactionResponse;
import com.fpt.h2s.utilities.Criteria;
import com.fpt.h2s.utilities.SpringBeans;
import jakarta.persistence.criteria.JoinType;
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
public class BusinessAdminSearchTransactionCommand implements BaseCommand<BusinessAdminSearchTransactionCommand.BusinessOwnerSearchTransactionCommandRequest, ListResult<BusinessAdminTransactionResponse>> {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    @Override
    public ApiResponse<ListResult<BusinessAdminTransactionResponse>> execute(final BusinessAdminSearchTransactionCommand.BusinessOwnerSearchTransactionCommandRequest request) {

        Integer currentUserId = User.currentUserId().orElseThrow();
        User currentUser = this.userRepository
                .findById(currentUserId)
                .orElseThrow(() -> ApiException.badRequest("Không thể tìm thấy người dùng với id = {}.", currentUserId));
        Specification<Transaction> companyCriteria = (root, query, criteriaBuilder) ->
                criteriaBuilder.or(
                        criteriaBuilder.equal(root.join("creator", JoinType.LEFT).get("companyId"), currentUser.getCompanyId()),
                        criteriaBuilder.equal(root.join("receiver", JoinType.LEFT).get("companyId"), currentUser.getCompanyId())
                );
        final Specification<Transaction> criteria = request.criteriaList
                .stream()
                .map(BusinessAdminSearchTransactionCommand.CriteriaRequest::toSpecification)
                .reduce(Specification::and)
                .orElse(null);
        Specification<Transaction> finalCriteria = companyCriteria.and(criteria);
        final Page<Transaction> transactions = this.transactionRepository.findAll(finalCriteria, request.toPageRequest());
        return ApiResponse.success(ListResult.of(transactions).map(BusinessAdminTransactionResponse::of));
    }

    @Getter
    @Builder(toBuilder = true)
    @FieldNameConstants
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, creatorVisibility = JsonAutoDetect.Visibility.ANY)
    public static class BusinessOwnerSearchTransactionCommandRequest {
        private final List<BusinessAdminSearchTransactionCommand.CriteriaRequest> criteriaList;
        private final Integer size;

        private final Integer page;

        private final String orderBy;

        private final Boolean descending;

        public PageRequest toPageRequest() {
            final String orderBy = Optional.ofNullable(this.orderBy).orElse(Transaction.Fields.id);
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
        public static final Set<String> REQUIRED_FIELDS = Set.of(Transaction.Fields.paymentMethod ,Transaction.Fields.createdAt);
        private final String key;
        private final Criteria operation;
        private final Object[] value;

        private static Map<String, Class<? extends JPAEnumConverter<?>>> converterMap;

        static {
            BusinessAdminSearchTransactionCommand.CriteriaRequest.converterMap = Map.ofEntries();
        }

        public Specification<Transaction> toSpecification() {
            if (!BusinessAdminSearchTransactionCommand.CriteriaRequest.REQUIRED_FIELDS.contains(this.key)) {
                throw ApiException.badRequest("Key must be one of {}", BusinessAdminSearchTransactionCommand.CriteriaRequest.REQUIRED_FIELDS);
            }
            final Object[] values = this.getValue();
            return this.operation.<Transaction>value(values).apply(this.key);
        }

        public Object[] getValue() {
            if (!BusinessAdminSearchTransactionCommand.CriteriaRequest.converterMap.containsKey(this.key)) {
                return this.value;
            }
            final JPAEnumConverter<?> converter = SpringBeans.getBean(BusinessAdminSearchTransactionCommand.CriteriaRequest.converterMap.get(this.key));
            return Arrays.stream(this.value).map(v -> converter.convertToEntityAttribute((String) v)).toArray();
        }
    }
}
