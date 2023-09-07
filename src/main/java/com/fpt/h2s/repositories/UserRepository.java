package com.fpt.h2s.repositories;

import ananta.utility.ListEx;
import com.fpt.h2s.models.entities.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.Query;

import java.sql.Timestamp;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public interface UserRepository extends BaseRepository<User, Integer> {
    boolean existsByEmail(String email);
    boolean existsByPhone(String phone);

    boolean existsByUsername(String username);

    @Query(nativeQuery = true, value = """
        select count(*) from users
        """)
    Integer countAll();

    @Query(nativeQuery = true, value = """
        select count(*) from users where created_at > :time
        """)
    Integer countAllSince(Timestamp time);

    Page<User> findAll(Specification<User> specification, Pageable pageable);

    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String email);
    Optional<User> findByPhone(String phone);

    @Query(nativeQuery = true, value = """
        SELECT * FROM users where phone LIKE :phone limit 1
        """)
    Optional<User> findByPhoneEndingWith(String phone);

    @Query(nativeQuery = true, value = """ 
        SELECT * FROM users where roles = :role
        """)
    List<User> findAllUsersByRole(String role);

    default <R> Map<Integer, User> findAllByIdsIn(final Collection<R> collection, final Function<R, Integer> function) {
        final List<Integer> ids = ListEx.listOf(collection).stream().map(function).filter(Objects::nonNull).toList();
        return this.findAllById(ids).stream().collect(Collectors.toMap(User::getId, x -> x));
    }

    default <R, X> Map<Integer, X> findAllByIdsIn(final Collection<R> collection, final Function<R, Integer> function, Function<User, X> valueFunction) {
        final List<Integer> ids = ListEx.listOf(collection).stream().map(function).filter(Objects::nonNull).toList();
        return this.findAllById(ids).stream().collect(Collectors.toMap(User::getId, valueFunction));
    }
}
