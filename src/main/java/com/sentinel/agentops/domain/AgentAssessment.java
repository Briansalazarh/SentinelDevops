package com.sentinel.agentops.domain;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

public class AgentAssessment {

    private UUID eventId;
    private Instant requestedAt;
    private String tenantId;
    private AgentMetadata agentMetadata;
    private List<SamplePayload> samplePayloads = new ArrayList<>();
    private final List<AssessmentFinding> findings = new CopyOnWriteArrayList<>();
    private AssessmentStatus status = AssessmentStatus.APPROVED;
    private Instant processedAt;
    private long processingDurationMillis;

    public AgentAssessment() {
    }

    public AgentAssessment(
        UUID eventId,
        Instant requestedAt,
        String tenantId,
        AgentMetadata agentMetadata,
        List<SamplePayload> samplePayloads,
        Collection<AssessmentFinding> findings,
        AssessmentStatus status,
        Instant processedAt,
        long processingDurationMillis) {

        setEventId(eventId);
        setRequestedAt(requestedAt);
        setTenantId(tenantId);
        setAgentMetadata(agentMetadata);
        setSamplePayloads(samplePayloads);
        replaceFindings(findings);
        setStatus(status);
        setProcessedAt(processedAt);
        setProcessingDurationMillis(processingDurationMillis);
    }

    public static AgentAssessment from(AgentAssessmentRequested request) {
        return new AgentAssessment(
            request.eventId(),
            request.timestamp(),
            request.tenantId(),
            request.agentMetadata(),
            request.samplePayloads(),
            List.of(),
            AssessmentStatus.APPROVED,
            null,
            0L
        );
    }

    public synchronized void addFinding(AssessmentFinding finding) {
        findings.add(Objects.requireNonNull(finding, "finding must not be null"));
    }

    public synchronized void replaceFindings(Collection<AssessmentFinding> newFindings) {
        findings.clear();
        if (newFindings != null) {
            newFindings.stream()
                .filter(Objects::nonNull)
                .forEach(findings::add);
        }
    }

    public synchronized List<AssessmentFinding> getFindings() {
        return List.copyOf(findings);
    }

    public synchronized void setFindings(Collection<AssessmentFinding> newFindings) {
        replaceFindings(newFindings);
    }

    public UUID getEventId() {
        return eventId;
    }

    public void setEventId(UUID eventId) {
        this.eventId = Objects.requireNonNull(eventId, "eventId must not be null");
    }

    public Instant getRequestedAt() {
        return requestedAt;
    }

    public void setRequestedAt(Instant requestedAt) {
        this.requestedAt = Objects.requireNonNull(requestedAt, "requestedAt must not be null");
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = Objects.requireNonNull(tenantId, "tenantId must not be null");
    }

    public AgentMetadata getAgentMetadata() {
        return agentMetadata;
    }

    public void setAgentMetadata(AgentMetadata agentMetadata) {
        this.agentMetadata = Objects.requireNonNull(agentMetadata, "agentMetadata must not be null");
    }

    public List<SamplePayload> getSamplePayloads() {
        return Collections.unmodifiableList(samplePayloads);
    }

    public void setSamplePayloads(List<SamplePayload> samplePayloads) {
        this.samplePayloads = samplePayloads == null ? new ArrayList<>() : new ArrayList<>(samplePayloads);
    }

    public AssessmentStatus getStatus() {
        return status;
    }

    public void setStatus(AssessmentStatus status) {
        this.status = Objects.requireNonNull(status, "status must not be null");
    }

    public Instant getProcessedAt() {
        return processedAt;
    }

    public void setProcessedAt(Instant processedAt) {
        this.processedAt = processedAt;
    }

    public long getProcessingDurationMillis() {
        return processingDurationMillis;
    }

    public void setProcessingDurationMillis(long processingDurationMillis) {
        this.processingDurationMillis = processingDurationMillis;
    }
}