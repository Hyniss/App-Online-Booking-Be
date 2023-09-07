package com.fpt.h2s.services.commands.transactions;

import ananta.utility.ListEx;
import ananta.utility.MapEx;
import ananta.utility.StreamEx;
import ananta.utility.StringEx;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fpt.h2s.configurations.requests.DataContext;
import com.fpt.h2s.models.domains.ApiResponse;
import com.fpt.h2s.models.domains.BaseRequest;
import com.fpt.h2s.models.domains.BaseValidator;
import com.fpt.h2s.models.entities.*;
import com.fpt.h2s.models.exceptions.ApiException;
import com.fpt.h2s.repositories.*;
import com.fpt.h2s.repositories.projections.AvailableRoomRecord;
import com.fpt.h2s.services.PaymentService;
import com.fpt.h2s.services.commands.BaseCommand;
import com.fpt.h2s.services.commands.accommodation.SearchAccommodationAvailableRoomsCommand;
import com.fpt.h2s.services.commands.accommodation.SearchAccommodationAvailableRoomsCommand.RoomResponse;
import com.fpt.h2s.services.commands.requests.Payment;
import com.fpt.h2s.services.commands.user.utils.Urls;
import com.fpt.h2s.utilities.*;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.experimental.FieldNameConstants;
import lombok.extern.jackson.Jacksonized;
import lombok.extern.log4j.Log4j2;
import org.hibernate.validator.constraints.Length;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.fpt.h2s.models.entities.BookingRequest.Status.*;

@Log4j2
@Service
@Transactional
@RequiredArgsConstructor
public class CreateBookingTransactionRequestCommand implements BaseCommand<CreateBookingTransactionRequestCommand.Request, CreateBookingTransactionRequestCommand.Response> {

    public static final int TRANSACTION_DURATION = 15;
    private final PaymentService paymentService;
    private final RoomRepository roomRepository;
    private final BookingRequestRepository bookingRequestRepository;
    private final TransactionRepository transactionRepository;
    private final BookingRequestDetailRepository bookingRequestDetailRepository;
    private final TravelStatementRepository travelStatementRepository;
    private final UserRepository userRepository;

    @Override
    @SneakyThrows
    public ApiResponse<Response> execute(final Request request) {
        Integer userId = User.currentUserId().orElseThrow(ApiException::forbidden);
        User user = this.userRepository.getById(userId, "Không thể tìm thấy người dùng với id = {}", userId);

        final List<BookingRequestDetail> detailsToSave = this.getBookingRoomDetailsToSave(request);

        String lastTransactionKey = getLastTransactionKey(userId);
        String currentTransactionJson = jsonOf(request);
        String lastTransactionJson = RedisRepository.get(lastTransactionKey).orElse(null);

        final List<Room> rooms = this.getRooms(request);

        if (lastTransactionJson != null) {
            Integer bookingId = RedisRepository.get(lastTransactionJson).map(Integer::parseInt).orElseThrow();
            cancelLastBooking(bookingId);
        }

        checkIfUserCanBookRooms(user, rooms);

        final BookingRequest bookingRequest = this.bookingRequestRepository.save(this.getBookingRequest(request, rooms));
        saveBookingDetails(detailsToSave, bookingRequest);

        long totalPriceToPay = detailsToSave.stream().mapToLong(BookingRequestDetail::getPrice).sum();
        Payment.PurchaseResponse payRequest = this.createPayRequest(totalPriceToPay, bookingRequest.getId(), request, user);

        updateVersionFor(rooms);
        saveRequestToVerifyAfterBookedSuccessfully(bookingRequest.getId(), request, user, payRequest);

        RedisRepository.set(lastTransactionKey, currentTransactionJson, Duration.ofMinutes(TRANSACTION_DURATION));
        RedisRepository.set(currentTransactionJson, bookingRequest.getId(), Duration.ofMinutes(TRANSACTION_DURATION));

        return ApiResponse.success(Response.builder().id(bookingRequest.getId()).request(payRequest).build());
    }

