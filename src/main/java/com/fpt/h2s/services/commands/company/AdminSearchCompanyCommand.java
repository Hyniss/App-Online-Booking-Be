package com.fpt.h2s.services.commands.company;

import ananta.utility.StreamEx;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fpt.h2s.models.domains.ApiResponse;
import com.fpt.h2s.models.domains.ListResult;
import com.fpt.h2s.models.domains.SearchRequest;
import com.fpt.h2s.models.entities.Company;
import com.fpt.h2s.models.entities.converters.JPAEnumConverter;
import com.fpt.h2s.models.exceptions.ApiException;
import com.fpt.h2s.repositories.CompanyRepository;
import com.fpt.h2s.repositories.UserRepository;
import com.fpt.h2s.services.commands.BaseCommand;
import com.fpt.h2s.services.commands.responses.CompanyResponse;
import com.fpt.h2s.services.commands.responses.UserResponse;
import com.fpt.h2s.utilities.Criteria;
import com.fpt.h2s.utilities.SpringBeans;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldNameConstants;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AdminSearchCompanyCommand implements BaseCommand<AdminSearchCompanyCommand.Request, ListResult<CompanyResponse>> {
    
    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;
    
    @Override
    public ApiResponse<ListResult<CompanyResponse>> execute(final AdminSearchCompanyCommand.Request request) {
        final Specification<Company> criteria = StreamEx.from(request.criteriaList)
            .map(AdminSearchCompanyCommand.CriteriaRequest::toSpecification)
            .reduce(Specification::and)
            .orElse(null);
        
        final Page<Company> users = this.companyRepository.findAll(criteria, request.toPageRequest());
        final Map<Integer, UserResponse> ownerMapToId = this.userRepository.findAllByIdsIn(users.getContent(), Company::getOwnerId, UserResponse::of);
        return ApiResponse.success(ListResult.of(users).map(company -> CompanyResponse.of(company).withOwner(ownerMapToId.get(company.getOwnerId()))));
    }
    
    @Getter
    @Builder(toBuilder = true)
    @FieldNameConstants
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, creatorVisibility = JsonAutoDetect.Visibility.ANY)
    public static class Request extends SearchRequest {
        private final List<AdminSearchCompanyCommand.CriteriaRequest> criteriaList;
        
        private final Integer size;
        
        private final Integer page;
        
        private final String orderBy;
        
        private final Boolean isDescending;
    }
    
    
    @Getter
    @Builder
    @FieldNameConstants
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, creatorVisibility = JsonAutoDetect.Visibility.ANY)
    public static class CriteriaRequest {
        public static final Set<String> REQUIRED_FIELDS = Set.of(
            Company.Fields.name, Company.Fields.size, Company.Fields.id, Company.Fields.shortName,
            Company.Fields.address, Company.Fields.status, Company.Fields.contact, Company.Fields.contactName, Company.Fields.createdAt
        );
        private final String key;
        private final Criteria operation;
        private final Object[] value;
        
        private static Map<String, Class<? extends JPAEnumConverter<?>>> converterMap;
        
        static {
            AdminSearchCompanyCommand.CriteriaRequest.converterMap = Map.ofEntries(
                Map.entry(Company.Fields.size, Company.Size.Converter.class),
                Map.entry(Company.Fields.status, Company.Status.Converter.class)
            );
        }
        
        public Specification<Company> toSpecification() {
            if (!AdminSearchCompanyCommand.CriteriaRequest.REQUIRED_FIELDS.contains(this.key)) {
                throw ApiException.badRequest("Key must be one of {}", AdminSearchCompanyCommand.CriteriaRequest.REQUIRED_FIELDS);
            }
            final Object[] values = this.getValue();
            return this.operation.<Company>value(values).apply(this.key);
        }
        
        public Object[] getValue() {
            if (!AdminSearchCompanyCommand.CriteriaRequest.converterMap.containsKey(this.key)) {
                return this.value;
            }
            final JPAEnumConverter<?> converter = SpringBeans.getBean(AdminSearchCompanyCommand.CriteriaRequest.converterMap.get(this.key));
            return Arrays.stream(this.value).map(v -> converter.convertToEntityAttribute((String) v)).toArray();
        }
    }
}
