package com.sentinel.agentops.infrastructure.messaging.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sentinel.agentops.domain.AgentAssessmentRequested;
import org.springframework.kafka.support.serializer.JsonDeserializer;

public class AgentAssessmentRequestedJsonDeserializer extends JsonDeserializer<AgentAssessmentRequested> {

    public AgentAssessmentRequestedJsonDeserializer(ObjectMapper objectMapper) {
        super(AgentAssessmentRequested.class, objectMapper, false);
        addTrustedPackages("com.sentinel.agentops.domain");
        setUseTypeHeaders(false);
    }
}