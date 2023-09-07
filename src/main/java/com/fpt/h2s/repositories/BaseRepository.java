package com.fpt.h2s.repositories;

import com.fpt.h2s.models.exceptions.ApiException;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;


@NoRepositoryBean
public interface BaseRepository<T, ID> extends JpaRepository<T, ID> {
    @SuppressWarnings("deprecation")
    default T getById(final ID id, final String message, @Nullable final Object... args) {
        return this.findById(id).orElseThrow(() -> ApiException.notFound(message, args));
    }
    
    @Override
    boolean existsById(@NonNull final ID id);
    
}
