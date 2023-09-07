package com.fpt.h2s.services.commands.responses;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fpt.h2s.models.entities.Category;
import com.fpt.h2s.utilities.Mappers;
import lombok.*;
import lombok.experimental.FieldNameConstants;

@With
@Getter
@Builder(toBuilder = true)
@FieldNameConstants
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, creatorVisibility = JsonAutoDetect.Visibility.ANY)
public class CategoryResponse {
    private Integer id;

    private String name;

    private String image;

    private Category.Type type;

    public static CategoryResponse of(final Category category) {
        return Mappers.convertTo(CategoryResponse.class, category);
    }

}
