package com.sentinel.agentops.application.validation;

import com.sentinel.agentops.domain.AgentAssessmentRequested;
import com.sentinel.agentops.domain.AssessmentFinding;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class DefaultAssessmentValidationPipeline implements AssessmentValidationPipeline {

    private final List<AssessmentValidationHook> hooks;

    public DefaultAssessmentValidationPipeline(List<AssessmentValidationHook> hooks) {
        this.hooks = List.copyOf(hooks);
    }

    @Override
    public List<AssessmentFinding> evaluate(AgentAssessmentRequested request) {
        List<AssessmentFinding> findings = new ArrayList<>();
        for (AssessmentValidationHook hook : hooks) {
            findings.addAll(hook.analyze(request));
        }
        return List.copyOf(findings);
    }
}