package com.sentinel.agentops.domain;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public record AgentAssessmentRequested(
    UUID eventId,
    Instant timestamp,
    String tenantId,
    AgentMetadata agentMetadata,
    List<SamplePayload> samplePayloads
) {
    public AgentAssessmentRequested {
        Objects.requireNonNull(eventId, "eventId must not be null");
        Objects.requireNonNull(timestamp, "timestamp must not be null");
        Objects.requireNonNull(tenantId, "tenantId must not be null");
        Objects.requireNonNull(agentMetadata, "agentMetadata must not be null");
        samplePayloads = samplePayloads == null ? List.of() : List.copyOf(samplePayloads);
    }
}