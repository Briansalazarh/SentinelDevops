package com.sentinel.agentops.infrastructure.persistence.cosmos.adapter;

import com.sentinel.agentops.domain.AgentAssessment;
import com.sentinel.agentops.domain.port.AgentAssessmentRepositoryPort;
import com.sentinel.agentops.infrastructure.persistence.cosmos.document.AgentAssessmentDocument;
import com.sentinel.agentops.infrastructure.persistence.cosmos.mapper.AgentAssessmentDocumentMapper;
import com.sentinel.agentops.infrastructure.persistence.cosmos.repository.AgentAssessmentDocumentRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class CosmosAgentAssessmentRepositoryAdapter implements AgentAssessmentRepositoryPort {

    private final AgentAssessmentDocumentRepository repository;
    private final AgentAssessmentDocumentMapper mapper;

    public CosmosAgentAssessmentRepositoryAdapter(
        AgentAssessmentDocumentRepository repository,
        AgentAssessmentDocumentMapper mapper) {

        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public AgentAssessment save(AgentAssessment assessment) {
        AgentAssessmentDocument document = mapper.toDocument(assessment);
        AgentAssessmentDocument persisted = repository.save(document);
        return mapper.toDomain(persisted);
    }

    @Override
    public Optional<AgentAssessment> findByEventId(UUID eventId) {
        return repository.findById(eventId.toString()).map(mapper::toDomain);
    }
}