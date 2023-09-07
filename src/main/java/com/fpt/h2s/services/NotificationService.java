package com.fpt.h2s.services;

import ananta.utility.ListEx;
import ananta.utility.StringEx;
import com.fpt.h2s.utilities.LocalDateTimes;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;

public interface NotificationService {

    void send(Function<Notification.Builder, Notification.Builder> notification);

    @Getter
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    class Notification {
        private final String content;

        private final List<Integer> userIds;

        public static Builder to(final Collection<Integer> userIds) {
            return new Builder().toUsers(userIds);
        }

        public static Builder to(final Integer userId) {
            return new Builder().toUser(userId);
        }

        public static class Builder {
            private final List<Integer> userIds = new ArrayList<>();

            private String content;

            public Builder withContent(final String content, Object... args) {
                Objects.requireNonNull(content, "Content should not be null");
                this.content = StringEx.format(content, args).trim();
                return this;
            }

            public Builder toUser(final Integer userId) {
                assert userId != null : "User id must be not null";
                this.userIds.add(userId);
                return this;
            }

            public Builder toUsers(final Collection<Integer> userIds) {
                this.userIds.addAll(ListEx.nonNullListOf(userIds));
                return this;
            }


            public Builder toUsers(final Integer... userIds) {
                this.userIds.addAll(ListEx.nonNullListOf(userIds));
                return this;
            }

            public Notification build() {
                return new Notification(this.content, this.userIds);
            }
        }

    }
}
