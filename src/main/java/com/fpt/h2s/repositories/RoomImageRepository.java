package com.fpt.h2s.repositories;

import com.fpt.h2s.models.entities.RoomImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface RoomImageRepository extends JpaRepository<RoomImage, Integer> {
    List<RoomImage> findAllByRoomIdIn(Collection<Integer> roomIds);
}
