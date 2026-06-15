package com.sentinel.agentops.application.validation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.sentinel.agentops.domain.AgentAssessmentRequested;
import com.sentinel.agentops.domain.AssessmentCategory;
import com.sentinel.agentops.domain.AssessmentFinding;
import com.sentinel.agentops.domain.AssessmentSeverity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
public class FoundryIqComplianceHook implements AssessmentValidationHook {

    private static final Logger log = LoggerFactory.getLogger(FoundryIqComplianceHook.class);

    private final RestClient restClient;

    public FoundryIqComplianceHook(
        RestClient.Builder restClientBuilder,
        @Value("${microsoft.iq.foundry.endpoint:https://api.foundry.microsoft.com/v1}") String endpoint,
        @Value("${microsoft.iq.foundry.api-key:mock-key}") String apiKey) {

        this.restClient = restClientBuilder
            .baseUrl(endpoint)
            .defaultHeader("api-key", apiKey)
            .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .build();
    }

    @Override
    public List<AssessmentFinding> analyze(AgentAssessmentRequested request) {
        try {
            FoundryIqEvaluateResponse response = restClient.post()
                .uri("/policies/evaluate")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(new FoundryIqEvaluateRequest(
                    new FoundryIqMetaProfile(
                        request.agentMetadata().systemPrompt(),
                        request.agentMetadata().assignedTools(),
                        request.agentMetadata().targetModel())))
                .retrieve()
                .body(FoundryIqEvaluateResponse.class);

            return mapComplianceViolations(response);
        } catch (RestClientException ex) {
            log.warn("Foundry IQ no disponible para tenantId={} eventId={}", request.tenantId(), request.eventId(), ex);
            return List.of(new AssessmentFinding(
                AssessmentCategory.COMPLIANCE,
                AssessmentSeverity.MEDIUM,
                "Foundry IQ service is offline or unreachable; compliance audit was skipped.",
                getClass().getSimpleName(),
                Instant.now()));
        }
    }

    private List<AssessmentFinding> mapComplianceViolations(FoundryIqEvaluateResponse response) {
        if (response == null) {
            return List.of();
        }

        List<FoundryViolation> violations = response.violations();
        if (violations == null || violations.isEmpty()) {
            return List.of();
        }

        List<AssessmentFinding> findings = new ArrayList<>();
        for (FoundryViolation violation : violations) {
            if (violation == null) {
                continue;
            }
            String detail = composeViolationMessage(violation);
            findings.add(new AssessmentFinding(
                AssessmentCategory.COMPLIANCE,
                AssessmentSeverity.HIGH,
                detail,
                getClass().getSimpleName(),
                Instant.now()));
        }

        return List.copyOf(findings);
    }

    private String composeViolationMessage(FoundryViolation violation) {
        String code = violation.code();
        String message = violation.message();

        if (code != null && !code.isBlank() && message != null && !message.isBlank()) {
            return code + ": " + message;
        }

        return safeText(message, code, "Foundry IQ compliance violation detected");
    }

    private String safeText(String primary, String secondary, String fallback) {
        if (primary != null && !primary.isBlank()) {
            return primary;
        }
        if (secondary != null && !secondary.isBlank()) {
            return secondary;
        }
        return fallback;
    }

    public record FoundryIqEvaluateRequest(@JsonProperty("metaProfile") FoundryIqMetaProfile metaProfile) {
    }

    public record FoundryIqMetaProfile(
        @JsonProperty("systemPrompt") String systemPrompt,
        @JsonProperty("assignedTools") Collection<String> assignedTools,
        @JsonProperty("model") String model) {
        public FoundryIqMetaProfile {
            assignedTools = assignedTools == null ? List.of() : List.copyOf(assignedTools);
            model = Objects.requireNonNullElse(model, "unknown-model");
            systemPrompt = Objects.requireNonNullElse(systemPrompt, "");
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record FoundryIqEvaluateResponse(
        @JsonProperty("violations") List<FoundryViolation> violations,
        @JsonProperty("auditResult") FoundryAuditResult auditResult) {

        public List<FoundryViolation> violations() {
            if (violations != null) {
                return violations;
            }
            return auditResult == null ? List.of() : auditResult.violations();
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record FoundryAuditResult(@JsonProperty("violations") List<FoundryViolation> violations) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record FoundryViolation(
        @JsonAlias({"ruleName"})
        @JsonProperty("code") String code,
        @JsonAlias({"description"})
        @JsonProperty("message") String message,
        @JsonProperty("severity") String severity) {
    }
}