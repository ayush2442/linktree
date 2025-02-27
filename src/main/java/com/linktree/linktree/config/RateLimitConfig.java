package com.linktree.linktree.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
@ConfigurationProperties(prefix = "app.ratelimit")
@Data
public class RateLimitConfig {
    private int capacity;
    private int refillRate;
    private int refillTime;

    @Bean
    public Bucket authRateLimitBucket() {
        Bandwidth limit = Bandwidth.classic(capacity,
                Refill.greedy(refillRate, Duration.ofSeconds(refillTime)));
        return Bucket.builder().addLimit(limit).build();
    }
}
