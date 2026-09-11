package com.sentinel.agentops;

import com.sentinel.agentops.infrastructure.config.SentinelCosmosProperties;
import com.sentinel.agentops.infrastructure.config.SentinelKafkaProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.retry.annotation.EnableRetry;

@SpringBootApplication
@EnableRetry
@ConfigurationPropertiesScan(basePackageClasses = {
    SentinelKafkaProperties.class,
    SentinelCosmosProperties.class
})
public class SentinelAgentOpsApplication {

    public static void main(String[] args) {
        SpringApplication.run(SentinelAgentOpsApplication.class, args);
    }
}