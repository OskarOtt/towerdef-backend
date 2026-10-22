package no.oskott.towerdefbackend.config;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "towerdef")
public record TowerdefProperties(Security security, RateLimit rateLimit) {

    public record Security(String secret, List<String> allowedOrigins, boolean trustForwardedFor, int runTtlMinutes) {
    }

    public record RateLimit(int startPerMinute, int submitPerMinute, int readPerMinute) {
    }
}
