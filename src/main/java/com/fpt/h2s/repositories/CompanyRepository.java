package com.fpt.h2s.repositories;

import com.fpt.h2s.models.entities.Company;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface CompanyRepository extends BaseRepository<Company, Integer> {
    Page<Company> findAll(Specification<Company> specification, Pageable pageable);
    boolean existsByNameIgnoreCase(String name);
    Optional<Company> findByName(String company);

    @Query(nativeQuery = true, value = "SELECT * FROM companies where upper(name) = upper(:company) LIMIT 1")
    Optional<Company> findOneByNameIgnoreCase(String company);

}
