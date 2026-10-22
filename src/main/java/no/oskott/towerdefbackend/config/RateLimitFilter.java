package no.oskott.towerdefbackend.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/** Per-IP token bucket limiter, in-memory (single instance). */
@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private static final long CLEANUP_INTERVAL_NANOS = 60_000_000_000L;

    private final TowerdefProperties props;
    private final ClientIp clientIp;
    private final ConcurrentMap<String, Bucket> buckets = new ConcurrentHashMap<>();
    private volatile long lastCleanup = System.nanoTime();

    public RateLimitFilter(TowerdefProperties props, ClientIp clientIp) {
        this.props = props;
        this.clientIp = clientIp;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith("/api/") || "OPTIONS".equals(request.getMethod());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String uri = request.getRequestURI();
        String group;
        int perMinute;
        if ("POST".equals(request.getMethod()) && uri.endsWith("/score")) {
            group = "submit";
            perMinute = props.rateLimit().submitPerMinute();
        } else if ("POST".equals(request.getMethod())) {
            group = "start";
            perMinute = props.rateLimit().startPerMinute();
        } else {
            group = "read";
            perMinute = props.rateLimit().readPerMinute();
        }
        cleanup();
        Bucket bucket = buckets.computeIfAbsent(group + "|" + clientIp.resolve(request), k -> new Bucket(perMinute));
        if (!bucket.tryConsume()) {
            response.setStatus(429);
            response.setHeader("Retry-After", "10");
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"rate_limited\"}");
            return;
        }
        chain.doFilter(request, response);
    }

    private void cleanup() {
        long now = System.nanoTime();
        if (now - lastCleanup > CLEANUP_INTERVAL_NANOS) {
            lastCleanup = now;
            buckets.values().removeIf(b -> b.idleNanos(now) > CLEANUP_INTERVAL_NANOS);
        }
    }

    static final class Bucket {
        private final double capacity;
        private final double refillPerNano;
        private double tokens;
        private long last = System.nanoTime();

        Bucket(int perMinute) {
            this.capacity = perMinute;
            this.tokens = perMinute;
            this.refillPerNano = perMinute / 60_000_000_000.0;
        }

        synchronized boolean tryConsume() {
            long now = System.nanoTime();
            tokens = Math.min(capacity, tokens + (now - last) * refillPerNano);
            last = now;
            if (tokens >= 1) {
                tokens -= 1;
                return true;
            }
            return false;
        }

        synchronized long idleNanos(long now) {
            return now - last;
        }
    }
}
