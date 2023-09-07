package com.fpt.h2s.models.entities;

import com.fpt.h2s.models.entities.converters.JPAEnumConverter;
import com.fpt.h2s.models.entities.listeners.CreatorIdListener;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldNameConstants;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@FieldNameConstants
@ToString
/* Jpa */
@Entity
@DynamicInsert
@DynamicUpdate
@SuperBuilder(toBuilder = true)
@EntityListeners({CreatorIdListener.class})
@Table(name = "price_histories")
public class PriceHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Long amount;

    private Integer roomId;

    @Convert(converter = PriceHistory.Type.Converter.class)
    private Type type;

    @Convert(converter = PriceHistory.DayType.Converter.class)
    private DayType dayType;

    private LocalDate fromDate;

    private LocalDate toDate;

    private Integer creatorId;

    @CreationTimestamp
    private Timestamp createdAt;

    @UpdateTimestamp
    private Timestamp updatedAt;

    private Timestamp deletedAt;

    public enum Type {
        PRICE, DISCOUNT;

        @Component
        public static class Converter extends JPAEnumConverter<PriceHistory.Type> {
        }

    }

    public enum DayType {
        WEEKDAY, SPECIAL_DAY, WEEKEND, CUSTOM;

        @Component
        public static class Converter extends JPAEnumConverter<PriceHistory.DayType> {
        }

    }
}
