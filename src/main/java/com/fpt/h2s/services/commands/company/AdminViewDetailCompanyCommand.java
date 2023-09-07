package com.fpt.h2s.services.commands.company;

import com.fpt.h2s.models.domains.ApiResponse;
import com.fpt.h2s.models.domains.BaseRequest;
import com.fpt.h2s.models.entities.Company;
import com.fpt.h2s.repositories.CompanyRepository;
import com.fpt.h2s.services.commands.BaseCommand;
import com.fpt.h2s.services.commands.responses.CompanyResponse;
import com.fpt.h2s.services.commands.responses.UserResponse;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldNameConstants;
import lombok.extern.jackson.Jacksonized;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminViewDetailCompanyCommand implements BaseCommand<AdminViewDetailCompanyCommand.Request, CompanyResponse> {
    
    private final CompanyRepository companyRepository;
    
    @Override
    public ApiResponse<CompanyResponse> execute(final Request request) {
        final Integer companyId = request.getCompanyId();
        final Company company = this.companyRepository.findById(companyId).orElseThrow();
        final CompanyResponse response = CompanyResponse
            .of(company)
            .withIsEditable(false)
            .withOwner(UserResponse.of(company.getOwner()));
        return ApiResponse.success(response);
    }
    
    @Getter
    @Builder(toBuilder = true)
    @FieldNameConstants
    @Jacksonized
    public static class Request extends BaseRequest {
        private final Integer companyId;
    }
}
