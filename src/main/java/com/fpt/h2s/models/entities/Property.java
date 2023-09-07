package com.fpt.h2s.models.entities;

import jakarta.persistence.*;
import com.fpt.h2s.models.entities.converters.JPAEnumConverter;
import lombok.*;
import lombok.experimental.FieldNameConstants;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.springframework.stereotype.Component;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@FieldNameConstants
@ToString
/* Jpa */
@Entity
@DynamicInsert
@DynamicUpdate
@Table(name = "property_keys")
public class Property {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String value;

    @Convert(converter = Type.Converter.class)
    private Type type;

    private boolean isSearchable;


    public enum Type {
        ROOM, AMENITY;

        @Component
        public static class Converter extends JPAEnumConverter<Type> {
        }
    }
}
