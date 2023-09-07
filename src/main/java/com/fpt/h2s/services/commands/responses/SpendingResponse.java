package com.fpt.h2s.services.commands.responses;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class SpendingResponse {
    private Integer month;
    private Long amount;

}
