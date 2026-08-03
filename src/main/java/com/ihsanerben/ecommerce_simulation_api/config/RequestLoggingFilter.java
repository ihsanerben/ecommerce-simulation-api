package com.ihsanerben.ecommerce_simulation_api.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
public class RequestLoggingFilter extends OncePerRequestFilter {

    public static final String REQUEST_ID_HEADER = "X-Request-ID";
    public static final String REQUEST_ID_MDC_KEY = "requestId";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        String requestId = UUID.randomUUID().toString();
        long startedAt = System.nanoTime();
        MDC.put(REQUEST_ID_MDC_KEY, requestId);
        response.setHeader(REQUEST_ID_HEADER, requestId);

        try {
            filterChain.doFilter(request, response);
        } finally {
            long durationMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startedAt);
            logRequest(request, response.getStatus(), durationMs);
            MDC.remove(REQUEST_ID_MDC_KEY);
        }
    }

    private void logRequest(HttpServletRequest request, int status, long durationMs) {
        if (status >= HttpServletResponse.SC_BAD_REQUEST) {
            log.warn("event=http_request method={} path={} status={} durationMs={}",
                    request.getMethod(), request.getRequestURI(), status, durationMs);
            return;
        }
        log.info("event=http_request method={} path={} status={} durationMs={}",
                request.getMethod(), request.getRequestURI(), status, durationMs);
    }
}
