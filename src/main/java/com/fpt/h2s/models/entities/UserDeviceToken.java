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
@Table(name = "user_device_tokens")
public class UserDeviceToken {
    @EmbeddedId
    private PK id;

    @CreationTimestamp
    private Timestamp createdAt;

    @UpdateTimestamp
    private Timestamp updatedAt;

    @Embeddable
    @With
    @Getter
    @SuperBuilder(toBuilder = true)
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldNameConstants
    @ToString
    public static class PK {
        private Integer userId;
        private String token;
    }

}