    private void updateVersionFor(List<Room> rooms) {
        roomRepository.saveAll(rooms.stream().map(room -> room.withUpdatedAt(Timestamp.from(Instant.now()))).toList());
    }

    public static String getLastTransactionKey(Integer userId) {
        return "user-%s-booking".formatted(userId);
    }

    private boolean isLastTransactionCanceled(Integer bookingId) {
        try {
            BookingRequest bookingRequest = bookingRequestRepository.getById(bookingId);
            return bookingRequest.getStatus() == CANCELED;
        } catch (Exception e) {
            return true;
        }
    }

    private static void checkIfUserCanBookRooms(User user, List<Room> rooms) {
        Accommodation accommodation = rooms.get(0).getAccommodation();
        if (accommodation.isCreatedBy(user)) {
            throw ApiException.badRequest("Bạn không được phép đặt chỗ ở thuộc sở hữu của bạn.");
        }
    }

    private static boolean bookingTheSameWithLastTransaction(String currentTransactionJson, String lastTransactionJson) {
        return currentTransactionJson.equals(lastTransactionJson);
    }

    private void cancelLastBooking(Integer bookingId) {
        try {
            BookingRequest bookingRequest = bookingRequestRepository.getById(bookingId);
            if (bookingRequest.getStatus() == PENDING) {
                BookingRequest bookingToCancel = bookingRequest.withStatus(UN_PURCHASED);
                bookingRequestRepository.save(bookingToCancel);
            }
        } catch (Exception e) {
            log.warn("Cancel last booking with id {} failed.", bookingId);
        }
    }

    private void saveBookingDetails(List<BookingRequestDetail> details, BookingRequest bookingRequest) {
        List<BookingRequestDetail> detailsToSave = ListEx.listOf(details, detail -> detail.withBookingRequestId(bookingRequest.getId()));
        this.bookingRequestDetailRepository.saveAll(detailsToSave);
    }

    private static String jsonOf(Request request) {
        HashMap<Object, Object> payload = new HashMap<>();
        payload.put("id", User.currentUserId().orElseThrow());
        payload.put("data", request);
        return Mappers.jsonOf(payload);
    }

    private List<Room> getRooms(final Request request) {
        return this.roomRepository.findWithLockingByIdIn(ListEx.listOf(request.getDetails(), BookingRoomDetails::getRoomId));
    }


    private Payment.PurchaseResponse createPayRequest(long totalPrice, Integer bookingId, final Request request, User user) {
        long priceToPay = (long) (totalPrice * (user.is(User.Role.BUSINESS_ADMIN) ? 1.03d : 1.08));
        String returnUrl = StringEx.format("{}book/{}/transaction", Urls.getClientSidePath());
        return (Payment.PurchaseResponse) this.paymentService.createPayRequest(priceToPay, returnUrl);
    }

    private static void saveRequestToVerifyAfterBookedSuccessfully(Integer bookingId, Request request, User user, Payment.PurchaseResponse response) {
        final String redisToken = StringEx.format("tr-code-{}", response.getId());

        final HashMap<String, Integer> payload = new HashMap<>();
        payload.put(Transaction.Fields.creatorId, user.getId());
        payload.put("bookingId", bookingId);
        payload.put("creatorId", User.currentUserId().orElseThrow());
        if (request.statement != null) {
            payload.put("statementId", request.statement);
        }
        RedisRepository.set(redisToken, payload, Duration.ofMinutes(15));
    }

    private List<BookingRequestDetail> getBookingRoomDetailsToSave(final Request request) {
        String token = Tokens.getTokenFrom(MoreRequests.getCurrentHttpRequest());
        List<RoomResponse> searchRooms = RedisRepository
            .get("search-rooms-%s".formatted(token), new TypeReference<List<RoomResponse>>() {
            })
            .orElseThrow(() -> ApiException.forbidden("Có gì đó không ổn. Xin vui lòng thử lại bằng cách quay lại trang tìm kiếm."));

        Map<Integer, RoomResponse> searchRoomsMapToId = MapEx.mapOf(searchRooms, RoomResponse::getId);

        List<BookingRequestDetail> bookingRequestDetails = request.toBookingRoomDetails();
        return bookingRequestDetails.stream().map(detail -> {
            final RoomResponse room = searchRoomsMapToId.get(detail.getRoomId());
            final long totalPrice = room.getDiscountedPrice() * detail.getTotalRooms();
            final long totalOriginalPrice = room.getOriginalPrice() * detail.getTotalRooms();
            return detail.withPrice(totalPrice).withOriginalPrice(totalOriginalPrice);
        }).toList();
    }

