package com.fpt.h2s.models.entities;

import com.fpt.h2s.models.entities.listeners.CreatorIdListener;
import jakarta.persistence.Table;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldNameConstants;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.*;

import java.sql.Timestamp;

@With
@Getter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@FieldNameConstants
@ToString
/* Jpa */
@Entity
@DynamicInsert
@DynamicUpdate
@EntityListeners({CreatorIdListener.class})
@BatchSize(size = 50)
@Table(name = "notification_histories")
public class NotificationHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Integer userId;

    private String payload;

    private Timestamp readAt;
    @CreationTimestamp
    private Timestamp createdAt;

    @UpdateTimestamp
    private Timestamp updatedAt;

    public boolean isUnread() {
        return getReadAt() == null;
    }
}
