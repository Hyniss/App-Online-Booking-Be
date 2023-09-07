package com.fpt.h2s.repositories;

import com.fpt.h2s.models.entities.RoomProperty;
import com.fpt.h2s.repositories.projections.RoomPropertyDetail;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface RoomPropertyRepository extends JpaRepository<RoomProperty, RoomProperty.PK> {


    @Query(value = """
        SELECT property_keys.id, property_keys.value as 'key', property_keys.type, rooms_properties.room_id as roomId, rooms_properties.value
        FROM h2s.rooms_properties
        JOIN property_keys on property_keys.id = rooms_properties.key_id
        where room_id in :roomIds
        """, nativeQuery = true)
    List<RoomPropertyDetail> findAllPropertiesOfRooms(Collection<Integer> roomIds);

    @Query(value = "SELECT rp.key_id, p.value, p.type, rp.value " +
        "FROM rooms_properties rp " +
        "JOIN property_keys p ON rp.key_id = p.id " +
        "WHERE rp.room_id = :roomId", nativeQuery = true)
    Set<Object[]> findJoinedDataByRoomId(@Param("roomId") Integer roomId);

    List<RoomProperty> findAll(Specification<RoomProperty> criteria);
}
