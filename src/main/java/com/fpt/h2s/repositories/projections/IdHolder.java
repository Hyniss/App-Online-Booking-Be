package com.fpt.h2s.repositories.projections;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public interface IdHolder {
    Integer getId();

    static Set<Integer> unwrapList(final List<IdHolder> idHolders) {
        return idHolders.stream().map(IdHolder::getId).collect(Collectors.toSet());
    }
}
