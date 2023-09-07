package com.fpt.h2s.repositories;

import com.fpt.h2s.models.entities.Transaction;
import com.fpt.h2s.services.commands.responses.SpendingResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Integer> {
    @Query("SELECT new com.fpt.h2s.services.commands.responses.SpendingResponse(MONTH(t.createdAt), SUM(t.amount)) FROM Transaction t, BookingRequest b, User u where YEAR(t.createdAt) = :currentYear and u.id = b.userId and b.transactionId = t.id and u.id = :userId GROUP BY MONTH(t.createdAt)")
    List<SpendingResponse> sumAmountByMonth(@Param("currentYear") int currentYear, @Param("userId") int userId);

    Optional<Transaction> findByTransactionRequestNo(String transactionNo);


    @Query(nativeQuery = true, value = """
        SELECT * FROM transactions where creator_id = :userId or receiver_id = :userId
        """)
    Page<Transaction> findAllOfUser(int userId, Pageable pageable);

    Page<Transaction> findAll(Specification<Transaction> specification, Pageable pageable);
}
