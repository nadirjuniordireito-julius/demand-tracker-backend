package com.demandtracker.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Log estruturado por requisição (método, path, status, duração, correlationId).
 * Não loga corpo da requisição/resposta.
 */
@Component
public class RequestLoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);

    /** 404 esperado (demanda sem execução) — não loga em INFO nem DEBUG. */
    private static final String PATH_404_OMITIDO_PREFIX = "/api/demandas-execucao/demanda/";

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        long start = System.currentTimeMillis();
        try {
            filterChain.doFilter(request, response);
        } finally {
            long duration = System.currentTimeMillis() - start;
            logRequest(request, response, duration);
        }
    }

    private void logRequest(HttpServletRequest request, HttpServletResponse response, long durationMs) {
        int status = response.getStatus();
        String path = request.getRequestURI();

        if (status == HttpServletResponse.SC_NOT_FOUND && path.startsWith(PATH_404_OMITIDO_PREFIX)) {
            return;
        }

        String correlationId = org.slf4j.MDC.get(CorrelationIdFilter.MDC_KEY);
        String cid = correlationId != null ? correlationId : "-";
        String method = request.getMethod();

        if (status == HttpServletResponse.SC_NOT_FOUND) {
            if (log.isDebugEnabled()) {
                log.debug("request={} path={} status={} durationMs={} correlationId={}",
                        method, path, status, durationMs, cid);
            }
            return;
        }

        if (log.isInfoEnabled()) {
            log.info("request={} path={} status={} durationMs={} correlationId={}",
                    method, path, status, durationMs, cid);
        }
    }
}
