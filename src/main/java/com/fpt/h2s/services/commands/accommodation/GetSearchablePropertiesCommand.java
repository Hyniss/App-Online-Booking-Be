package com.fpt.h2s.services.commands.accommodation;

import com.fpt.h2s.models.domains.ApiResponse;
import com.fpt.h2s.models.entities.Property;
import com.fpt.h2s.repositories.PropertyRepository;
import com.fpt.h2s.services.commands.BaseCommand;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldNameConstants;
import lombok.extern.jackson.Jacksonized;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetSearchablePropertiesCommand implements BaseCommand<Void, GetSearchablePropertiesCommand.Response> {

    private final PropertyRepository propertyRepository;

    @Override
    public ApiResponse<Response> execute(final Void request) {
        final List<Property> properties = this.propertyRepository.findAll();
        return ApiResponse.success(Response.builder().properties(properties).build());
    }

    @Getter
    @Builder(toBuilder = true)
    @FieldNameConstants
    @Jacksonized
    public static class Response {
        private List<Property> properties;
    }
}
