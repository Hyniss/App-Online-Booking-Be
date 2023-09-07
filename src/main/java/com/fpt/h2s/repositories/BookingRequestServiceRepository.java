package com.fpt.h2s.repositories;

import com.fpt.h2s.models.entities.BookingRequestService;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingRequestServiceRepository extends JpaRepository<BookingRequestService, Integer> {
}
