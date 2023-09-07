package com.fpt.h2s.services.commands.boookingrequest;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fpt.h2s.models.domains.ApiResponse;
import com.fpt.h2s.models.domains.BaseRequest;
import com.fpt.h2s.models.domains.BaseValidator;
import com.fpt.h2s.models.entities.BookingRequest;
import com.fpt.h2s.models.entities.EmailTemplate;
import com.fpt.h2s.models.entities.User;
import com.fpt.h2s.models.exceptions.ApiException;
import com.fpt.h2s.repositories.BookingRequestRepository;
import com.fpt.h2s.repositories.UserRepository;
import com.fpt.h2s.services.NotificationService;
import com.fpt.h2s.services.PaymentService;
import com.fpt.h2s.services.commands.BaseCommand;
import com.fpt.h2s.services.commands.responses.BookingRequestResponse;
import com.fpt.h2s.workers.MailWorker;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import lombok.experimental.FieldNameConstants;
import org.hibernate.validator.constraints.Length;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Objects;
import java.util.Set;


@Service
@RequiredArgsConstructor
public class ChangeStatusBookingRequestCommand implements BaseCommand<ChangeStatusBookingRequestCommand.ChangeStatusBookingRequestRequest, BookingRequestResponse> {
    private final BookingRequestRepository bookingRequestRepository;
    private final UserRepository userRepository;
    private final MailWorker mailWorker;
    private final PaymentService paymentService;
    private final NotificationService notificationService;

    private static final Map<BookingRequest.Status, Set<BookingRequest.Status>> nextStatusesMapToCurrentStatus = Map.ofEntries(
            Map.entry(BookingRequest.Status.PURCHASED, Set.of(BookingRequest.Status.CANCELED))
    );

    @Override
    public ApiResponse<BookingRequestResponse> execute(ChangeStatusBookingRequestCommand.ChangeStatusBookingRequestRequest request) {
        final BookingRequest bookingRequest = this.bookingRequestRepository
                .findById(request.getId())
                .orElseThrow(() -> ApiException.badRequest("Không tìm thấy yêu cầu đặt phòng."));

        Integer currentUserId = User.currentUserId()
                .orElseThrow();

        final User currentUser = this.userRepository
                .findById(currentUserId)
                .orElseThrow(() -> ApiException.badRequest("Không thể tìm thấy người dùng với id = {}.", currentUserId));

        if(currentUser.is(User.Role.CUSTOMER)) {
            if(!Objects.equals(bookingRequest.getUserId(), currentUserId)) {
                throw ApiException.badRequest("Yêu cầu đặt phòng này không thuộc quyền quản lí của bạn.");
            }
        }
        else if(currentUser.is(User.Role.BUSINESS_ADMIN)) {
            if(!Objects.equals(bookingRequest.getUser().getCompanyId(), currentUser.getCompanyId())) {
                throw ApiException.badRequest("Yêu cầu đặt phòng này không thuộc quyền quản lí của bạn.");
            }
        }

        final BookingRequest.Status currentStatus = bookingRequest.getStatus();
        final BookingRequest.Status nextStatus = request.getStatus();

        if (!ChangeStatusBookingRequestCommand.nextStatusesMapToCurrentStatus.containsKey(currentStatus)) {
            throw ApiException.badRequest("Không thể thay đổi trạng thái của yêu cầu đặt phòng.");
        }
        final Set<BookingRequest.Status> availableStatusesToChange = ChangeStatusBookingRequestCommand.nextStatusesMapToCurrentStatus.get(currentStatus);
        if (!availableStatusesToChange.contains(nextStatus)) {
            throw ApiException.badRequest("Trạng thái mới phải là một trong những trạng thái sau đây {}", availableStatusesToChange);
        }

        //refund
//        final Integer transactionId = bookingRequest.getTransactionId();
//        int percentage = 100;
//
//        long threeDaysInMillis = 3L * 24 * 60 * 60 * 1000; // 3 days
//        long threeDaysBeforeMillis = bookingRequest.getCheckinAt().getTime() - threeDaysInMillis;
//        Timestamp threeDaysBeforeCheckin = new Timestamp(threeDaysBeforeMillis);
//        if(threeDaysBeforeCheckin.before(Timestamp.from(Instant.now()))){
//            percentage = 30;
//        }
//
//        Transaction transaction = SpringBeans.getBean(TransactionRepository.class).getById(transactionId);
//        PaymentService.RefundRequest refundRequest = PaymentService.RefundRequest
//                .builder()
//                .percentage(percentage)
//                .username(request.getCardHolder().toUpperCase())
//                .transaction(transaction)
//                .build();
//        this.paymentService.refund(refundRequest);

        BookingRequest bookingRequestToChange = bookingRequest.toBuilder().status(request.getStatus()).build();
        this.bookingRequestRepository.save(bookingRequestToChange);

        if(currentUser.is(User.Role.BUSINESS_ADMIN)) {
            sendMail(bookingRequestToChange, currentUser);
            notificationService.send(notification -> notification.toUsers(bookingRequestToChange.getCreatorId()).withContent("Yêu cầu đặt phòng cho đơn " + ((bookingRequestToChange.getTravelStatement() == null) ? bookingRequestToChange.getTravelStatement().getName() : "") + " của bạn đã bị hủy bởi quản trị viên của công ty."));
        }

        notificationService.send(notification -> notification.toUsers(bookingRequestToChange.getAccommodation().getCreatorId()).withContent("Yêu cầu đặt phòng cho đơn với id = " + bookingRequest.getId() + " đã bị hủy."));

        return ApiResponse.success("Thay đổi trạng thái yêu cầu đặt phòng thành công.", BookingRequestResponse.of(bookingRequestToChange));
    }

