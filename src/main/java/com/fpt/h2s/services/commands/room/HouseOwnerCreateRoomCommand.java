package com.fpt.h2s.services.commands.room;

import com.fpt.h2s.models.domains.ApiResponse;
import com.fpt.h2s.models.entities.*;
import com.fpt.h2s.models.exceptions.ApiException;
import com.fpt.h2s.repositories.*;
import com.fpt.h2s.services.commands.BaseCommand;
import com.fpt.h2s.services.commands.requests.HouseOwnerCreateRoomRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@Log4j2
@Service
@Transactional
@RequiredArgsConstructor
public class HouseOwnerCreateRoomCommand
        implements BaseCommand<HouseOwnerCreateRoomRequest, Void> {

    private final AccommodationRepository accommodationRepository;
    private final RoomRepository roomRepository;
    private final RoomImageRepository roomImageRepository;
    private final RoomPropertyRepository roomPropertyRepository;
    private final PriceHistoryRepository priceHistoryRepository;

    @Override
    public ApiResponse<Void> execute(final HouseOwnerCreateRoomRequest request) {
        final Accommodation originAccommodation = this.accommodationRepository
                .findById(request.getAccommodationId())
                .orElseThrow(() -> ApiException.badRequest("Không thể tìm thấy chỗ ở với id = {}.", request.getAccommodationId()));


        final Room roomToSave = request.toRoom(request.getAccommodationId());
        final Room savedRoom = this.roomRepository.save(roomToSave);
        this.saveRoomImageAndProperty(request, savedRoom);
        this.savePriceHistory(request, savedRoom);
        this.saveUpdatedAccommodation(request, originAccommodation, savedRoom);
        return ApiResponse.success("Tạo phòng thành công.");
    }

    private void saveRoomImageAndProperty(final HouseOwnerCreateRoomRequest request,
                                          final Room savedRoom) {
        final Set<RoomProperty> roomPropertiesToSave = request.toRoomProperty(savedRoom);
        this.roomPropertyRepository.saveAll(roomPropertiesToSave);

        final Set<RoomImage> roomImageToSave = request.toRoomImage(savedRoom);
        this.roomImageRepository.saveAll(roomImageToSave);
    }

    private void savePriceHistory(final HouseOwnerCreateRoomRequest request,
                                  final Room saveRoom) {

        final Set<PriceHistory> priceHistories = Stream.of(
                request.toDiscountByNormalDay(saveRoom),
                request.toDiscountByWeekendDay(saveRoom),
                request.toDiscountBySpecialDay(saveRoom),
                request.toPriceByNormalDay(saveRoom),
                request.toPriceByWeekendDay(saveRoom),
                request.toPriceBySpecialDay(saveRoom)
        ).collect(Collectors.toSet());

        this.priceHistoryRepository.saveAll(priceHistories);
    }

    private void saveUpdatedAccommodation(final HouseOwnerCreateRoomRequest request,
                                          final Accommodation originAccommodation,
                                          final Room savedRoom) {
        final Accommodation accommodation = request.toAccommodation(originAccommodation, savedRoom)
                .orElseThrow(() -> ApiException.badRequest("Có gì đó không ổn. Xin vui lòng kiểm tra lại thông tin"));
        this.accommodationRepository.save(accommodation);
    }

}
