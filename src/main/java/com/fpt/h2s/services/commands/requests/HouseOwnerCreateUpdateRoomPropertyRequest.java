package com.fpt.h2s.services.commands.requests;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fpt.h2s.models.domains.BaseRequest;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldNameConstants;

@Getter
@Builder
@FieldNameConstants
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, creatorVisibility = JsonAutoDetect.Visibility.ANY)
public class HouseOwnerCreateUpdateRoomPropertyRequest extends BaseRequest {

    private Integer keyId;

    private Integer value;
}
