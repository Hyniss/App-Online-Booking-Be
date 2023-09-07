package com.fpt.h2s.services.commands.responses;

import ananta.utility.StringEx;
import com.fpt.h2s.models.entities.Property;
import com.fpt.h2s.models.entities.RoomProperty;
import com.fpt.h2s.repositories.projections.RoomPropertyDetail;
import lombok.Builder;
import lombok.Getter;
import lombok.With;
import lombok.experimental.FieldNameConstants;
import lombok.extern.jackson.Jacksonized;

import java.util.Objects;

@With
@Getter
@Builder
@FieldNameConstants
@Jacksonized
public class DetailedRoomProperty {
    private Integer id;
    private String value;
    private Property.Type type;

    public static DetailedRoomProperty of(final RoomPropertyDetail detail) {
        if (Objects.equals(detail.getValue(), "0")) {
            return null;
        }
        return builder()
            .id(detail.getId())
            .value(getFormattedMessage(detail.getType(), detail.getKey(), detail.getValue()))
            .type(detail.getType())
            .build();
    }

    private static String getFormattedMessage(Property.Type type, String key, String value) {
        if (type == Property.Type.ROOM) {
            return value + " " + key;
        }
        return StringEx.format("Mỗi phòng có {} {}", value, key);
    }

    public static DetailedRoomProperty of(RoomProperty detail) {
        if (Objects.equals(detail.getValue(), "0")) {
            return null;
        }
        return builder()
            .id(detail.getId().getKeyId())
            .value(getFormattedMessage(detail.getProperty().getType(), detail.getProperty().getValue(), detail.getValue()))
            .type(detail.getProperty().getType())
            .build();
    }

}
