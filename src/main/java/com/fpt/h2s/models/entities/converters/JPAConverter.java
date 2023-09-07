package com.fpt.h2s.models.entities.converters;

import com.fpt.h2s.utilities.Generalizable;
import jakarta.persistence.AttributeConverter;

public abstract class JPAConverter<T, V> extends Generalizable<T> implements AttributeConverter<T, V> {}