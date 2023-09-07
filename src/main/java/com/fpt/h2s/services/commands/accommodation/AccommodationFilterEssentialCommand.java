package com.fpt.h2s.services.commands.accommodation;

import com.fpt.h2s.models.domains.ApiResponse;
import com.fpt.h2s.models.entities.Property;
import com.fpt.h2s.repositories.CategoryRepository;
import com.fpt.h2s.repositories.PropertyRepository;
import com.fpt.h2s.services.commands.BaseCommand;
import com.fpt.h2s.services.commands.responses.CategoryResponse;
import com.fpt.h2s.services.commands.responses.PropertyResponse;
import com.fpt.h2s.utilities.Caches;
import lombok.*;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AccommodationFilterEssentialCommand implements BaseCommand<Void, AccommodationFilterEssentialCommand.Response> {

    private final CategoryRepository categoryRepository;
    private final PropertyRepository propertyRepository;


    @Override
    public ApiResponse<Response> execute(final Void request) {
        final List<CategoryResponse> amenities = Caches.storeIfNotFound(
            "getSearchableAmenities", () -> this.categoryRepository
            .findAllSuggestionAmenities()
            .stream()
            .map(CategoryResponse::of)
            .toList(),
            Duration.ofMinutes(15)
        );

        final List<PropertyResponse> details = Caches.storeIfNotFound(
                "getSearchDetails", () -> this.propertyRepository
                        .findAll()
                        .stream()
                        .filter(Property::isSearchable)
                        .map(PropertyResponse::of)
                        .toList(),
            Duration.ofMinutes(15)
        );

        final Response response = Response.builder().amenities(amenities).details(details).build();
        return ApiResponse.success(response);
    }

    @AllArgsConstructor
    @Getter
    @Setter
    @Builder
    public static class Response {
        private final List<CategoryResponse> amenities;
        private final List<PropertyResponse> details;
    }
}
