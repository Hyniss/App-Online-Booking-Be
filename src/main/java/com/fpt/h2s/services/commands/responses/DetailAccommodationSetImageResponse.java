package com.fpt.h2s.services.commands.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@AllArgsConstructor
public class DetailAccommodationSetImageResponse {
    private Integer id;

    private Integer typeId;

    private String name;

    private String url;
}
