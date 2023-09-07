package com.fpt.h2s.services.commands.user;

import com.fpt.h2s.models.domains.ApiResponse;
import com.fpt.h2s.repositories.BookingRequestRepository;
import com.fpt.h2s.repositories.UserRepository;
import com.fpt.h2s.services.commands.BaseCommand;
import com.fpt.h2s.utilities.LocalDateTimes;
import com.fpt.h2s.utilities.Mappers;
import lombok.*;
import lombok.experimental.FieldNameConstants;
import lombok.extern.jackson.Jacksonized;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.fpt.h2s.utilities.QueryValues.UNREACHABLE_PAST;

@Service
@RequiredArgsConstructor
public class AdminDashboardCommand implements BaseCommand<Void, AdminDashboardCommand.DashboardResponse> {

    private final UserRepository userRepository;
    private final BookingRequestRepository bookingRequestRepository;

    @Override
    public ApiResponse<DashboardResponse> execute(Void request) {
        Timestamp lastMonth = Timestamp.valueOf(LocalDateTime.now().minusMonths(1));

        Integer totalUsers = userRepository.countAll();
        Integer totalUsersJoinedLastMonth = userRepository.countAllSince(lastMonth);


        Integer totalBookingRequests = bookingRequestRepository.countSuccessRequestsSince(UNREACHABLE_PAST);
        Integer totalBookingRequestsLastMonth = bookingRequestRepository.countSuccessRequestsSince(lastMonth);

        Long totalRevenue = bookingRequestRepository.getTotalRevenueSince(UNREACHABLE_PAST);
        Long totalRevenueLastMonth = bookingRequestRepository.getTotalRevenueSince(lastMonth);

        LocalDateTime today = LocalDateTimes.startDayOf(LocalDateTime.now());
        LocalDateTime lastWeekStart = today.minusWeeks(1).with(DayOfWeek.MONDAY);
        LocalDateTime lastMonthStart = today.minusMonths(1).withDayOfMonth(1);

        Long todayRevenue = bookingRequestRepository.getTotalRevenueSince(Timestamp.valueOf(today));
        Long lastWeekRevenue = bookingRequestRepository.getTotalRevenueSince(Timestamp.valueOf(lastWeekStart));
        Long lastMonthRevenue = bookingRequestRepository.getTotalRevenueSince(Timestamp.valueOf(lastMonthStart));

        List<MonthRevenue> revenues = getRevenuesOfLast6Months();

        DashboardResponse response = DashboardResponse
            .builder()
            .users(DashboardItem.of(totalUsers, totalUsersJoinedLastMonth))
            .bookings(DashboardItem.of(totalBookingRequests, totalBookingRequestsLastMonth))
            .earnings(DashboardItem.of(totalRevenue, totalRevenueLastMonth))
            .todayRevenue(Optional.ofNullable(todayRevenue).orElse(0L))
            .lastWeekRevenue(Optional.ofNullable(lastWeekRevenue).orElse(0L))
            .lastMonthRevenue(Optional.ofNullable(lastMonthRevenue).orElse(0L))
            .revenueOfMonths(revenues)
            .build();

        return ApiResponse.success(response);
    }
    @NotNull
    private List<MonthRevenue> getRevenuesOfLast6Months() {
        Map<Integer, Long> revenueMap = bookingRequestRepository
            .getRevenuesOfLastNMonths(6)
            .stream()
            .map(v -> Mappers.fromTuple(v, MonthRevenue.class))
            .collect(Collectors.toMap(MonthRevenue::getMonth, MonthRevenue::getAmount));

        LocalDateTime now = LocalDateTime.now();
        return Stream.of(
            MonthRevenue.of(now),
            MonthRevenue.of(now.minusMonths(1)),
            MonthRevenue.of(now.minusMonths(2)),
            MonthRevenue.of(now.minusMonths(3)),
            MonthRevenue.of(now.minusMonths(4)),
            MonthRevenue.of(now.minusMonths(5))
        )
            .map(
                monthRevenue -> revenueMap.containsKey(monthRevenue.getMonth())
                ? monthRevenue.withAmount(revenueMap.get(monthRevenue.getMonth()))
                : monthRevenue
            )
            .toList();
    }

    @Getter
    @Builder
    @FieldNameConstants
    @Jacksonized
    public static class DashboardResponse {

        private DashboardItem<Integer> users;
        private DashboardItem<Integer> bookings;
        private DashboardItem<Long> earnings;
        private Long todayRevenue;
        private Long lastWeekRevenue;
        private Long lastMonthRevenue;
        private List<MonthRevenue> revenueOfMonths;
    }

    @Getter
    @Builder
    @FieldNameConstants
    @Jacksonized
    public static class DashboardItem<T extends Number> {
        private T total;
        private T totalLastMonth;
        private Float increasePercentage;

        public static <T extends Number> DashboardItem<T> of(T total, T totalLastMonth) {
            return (DashboardItem<T>) DashboardItem.builder().total(total).totalLastMonth(totalLastMonth).increasePercentage(total.floatValue() * 100f / (total.floatValue() - totalLastMonth.floatValue()) - 100f).build();
        }
    }

    @Getter
    @Setter
    @With
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class MonthRevenue {
        private Integer month;
        private Integer year;
        private Long amount;

        public static MonthRevenue of(LocalDateTime month) {
            return MonthRevenue.builder().month(month.getMonth().getValue()).year(month.getYear()).amount(0L).build();
        }
    }
}
