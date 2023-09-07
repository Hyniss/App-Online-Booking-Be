package com.fpt.h2s.utilities;

import io.hypersistence.utils.hibernate.query.SQLExtractor;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import lombok.experimental.UtilityClass;
import org.hibernate.query.sqm.tree.expression.ValueBindJpaCriteriaParameter;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

@UtilityClass
public class MoreJPA {

    public static <T> String toSqlString(Specification<T> criteria, Class<T> tableClass) {
        EntityManager entityManager = SpringBeans.getBean(EntityManager.class);

        CriteriaQuery<T> criteriaQuery = buildCriteriaQuery(entityManager, tableClass, criteria);
        TypedQuery<T> query = entityManager.createQuery(criteriaQuery);

        List<Object> parameters = query
            .getParameters()
            .stream()
            .map(ValueBindJpaCriteriaParameter.class::cast)
            .map(ValueBindJpaCriteriaParameter::getValue)
            .toList();

        String sql = SQLExtractor.from(query);

        for (Object value : parameters) {
            sql = sql.replace("?", String.valueOf(value));
        }
        return sql;
    }

    private static <T> CriteriaQuery<T> buildCriteriaQuery(EntityManager entityManager, Class<T> tableClass, Specification<T> criteria) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<T> query = cb.createQuery(tableClass);
        Root<T> root = query.from(tableClass);
        query = query.where(criteria.toPredicate(root, query, cb));
        return query;
    }
}
