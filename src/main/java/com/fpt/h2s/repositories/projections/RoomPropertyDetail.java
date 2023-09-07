package com.fpt.h2s.repositories.projections;

import com.fpt.h2s.models.entities.Property;

public interface RoomPropertyDetail {
    Integer getId();
    String getKey();
    Integer getRoomId();
    String getValue();
    Property.Type getType();
}
