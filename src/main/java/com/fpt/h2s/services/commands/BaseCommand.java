package com.fpt.h2s.services.commands;

import com.fpt.h2s.models.domains.ApiResponse;

public interface BaseCommand<T, R> {
    ApiResponse<R> execute(T request);
}
