package com.sentinel.agentops.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "sentinel.cosmos")
public record SentinelCosmosProperties(
    String endpoint,
    String key,
    String database,
    String container,
    Duration requestTimeout,
    Duration connectTimeout,
    Duration idleConnectionTimeout,
    Duration idleEndpointTimeout,
    int maxConnectionsPerEndpoint,
    int maxRequestsPerConnection
) {
}