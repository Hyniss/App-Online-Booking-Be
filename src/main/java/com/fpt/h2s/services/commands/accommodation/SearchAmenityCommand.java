package com.fpt.h2s.services.commands.accommodation;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fpt.h2s.models.domains.ApiResponse;
import com.fpt.h2s.models.domains.ListResult;
import com.fpt.h2s.models.domains.SearchRequest;
import com.fpt.h2s.models.entities.Category;
import com.fpt.h2s.repositories.CategoryRepository;
import com.fpt.h2s.services.commands.BaseCommand;
import com.fpt.h2s.services.commands.responses.CategoryResponse;
import com.fpt.h2s.utilities.Mappers;
import com.fpt.h2s.utilities.QueryValues;
import lombok.*;
import lombok.experimental.FieldNameConstants;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SearchAmenityCommand implements BaseCommand<SearchAmenityCommand.Request, ListResult<CategoryResponse>> {

    private final CategoryRepository categoryRepository;

    @Override
    public ApiResponse<ListResult<CategoryResponse>> execute(final Request request) {
        final Page<Category> page = this.categoryRepository.findAllByNameAndType(QueryValues.like(request.name), Category.Type.AMENITY.name(), request.toPageRequest());
        final ListResult<CategoryResponse> response = ListResult.of(page).map(x -> Mappers.convertTo(CategoryResponse.class, x));
        return ApiResponse.success(response);
    }

    @Getter
    @Builder(toBuilder = true)
    @FieldNameConstants
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, creatorVisibility = JsonAutoDetect.Visibility.ANY)
    public static class Request extends SearchRequest {
        private String orderBy;
        private Boolean isDescending;
        private Integer size;
        private Integer page;
        private String name;

    }
}
