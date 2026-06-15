package com.sentinel.agentops.application.service;

import com.sentinel.agentops.application.port.AgentAssessmentIngestionUseCase;
import com.sentinel.agentops.application.validation.AssessmentValidationPipeline;
import com.sentinel.agentops.domain.AgentAssessment;
import com.sentinel.agentops.domain.AgentAssessmentRequested;
import com.sentinel.agentops.domain.AssessmentFinding;
import com.sentinel.agentops.domain.AssessmentStatus;
import com.sentinel.agentops.domain.port.AgentAssessmentRepositoryPort;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Service
public class DefaultAgentAssessmentIngestionService implements AgentAssessmentIngestionUseCase {

    private static final Logger log = LoggerFactory.getLogger(DefaultAgentAssessmentIngestionService.class);

    private final AssessmentValidationPipeline validationPipeline;
    private final AgentAssessmentRepositoryPort repository;
    private final MeterRegistry meterRegistry;
    private final Counter ingestedCounter;
    private final Counter failedCounter;

    public DefaultAgentAssessmentIngestionService(
        AssessmentValidationPipeline validationPipeline,
        AgentAssessmentRepositoryPort repository,
        MeterRegistry meterRegistry) {

        this.validationPipeline = validationPipeline;
        this.repository = repository;
        this.meterRegistry = meterRegistry;
        this.ingestedCounter = Counter.builder("sentinel.agent_assessments.ingested_total")
            .description("Total de evaluaciones ingeridas")
            .register(meterRegistry);
        this.failedCounter = Counter.builder("sentinel.agent_assessments.failed_total")
            .description("Total de evaluaciones fallidas")
            .register(meterRegistry);
    }

    @Override
    public AgentAssessment ingest(AgentAssessmentRequested request) {
        Timer.Sample sample = Timer.start(meterRegistry);
        Instant processedAt = Instant.now();
        try {
            List<AssessmentFinding> findings = validationPipeline.evaluate(request);
            AssessmentStatus status = AssessmentStatus.fromFindings(findings);
            AgentAssessment assessment = AgentAssessment.from(request);
            assessment.replaceFindings(findings);
            assessment.setStatus(status);
            assessment.setProcessedAt(processedAt);
            assessment.setProcessingDurationMillis(Duration.between(request.timestamp(), processedAt).toMillis());

            AgentAssessment savedAssessment = repository.save(assessment);
            sample.stop(Timer.builder("sentinel.agent_assessments.processing.latency")
                .description("Latencia de procesamiento de evaluación de agente")
                .tag("status", savedAssessment.getStatus().name())
                .register(meterRegistry));
            ingestedCounter.increment();

            log.info(
                "Assessment procesada eventId={} tenantId={} status={} findings={} durationMs={}",
                savedAssessment.getEventId(),
                savedAssessment.getTenantId(),
                savedAssessment.getStatus(),
                savedAssessment.getFindings().size(),
                savedAssessment.getProcessingDurationMillis());

            return savedAssessment;
        } catch (RuntimeException ex) {
            failedCounter.increment();
            sample.stop(Timer.builder("sentinel.agent_assessments.processing.latency")
                .description("Latencia de procesamiento de evaluación de agente")
                .tag("status", "FAILED")
                .register(meterRegistry));
            log.error("Fallo procesando assessment eventId={} tenantId={}", request.eventId(), request.tenantId(), ex);
            throw ex;
        }
    }
}