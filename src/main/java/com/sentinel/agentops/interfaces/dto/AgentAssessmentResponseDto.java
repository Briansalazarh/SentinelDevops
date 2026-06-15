package com.sentinel.agentops.interfaces.dto;

import com.sentinel.agentops.domain.AssessmentStatus;

import java.time.Instant;
import java.util.UUID;

public record AgentAssessmentResponseDto(
    UUID eventId,
    String tenantId,
    AssessmentStatus status,
    int findingsCount,
    Instant processedAt,
    long processingDurationMillis
) {
}