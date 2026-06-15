package com.sentinel.agentops.application.validation;

import com.sentinel.agentops.domain.AgentAssessmentRequested;
import com.sentinel.agentops.domain.AssessmentFinding;

import java.util.List;

public interface AssessmentValidationHook {

    List<AssessmentFinding> analyze(AgentAssessmentRequested request);
}