package com.fpt.h2s.models.domains;

import ananta.utility.StringEx;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.Optional;

public abstract class SearchRequest {
    
    protected abstract String getOrderBy();
    protected abstract Integer getPage();
    protected abstract Integer getSize();
    protected abstract Boolean getIsDescending();
    
    public PageRequest toPageRequest() {
        final int page = Optional.ofNullable(this.getPage()).orElse(1);
        final int size = Optional.ofNullable(this.getSize()).orElse(10);
        if (StringEx.isBlank(getOrderBy())) {
            return PageRequest.of(page - 1, size);
        }
        
        final Boolean descending = Optional.ofNullable(this.getIsDescending()).orElse(true);
        final Sort.Direction direction = descending ? Sort.Direction.DESC : Sort.Direction.ASC;
        return PageRequest.of(page - 1, size, direction, this.getOrderBy());
    }
}
