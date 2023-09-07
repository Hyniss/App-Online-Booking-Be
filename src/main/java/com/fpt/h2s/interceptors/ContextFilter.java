package com.fpt.h2s.interceptors;

import com.fpt.h2s.configurations.requests.DataContext;
import com.fpt.h2s.configurations.requests.RequestBodyExceptionContextHolder;
import com.fpt.h2s.utilities.MoreRequests;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.io.IOException;

@Log4j2
@Configuration
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ContextFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
        @NonNull final HttpServletRequest request,
        @NonNull final HttpServletResponse response,
        @NonNull final FilterChain filterChain
    ) throws ServletException, IOException {
        try {
            log.info("Running method {} with full path: {}", request.getMethod(), MoreRequests.getUrlOf(request));
            response.addHeader("Access-Control-Allow-Origin", "*");
            response.addHeader("Access-Control-Allow-Methods", "*");
            response.addHeader("Access-Control-Allow-Headers", "*");
            response.addHeader("Access-Control-Expose-Headers", "Access-Control-Allow-Origin, Access-Control-Allow-Credentials");
            response.addHeader("Access-Control-Allow-Credentials", "true");
            response.addIntHeader("Access-Control-Max-Age", 10);

            final ContentCachingRequestWrapper wrapper = new ContentCachingRequestWrapper(request);
            filterChain.doFilter(wrapper, response);
        } finally {
            RequestBodyExceptionContextHolder.clearExceptions();
            DataContext.clear();
        }
    }
}