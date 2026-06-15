package com.sentinel.agentops.infrastructure.messaging.kafka;

import com.sentinel.agentops.application.port.AgentAssessmentIngestionUseCase;
import com.sentinel.agentops.domain.AgentAssessmentRequested;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Component
public class AgentAssessmentRequestedListener {

    private static final Logger log = LoggerFactory.getLogger(AgentAssessmentRequestedListener.class);

    private final AgentAssessmentIngestionUseCase ingestionUseCase;

    public AgentAssessmentRequestedListener(AgentAssessmentIngestionUseCase ingestionUseCase) {
        this.ingestionUseCase = ingestionUseCase;
    }

    @RetryableTopic(
        attempts = "4",
        backoff = @org.springframework.retry.annotation.Backoff(delay = 250, multiplier = 2.0, maxDelay = 5000),
        dltTopicSuffix = ".DLT",
        kafkaTemplate = "agentAssessmentKafkaTemplate",
        listenerContainerFactory = "agentAssessmentKafkaListenerContainerFactory",
        autoCreateTopics = "true"
    )
    @KafkaListener(
        topics = "${sentinel.kafka.topics.assessments}",
        containerFactory = "agentAssessmentKafkaListenerContainerFactory")
    public void onMessage(
        AgentAssessmentRequested event,
        Acknowledgment acknowledgment,
        ConsumerRecord<String, AgentAssessmentRequested> record,
        @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {

        log.debug("Recibido evento Kafka topic={} partition={} offset={} eventId={}",
            topic,
            record.partition(),
            record.offset(),
            event.eventId());

        ingestionUseCase.ingest(event);
        acknowledgment.acknowledge();
    }
}