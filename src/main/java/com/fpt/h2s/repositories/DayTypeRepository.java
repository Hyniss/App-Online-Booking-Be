package com.fpt.h2s.repositories;

import com.fpt.h2s.models.entities.DayType;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;

public interface DayTypeRepository extends JpaRepository<DayType, LocalDate> {
}
