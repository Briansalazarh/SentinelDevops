package com.sentinel.agentops.infrastructure.persistence.cosmos.repository;

import com.azure.spring.data.cosmos.repository.CosmosRepository;
import com.sentinel.agentops.infrastructure.persistence.cosmos.document.AgentAssessmentDocument;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AgentAssessmentDocumentRepository extends CosmosRepository<AgentAssessmentDocument, String> {

    Optional<AgentAssessmentDocument> findByEventIdAndTenantId(String eventId, String tenantId);
}