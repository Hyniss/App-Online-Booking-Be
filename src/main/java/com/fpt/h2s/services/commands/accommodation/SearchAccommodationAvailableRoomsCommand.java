package com.fpt.h2s.services.commands.accommodation;

import ananta.utility.ListEx;
import ananta.utility.MapEx;
import ananta.utility.SetEx;
import com.fpt.h2s.models.domains.ApiResponse;
import com.fpt.h2s.models.domains.BaseRequest;
import com.fpt.h2s.models.entities.RoomImage;
import com.fpt.h2s.models.entities.RoomProperty;
import com.fpt.h2s.models.entities.User;
import com.fpt.h2s.repositories.*;
import com.fpt.h2s.repositories.projections.AvailableRoomRecord;
import com.fpt.h2s.repositories.projections.RoomPropertyDetail;
import com.fpt.h2s.services.commands.BaseCommand;
import com.fpt.h2s.services.commands.responses.CategoryResponse;
import com.fpt.h2s.services.commands.responses.DetailedRoomProperty;
import com.fpt.h2s.utilities.*;
import lombok.*;
import lombok.experimental.FieldNameConstants;
import lombok.extern.jackson.Jacksonized;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class SearchAccommodationAvailableRoomsCommand implements BaseCommand<SearchAccommodationAvailableRoomsCommand.Request, List<SearchAccommodationAvailableRoomsCommand.RoomResponse>> {

    private final RoomRepository roomRepository;
    private final CategoryRepository categoryRepository;
    private final RoomPropertyRepository roomPropertyRepository;
    private final PriceHistoryRepository priceHistoryRepository;
    private final RoomImageRepository roomImageRepository;
    @Override
    public ApiResponse<List<RoomResponse>> execute(final Request request) {
        final List<AvailableRoomRecord> rooms = this.searchAvailableRooms(request);
        final Map<Integer, PriceDTO> priceMapToRoomId = getPriceMapToRoomId(request, rooms);
        final Map<Integer, List<CategoryResponse>> categoriesMapToRoomId = getCategoriesMapToRoomId(rooms);
        final Map<Integer, List<RoomPropertyDetail>> roomPropertiesMapToId = this.getRoomProperties(rooms);
        final Map<Integer, List<RoomImage>> roomImagesMapToRoomId = this.getRoomImages(rooms);

        final List<RoomResponse> responses = rooms.stream().map(room -> {
                PriceDTO price = priceMapToRoomId.get(room.getId());
                return RoomResponse
                    .builder()
                    .properties(roomPropertiesMapToId.get(room.getId()).stream().map(DetailedRoomProperty::of).filter(Objects::nonNull).toList())
                    .amenities(ListEx.emptyListIfNull(categoriesMapToRoomId.get(room.getId())))
                    .id(room.getId())
                    .name(room.getName())
                    .originalPrice(Optional.ofNullable(price).map(PriceDTO::getPrice).orElse(0L))
                    .discount(Optional.ofNullable(price).map(PriceDTO::getDiscount).orElse(0))
                    .discountedPrice(Optional.ofNullable(price).map(PriceDTO::getDisplayPrice).orElse(0L))
                    .totalRoomsLeft(room.getAvailableRooms())
                    .images(roomImagesMapToRoomId.get(room.getId()))
                    .build();
            }
        ).toList();

        cacheForLaterBook(request, responses);
        return ApiResponse.success(responses);
    }

    private static void cacheForLaterBook(Request request, List<RoomResponse> responses) {
        if (User.currentUserId().isPresent()) {
            String token = Tokens.getTokenFrom(MoreRequests.getCurrentHttpRequest());
            RedisRepository.set("search-request-%s".formatted(token), request);
            RedisRepository.set("search-rooms-%s".formatted(token), responses, Duration.ofHours(1));
        }
    }

    private Map<Integer, List<CategoryResponse>> getCategoriesMapToRoomId(List<AvailableRoomRecord> rooms) {
        Set<Integer> roomIds = SetEx.setOf(rooms, AvailableRoomRecord::getId);

        return this.categoryRepository
            .findAllByRoomsIds(roomIds)
            .stream()
            .map(tuple -> Mappers.fromTuple(tuple, RoomCategory.class))
            .collect(
                Collectors.groupingBy(
                    RoomCategory::getRoomId,
                    ImmutableCollectors.toList(category -> Mappers.convertTo(CategoryResponse.class, category))
                )
            );
    }

    @NotNull
    private Map<Integer, PriceDTO> getPriceMapToRoomId(Request request, List<AvailableRoomRecord> rooms) {
        Set<Integer> roomIds = SetEx.setOf(rooms, AvailableRoomRecord::getId);
        List<Map<String, Object>> tuples = priceHistoryRepository
            .findPricesOf(
                roomIds,
                QueryValues.unreachablePastOr(request.getFromDate()),
                QueryValues.unreachableFutureOr(request.getToDate())
            );

        List<PriceDTO> prices = tuples.stream().map(tuple -> Mappers.fromTuple(tuple, PriceDTO.class)).toList();

        return MapEx.mapOf(prices, PriceDTO::getRoomId);
    }

    public List<AvailableRoomRecord> searchAvailableRooms(final Request request) {
        final Specification<RoomProperty> criteria = request.criteriaList
            .stream()
            .map(CriteriaRequest::toSpecification)
            .reduce(Specification::and)
            .orElse(null);

        if (criteria == null) {
            return this.roomRepository.findAvailableRoomsOfAccommodationBetween(
                QueryValues.unreachablePastOr(request.getFromDate()),
                QueryValues.unreachableFutureOr(request.getToDate()),
                request.accommodationId,
                QueryValues.integerList(null)
            );
        }

        Set<Integer> roomsThatMeetCriteria = roomRepository.getRoomsThatMeet(criteria, request.criteriaList.stream().filter(Objects::nonNull).toList().size());

        return this.roomRepository.findAvailableRoomsOfAccommodationBetween(
            QueryValues.unreachablePastOr(request.getFromDate()),
            QueryValues.unreachableFutureOr(request.getToDate()),
            request.accommodationId,
            roomsThatMeetCriteria
        );
    }

    @NotNull
    private Map<Integer, List<RoomPropertyDetail>> getRoomProperties(final List<AvailableRoomRecord> rooms) {
        final Set<Integer> roomIds = SetEx.setOf(rooms, AvailableRoomRecord::getId);
        return this.roomPropertyRepository
            .findAllPropertiesOfRooms(roomIds)
            .stream()
            .collect(Collectors.groupingBy(RoomPropertyDetail::getRoomId));
    }

    private Map<Integer, List<RoomImage>> getRoomImages(final List<AvailableRoomRecord> rooms) {
        final Set<Integer> roomIds = SetEx.setOf(rooms, AvailableRoomRecord::getId);
        return this.roomImageRepository
            .findAllByRoomIdIn(roomIds)
            .stream()
            .collect(Collectors.groupingBy(RoomImage::getRoomId));
    }

    @Getter
    @Builder
    @FieldNameConstants
    @Jacksonized
    public static final class Request extends BaseRequest {

        @jakarta.validation.constraints.NotNull
        private final Integer accommodationId;
        private final Timestamp fromDate;
        private final Timestamp toDate;
        private final List<CriteriaRequest> criteriaList;

        public Timestamp getFromDate() {
            return fromDate == null ? null : Timestamp.valueOf(LocalDateTimes.startDayOf(fromDate));
        }
        public Timestamp getToDate() {
            return toDate == null ? null : Timestamp.valueOf(LocalDateTimes.startDayOf(toDate).minusDays(1));
        }

    }

    @Getter
    @Builder
    @Jacksonized
    public static class CriteriaRequest {
        private final Integer key;
        private final Criteria operation;
        private final Object[] value;

        public Specification<RoomProperty> toSpecification() {
            final Object[] values = this.getValue();
            final Specification<RoomProperty> specification = (root, query, cb) -> cb.equal(root.get(RoomProperty.Fields.id).get(RoomProperty.PK.Fields.keyId), this.key);
            return specification.and(this.operation.<RoomProperty>value(values).apply(RoomProperty.Fields.value));
        }
    }

    @With
    @Getter
    @Builder
    @FieldNameConstants
    @Jacksonized
    public static final class RoomResponse extends BaseRequest {
        private Integer id;

        private String name;
        private Long originalPrice;
        private Integer discount;
        private Long discountedPrice;
        private Integer totalRoomsLeft;
        private List<RoomImage> images;
        private List<CategoryResponse> amenities;
        private List<DetailedRoomProperty> properties;

    }

    @Getter
    @Setter
    @NoArgsConstructor
    private static class PriceDTO {
        private Integer roomId;
        private Long price;
        private Integer discount;
        private Long displayPrice;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    private static class RoomCategory {
        private Integer roomId;
        private Integer categoryId;
        private String name;
        private String image;
    }
}
