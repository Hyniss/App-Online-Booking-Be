package com.fpt.h2s.services.commands.boookingrequest;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fpt.h2s.models.domains.ApiResponse;
import com.fpt.h2s.models.domains.ListResult;
import com.fpt.h2s.models.entities.BookingRequest;
import com.fpt.h2s.models.entities.User;
import com.fpt.h2s.models.entities.converters.JPAEnumConverter;
import com.fpt.h2s.models.exceptions.ApiException;
import com.fpt.h2s.repositories.BookingRequestRepository;
import com.fpt.h2s.repositories.UserRepository;
import com.fpt.h2s.services.commands.BaseCommand;
import com.fpt.h2s.services.commands.responses.BookingRequestResponse;
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
public class SearchBookingRequestCommand implements BaseCommand<SearchBookingRequestCommand.SearchBookingRequestCommandRequest, ListResult<BookingRequestResponse>> {
    private final BookingRequestRepository bookingRequestRepository;

    private final UserRepository userRepository;

    @Override
    public ApiResponse<ListResult<BookingRequestResponse>> execute(final SearchBookingRequestCommand.SearchBookingRequestCommandRequest request) {

        Integer currentUserId = User.currentUserId()
                .orElseThrow();

        User currentUser = this.userRepository
                .findById(currentUserId)
                .orElseThrow(() -> ApiException.badRequest("Không thể tìm thấy người dùng với id = {}.", currentUserId));

        Specification<BookingRequest> ownerCriteria = (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("userId"), currentUserId);

        final Specification<BookingRequest> criteria = request.criteriaList
                .stream()
                .map(SearchBookingRequestCommand.CriteriaRequest::toSpecification)
                .reduce(Specification::and)
                .orElse(null);

        Specification<BookingRequest> finalCriteria = ownerCriteria.and(criteria).and((root, query, criteriaBuilder) ->
                criteriaBuilder.and(
                        criteriaBuilder.notEqual(root.get("status"), BookingRequest.Status.PENDING),
                        criteriaBuilder.notEqual(root.get("status"), BookingRequest.Status.UN_PURCHASED)
                )
        );

        final Page<BookingRequest> bookingRequests = this.bookingRequestRepository.findAll(finalCriteria, request.toPageRequest());
        return ApiResponse.success(ListResult.of(bookingRequests).map(BookingRequestResponse::of));
    }

    @Getter
    @Builder(toBuilder = true)
    @FieldNameConstants
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, creatorVisibility = JsonAutoDetect.Visibility.ANY)
    public static class SearchBookingRequestCommandRequest {
        private final List<SearchBookingRequestCommand.CriteriaRequest> criteriaList;
        private final Integer size;

        private final Integer page;

        private final String orderBy;

        private final Boolean descending;

        public PageRequest toPageRequest() {
            final String orderBy = Optional.ofNullable(this.orderBy).orElse(BookingRequest.Fields.id);
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
        public static final Set<String> REQUIRED_FIELDS = Set.of(BookingRequest.Fields.totalRooms, BookingRequest.Fields.status, BookingRequest.Fields.createdAt, BookingRequest.Fields.checkinAt, BookingRequest.Fields.checkoutAt);
        private final String key;
        private final Criteria operation;
        private final Object[] value;

        private static Map<String, Class<? extends JPAEnumConverter<?>>> converterMap;

        static {
            SearchBookingRequestCommand.CriteriaRequest.converterMap = Map.ofEntries(
                    Map.entry(BookingRequest.Fields.status, BookingRequest.Status.Converter.class)
            );
        }

        public Specification<BookingRequest> toSpecification() {
            if (!SearchBookingRequestCommand.CriteriaRequest.REQUIRED_FIELDS.contains(this.key)) {
                throw ApiException.badRequest("Key must be one of {}", SearchBookingRequestCommand.CriteriaRequest.REQUIRED_FIELDS);
            }
            final Object[] values = this.getValue();
            return this.operation.<BookingRequest>value(values).apply(this.key);
        }

        public Object[] getValue() {
            if (!SearchBookingRequestCommand.CriteriaRequest.converterMap.containsKey(this.key)) {
                return this.value;
            }
            final JPAEnumConverter<?> converter = SpringBeans.getBean(SearchBookingRequestCommand.CriteriaRequest.converterMap.get(this.key));
            return Arrays.stream(this.value).map(v -> converter.convertToEntityAttribute((String) v)).toArray();
        }
    }
}
