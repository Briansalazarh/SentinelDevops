package com.sentinel.agentops.infrastructure.persistence.cosmos;

import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.DirectConnectionConfig;
import com.azure.spring.data.cosmos.config.AbstractCosmosConfiguration;
import com.azure.spring.data.cosmos.config.CosmosConfig;
import com.azure.spring.data.cosmos.repository.config.EnableCosmosRepositories;
import com.sentinel.agentops.infrastructure.config.SentinelCosmosProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collection;
import java.util.List;

@Configuration
@EnableCosmosRepositories(basePackages = "com.sentinel.agentops.infrastructure.persistence.cosmos.repository")
public class CosmosConfiguration extends AbstractCosmosConfiguration {

    private final SentinelCosmosProperties properties;

    public CosmosConfiguration(SentinelCosmosProperties properties) {
        this.properties = properties;
    }

    @Bean
    public CosmosClientBuilder getCosmosClientBuilder() {
        DirectConnectionConfig directConnectionConfig = DirectConnectionConfig.getDefaultConfig()
            .setConnectTimeout(properties.connectTimeout())
            .setIdleConnectionTimeout(properties.idleConnectionTimeout())
            .setIdleEndpointTimeout(properties.idleEndpointTimeout())
            .setMaxConnectionsPerEndpoint(properties.maxConnectionsPerEndpoint())
            .setMaxRequestsPerConnection(properties.maxRequestsPerConnection())
            .setNetworkRequestTimeout(properties.requestTimeout())
            .setConnectionEndpointRediscoveryEnabled(true);

        return new CosmosClientBuilder()
            .endpoint(properties.endpoint())
            .key(properties.key())
            .directMode(directConnectionConfig)
            .contentResponseOnWriteEnabled(false)
            .userAgentSuffix("sentinel-agentops");
    }

    @Override
    protected String getDatabaseName() {
        return properties.database();
    }

    @Override
    protected Collection<String> getMappingBasePackages() {
        return List.of("com.sentinel.agentops.infrastructure.persistence.cosmos.document");
    }

    @Override
    public CosmosConfig cosmosConfig() {
        return CosmosConfig.builder()
            .enableQueryMetrics(true)
            .enableIndexMetrics(true)
            .build();
    }
}