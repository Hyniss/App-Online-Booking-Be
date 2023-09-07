package com.fpt.h2s.utilities;

import org.springframework.data.jpa.domain.Specification;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public enum Criteria {
    
    CONTAINS {
        @Override
        public <T> Function<String, Specification<T>> value(final Object... value) {
            final String dbValue = "%" + Optional.ofNullable(value).map(x -> x[0]).map(Object::toString).map(String::trim).orElse("") + "%";
            return (field) -> (root, query, cb) -> cb.like(root.get(field), dbValue);
        }
    },
    
    STARTS_WITH {
        @Override
        public <T> Function<String, Specification<T>> value(final Object... value) {
            if (value.length == 0) {
                return (field) -> (root, query, cb) -> cb.isTrue(cb.literal(true));
            }
            
            final String dbValue = Optional.ofNullable(value[0]).map(Object::toString).map(String::trim).orElse("") + "%";
            return (field) -> (root, query, cb) -> cb.like(root.get(field), dbValue);
        }
    },
    
    ENDS_WITH {
        @Override
        public <T> Function<String, Specification<T>> value(final Object... value) {
            if (value.length == 0) {
                return (field) -> (root, query, cb) -> cb.isTrue(cb.literal(true));
            }
            final String dbValue = "%" + Optional.ofNullable(value[0]).map(Object::toString).map(String::trim).orElse("");
            return (field) -> (root, query, cb) -> cb.like(root.get(field), dbValue);
        }
    },
    
    EQUALS {
        @Override
        public <T> Function<String, Specification<T>> value(final Object... value) {
            final Object dbValue = this.cleanValue(value);
            if (dbValue == null) {
                return (field) -> (root, query, cb) -> cb.isTrue(cb.literal(true));
            }
            return (field) -> (root, query, cb) -> cb.equal(root.get(field), dbValue);
        }
        
        private Object cleanValue(final Object[] value) {
            if (value.length == 0) {
                return null;
            }
            final Object obj = value[0];
            if (obj instanceof String v) {
                return v.trim();
            }
            return obj;
        }
    },
    
    GREATER_THAN {
        @Override
        public <T> Function<String, Specification<T>> value(final Object... value) {
            if (value.length == 0) {
                return (field) -> (root, query, cb) -> cb.isTrue(cb.literal(true));
            }
            final long number = this.longOf(value);
            return (field) -> (root, query, cb) -> cb.greaterThan(root.get(field),  String.valueOf(number));
        }
        
        private long longOf(final Object[] value) {
            try {
                return Long.parseLong(value[0].toString());
            } catch (final Exception e) {
                return 0L;
            }
        }
    },
    
    LESS_THAN {
        @Override
        public <T> Function<String, Specification<T>> value(final Object... value) {
            if (value.length == 0) {
                return (field) -> (root, query, cb) -> cb.isTrue(cb.literal(true));
            }
            final long number = this.longOf(value);
            return (field) -> (root, query, cb) -> cb.lessThan(root.get(field), String.valueOf(number));
        }
        
        private long longOf(final Object[] value) {
            try {
                return Long.parseLong(value[0].toString());
            } catch (final Exception e) {
                return 0L;
            }
        }
    },
    
    IN {
        @Override
        public <T> Function<String, Specification<T>> value(final Object... value) {
            if (value.length == 0) {
                return (field) -> (root, query, cb) -> cb.isTrue(cb.literal(true));
            }
            return (field) -> (root, query, cb) -> root.get(field).in(List.of(value));
        }
    },
    
    BEFORE {
        @Override
        public <T> Function<String, Specification<T>> value(final Object... value) {
            if (value.length == 0) {
                return (field) -> (root, query, cb) -> cb.isTrue(cb.literal(true));
            }
            final Timestamp time = Timestamp.valueOf((String) value[0]);
            
            return (field) -> (root, query, cb) -> cb.lessThan(root.get(field), time);
        }
    },
    
    AFTER {
        @Override
        public <T> Function<String, Specification<T>> value(final Object... value) {
            if (value.length == 0) {
                return (field) -> (root, query, cb) -> cb.isTrue(cb.literal(true));
            }
            final Timestamp time = Timestamp.valueOf((String) value[0]);
            
            return (field) -> (root, query, cb) -> cb.greaterThan(root.get(field), time);
        }
    },
    BETWEEN {
        @Override
        public <T> Function<String, Specification<T>> value(final Object... value) {
            if (value.length < 2) {
                return (field) -> (root, query, cb) -> cb.isTrue(cb.literal(true));
            }
            final Timestamp start = Timestamp.valueOf((String) value[0]);
            final Timestamp end = Timestamp.valueOf((String) value[1]);
            
            return (field) -> (root, query, cb) -> cb.between(root.get(field), start, end);
        }
    };
    
    public abstract <T> Function<String, Specification<T>> value(Object... value);
    
}
