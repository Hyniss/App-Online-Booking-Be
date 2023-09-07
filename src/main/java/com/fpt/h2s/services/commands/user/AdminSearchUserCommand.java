package com.fpt.h2s.services.commands.user;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fpt.h2s.models.domains.ApiResponse;
import com.fpt.h2s.models.domains.ListResult;
import com.fpt.h2s.models.domains.SearchRequest;
import com.fpt.h2s.models.entities.User;
import com.fpt.h2s.models.entities.converters.JPAEnumConverter;
import com.fpt.h2s.models.exceptions.ApiException;
import com.fpt.h2s.repositories.UserRepository;
import com.fpt.h2s.services.commands.BaseCommand;
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
public class AdminSearchUserCommand implements BaseCommand<AdminSearchUserCommand.Request, ListResult<UserResponse>> {
    
    private final UserRepository userRepository;
    
    @Override
    public ApiResponse<ListResult<UserResponse>> execute(final Request request) {
        final Specification<User> criteria = request.criteriaList
            .stream()
            .map(CriteriaRequest::toSpecification)
            .reduce(Specification::and)
            .orElse(null);
        
        final Page<User> users = this.userRepository.findAll(criteria, request.toPageRequest());
        return ApiResponse.success(ListResult.of(users).map(UserResponse::of));
    }
    
    @Getter
    @Builder(toBuilder = true)
    @FieldNameConstants
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, creatorVisibility = JsonAutoDetect.Visibility.ANY)
    public static class Request extends SearchRequest {
        private final List<CriteriaRequest> criteriaList;
        
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
        public static final Set<String> REQUIRED_FIELDS = Set.of(User.Fields.email, User.Fields.username, User.Fields.phone, User.Fields.roles, User.Fields.status, User.Fields.createdAt);
        private final String key;
        private final Criteria operation;
        private final Object[] value;
        
        private static Map<String, Class<? extends JPAEnumConverter<?>>> converterMap;
        
        static {
            CriteriaRequest.converterMap = Map.ofEntries(
                Map.entry(User.Fields.status, User.Status.Converter.class)
            );
        }
        
        public Specification<User> toSpecification() {
            if (!CriteriaRequest.REQUIRED_FIELDS.contains(this.key)) {
                throw ApiException.badRequest("Key must be one of {}", CriteriaRequest.REQUIRED_FIELDS);
            }
            final Object[] values = this.getValue();
            return this.operation.<User>value(values).apply(this.key);
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
