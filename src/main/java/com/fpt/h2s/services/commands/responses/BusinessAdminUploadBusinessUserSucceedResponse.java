package com.fpt.h2s.services.commands.responses;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class BusinessAdminUploadBusinessUserSucceedResponse {

    private String fileData;
    private List<Integer> responses;

    public static BusinessAdminUploadBusinessUserSucceedResponse of(String fileData, List<Integer> responses) {
        return BusinessAdminUploadBusinessUserSucceedResponse.builder()
                .fileData(fileData)
                .responses(responses)
                .build();
    }

}
