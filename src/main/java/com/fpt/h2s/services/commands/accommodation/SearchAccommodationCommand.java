package com.fpt.h2s.services.commands.accommodation;

import ananta.utility.ListEx;
import ananta.utility.SetEx;
import ananta.utility.StreamEx;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fpt.h2s.models.domains.ApiResponse;
import com.fpt.h2s.models.domains.ListResult;
import com.fpt.h2s.models.domains.Range;
import com.fpt.h2s.models.domains.SearchRequest;
import com.fpt.h2s.models.entities.Accommodation;
import com.fpt.h2s.models.entities.Room;
import com.fpt.h2s.models.entities.RoomProperty;
import com.fpt.h2s.models.exceptions.ApiException;
import com.fpt.h2s.repositories.AccommodationRepository;
import com.fpt.h2s.repositories.RoomRepository;
import com.fpt.h2s.repositories.projections.IdHolder;
import com.fpt.h2s.services.commands.BaseCommand;
import com.fpt.h2s.services.commands.accommodation.SearchAccommodationAvailableRoomsCommand.CriteriaRequest;
import com.fpt.h2s.services.commands.responses.AccommodationResponse;
import com.fpt.h2s.utilities.FluentSearch;
import com.fpt.h2s.utilities.LocalDateTimes;
import com.fpt.h2s.utilities.MoreStrings;
import com.fpt.h2s.utilities.QueryValues;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldNameConstants;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.JpaSort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class SearchAccommodationCommand implements BaseCommand<SearchAccommodationCommand.Request, ListResult<AccommodationResponse>> {

    private static final Set<Integer> ALL_IDS = null;
    private static final Set<Integer> NO_IDS_FOUND = Collections.emptySet();

    private final AccommodationRepository accommodationRepository;
    private final RoomRepository roomRepository;
    private final AccommodationAdapter adapter;

    @Override
    public ApiResponse<ListResult<AccommodationResponse>> execute(final Request request) {
        Page<Accommodation> page = FluentSearch
            .start(() -> searchAccommodationsHavingViewsIn(request.getViewIds()))
            .then((ids) -> searchAccommodationsHavingMapLocationNear(request.getCoordinate(), ids))
            .then((ids) -> searchAccommodationsHavingAmenitiesIn(request.getAmenityIds(), ids))
            .then((ids) -> searchAccommodationsHavingRoomsMetDetails(request, ids))
            .then((ids) -> searchAccommodationsByTypeWithPaging(request.types, request.toPageRequest(), ids))
            .get();

        final ListResult<AccommodationResponse> response = ListResult.of(page).withContent(this.adapter.responsesOf(page.getContent()));
        return ApiResponse.success(response);
    }

    private Set<Integer> searchAccommodationsHavingViewsIn(List<Integer> viewIds) {
        Set<Integer> cleanViewIds = StreamEx.from(viewIds).filter(Objects::nonNull).collect(Collectors.toSet());
        if (cleanViewIds.isEmpty()) {
            return ALL_IDS;
        }
        return IdHolder.unwrapList(accommodationRepository.findAllByViewIdsIn(cleanViewIds, cleanViewIds.size()));
    }

    private Set<Integer> searchAccommodationsHavingAmenitiesIn(final List<Integer> categoryIds, Set<Integer> previousQueryFoundIds) {
        if (ListEx.isEmpty(categoryIds)) {
            return previousQueryFoundIds;
        }
        if (previousQueryFoundIds != ALL_IDS && previousQueryFoundIds.isEmpty()) {
            return NO_IDS_FOUND;
        }
        List<IdHolder> ids = this.accommodationRepository.findAllByRoomHavingAmenities(ListEx.listOf(categoryIds), categoryIds.size(), QueryValues.integerList(previousQueryFoundIds));
        return IdHolder.unwrapList(ids);
    }

    private Set<Integer> searchAccommodationsHavingMapLocationNear(Request.Coordinate coordinate, Set<Integer> previousQueryFoundIds) {
        if (previousQueryFoundIds != ALL_IDS && previousQueryFoundIds.isEmpty()) {
            return NO_IDS_FOUND;
        }

        if (coordinate == null || coordinate.lng() == null || coordinate.lat() == null || coordinate.range() == null) {
            return previousQueryFoundIds;
        }

        List<IdHolder> ids = accommodationRepository.findAccommodationsByCoordinateNear(
            coordinate.lng(),
            coordinate.lat(),
            coordinate.range(),
            QueryValues.integerList(previousQueryFoundIds)
        );
        return IdHolder.unwrapList(ids);
    }

    private Set<Integer> searchAccommodationsHavingRoomsMetDetails(Request request, Set<Integer> previousQueryFoundIds) {
        if (previousQueryFoundIds != ALL_IDS && previousQueryFoundIds.isEmpty()) {
            return NO_IDS_FOUND;
        }

        return FluentSearch
            .start(() -> searchRoomsHavingDetailsMet(request.getCriteriaList()))
            .then(roomIds -> searchAccommodationsHavingRoomsAvailableFor(request, roomIds, previousQueryFoundIds))
            .get();
    }

    private Set<Integer> searchRoomsHavingDetailsMet(List<CriteriaRequest> criteriaList) {
        final Specification<RoomProperty> criteria = StreamEx
            .from(criteriaList)
            .map(CriteriaRequest::toSpecification)
            .reduce(Specification::and)
            .orElse(null);

        if (criteria == null) {
            return ALL_IDS;
        }

        return roomRepository.getRoomsThatMeet(criteria, criteriaList.size());
    }

    private Set<Integer> searchAccommodationsHavingRoomsAvailableFor(Request request, Set<Integer> roomIds, Set<Integer> previousQueryFoundIds) {
        if (previousQueryFoundIds != ALL_IDS && previousQueryFoundIds.isEmpty()) {
            return NO_IDS_FOUND;
        }

        if (roomIds != ALL_IDS && roomIds.isEmpty()) {
            return NO_IDS_FOUND;
        }

        if (request.getDates() == null || request.getDates().getStart() == null || request.getDates().getEnd() == null) {
            if (roomIds == ALL_IDS) {
                return previousQueryFoundIds;
            }

            Set<Integer> accommodationIdsHavingRoomsMet = roomRepository.findAllById(roomIds).stream().map(Room::getAccommodationId).collect(Collectors.toSet());
            List<Integer> accommodationsThatExistingInBoth = ListEx.inBothList(ListEx.listOf(accommodationIdsHavingRoomsMet), ListEx.listOf(previousQueryFoundIds));
            return SetEx.setOf(accommodationsThatExistingInBoth);
        }

        Range<Long> priceRange = Optional.ofNullable(request.getPriceRange()).orElse(Range.blank());
        Integer totalRooms = Optional.ofNullable(request.totalRooms).orElse(0);

        Range<Timestamp> dateRange = Range.of(
            Timestamp.valueOf(LocalDateTimes.startDayOf(request.getDates().getStart())),
            Timestamp.valueOf(LocalDateTimes.startDayOf(request.getDates().getEnd()).minusDays(1))
        );

        List<IdHolder> accommodationIdsFound = accommodationRepository.findAllAccommodationsThatHavingRoomsMeet(
            dateRange.getStart(),
            dateRange.getEnd(),
            totalRooms,
            QueryValues.integerList(searchRoomIdsMeetingPriceIn(priceRange, dateRange, request, roomIds)),
            QueryValues.integerList(previousQueryFoundIds)
        );

        return IdHolder.unwrapList(accommodationIdsFound);
    }

    private Set<Integer> searchRoomIdsMeetingPriceIn(Range<Long> priceRange, Range<Timestamp> dateRange, Request request, Collection<Integer> roomIds) {
        if (roomIds != ALL_IDS && roomIds.isEmpty()) {
            return NO_IDS_FOUND;
        }

        if (request.isSearchTotalPrice()) {
            return roomRepository.findAllRoomsHavingTotalPriceBetween(
                priceRange.getStartOr(0L),
                priceRange.getEndOr(500_000_000L),
                dateRange.getStart(),
                dateRange.getEnd(),
                QueryValues.integerList(roomIds)
            );
        }

        return roomRepository.findAllRoomsHavingEveryDayPriceBetween(
            priceRange.getStartOr(0L),
            priceRange.getEndOr(500_000_000L),
            dateRange.getStart(),
            dateRange.getEnd(),
            QueryValues.integerList(roomIds)
        );
    }

    private Page<Accommodation> searchAccommodationsByTypeWithPaging(List<Accommodation.Type> types, PageRequest pageable, Set<Integer> previousQueryFoundIds) {
        return this.accommodationRepository.findAllThatMeet(
            QueryValues.enumListOf(types),
            QueryValues.enumListOf(List.of(Accommodation.Status.OPENING)),
            QueryValues.integerList(previousQueryFoundIds),
            pageable
        );
    }

    @Getter
    @Builder(toBuilder = true)
    @FieldNameConstants
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, creatorVisibility = JsonAutoDetect.Visibility.ANY)
    public static class Request extends SearchRequest {
        private static final Set<Object> REQUIRED_ORDERS = Set.of(Accommodation.Fields.reviewRate, Accommodation.Fields.totalViews);
        private String orderBy;
        private Boolean isDescending;
        private Integer size;
        private Integer page;
        private Integer totalRooms;
        private Range<Long> priceRange;
        private Range<Timestamp> dates;
        private Coordinate coordinate;
        private List<Accommodation.Type> types;
        private List<Integer> amenityIds;
        private List<Integer> viewIds;
        private boolean searchTotalPrice;

        private record Coordinate(Double lng, Double lat, Double range) {

        }

        private final List<CriteriaRequest> criteriaList;

        private static String orderByOf(final String orderBy) {
            if (orderBy == null || orderBy.equals(Accommodation.Fields.totalViews)) {
                return "total_bookings * (1 + (review_rate / 5))";
            }
            return MoreStrings.snakeCaseOf(orderBy);
        }

        @Override
        public PageRequest toPageRequest() {
            final int page = Optional.ofNullable(this.getPage()).orElse(1);
            final int size = Optional.ofNullable(this.getSize()).orElse(10);

            if (this.getOrderBy() != null && !REQUIRED_ORDERS.contains(this.getOrderBy())) {
                throw ApiException.badRequest("Chỉ hỗ trợ theo: {}", REQUIRED_ORDERS);
            }
            final String orderBy = orderByOf(this.getOrderBy());

            final Boolean descending = Optional.ofNullable(this.getIsDescending()).orElse(true);
            final Sort.Direction direction = descending ? Sort.Direction.DESC : Sort.Direction.ASC;
            return PageRequest.of(page - 1, size, JpaSort.unsafe(direction, orderBy));
        }
    }

}
