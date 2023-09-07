package com.fpt.h2s.services.commands.responses;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fpt.h2s.models.entities.PriceHistory;
import com.fpt.h2s.repositories.projections.PriceHistoryType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@AllArgsConstructor
public class HouseOwnerViewDetailAccommodationAmountResponse {
    private Integer id;
    @JsonIgnore
    private Integer roomId;
    private PriceHistory.Type type;
    private PriceHistory.DayType dayType;
    private String fromDate;
    private String toDate;
    private Integer amount;
    private Integer price;

    public static HouseOwnerViewDetailAccommodationAmountResponse of(PriceHistoryType priceHistoryType) {
      return HouseOwnerViewDetailAccommodationAmountResponse.builder()
              .id(priceHistoryType.getId())
              .roomId(priceHistoryType.getRoomId())
              .type(priceHistoryType.getType())
              .dayType(priceHistoryType.getDayType())
              .fromDate(priceHistoryType.getFromDate())
              .toDate(priceHistoryType.getToDate())
              .amount(priceHistoryType.getAmount())
              .price(priceHistoryType.getPrice())
              .build();
    }
}
