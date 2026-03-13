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
import org.springframework.beans.factory.annotation.Qualifier;
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
    @Qualifier("bucketConfiguration")
    Supplier<BucketConfiguration> bucketConfiguration;

    @Autowired
    @Qualifier("publicBucketConfiguration")
    Supplier<BucketConfiguration> publicBucketConfiguration;

    @Autowired
    @Qualifier("streamingBucketConfiguration")
    Supplier<BucketConfiguration> streamingBucketConfiguration;

    @Autowired
    ProxyManager<String> proxyManager;

    private static final String PUBLIC_PREFIX = "/api/public/";
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    protected boolean shouldNotFilterAsyncDispatch() {
        return false;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, @NotNull HttpServletResponse response,
            @NotNull FilterChain filterChain) throws IOException, ServletException {

        String uri = request.getRequestURI();

        if (uri.startsWith("/actuator")) {
            filterChain.doFilter(request, response);
            return;
        }

        String clientIp = getClientIp(request);
        boolean isPublic = uri.startsWith(PUBLIC_PREFIX);
        boolean isStream = uri.endsWith("/stream");

        String key;
        Supplier<BucketConfiguration> config;

        if (isStream) {
            // Daily limit for streaming endpoints (chat and quiz)
            key = clientIp + ":stream";
            config = streamingBucketConfiguration;
        } else if (isPublic) {
            // Public endpoints use a stricter dedicated bucket
            key = clientIp + ":public";
            config = publicBucketConfiguration;
        } else {
            // Default authenticated bucket
            key = clientIp;
            config = bucketConfiguration;
        }

        try {
            Bucket bucket = proxyManager.builder().build(key, config.get());
            ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

            if (probe.isConsumed()) {
                response.setHeader("X-RateLimit-Remaining", String.valueOf(probe.getRemainingTokens()));
                filterChain.doFilter(request, response);
            } else {
                long retryAfter = TimeUnit.NANOSECONDS.toSeconds(probe.getNanosToWaitForRefill());
                log.warn("Rate limit exceeded for IP: {} on {} endpoint", clientIp,
                        isPublic ? "public" : "authenticated");
                response.setStatus(429);
                response.setHeader("X-RateLimit-Retry-After-Seconds", String.valueOf(retryAfter));
                response.setContentType("application/json");
                MAPPER.writeValue(response.getWriter(),
                        ApiResponse.error("Too many requests. Please try again in " + retryAfter + " seconds."));
            }
        } catch (Exception e) {
            log.error("Rate limit check failed for IP: {}. Allowing request to proceed.", clientIp, e);
            // Fail open: let the request through if Redis is unavailable rather than
            // blocking all users
            filterChain.doFilter(request, response);
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        return (xForwardedFor != null ? xForwardedFor.split(",")[0].trim() : request.getRemoteAddr());
    }
}
