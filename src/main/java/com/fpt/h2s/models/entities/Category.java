package com.fpt.h2s.models.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fpt.h2s.models.entities.converters.JPAEnumConverter;
import com.fpt.h2s.models.entities.listeners.CreatorIdListener;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldNameConstants;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.springframework.stereotype.Component;

import java.util.Set;

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
@Table(name = "categories")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;

    private String image;
    @Convert(converter = Type.Converter.class)
    private Type type;

    @JsonIgnore
    @ManyToMany(mappedBy = "categories")
    private Set<Accommodation> accommodations;
    
    public enum Type {
        LOCATION, AMENITY;
    
        @Component
        public static class Converter extends JPAEnumConverter<Type> {
        }
    }

}
