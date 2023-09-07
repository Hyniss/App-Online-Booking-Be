package com.fpt.h2s.models.entities.listeners;

import ananta.utility.ReflectionEx;
import com.fpt.h2s.models.entities.User;
import jakarta.persistence.PrePersist;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Component
@RequiredArgsConstructor
public class CreatorIdListener {
    @PrePersist
    public void onPrePersist(final Object entity) {
        try {
            final Integer userId = User.currentUserId().orElse(null);
            if (userId == null) {
                return;
            }

            final String fieldName = User.Fields.creatorId;
            if (entity instanceof final Collection<?> collection) {
                collection.stream()
                    .filter(item -> ReflectionEx.findFieldValue(fieldName, item).isEmpty())
                    .forEach(item -> ReflectionEx.setFieldValue(item, fieldName, userId));
                return;
            }
            if (ReflectionEx.findFieldValue(fieldName, entity).isEmpty()) {
                ReflectionEx.setFieldValue(entity, fieldName, userId);
            }
        } catch (final Exception ignored) {
        }
    }
}

