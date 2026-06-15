package com.sentinel.agentops.domain;

import java.time.Instant;
import java.util.Objects;

public record AssessmentFinding(
    AssessmentCategory category,
    AssessmentSeverity severity,
    String detail,
    String detector,
    Instant detectedAt
) {
    public AssessmentFinding {
        Objects.requireNonNull(category, "category must not be null");
        Objects.requireNonNull(severity, "severity must not be null");
        Objects.requireNonNull(detail, "detail must not be null");
        Objects.requireNonNull(detector, "detector must not be null");
        Objects.requireNonNull(detectedAt, "detectedAt must not be null");
    }
}