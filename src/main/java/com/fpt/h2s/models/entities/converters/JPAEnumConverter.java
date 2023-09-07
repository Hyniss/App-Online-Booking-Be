package com.fpt.h2s.models.entities.converters;

import ananta.utility.ListEx;
import ananta.utility.MapEx;
import ananta.utility.StringEx;
import com.fpt.h2s.utilities.MoreStrings;

import java.util.Map;

public class JPAEnumConverter<T extends Enum<T>> extends JPAConverter<T, String> {

    private Map<String, T> enumValueMap;

    @Override
    public String convertToDatabaseColumn(final T value) {
        if (value == null) {
            return null;
        }
        return value.name();
    }

    @Override
    public T convertToEntityAttribute(final String databaseValue) {
        if (this.enumValueMap == null) {
            this.enumValueMap = MapEx.mapOf(ListEx.listOf(getGenericClass().getEnumConstants()), Enum::name);
        }

        if (StringEx.isBlank(databaseValue)) {
            return null;
        }

        final T foundValue = this.enumValueMap.get(databaseValue);

        if (foundValue != null) {
            return foundValue;
        }

        // It seems like database value is in wrong format, so we need to modify it to the SCREAM_SNAKE_CASE
        // and find with the modified value.
        final String name = MoreStrings.screamSnakeCaseOf(databaseValue);
        return this.enumValueMap.get(name);
    }
}