    private BookingRequest getBookingRequest(final Request request, final List<Room> rooms) {
        final Integer accommodationId = rooms.get(0).getAccommodationId();
        BookingRequest bookingRequest = request.toBookingRequest().withAccommodationId(accommodationId);
        if (request.statement == null) {
            final Integer userId = User.currentUserId().orElseThrow();
            return bookingRequest.withUserId(userId);
        }
        TravelStatement statement = travelStatementRepository.findById(request.statement).orElseThrow(() -> ApiException.notFound("Không thể tìm thấy tờ trình với id = {}.", request.statement));
        return bookingRequest.withUserId(statement.getCreatorId());
    }

    @Builder
    @Getter
    @FieldNameConstants
    @Jacksonized
    public static class Request extends BaseRequest {

        public Payment.PurchaseRequest toPurchaseRequest(final Long amount) {
            final String transactionId = MoreStrings.randomNumber(12);
            return Payment.PurchaseRequest
                .builder()
                .transactionId(transactionId)
                .returnUrl(StringEx.format("{}book/{}/transaction", Urls.getClientSidePath(), transactionId))
                .amount(amount)
                .type(Payment.PurchaseType.ATM)
                .displayLanguage(Payment.DisplayLanguage.VIETNAMESE)
                .build();
        }

        @NotNull
        private final Timestamp fromDate;
        @NotNull
        private final Timestamp toDate;
        @NotNull
        private final List<BookingRoomDetails> details;

        @NotBlank(message = "Xin vui lòng nhập số điện thoại")
        @jakarta.validation.constraints.Pattern(regexp = "^(0?)(3[2-9]|5[6|8|9]|7[0|6-9]|8[0-6|8|9]|9[0-4|6-9])[0-9]{7}$", message = "Số điện thoại không hợp lệ.")
        private final String phone;

        @Length(min = 4, max = 64, message = "Xin vui lòng nhập tên liên hệ trong khoảng 4 đến 64 ký tự")
        private final String contact;

        @Length(max = 400, message = "Xin vui lòng nhập ghi chú trong khoảng 400 ký tự")
        private final String note;

        private final Integer statement;

        public BookingRequest toBookingRequest() {
            return
                BookingRequest
                    .builder()
                    .contact(this.getPhone())
                    .contactName(contact)
                    .note(note)
                    .status(PENDING)
                    .totalRooms(StreamEx.from(this.details).mapToInt(BookingRoomDetails::getTotalRooms).sum())
                    .checkinAt(this.fromDate)
                    .checkoutAt(this.toDate)
                    .build();
        }

        public List<BookingRequestDetail> toBookingRoomDetails() {
            return this.details.stream().map(BookingRoomDetails::toDetails).toList();
        }
    }

    @Builder
    @Getter
    @FieldNameConstants
    public static class BookingRoomDetails {
        private final Integer totalRooms;
        private final Integer roomId;

        public BookingRequestDetail toDetails() {
            return BookingRequestDetail.builder().roomId(this.roomId).totalRooms(this.totalRooms).build();
        }
    }

    @Component
    @RequiredArgsConstructor
    public static class Validator extends BaseValidator<Request> {

        private final RoomRepository roomRepository;
        @Override
        protected void validate() {
            this.rejectIfEmpty(Request.Fields.fromDate, this::validateFromDate);
            this.rejectIfEmpty(Request.Fields.toDate, this::validateToDate);
            this.rejectIfEmpty(Request.Fields.details, this::validateDetails);
        }

