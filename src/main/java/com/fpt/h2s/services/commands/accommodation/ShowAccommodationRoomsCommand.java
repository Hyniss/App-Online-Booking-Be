package com.fpt.h2s.services.commands.accommodation;

import ananta.utility.ListEx;
import ananta.utility.SetEx;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fpt.h2s.models.domains.ApiResponse;
import com.fpt.h2s.models.domains.BaseRequest;
import com.fpt.h2s.models.entities.*;
import com.fpt.h2s.repositories.*;
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
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class ShowAccommodationRoomsCommand implements BaseCommand<ShowAccommodationRoomsCommand.Request, List<ShowAccommodationRoomsCommand.RoomResponse>> {

    private final RoomRepository roomRepository;
    private final CategoryRepository categoryRepository;
    private final RoomPropertyRepository roomPropertyRepository;
    private final PriceHistoryRepository priceHistoryRepository;
    private final RoomImageRepository roomImageRepository;
    @Override
    public ApiResponse<List<RoomResponse>> execute(final Request request) {
        List<Room> rooms = roomRepository.findAllByAccommodationId(request.getAccommodationId());

        Set<Integer> roomIds = SetEx.setOf(rooms, Room::getId);
        final Map<Integer, List<PriceDetail>> priceMapToRoomId = getPricesMapToRoomId(roomIds);
        final Map<Integer, List<CategoryResponse>> categoriesMapToRoomId = getCategoriesMapToRoomId(roomIds);
        final Map<Integer, List<RoomPropertyDetail>> roomPropertiesMapToId = this.getRoomProperties(roomIds);
        final Map<Integer, List<RoomImage>> roomImagesMapToRoomId = this.getRoomImages(roomIds);

        final List<RoomResponse> responses = rooms.stream().map(room -> RoomResponse
                .builder()
                .properties(roomPropertiesMapToId.get(room.getId()).stream().map(DetailedRoomProperty::of).filter(Objects::nonNull).toList())
                .amenities(ListEx.emptyListIfNull(categoriesMapToRoomId.get(room.getId())))
                .id(room.getId())
                .name(room.getName())
                .priceDetails(priceMapToRoomId.get(room.getId()))
                .images(roomImagesMapToRoomId.get(room.getId()))
                .build()
        ).toList();

        cacheForLaterBook(responses);
        return ApiResponse.success(responses);
    }

    private static void cacheForLaterBook(List<RoomResponse> responses) {
        if (User.currentUserId().isPresent()) {
            RedisRepository.set("search-rooms-%s".formatted(Tokens.getTokenFrom(MoreRequests.getCurrentHttpRequest())), responses);
        }
    }

    private Map<Integer, List<CategoryResponse>> getCategoriesMapToRoomId(Set<Integer> roomIds) {
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

    private Map<Integer, List<PriceDetail>> getPricesMapToRoomId(Set<Integer> roomIds) {
        List<Map<String, Object>> tuples = priceHistoryRepository.findNonCustomPricesOfRooms(roomIds);
        return tuples
            .stream()
            .map(tuple -> Mappers.fromTuple(tuple, PriceDetail.class))
            .collect(Collectors.groupingBy(PriceDetail::getRoomId));
    }

    @NotNull
    private Map<Integer, List<RoomPropertyDetail>> getRoomProperties(Set<Integer> roomIds) {
        return this.roomPropertyRepository
            .findAllPropertiesOfRooms(roomIds)
            .stream()
            .collect(Collectors.groupingBy(RoomPropertyDetail::getRoomId));
    }

    private Map<Integer, List<RoomImage>> getRoomImages(Set<Integer> roomIds) {
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
        private List<PriceDetail> priceDetails;
        private List<RoomImage> images;
        private List<CategoryResponse> amenities;
        private List<DetailedRoomProperty> properties;

    }

    @Getter
    @Setter
    @NoArgsConstructor
    private static class PriceDetail {

        @JsonIgnore
        private Integer roomId;

        @JsonProperty("originalPrice")
        private Long price;
        private Integer discount;
        @JsonProperty("discountedPrice")
        private Long displayPrice;
        private DayType.Type dayType;
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
