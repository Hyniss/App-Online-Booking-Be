package com.fpt.h2s.repositories;

import com.fpt.h2s.models.entities.UserDeviceToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserDeviceTokenRepository extends JpaRepository<UserDeviceToken, UserDeviceToken.PK> {
    @Query(value = """
         SELECT * FROM user_device_tokens where user_id in :userIds
        """, nativeQuery = true)
    List<UserDeviceToken> findAllByUserIds(List<Integer> userIds);

    @Query(value = """
         SELECT * FROM user_device_tokens where user_id = :userId AND token = :token LIMIT 1
        """, nativeQuery = true)
    Optional<UserDeviceToken> findByUserIdAndToken(Integer userId, String token);
}
