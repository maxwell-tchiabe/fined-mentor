package com.fined.mentor.auth.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fined.mentor.auth.dto.ApiResponse;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.ConsumptionProbe;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Slf4j
@Component
@Order(1)
public class RateLimitFilter extends OncePerRequestFilter {

    @Autowired
    Supplier<BucketConfiguration> bucketConfiguration;

    @Autowired
    ProxyManager<String> proxyManager;

    @Override
    protected boolean shouldNotFilterAsyncDispatch() {
        return false;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, @NotNull HttpServletResponse response,
            @NotNull FilterChain filterChain) throws IOException, ServletException {
        if (request.getRequestURI().startsWith("/actuator")) {
            filterChain.doFilter(request, response);
            return;
        }

        String key = getClientIp(request);

        try {
            Bucket bucket = proxyManager.builder().build(key, bucketConfiguration.get());
            ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

            if (probe.isConsumed()) {
                response.setHeader("X-RateLimit-Remaining", String.valueOf(probe.getRemainingTokens()));
                filterChain.doFilter(request, response);
            } else {
                response.setStatus(429);
                response.setHeader("X-RateLimit-Retry-After-Seconds",
                        String.valueOf(TimeUnit.NANOSECONDS.toSeconds(probe.getNanosToWaitForRefill())));
                response.setContentType("application/json");
                MAPPER.writeValue(response.getWriter(), ApiResponse.error("Too many requests"));
            }
        } catch (Exception e) {
            log.error("Rate limit failed for IP: {}. Denying access.", key, e);
            response.setStatus(503);
            MAPPER.writeValue(response.getWriter(), ApiResponse.error("Service unavailable"));
        }
    }

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        return (xForwardedFor != null ? xForwardedFor.split(",")[0].trim() : request.getRemoteAddr());
    }

}