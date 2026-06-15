package com.sentinel.agentops.domain.port;

import com.sentinel.agentops.domain.AgentAssessment;

import java.util.Optional;
import java.util.UUID;

public interface AgentAssessmentRepositoryPort {

    AgentAssessment save(AgentAssessment assessment);

    Optional<AgentAssessment> findByEventId(UUID eventId);
}