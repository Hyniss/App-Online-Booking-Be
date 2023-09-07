package com.fpt.h2s.services.commands.responses;

import com.fpt.h2s.models.entities.Property;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@AllArgsConstructor
public class DetailAccommodationSetPropertiesResponse {
    Integer keyId;
    String key;
    Property.Type type;
    String value;
}
