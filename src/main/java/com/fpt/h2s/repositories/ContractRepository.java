package com.fpt.h2s.repositories;

import com.fpt.h2s.models.entities.Contract;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContractRepository extends JpaRepository<Contract, Integer> {
    Page<Contract> findAll(Specification<Contract> specification, Pageable pageable);

    Contract findContractByCreatorId(Integer creatorId);
}
