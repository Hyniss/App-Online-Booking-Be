package com.fpt.h2s.repositories;

import com.fpt.h2s.models.entities.AccommodationImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface AccommodationImageRepository extends JpaRepository<AccommodationImage, Integer> {
    
    List<AccommodationImage> findAllByAccommodationIdIn(Collection<Integer> accommodationIds);

    Set<AccommodationImage> findAllByAccommodationId(Integer accommodationId);
    
}
