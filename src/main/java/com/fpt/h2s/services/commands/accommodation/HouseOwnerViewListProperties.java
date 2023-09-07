package com.fpt.h2s.services.commands.accommodation;

import com.fpt.h2s.models.domains.ApiResponse;
import com.fpt.h2s.models.entities.Property;
import com.fpt.h2s.repositories.PropertyRepository;
import com.fpt.h2s.services.commands.BaseCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class HouseOwnerViewListProperties
        implements BaseCommand<Void, List<Property>> {

    private final PropertyRepository propertyRepository;
    @Override
    public ApiResponse<List<Property>> execute(Void request) {
        Map<Boolean, List<Property>> categoriesByType = this.propertyRepository.findAll().stream()
                .collect(Collectors.groupingBy(Property::isSearchable));
        return ApiResponse.success(categoriesByType.get(true));
    }
}
