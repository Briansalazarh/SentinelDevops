package com.sentinel.agentops.application.validation;

import com.sentinel.agentops.domain.AgentAssessmentRequested;
import com.sentinel.agentops.domain.AssessmentFinding;

import java.util.List;

public interface AssessmentValidationPipeline {

    List<AssessmentFinding> evaluate(AgentAssessmentRequested request);
}