package com.fpt.h2s.services.commands.accommodation;


import com.fpt.h2s.models.domains.ApiResponse;
import com.fpt.h2s.models.entities.Accommodation;
import com.fpt.h2s.models.entities.Property;
import com.fpt.h2s.models.entities.Room;
import com.fpt.h2s.repositories.*;
import com.fpt.h2s.services.commands.BaseCommand;
import com.fpt.h2s.services.commands.requests.DetailAccommodationFilterRequest;
import com.fpt.h2s.services.commands.responses.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ViewDetailAccommodationFilterCommand implements
        BaseCommand<DetailAccommodationFilterRequest, DetailAccommodationResponse> {

    private final AccommodationRepository accommodationRepository;
    private final RoomPropertyRepository roomPropertyRepository;
    private final RoomRepository roomRepository;
    private final BookingRequestRepository bookingRequestRepository;
    private final PriceHistoryRepository priceHistoryRepository;
    private Integer duration;

    @Override
    public ApiResponse<DetailAccommodationResponse> execute(final DetailAccommodationFilterRequest request) {
        final DetailAccommodationResponse response = this.getAccommodationDetail(request);
        return ApiResponse.success(response);
    }

    private DetailAccommodationResponse getAccommodationDetail(final DetailAccommodationFilterRequest request) {
        final Accommodation accommodation = this.accommodationRepository.findById(request.getId()).orElseThrow();
        final List<Room> rooms = this.roomRepository.findRoomByProperty(
                request.getNumberOfBeds(),
                request.getNumberOfAdults(),
                request.getNumberOfChildren(),
                request.getNumberOfPets(),
                request.getId()
        );
        return DetailAccommodationResponse.of(
                accommodation,
                accommodation.getImages(),
                accommodation.getCategories(),
                (request.getCheckIn() == null && request.getCheckOut() == null) ? getRooms(rooms) : getRoomsWithDate(request, rooms),
                (request.getCheckIn() == null && request.getCheckOut() == null) ? 0 : duration
        );
    }

    private Set<DetailAccommodationRoomResponse> getRooms(final List<Room> rooms) {
        return rooms.stream()
                .map(room -> new DetailAccommodationRoomResponse(
                        room.getId(),
                        room.getName(),
                        room.getStatus(),
                        null,
                        room.getTotalRooms(),
                        getImages(room),
                        getProperties(room),
                        null,
                        null
                ))
                .collect(Collectors.toSet());
    }

    private Set<DetailAccommodationRoomResponse> getRoomsWithDate(final DetailAccommodationFilterRequest request,
                                                                  final List<Room> rooms) {
        return rooms.stream()
                .map(room -> {
                    DetailAccommodationRoomRemainingResponse remainingRoom = remainingRoomByDate(request, room.getId());
                    return new DetailAccommodationRoomResponse(
                            room.getId(),
                            room.getName(),
                            room.getStatus(),
                            getTotalBookingPrice(request.getCheckIn(), request.getCheckOut(), room.getId()),
                            room.getTotalRooms(),
                            getImages(room),
                            getProperties(room),
                            remainingRoom,
                            null
                    );
                })
                .collect(Collectors.toSet());
    }

    private Set<DetailAccommodationSetPropertiesResponse> getProperties(final Room room) {
        final Set<Object[]> results = this.roomPropertyRepository.findJoinedDataByRoomId(room.getId());
        return results.stream()
                .map(result -> new DetailAccommodationSetPropertiesResponse(
                        Integer.parseInt(result[0].toString()),
                        result[1].toString(),
                        Property.Type.valueOf(result[2].toString()),
                        result[3].toString()
                ))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private Set<DetailAccommodationSetImageResponse> getImages(final Room room) {
        return room.getImages()
                .stream()
                .map(image -> new DetailAccommodationSetImageResponse(
                        image.getId(),
                        image.getRoomId(),
                        image.getName(),
                        image.getImage()
                ))
                .collect(Collectors.toSet());
    }


    private DetailAccommodationRoomRemainingResponse remainingRoomByDate(final DetailAccommodationFilterRequest request,
                                                                         final Integer roomId) {
        final Object[][] result = this.bookingRequestRepository.
                getBookingResultsByRoomIdCheckInCheckOut(
                        roomId,
                        request.getCheckIn(),
                        request.getCheckOut(),
                        request.getTotalRoomBook()
                );

        if (result[0][0] == null || result[0][1] == null || result[0][2] == null) return null;
        return DetailAccommodationRoomRemainingResponse.of(result[0][0], result[0][1], result[0][2]);
    }

    private Long getTotalBookingPrice(final Timestamp checkInDate,
                                      final Timestamp checkOutDate,
                                      final Integer roomId) {
        final LocalDate checkIn = checkInDate.toLocalDateTime().toLocalDate();
        final LocalDate checkOut = checkOutDate.toLocalDateTime().toLocalDate();
        final List<Object[]> results = this.priceHistoryRepository.getAmountByCheckInCheckOutDate(roomId, checkIn, checkOut);
        duration = results.size();
        return (long) results.stream()
                .mapToDouble( result ->
                        (1 - (Integer.parseInt(result[1].toString())* 0.01)) *
                        Long.parseLong(result[2].toString()))
                .sum();
    }

}
