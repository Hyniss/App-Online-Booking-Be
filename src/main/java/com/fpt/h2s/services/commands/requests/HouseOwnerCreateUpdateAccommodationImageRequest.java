package com.fpt.h2s.services.commands.requests;


import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fpt.h2s.models.domains.BaseRequest;
import lombok.*;
import lombok.experimental.FieldNameConstants;

@Getter
@Builder
@FieldNameConstants
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, creatorVisibility = JsonAutoDetect.Visibility.ANY)
public class HouseOwnerCreateUpdateAccommodationImageRequest extends BaseRequest {

    private String name;

    private String url;

}