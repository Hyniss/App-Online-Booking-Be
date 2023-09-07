package com.fpt.h2s.repositories;

import com.fpt.h2s.models.entities.NotificationHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface NotificationHistoryRepository extends JpaRepository<NotificationHistory, Integer> {

    @Query(nativeQuery = true, value = """
        SELECT * FROM notification_histories where user_id = :userId order by created_at desc
        """)
    List<NotificationHistory> findAllByUserId(Integer userId);

}
