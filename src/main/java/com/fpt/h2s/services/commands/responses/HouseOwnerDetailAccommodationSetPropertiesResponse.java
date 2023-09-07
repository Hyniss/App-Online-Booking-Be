package com.fpt.h2s.services.commands.responses;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@AllArgsConstructor
public class HouseOwnerDetailAccommodationSetPropertiesResponse {
    Integer keyId;
    String value;
}
