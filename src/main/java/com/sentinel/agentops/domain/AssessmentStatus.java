package com.sentinel.agentops.domain;

import java.util.List;

public enum AssessmentStatus {
    APPROVED,
    REVIEW_REQUIRED,
    REJECTED;

    public static AssessmentStatus fromFindings(List<AssessmentFinding> findings) {
        boolean criticalOrHigh = findings.stream()
            .anyMatch(finding -> finding.severity() == AssessmentSeverity.CRITICAL || finding.severity() == AssessmentSeverity.HIGH);
        if (criticalOrHigh) {
            return REJECTED;
        }

        boolean reviewRequired = findings.stream()
            .anyMatch(finding -> finding.severity() == AssessmentSeverity.MEDIUM || finding.severity() == AssessmentSeverity.LOW);
        return reviewRequired ? REVIEW_REQUIRED : APPROVED;
    }
}