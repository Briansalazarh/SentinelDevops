package com.sentinel.agentops.infrastructure.messaging.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.sentinel.agentops.infrastructure.config.SentinelKafkaProperties;
import com.sentinel.agentops.domain.AgentAssessmentRequested;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConfiguration {

    @Bean
    public ObjectMapper sentinelObjectMapper() {
        return new ObjectMapper().registerModule(new JavaTimeModule());
    }

    @Bean
    public DefaultKafkaProducerFactory<String, AgentAssessmentRequested> agentAssessmentProducerFactory(
        ObjectMapper sentinelObjectMapper,
        @Value("${spring.kafka.bootstrap-servers}") String bootstrapServers,
        @Value("${spring.kafka.producer.acks:all}") String acks) {

        Map<String, Object> producerProps = new HashMap<>();
        producerProps.put(org.apache.kafka.clients.CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        producerProps.put(ProducerConfig.ACKS_CONFIG, acks);
        producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(producerProps, new StringSerializer(), new JsonSerializer<>(sentinelObjectMapper));
    }

    @Bean
    public KafkaTemplate<String, AgentAssessmentRequested> agentAssessmentKafkaTemplate(
        DefaultKafkaProducerFactory<String, AgentAssessmentRequested> agentAssessmentProducerFactory) {

        return new KafkaTemplate<>(agentAssessmentProducerFactory);
    }

    @Bean
    public DefaultKafkaConsumerFactory<String, AgentAssessmentRequested> agentAssessmentConsumerFactory(
        ObjectMapper sentinelObjectMapper,
        @Value("${spring.kafka.bootstrap-servers}") String bootstrapServers,
        @Value("${spring.kafka.consumer.group-id}") String groupId,
        @Value("${spring.kafka.consumer.auto-offset-reset:earliest}") String autoOffsetReset) {

        Map<String, Object> consumerProps = new HashMap<>();
        consumerProps.put(org.apache.kafka.clients.CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, autoOffsetReset);
        consumerProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, AgentAssessmentRequestedJsonDeserializer.class);
        return new DefaultKafkaConsumerFactory<>(
            consumerProps,
            new StringDeserializer(),
            new AgentAssessmentRequestedJsonDeserializer(sentinelObjectMapper));
    }

    @Bean(name = "agentAssessmentKafkaListenerContainerFactory")
    public ConcurrentKafkaListenerContainerFactory<String, AgentAssessmentRequested> agentAssessmentKafkaListenerContainerFactory(
        DefaultKafkaConsumerFactory<String, AgentAssessmentRequested> agentAssessmentConsumerFactory,
        SentinelKafkaProperties properties) {

        ConcurrentKafkaListenerContainerFactory<String, AgentAssessmentRequested> factory =
            new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(agentAssessmentConsumerFactory);
        factory.setConcurrency(properties.listener().concurrency());
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        return factory;
    }

    @Bean
    public NewTopic agentAssessmentsTopic(SentinelKafkaProperties properties) {
        return new NewTopic(
            properties.topics().assessments(),
            properties.topics().partitions(),
            properties.topics().replicationFactor());
    }

    @Bean
    public NewTopic agentAssessmentsDltTopic(SentinelKafkaProperties properties) {
        return new NewTopic(
            properties.topics().assessmentsDlt(),
            properties.topics().partitions(),
            properties.topics().replicationFactor());
    }
}