    public void sendMail(BookingRequest bookingRequest, User currentUser) {
        this.mailWorker.sendMail(
                mail -> mail
                        .sendTo(bookingRequest.getUser().getEmail())
                        .withTemplate(EmailTemplate.Key.BUSINESS_CANCEL_BOOKING_REQUEST)
                        .withProperty("username", bookingRequest.getUser().getUsername())
                        .withProperty("travelStatement", bookingRequest.getTravelStatement().getName())
                        .withProperty("status", bookingRequest.getStatus())
                        .withProperty("businessAdmin", currentUser.getUsername())
                        .withSuccessMessage("Send mail successfully.")
        );
    }

    @Getter
    @Builder(toBuilder = true)
    @FieldNameConstants
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, creatorVisibility = JsonAutoDetect.Visibility.ANY)
    public static class ChangeStatusBookingRequestRequest extends BaseRequest {
        @NonNull
        private final Integer id;
        @NonNull
        private final BookingRequest.Status status;

        @NotBlank(message = "Xin vui lòng nhập tên tài khoản/tên chủ thẻ.")
        @Length(min = 4, max = 60, message = "Vui lòng nhập tên chủ thẻ trong khoảng 4 đến 60 kí tự.")
        @Pattern(regexp="^[a-zA-Z\\s]*$", message = "Tên tài khoản/chủ thẻ chỉ có tiếng việt không dấu, không có số hoặc kí tự đặc biệt.")
        private final String cardHolder;

        @Component
        @RequiredArgsConstructor
        public static class Validator extends BaseValidator<ChangeStatusBookingRequestCommand.ChangeStatusBookingRequestRequest> {
            private final BookingRequestRepository bookingRequestRepository;

            @Override
            protected void validate() {
                this.rejectIfEmpty(ChangeStatusBookingRequestCommand.ChangeStatusBookingRequestRequest.Fields.id, this::validateBookingRequestId);
            }

            private String validateBookingRequestId() {

                this.bookingRequestRepository
                        .findById(this.request.id)
                        .orElseThrow(() -> ApiException.badRequest("Không tìm thấy yêu cầu đặt phòng."));

                return null;
            }

        }
    }
}
