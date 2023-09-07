package com.fpt.h2s.repositories;

import com.fpt.h2s.models.entities.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface CategoryRepository extends JpaRepository<Category, Integer> {
    List<Category> findAllByType(Category.Type type);

    @Query(nativeQuery = true, value = """
        SELECT * FROM categories
        where CONVERT(NAME USING utf8) LIKE _utf8 :name COLLATE utf8_general_ci and type = :type
        """)
    Page<Category> findAllByNameAndType(String name, String type, Pageable pageable);

    @Query(value = """
        SELECT * FROM h2s.categories where is_suggestion = true and type = 'AMENITY';
        """, nativeQuery = true)
    List<Category> findAllSuggestionAmenities();

    @Query(value = """
        SELECT room_id, category_id, name, image
        FROM room_amenities
        INNER JOIN categories ON room_amenities.category_id = categories.id
        WHERE
        	room_id in :roomIds AND type = 'AMENITY'
        ORDER BY is_suggestion desc, category_id
        """, nativeQuery = true)
    List<Map<String, Object>> findAllByRoomsIds(Collection<Integer> roomIds);

}