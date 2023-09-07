package com.fpt.h2s.services.commands.accommodation;

//@Log4j2
//@Transactional
//@RequiredArgsConstructor
//@Service
//public class BookingAccommodationCommand implements BaseCommand<BookingAccommodationCommand.Request, Void> {
//
//    private final RoomRepository roomRepository;
//    private final BookingRequestRepository bookingRequestRepository;
//    private final BookingRequestServiceRepository bookingRequestServiceRepository;
//    private final TransactionRepository transactionRepository;
//    private final BookingRequestDetailRepository bookingRequestDetailRepository;
//
//    @Override
//    public ApiResponse<Void> execute(final Request request) {
//        final BookingRequest bookingRequest = this.bookingRequestRepository.save(this.getBookingRequest(request));
//
//        final List<BookingRequestDetail> specificsToSave = this.getBookingRoomDetailsToSave(bookingRequest, request.toBookingRoomDetails());
//        this.bookingRequestDetailRepository.saveAll(specificsToSave);
//
////        final List<BookingRequestDetail> bookingRequestDetails = bookingRequest.getDetails().stream().map(detail -> detail.withBookingRequestId(bookingRequest.getId())).toList();
////        this.bookingRequestDetailRepository.saveAll(bookingRequestDetails);
//
//        this.updateTotalRoomsLeftForRooms();
//
//        return ApiResponse.success();
//    }
//
//
//}
