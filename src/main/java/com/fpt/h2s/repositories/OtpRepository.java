package com.fpt.h2s.repositories;

import com.fpt.h2s.models.entities.OtpDetail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OtpRepository extends JpaRepository<OtpDetail, Integer> {
    List<OtpDetail> findAllByOtpKey(String key);
}
