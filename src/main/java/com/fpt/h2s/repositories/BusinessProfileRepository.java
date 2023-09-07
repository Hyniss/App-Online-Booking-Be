package com.fpt.h2s.repositories;

import com.fpt.h2s.models.entities.BusinessProfile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BusinessProfileRepository extends JpaRepository<BusinessProfile, Integer> {
}
