package com.demandtracker.config;

import com.demandtracker.exception.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Rate limiting para /api/auth/login (proteção contra brute force).
 * Resposta de erro mantém formato ErrorResponse (contrato inalterado).
 */
@Component
public class RateLimitFilter extends OncePerRequestFilter {

    @Value("${app.rate-limit.login.max-attempts:5}")
    private int maxAttemptsPerWindow;
    @Value("${app.rate-limit.login.window-seconds:60}")
    private long windowSeconds;
    private long windowMs;
    private final Map<String, Window> windows = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper;

    public RateLimitFilter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @jakarta.annotation.PostConstruct
    void init() {
        this.windowMs = windowSeconds * 1000L;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path == null || !path.endsWith("/api/auth/login") || !"POST".equalsIgnoreCase(request.getMethod());
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        String key = clientKey(request);
        Window w = windows.computeIfAbsent(key, k -> new Window());
        if (w.isExpired(windowMs)) {
            w.reset();
        }
        if (w.count.incrementAndGet() > maxAttemptsPerWindow) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            ErrorResponse err = new ErrorResponse(
                    "Muitas tentativas de login. Tente novamente mais tarde.",
                    HttpStatus.TOO_MANY_REQUESTS.value(),
                    LocalDateTime.now()
            );
            response.getWriter().write(objectMapper.writeValueAsString(err));
            return;
        }
        filterChain.doFilter(request, response);
    }

    private static String clientKey(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            return xff.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private static class Window {
        final AtomicInteger count = new AtomicInteger(0);
        long start = System.currentTimeMillis();

        boolean isExpired(long windowMs) {
            return System.currentTimeMillis() - start > windowMs;
        }

        void reset() {
            start = System.currentTimeMillis();
            count.set(0);
        }
    }
}
