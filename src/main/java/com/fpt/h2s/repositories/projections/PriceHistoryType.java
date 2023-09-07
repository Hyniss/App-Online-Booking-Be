package com.fpt.h2s.repositories.projections;

import com.fpt.h2s.models.entities.PriceHistory;
import java.time.LocalDate;

public interface PriceHistoryType {
    Integer getId();
    Integer getRoomId();
    PriceHistory.Type getType();
    PriceHistory.DayType getDayType();
    String getFromDate();
    String getToDate();
    Integer getAmount();
    Integer getPrice();
}
