package com.fpt.h2s.services.commands.company;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fpt.h2s.models.domains.ApiResponse;
import com.fpt.h2s.models.domains.ListResult;
import com.fpt.h2s.models.entities.Company;
import com.fpt.h2s.models.entities.User;
import com.fpt.h2s.models.entities.converters.JPAEnumConverter;
import com.fpt.h2s.models.exceptions.ApiException;
import com.fpt.h2s.repositories.CompanyRepository;
import com.fpt.h2s.repositories.UserRepository;
import com.fpt.h2s.services.commands.BaseCommand;
import com.fpt.h2s.services.commands.responses.BusinessAdminSearchBusinessMemberResponse;
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
public class BusinessAdminSearchBusinessUserCommand implements
        BaseCommand<BusinessAdminSearchBusinessUserCommand.Request, ListResult<BusinessAdminSearchBusinessMemberResponse>> {

    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;

    @Override
    public ApiResponse<ListResult<BusinessAdminSearchBusinessMemberResponse>> execute(final Request request) {
        final Company company = this.companyRepository.findById(getCompanyId()).orElseThrow();
        if(company.getStatus() != Company.Status.ACTIVE) {
            return ApiResponse.badRequest("Công ty của bạn chưa được kích hoạt để thêm thành viên.");
        }

        Specification<User> ownerSpecification = (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("companyId"), company.getId());

        final Specification<User> criteria = request.criteriaList
                .stream()
                .map(BusinessAdminSearchBusinessUserCommand.CriteriaRequest::toSpecification)
                .reduce(Specification::and)
                .orElse(null);

        Specification<User> finalCriteria = ownerSpecification.and(criteria);
        final Page<User> businessUsers = this.userRepository.findAll(finalCriteria, request.toPageRequest());
        return ApiResponse.success(ListResult.of(businessUsers).map(BusinessAdminSearchBusinessMemberResponse::of));
    }

    @Getter
    @Builder(toBuilder = true)
    @FieldNameConstants
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, creatorVisibility = JsonAutoDetect.Visibility.ANY)
    public static class Request {

        private final List<CriteriaRequest> criteriaList;

        private final Integer size;

        private final Integer page;

        private final String orderBy;

        private final Boolean descending;

        public PageRequest toPageRequest() {
            final String orderBy = Optional.ofNullable(this.orderBy).orElse(User.Fields.id);
            final int page = Optional.ofNullable(this.page).orElse(1);
            final int size = Optional.ofNullable(this.size).orElse(10);
            final Boolean descending = Optional.ofNullable(this.descending).orElse(true);
            if (Objects.equals(orderBy, User.Fields.id)) {
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
        public static final Set<String> REQUIRED_FIELDS = Set.of(
                User.Fields.email,
                User.Fields.phone,
                User.Fields.username,
                User.Fields.status,
                User.Fields.roles
        );
        private final String key;
        private final Criteria operation;
        private final Object[] value;

        private static Map<String, Class<? extends JPAEnumConverter<?>>> converterMap;

        static {
            BusinessAdminSearchBusinessUserCommand.CriteriaRequest.converterMap = Map.ofEntries(
                    Map.entry(User.Fields.status, User.Status.Converter.class)
            );
        }

        public Specification<User> toSpecification() {
            if (!BusinessAdminSearchBusinessUserCommand.CriteriaRequest.REQUIRED_FIELDS.contains(this.key)) {
                throw ApiException.badRequest("Key must be one of {}", BusinessAdminSearchBusinessUserCommand.CriteriaRequest.REQUIRED_FIELDS);
            }
            final Object[] values = this.getValue();
            return this.operation.<User>value(values).apply(this.key);
        }


        public Object[] getValue() {
            if (!BusinessAdminSearchBusinessUserCommand.CriteriaRequest.converterMap.containsKey(this.key)) {
                return this.value;
            }
            final JPAEnumConverter<?> converter = SpringBeans.getBean(BusinessAdminSearchBusinessUserCommand.CriteriaRequest.converterMap.get(this.key));
            return Arrays.stream(this.value).map(v -> converter.convertToEntityAttribute((String) v)).toArray();
        }
    }

    private Integer getCompanyId() {
        Integer currentUserId = User.currentUserId()
                .orElseThrow(() -> ApiException.badRequest("Phiên đăng nhập hết hạn. Xin vui lòng đăng nhập."));
        final User user = this.userRepository.findById(currentUserId)
                .orElseThrow(() -> ApiException.badRequest("Không thể tìm thấy người dùng với id = {}.", currentUserId));
        return user.getCompanyId();
    }
}
