package com.fpt.h2s.services.commands.responses;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fpt.h2s.models.entities.Property;
import com.fpt.h2s.utilities.Mappers;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;
import lombok.experimental.FieldNameConstants;

@With
@Getter
@Builder(toBuilder = true)
@FieldNameConstants
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, creatorVisibility = JsonAutoDetect.Visibility.ANY)
public class PropertyResponse {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String value;

    private Property.Type type;

    public static PropertyResponse of(final Property property) {
        return Mappers.convertTo(PropertyResponse.class, property);
    }
}
