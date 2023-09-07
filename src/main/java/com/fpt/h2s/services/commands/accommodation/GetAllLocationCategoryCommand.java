package com.fpt.h2s.services.commands.accommodation;

import com.fpt.h2s.models.domains.ApiResponse;
import com.fpt.h2s.models.entities.Category;
import com.fpt.h2s.repositories.CategoryRepository;
import com.fpt.h2s.services.commands.BaseCommand;
import com.fpt.h2s.services.commands.responses.CategoryResponse;
import com.fpt.h2s.utilities.Mappers;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class GetAllLocationCategoryCommand implements BaseCommand<Void, List<CategoryResponse>> {

    private final CategoryRepository categoryRepository;

    @Override
    public ApiResponse<List<CategoryResponse>> execute(final Void request) {
        final List<Category> categories = this.categoryRepository.findAllByType(Category.Type.LOCATION);
        final List<CategoryResponse> responses = categories.stream().map(c -> Mappers.convertTo(CategoryResponse.class, c)).toList();
        return ApiResponse.success(responses);
    }
}
