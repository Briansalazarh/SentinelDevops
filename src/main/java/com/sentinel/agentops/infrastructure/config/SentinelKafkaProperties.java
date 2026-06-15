package com.sentinel.agentops.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "sentinel.kafka")
public record SentinelKafkaProperties(
    Topics topics,
    Listener listener,
    Retry retry
) {
    public record Topics(String assessments, String assessmentsDlt, int partitions, short replicationFactor) {
    }

    public record Listener(int concurrency) {
    }

    public record Retry(int attempts, Duration delay, double multiplier, Duration maxDelay, boolean autoCreateTopics) {
    }
}