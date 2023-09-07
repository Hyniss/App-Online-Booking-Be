package com.fpt.h2s.models.entities;

import com.fpt.h2s.models.entities.converters.JPAEnumConverter;
import com.fpt.h2s.models.entities.listeners.CreatorIdListener;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldNameConstants;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Setter
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
@Table(name = "dates")
public class DayType {

    @Id
    private LocalDate date;

    @Convert(converter = DayType.Type.Converter.class)
    private Type type;

    public enum Type {
        WEEKDAY, SPECIAL_DAY, WEEKEND;

        @Component
        public static class Converter extends JPAEnumConverter<DayType.Type> {
        }

    }
}
