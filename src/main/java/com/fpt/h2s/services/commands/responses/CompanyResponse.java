package com.fpt.h2s.services.commands.responses;

import com.fpt.h2s.models.entities.Company;
import com.fpt.h2s.utilities.Mappers;
import lombok.Builder;
import lombok.Getter;
import lombok.With;
import lombok.extern.jackson.Jacksonized;

import java.sql.Timestamp;
import java.util.List;

@Getter
@Builder(toBuilder = true)
@Jacksonized
@With
public class CompanyResponse {
    private final Integer id;
    private final UserResponse owner;
    private final String name;
    private final String shortName;
    private final String quotaCode;
    private final Company.Size size;
    private final Company.Status status;
    private final String address;
    private final String contact;
    private final String contactName;
    private final int discount;
    private final Timestamp createdAt;
    private final Timestamp updatedAt;
    private final Boolean isEditable;
    private final List<String> rejectMessages;

    public static CompanyResponse of(final Company company) {
        return Mappers.convertTo(CompanyResponse.class, company);
    }
}
