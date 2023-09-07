package com.fpt.h2s.repositories;

import com.fpt.h2s.models.entities.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserProfileRepository extends JpaRepository<UserProfile, Integer> {
    boolean existsByAccountNo(String accountNo);
}
