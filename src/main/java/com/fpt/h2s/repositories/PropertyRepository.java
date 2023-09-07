package com.fpt.h2s.repositories;

import com.fpt.h2s.models.entities.Property;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PropertyRepository extends JpaRepository<Property, Integer> {
    List<Property> findAllByType(Property.Type type);
}
