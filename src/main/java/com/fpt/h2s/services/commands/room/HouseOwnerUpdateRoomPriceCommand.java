package com.fpt.h2s.services.commands.room;

import com.fpt.h2s.models.domains.ApiResponse;
import com.fpt.h2s.models.entities.PriceHistory;
import com.fpt.h2s.repositories.PriceHistoryRepository;
import com.fpt.h2s.services.commands.BaseCommand;
import com.fpt.h2s.services.commands.requests.HouseOwnerUpdateRoomAmountRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class HouseOwnerUpdateRoomPriceCommand
        implements BaseCommand<HouseOwnerUpdateRoomAmountRequest, Void> {

    private final PriceHistoryRepository priceHistoryRepository;

    @Override
    public ApiResponse<Void> execute(final HouseOwnerUpdateRoomAmountRequest request) {
        this.updatePriceHistoryWhenFromDateEqual(request);
        this.updatePriceHistoryWhenToDateEqual(request);
        this.updatePriceHistoryWhenFromDateToDateInBound(request);
        if (!this.updatePriceHistoryWhenFromDateToDateOutBound(request)) {
            this.saveNewPriceHistory(request);
        }
        return ApiResponse.success("Áp dụng thay đổi giá phòng thành công");
    }

    private void saveNewPriceHistory(HouseOwnerUpdateRoomAmountRequest request) {
        final PriceHistory updatedPriceHistory = request.toAmount(null, null);
        this.priceHistoryRepository.save(updatedPriceHistory);
    }

    private boolean updatePriceHistoryWhenFromDateToDateOutBound(HouseOwnerUpdateRoomAmountRequest request) {
        int isSaved = 0;
        final Map<Long, List<PriceHistory>> priceHistories = this.priceHistoryRepository
                .findByRoomIdAndTypeAndDayType(request.getRoomId(), request.getType(), PriceHistory.DayType.CUSTOM)
                .stream()
                .filter(priceHistory -> priceHistory.getToDate() != null)
                .collect(Collectors.groupingBy(PriceHistory::getAmount));

        final List<PriceHistory> priceHistoriesToRemove = priceHistories.get(request.getAmount());
        //remove when amount equal;
        if (priceHistoriesToRemove != null) {
            this.priceHistoryRepository.deleteAll(priceHistoriesToRemove.stream()
                    .filter(priceHistory -> priceHistory.getFromDate().isAfter(request.getFromDate())
                            && priceHistory.getToDate().isBefore(request.getToDate())).toList());
        }
        //update when amount different
        final List<PriceHistory> priceHistoriesWithDifferentAmount = priceHistories.entrySet().stream()
                .filter(entry -> !entry.getKey().equals(request.getAmount()))
                .flatMap(entry -> entry.getValue().stream())
                .filter(priceHistory -> priceHistory.getFromDate().isAfter(request.getFromDate())
                        && priceHistory.getToDate().isBefore(request.getToDate()))
                .toList();

        if (priceHistoriesWithDifferentAmount.size() > 0) {
            LocalDate minFromDate = priceHistoriesWithDifferentAmount.stream()
                    .map(PriceHistory::getFromDate)
                    .min(LocalDate::compareTo)
                    .orElse(null);

            LocalDate maxToDate = priceHistoriesWithDifferentAmount.stream()
                    .map(PriceHistory::getToDate)
                    .max(LocalDate::compareTo)
                    .orElse(null);

            PriceHistory priceHistoryBefore = request.toAmount(null, minFromDate.minusDays(1));
            PriceHistory priceHistoryAfter = request.toAmount(maxToDate.plusDays(1), null);
            isSaved = this.priceHistoryRepository.saveAll(List.of(priceHistoryBefore, priceHistoryAfter)).size();
        }
        return isSaved > 0;
    }

    private void updatePriceHistoryWhenFromDateToDateInBound(final HouseOwnerUpdateRoomAmountRequest request) {
        final Map<Long, List<PriceHistory>> priceHistories = this.priceHistoryRepository
                .findByRoomIdAndTypeAndDayType(request.getRoomId(), request.getType(), PriceHistory.DayType.CUSTOM)
                .stream()
                .filter(priceHistory -> priceHistory.getToDate() != null)
                .collect(Collectors.groupingBy(PriceHistory::getAmount));
        final List<PriceHistory> priceHistoriesToRemove = priceHistories.get(request.getAmount());
        //remove when amount equal;
        if (priceHistoriesToRemove != null) {
            this.priceHistoryRepository.deleteAll(priceHistoriesToRemove.stream()
                    .filter(priceHistory -> priceHistory.getFromDate().isBefore(request.getFromDate())
                            && priceHistory.getToDate().isAfter(request.getToDate())).toList());
        }
        //update when amount different
        final List<PriceHistory> priceHistoriesWithDifferentAmount = priceHistories.entrySet().stream()
                .filter(entry -> !entry.getKey().equals(request.getAmount()))
                .flatMap(entry -> entry.getValue().stream())
                .filter(priceHistory -> priceHistory.getFromDate().isBefore(request.getFromDate())
                        && priceHistory.getToDate().isAfter(request.getToDate()))
                .toList();
        if (priceHistoriesWithDifferentAmount.size() > 0) {
            priceHistoriesWithDifferentAmount.stream()
                    .flatMap(priceHistory -> {
                        PriceHistory priceHistoryBefore = request.toToDate(priceHistory);
                        PriceHistory priceHistoryAfter = request.toNewFromDate(priceHistory);
                        return Stream.of(priceHistoryBefore, priceHistoryAfter);
                    })
                    .forEach(this.priceHistoryRepository::save);
        }
    }

    private void updatePriceHistoryWhenFromDateEqual(final HouseOwnerUpdateRoomAmountRequest request) {
        final Map<Long, List<PriceHistory>> priceHistories = this.priceHistoryRepository
                .findByRoomIdAndTypeAndDayTypeAndFromDate(request.getRoomId(), request.getType(), PriceHistory.DayType.CUSTOM, request.getFromDate())
                .stream().collect(Collectors.groupingBy(PriceHistory::getAmount));

        //remove when amount equal
        final List<PriceHistory> priceHistoriesToRemove = priceHistories.get(request.getAmount());
        if (priceHistoriesToRemove != null) {
            this.priceHistoryRepository.deleteAll(priceHistoriesToRemove);
            priceHistories.remove(request.getAmount());
        }

        //update when amount different
        final List<PriceHistory> priceHistoriesWithDifferentAmount = priceHistories.entrySet().stream()
                .filter(entry -> !entry.getKey().equals(request.getAmount()))
                .flatMap(entry -> entry.getValue().stream())
                .toList();
        if (priceHistoriesWithDifferentAmount.size() > 0) {
            priceHistoriesWithDifferentAmount.stream()
                    .filter(priceHistory -> priceHistory.getToDate() != null)
                    .forEach(priceHistory -> {
                        if (priceHistory.getToDate().isAfter(request.getToDate())) {
                            this.priceHistoryRepository.save(request.toFromDate(priceHistory));
                        } else {
                            this.priceHistoryRepository.delete(priceHistory);
                        }
                    });
        }
    }

    private void updatePriceHistoryWhenToDateEqual(final HouseOwnerUpdateRoomAmountRequest request) {
        final Map<Long, List<PriceHistory>> priceHistories = this.priceHistoryRepository
                .findByRoomIdAndTypeAndDayTypeAndToDate(request.getRoomId(), request.getType(), PriceHistory.DayType.CUSTOM, request.getToDate())
                .stream().collect(Collectors.groupingBy(PriceHistory::getAmount));

        //remove when amount equal
        final List<PriceHistory> priceHistoriesToRemove = priceHistories.get(request.getAmount());
        if (priceHistoriesToRemove != null) {
            this.priceHistoryRepository.deleteAll(priceHistoriesToRemove);
            priceHistories.remove(request.getAmount());
        }

        //update when amount different
        final List<PriceHistory> priceHistoriesWithDifferentAmount = priceHistories.entrySet().stream()
                .filter(entry -> !entry.getKey().equals(request.getAmount()))
                .flatMap(entry -> entry.getValue().stream())
                .toList();
        if (priceHistoriesWithDifferentAmount.size() > 0) {
            priceHistoriesWithDifferentAmount.stream()
                    .filter(priceHistory -> priceHistory.getFromDate() != null)
                    .forEach(priceHistory -> {
                        if (priceHistory.getFromDate().isBefore(request.getFromDate())) {
                            this.priceHistoryRepository.save(request.toToDate(priceHistory));
                        } else {
                            this.priceHistoryRepository.delete(priceHistory);
                        }
                    });
        }

    }

}
