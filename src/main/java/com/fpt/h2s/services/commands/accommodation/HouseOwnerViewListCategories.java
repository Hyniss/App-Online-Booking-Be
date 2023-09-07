package com.fpt.h2s.services.commands.accommodation;

import com.fpt.h2s.models.domains.ApiResponse;
import com.fpt.h2s.models.entities.Category;
import com.fpt.h2s.repositories.CategoryRepository;
import com.fpt.h2s.services.commands.BaseCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HouseOwnerViewListCategories
        implements BaseCommand<Void, Map<Category.Type, List<Category>>> {

    private final CategoryRepository categoryRepository;
    @Override
    public ApiResponse<Map<Category.Type, List<Category>>> execute(Void request) {
        Map<Category.Type, List<Category>> categoriesByType = this.categoryRepository.findAll().stream()
                .collect(Collectors.groupingBy(Category::getType));
        return ApiResponse.success(categoriesByType);
    }
}