        private String validateFromDate() {
            if (this.request.fromDate.before(Timestamp.from(Instant.now()))) {
                return "Xin vui lòng chọn ngày nhận phòng sau ngày hiện tại.";
            }
            return null;
        }

        private String validateToDate() {
            final LocalDateTime today = LocalDateTime.now();
            if (this.request.toDate.toLocalDateTime().isBefore(LocalDateTimes.endDayOf(today))) {
                return "Xin vui lòng chọn ngày trả phòng sau ngày hiện tại.";
            }
            if (!this.request.fromDate.before(this.request.toDate)) {
                return "Xin vui lòng chọn ngày trả phòng sau ngày nhận phòng.";
            }
            return null;
        }

        private String validateDetails() {
            if (ListEx.listOf(this.request.getDetails(), BookingRoomDetails::getTotalRooms).stream().anyMatch(x -> x <= 0)) {
                return "Tổng số phòng phải lớn hơn 0.";
            }

            final List<Integer> roomIds = ListEx.listOf(this.request.getDetails(), BookingRoomDetails::getRoomId);
            if (roomIds.isEmpty()) {
                return "Xin vui lòng chọn một phòng để thực hiện yêu cầu đặt phòng";
            }

            final List<Room> rooms = this.roomRepository.findAllById(roomIds);

            DataContext.store("rooms", rooms);

            final List<RoomResponse> searchRooms = RedisRepository
                .get("search-rooms-%s".formatted(Tokens.getTokenFrom(MoreRequests.getCurrentHttpRequest())), new TypeReference<List<RoomResponse>>() {
                })
                .orElseThrow(() -> ApiException.forbidden("Có gì đó không ổn. Xin vui lòng thử lại bằng cách quay lại trang tìm kiếm."));

            final List<Integer> invalidRoomIds = findInvalidRoomIds(roomIds, searchRooms);
            if (!invalidRoomIds.isEmpty()) {
                return "Các phòng sau không tồn tại: %s".formatted(invalidRoomIds);
            }

            final List<String> invalidRooms = this.findRoomsWhichIsBookingMoreThanAvailableRooms(searchRooms);
            if (!invalidRooms.isEmpty()) {
                return "Các phòng sau hiện tại đã hết phòng: %s. Xin vui lòng chọn phòng khác.".formatted(invalidRooms);
            }

            return null;
        }

        private List<String> findRoomsWhichIsBookingMoreThanAvailableRooms(final List<RoomResponse> rooms) {
            String token = Tokens.getTokenFrom(MoreRequests.getCurrentHttpRequest());
            SearchAccommodationAvailableRoomsCommand.Request searchRequest = RedisRepository.get("search-request-%s".formatted(token), new TypeReference<SearchAccommodationAvailableRoomsCommand.Request>() {
            }).orElseThrow(() -> ApiException.forbidden("Xin hãy quay lại trang tìm kiếm để đặt phòng lại."));

            List<AvailableRoomRecord> availableRoom = SpringBeans.getBean(SearchAccommodationAvailableRoomsCommand.class).searchAvailableRooms(searchRequest);

            final Map<Integer, Integer> totalRoomsLeftMapToRoomId = MapEx.mapOf(availableRoom, AvailableRoomRecord::getId, AvailableRoomRecord::getAvailableRooms);
            final Predicate<BookingRoomDetails> isBookingMoreThanAvailableRooms = room -> {
                final Integer availableRooms = totalRoomsLeftMapToRoomId.get(room.getRoomId());
                return room.getTotalRooms() > availableRooms;
            };
            final Set<Integer> invalidRoomsThatBookingMoreThanAvailableRooms = ListEx.listOf(this.request.getDetails())
                .stream()
                .filter(isBookingMoreThanAvailableRooms)
                .map(BookingRoomDetails::getRoomId)
                .collect(Collectors.toUnmodifiableSet());

            return rooms.stream()
                .filter(room -> invalidRoomsThatBookingMoreThanAvailableRooms.contains(room.getId()))
                .map(RoomResponse::getName)
                .toList();
        }

