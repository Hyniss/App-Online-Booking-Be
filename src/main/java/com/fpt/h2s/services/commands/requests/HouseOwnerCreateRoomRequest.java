package com.fpt.h2s.services.commands.requests;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fpt.h2s.models.domains.BaseRequest;
import com.fpt.h2s.models.entities.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldNameConstants;

import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@Builder
@FieldNameConstants
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, creatorVisibility = JsonAutoDetect.Visibility.ANY)
public class HouseOwnerCreateRoomRequest extends BaseRequest {

    @NotNull
    private Integer accommodationId;

    @NotNull
    private String name;

    @NotNull
    private Room.Status status;

    @NotNull
    private Long price;

    @NotNull
    private Integer weekdayDiscountPercent;

    @NotNull
    private Integer weekendDiscountPercent;

    @NotNull
    private Integer specialDayDiscountPercent;

    @NotNull
    private Integer weekdayPricePercent;

    @NotNull
    private Integer weekendPricePercent;

    @NotNull
    private Integer specialDayPricePercent;

    @NotNull
    private Integer count;

    @NotEmpty
    private Set<HouseOwnerCreateUpdateRoomImageRequest> roomImageRequests;

    @NotEmpty
    private Set<HouseOwnerCreateUpdateRoomPropertyRequest> roomPropertyRequests;

    @NotEmpty
    private Set<Integer> amenities;

    public Room toRoom(final Integer accommodationId) {
        return Room.builder()
                .accommodationId(accommodationId)
                .name(this.name)
                .status(this.status)
                .price(this.price)
                .totalRooms(this.count)
                .amenities(amenities.stream()
                        .map(amenity -> Category.builder()
                                .id(amenity)
                                .build())
                        .collect(Collectors.toSet()))
                .build();
    }


    public PriceHistory toDiscountByNormalDay(final Room savedRoom) {
        return PriceHistory.builder()
                .amount(this.weekdayDiscountPercent.longValue())
                .roomId(savedRoom.getId())
                .type(PriceHistory.Type.DISCOUNT)
                .dayType(PriceHistory.DayType.WEEKDAY)
                .fromDate(LocalDate.now())
                .toDate(null)
                .build();
    }

    public PriceHistory toDiscountByWeekendDay(final Room savedRoom) {
        return PriceHistory.builder()
                .amount(this.weekendDiscountPercent.longValue())
                .roomId(savedRoom.getId())
                .type(PriceHistory.Type.DISCOUNT)
                .dayType(PriceHistory.DayType.WEEKEND)
                .fromDate(LocalDate.now())
                .toDate(null)
                .build();
    }

    public PriceHistory toDiscountBySpecialDay(final Room savedRoom) {
        return PriceHistory.builder()
                .amount(this.specialDayDiscountPercent.longValue())
                .roomId(savedRoom.getId())
                .type(PriceHistory.Type.DISCOUNT)
                .dayType(PriceHistory.DayType.SPECIAL_DAY)
                .fromDate(LocalDate.now())
                .toDate(null)
                .build();
    }

    public PriceHistory toPriceByNormalDay(final Room savedRoom) {
        return PriceHistory.builder()
                .amount(this.weekdayPricePercent.longValue())
                .roomId(savedRoom.getId())
                .type(PriceHistory.Type.PRICE)
                .dayType(PriceHistory.DayType.WEEKDAY)
                .fromDate(LocalDate.now())
                .toDate(null)
                .build();
    }

    public PriceHistory toPriceByWeekendDay(final Room savedRoom) {
        return PriceHistory.builder()
                .amount(this.weekendPricePercent.longValue())
                .roomId(savedRoom.getId())
                .type(PriceHistory.Type.PRICE)
                .dayType(PriceHistory.DayType.WEEKEND)
                .fromDate(LocalDate.now())
                .toDate(null)
                .build();
    }

    public PriceHistory toPriceBySpecialDay(final Room savedRoom) {
        return PriceHistory.builder()
                .amount(this.specialDayPricePercent.longValue())
                .roomId(savedRoom.getId())
                .type(PriceHistory.Type.PRICE)
                .dayType(PriceHistory.DayType.SPECIAL_DAY)
                .fromDate(LocalDate.now())
                .toDate(null)
                .build();
    }


    public Set<RoomProperty> toRoomProperty(final Room savedRoom) {
        return this.getRoomPropertyRequests().stream()
                .map(roomPropertyRequest -> RoomProperty
                        .builder()
                        .id(new RoomProperty
                                .PK(savedRoom.getId(),
                                roomPropertyRequest.getKeyId()))
                        .value(roomPropertyRequest.getValue().toString())
                        .build())
                .collect(Collectors.toSet());
    }


    public Set<RoomImage> toRoomImage(final Room savedRoom) {
        return this.getRoomImageRequests().stream()
                .map(roomImageRequest -> RoomImage
                        .builder()
                        .roomId(savedRoom.getId())
                        .name(roomImageRequest.getName())
                        .image(roomImageRequest.getUrl())
                        .build())
                .collect(Collectors.toSet());
    }

    public Optional<Accommodation> toAccommodation(final Accommodation originAccommodation,
                                                   final Room savedRoom) {
        Integer updatedTotalRooms = originAccommodation.getTotalRoom() + count;
        final Long originMinPrice = originAccommodation.getMinPrice();
        final Long originMaxPrice = originAccommodation.getMaxPrice();
        if (savedRoom.getId() > 0) {
            long updatedMinPrice = 0L;
            Long updatedMaxPrice = 0L;
            if (originMinPrice == 0L && originMaxPrice == 0L) {
                updatedMinPrice = (this.price);
                updatedMaxPrice = (this.price);
            } else if (originMinPrice > this.price) {
                updatedMinPrice = (this.price);
                updatedMaxPrice = originMaxPrice;
            } else if (originMaxPrice < this.price) {
                updatedMinPrice = originMinPrice;
                updatedMaxPrice = (this.price);
            }
            if (updatedMaxPrice != 0L && updatedMinPrice != 0L) {
                return Optional.ofNullable(originAccommodation
                        .toBuilder()
                        .minPrice(updatedMinPrice)
                        .maxPrice(updatedMaxPrice)
                        .totalRoom(updatedTotalRooms)
                        .build());
            }
        }
        return Optional.ofNullable(originAccommodation
                .toBuilder()
                .totalRoom(updatedTotalRooms)
                .build());
    }


}
