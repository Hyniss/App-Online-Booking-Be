package com.fpt.h2s.repositories;

import com.fpt.h2s.models.entities.TravelStatement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

public interface TravelStatementRepository extends BaseRepository<TravelStatement, Integer> {
    Page<TravelStatement> findAll(Specification<TravelStatement> specification, Pageable pageable);
}