        private static List<Integer> findInvalidRoomIds(final List<Integer> roomIds, final List<RoomResponse> rooms) {
            final List<Integer> existingRoomIds = ListEx.listOf(rooms, RoomResponse::getId);
            return ListEx.inRightListOnly(existingRoomIds, roomIds);
        }

    }

    @Scheduled(fixedRate = 15, timeUnit = TimeUnit.SECONDS)
    public void cancelPendingRequestsAfterNotPayed() {
        final boolean isLocal = StringEx.isBlank(System.getenv("ENVIRONMENT")) || Objects.equals(System.getenv("ENVIRONMENT"), "LOCAL");
        if (isLocal) {
            return;
        }
        final List<BookingRequest> accommodations = this.bookingRequestRepository.findAllByStatusIn(List.of(PENDING));
        Timestamp timeToCancel = Timestamp.valueOf(LocalDateTime.now().minusMinutes(15));
        final List<BookingRequest> requestsToCancelDueToOutOfTime = accommodations
            .stream()
            .filter(request -> request.getCreatedAt().before(timeToCancel))
            .map(request -> request.withStatus(UN_PURCHASED))
            .toList();
        this.bookingRequestRepository.saveAll(requestsToCancelDueToOutOfTime);
        if (!requestsToCancelDueToOutOfTime.isEmpty()) {
            log.info("Cancel following booking requests: {}", ListEx.listOf(requestsToCancelDueToOutOfTime, BookingRequest::getId));
        }
    }

    @Scheduled(fixedRate = 15, timeUnit = TimeUnit.SECONDS)
    public void payBackMoneyToHouseOwnerAfterUserHasSuccessfullyUsedHouse() {
        final boolean isLocal = StringEx.isBlank(System.getenv("ENVIRONMENT")) || Objects.equals(System.getenv("ENVIRONMENT"), "LOCAL");
        if (isLocal) {
            return;
        }
        final List<BookingRequest> requestsToPay = this.getBookingRequestsThatNeedToPayToHouseOwner();
        this.bookingRequestRepository.saveAll(requestsToPay);

        final List<Transaction> transactions = getTransactionsToPayBackToHouseOwnerOf(requestsToPay);
        this.transactionRepository.saveAll(transactions);
        if (!transactions.isEmpty()) {
            log.info("Pay money to house owner after user had successfully used rooms: {}", ListEx.listOf(transactions, Transaction::getReceiverId));
        }
    }
    @org.jetbrains.annotations.NotNull
    private static List<Transaction> getTransactionsToPayBackToHouseOwnerOf(final List<BookingRequest> requestsToPay) {
        final Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        final String payDate = new SimpleDateFormat("yyyyMMddHHmmss").format(cld.getTime());

        return requestsToPay
            .stream()
            .map(request -> Transaction
                .builder()
                .amount((long) (request.getTransaction().getAmount() * 0.8))
                .paymentMethod(request.getTransaction().getPaymentMethod())
                .transactionRequestNo(request.getTransaction().getTransactionRequestNo())
                .bankTransactionNo(request.getTransaction().getBankTransactionNo())
                .payDate(payDate)
                .receiverId(request.getAccommodation().getOwnerId())
                .build()
            ).collect(Collectors.toList());
    }

    private List<BookingRequest> getBookingRequestsThatNeedToPayToHouseOwner() {
        final List<BookingRequest> accommodations = this.bookingRequestRepository.findAllByStatusIn(List.of(PURCHASED));
        final Timestamp today = Timestamp.valueOf(LocalDateTimes.startDayOf(LocalDateTime.now()).plusMinutes(1));
        return accommodations
            .stream()
            .filter(request -> request.getCheckinAt().before(today))
            .map(request -> request.withStatus(SUCCEED))
            .toList();
    }

    @Getter
    @Builder
    @FieldNameConstants
    @Jacksonized
    public static class Response {
        private final Payment.PurchaseResponse request;
        private final Integer id;
    }
}
