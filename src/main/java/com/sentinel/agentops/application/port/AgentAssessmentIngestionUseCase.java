package com.sentinel.agentops.application.port;

import com.sentinel.agentops.domain.AgentAssessment;
import com.sentinel.agentops.domain.AgentAssessmentRequested;

public interface AgentAssessmentIngestionUseCase {

    AgentAssessment ingest(AgentAssessmentRequested request);
}