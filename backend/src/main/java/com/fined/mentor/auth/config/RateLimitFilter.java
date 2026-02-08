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
import org.springframework.web.filter.OncePerRequestFilter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

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
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String path = request.getRequestURI();

        // Skip rate limiting for actuator endpoints (health, prometheus)
        if (path.startsWith("/actuator")) {
            filterChain.doFilter(request, response);
            return;
        }

        String key = request.getRemoteAddr();

        try {
            Bucket bucket = proxyManager.builder().build(key, bucketConfiguration);
            ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

            log.info("Rate limit check for IP: {} - Path: {} - Remaining: {}", key, path, probe.getRemainingTokens());

            if (probe.isConsumed()) {
                filterChain.doFilter(request, response);
            } else {
                log.warn("Rate limit exceeded for IP: {} - Path: {}", key, path);
                ApiResponse apiResponse = ApiResponse.error("Too many requests");

                ObjectMapper mapper = new ObjectMapper();
                response.setContentType("application/json");
                response.setHeader("X-Rate-Limit-Retry-After-Seconds",
                        "" + TimeUnit.NANOSECONDS.toSeconds(probe.getNanosToWaitForRefill()));
                response.setStatus(429);
                response.getWriter().write(mapper.writeValueAsString(apiResponse));
            }
        } catch (Exception e) {
            log.error("Error during rate limiting for IP: {}. Falling back to allowing request.", key, e);
            filterChain.doFilter(request, response);
        }
    }
}