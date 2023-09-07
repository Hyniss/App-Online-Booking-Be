package com.fpt.h2s.repositories;

import com.fpt.h2s.models.entities.EmailTemplate;
import com.fpt.h2s.models.exceptions.NoImplementationException;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.FluentQuery;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public interface EmailTemplateRepository extends JpaRepository<EmailTemplate, EmailTemplate.Key> {
    
    @Override
    default void flush() {
        throw new NoImplementationException();
    }
    
    @Override
    default <S extends EmailTemplate> @NotNull S saveAndFlush(@NotNull final S entity) {
        throw new NoImplementationException();
    }
    
    @Override
    default <S extends EmailTemplate> @NotNull List<S> saveAllAndFlush(@NotNull final Iterable<S> entities) {
        throw new NoImplementationException();
    }
    
    @Override
    default void deleteInBatch(@NotNull final Iterable<EmailTemplate> entities) {
        throw new NoImplementationException();
    }
    
    @Override
    default void deleteAllInBatch(@NotNull final Iterable<EmailTemplate> entities) {
        throw new NoImplementationException();
    }
    
    @Override
    @Deprecated
    default void deleteAllByIdInBatch(@NotNull final Iterable<EmailTemplate.Key> ids) {
        throw new NoImplementationException();
    }
    
    @Override
    @Deprecated
    default void deleteAllInBatch() {
        throw new NoImplementationException();
    }
    
    @Override
    @Deprecated
    default @NotNull EmailTemplate getOne(@NotNull final EmailTemplate.Key id) {
        throw new NoImplementationException();
    }
    
    @Override
    @Deprecated
    default @NotNull EmailTemplate getById(@NotNull final EmailTemplate.Key id) {
        throw new NoImplementationException();
    }
    
    @Override
    @Deprecated
    default @NotNull EmailTemplate getReferenceById(@NotNull final EmailTemplate.Key id) {
        throw new NoImplementationException();
    }
    
    @Override
    @Deprecated
    default <S extends EmailTemplate> @NotNull List<S> findAll(@NotNull final Example<S> example) {
        throw new NoImplementationException();
    }
    
    @Override
    @Deprecated
    default <S extends EmailTemplate> @NotNull List<S> findAll(@NotNull final Example<S> example, @NotNull final Sort sort) {
        throw new NoImplementationException();
    }
    
    @Override
    @Deprecated
    default <S extends EmailTemplate> @NotNull List<S> saveAll(@NotNull final Iterable<S> entities) {
        throw new NoImplementationException();
    }
    
    @Override
    @Deprecated
    default @NotNull List<EmailTemplate> findAll() {
        throw new NoImplementationException();
    }
    
    @Override
    @Deprecated
    default @NotNull List<EmailTemplate> findAllById(@NotNull final Iterable<EmailTemplate.Key> ids) {
        throw new NoImplementationException();
    }
    
    @Override
    @Deprecated
    default <S extends EmailTemplate> @NotNull S save(@NotNull final S entity) {
        throw new NoImplementationException();
    }
    
    @Override
    @Deprecated
    default boolean existsById(@NotNull final EmailTemplate.Key id) {
        throw new NoImplementationException();
    }
    
    @Override
    @Deprecated
    default long count() {
        throw new NoImplementationException();
    }
    
    @Override
    @Deprecated
    default void deleteById(@NotNull final EmailTemplate.Key id) {
        throw new NoImplementationException();
    }
    
    @Override
    @Deprecated
    default void delete(@NotNull final EmailTemplate entity) {
        throw new NoImplementationException();
    }
    
    @Override
    @Deprecated
    default void deleteAllById(@NotNull final Iterable<? extends EmailTemplate.Key> strings) {
        throw new NoImplementationException();
    }
    
    @Override
    @Deprecated
    default void deleteAll(@NotNull final Iterable<? extends EmailTemplate> entities) {
        throw new NoImplementationException();
    }
    
    @Override
    @Deprecated
    default void deleteAll() {
        throw new NoImplementationException();
    }
    
    @Override
    @Deprecated
    default @NotNull List<EmailTemplate> findAll(@NotNull final Sort sort) {
        throw new NoImplementationException();
    }
    
    @Override
    @Deprecated
    default @NotNull Page<EmailTemplate> findAll(@NotNull final Pageable pageable) {
        throw new NoImplementationException();
    }
    
    @Override
    @Deprecated
    default <S extends EmailTemplate> @NotNull Optional<S> findOne(@NotNull final Example<S> example) {
        throw new NoImplementationException();
    }
    
    @Override
    @Deprecated
    default <S extends EmailTemplate> @NotNull Page<S> findAll(@NotNull final Example<S> example, @NotNull final Pageable pageable) {
        throw new NoImplementationException();
    }
    
    @Override
    @Deprecated
    default <S extends EmailTemplate> long count(@NotNull final Example<S> example) {
        throw new NoImplementationException();
    }
    
    @Override
    @Deprecated
    default <S extends EmailTemplate> boolean exists(@NotNull final Example<S> example) {
        throw new NoImplementationException();
    }
    
    @Override
    @Deprecated
    default <S extends EmailTemplate, R> @NotNull R findBy(@NotNull final Example<S> example, @NotNull final Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) {
        throw new NoImplementationException();
    }
    
}