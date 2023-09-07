package com.fpt.h2s.models.domains;

import ananta.utility.ListEx;
import lombok.Getter;
import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

@Getter
public class ListResult<T> {

    private final List<T> items;
    private final int page;
    private final int size;
    private final long totalItems;
    private final int totalPages;
    private final String orderBy;
    private final boolean isAscending;
    private final boolean isEmpty;

    private ListResult(@NonNull final Page<T> page) {
        final Optional<Order> order = page.getSort().stream().findFirst();

        this.items = page.getContent();
        this.page = page.getNumber() + 1;
        this.size = page.getSize();
        this.totalItems = page.getTotalElements();
        this.totalPages = page.getTotalPages();
        this.orderBy = order.map(Order::getProperty).orElse(null);
        this.isAscending = order.map(Order::getDirection).map(Sort.Direction::isAscending).orElse(false);
        this.isEmpty = page.isEmpty();
    }

    private ListResult(@NonNull final ListResult<?> listResult, @NonNull final Collection<T> items) {
        this.items = ListEx.listOf(items);
        this.page = listResult.getPage();
        this.size = listResult.getSize();
        this.totalItems = listResult.getTotalItems();
        this.totalPages = listResult.getTotalPages();
        this.orderBy = listResult.getOrderBy();
        this.isAscending = listResult.isAscending();
        this.isEmpty = listResult.isEmpty();
    }

    public <R> ListResult<R> withContent(@NonNull final Collection<R> items) {
        return new ListResult<>(this, items);
    }

    public <R> ListResult<R> map(@NonNull final Function<T, R> function) {
        return this.withContent(this.items.stream().map(function).toList());
    }

    public static <T> ListResult<T> of(@NonNull final Page<T> page) {
        return new ListResult<>(page);
    }

}