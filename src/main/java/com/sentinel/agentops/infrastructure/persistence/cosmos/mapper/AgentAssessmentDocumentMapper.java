package com.sentinel.agentops.infrastructure.persistence.cosmos.mapper;

import com.sentinel.agentops.domain.AgentAssessment;
import com.sentinel.agentops.domain.AgentMetadata;
import com.sentinel.agentops.domain.SamplePayload;
import com.sentinel.agentops.infrastructure.persistence.cosmos.document.AgentAssessmentDocument;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.UUID;

@Mapper(componentModel = "spring")
public interface AgentAssessmentDocumentMapper {

    @Mapping(target = "id", expression = "java(assessment.getEventId().toString())")
    @Mapping(target = "eventId", expression = "java(assessment.getEventId().toString())")
    @Mapping(target = "agentId", source = "agentMetadata.agentId")
    @Mapping(target = "agentVersion", source = "agentMetadata.version")
    @Mapping(target = "targetModel", source = "agentMetadata.targetModel")
    @Mapping(target = "temperature", source = "agentMetadata.temperature")
    @Mapping(target = "systemPrompt", source = "agentMetadata.systemPrompt")
    @Mapping(target = "assignedTools", source = "agentMetadata.assignedTools")
    @Mapping(target = "samplePayloads", expression = "java(toDocumentSamples(assessment.getSamplePayloads()))")
    @Mapping(target = "findings", expression = "java(assessment.getFindings())")
    AgentAssessmentDocument toDocument(AgentAssessment assessment);

    @Mapping(target = "eventId", expression = "java(java.util.UUID.fromString(document.getEventId()))")
    @Mapping(target = "agentMetadata", expression = "java(new AgentMetadata(document.getAgentId(), document.getAgentVersion(), document.getTargetModel(), document.getTemperature(), document.getSystemPrompt(), document.getAssignedTools()))")
    @Mapping(target = "samplePayloads", expression = "java(toDomainSamples(document.getSamplePayloads()))")
    @Mapping(target = "findings", expression = "java(document.getFindings() == null ? java.util.List.of() : java.util.List.copyOf(document.getFindings()))")
    AgentAssessment toDomain(AgentAssessmentDocument document);

    default AgentAssessmentDocument.AgentSamplePayloadDocument toDocument(SamplePayload payload) {
        return new AgentAssessmentDocument.AgentSamplePayloadDocument(payload.userInput(), payload.agentOutput());
    }

    default SamplePayload toDomain(AgentAssessmentDocument.AgentSamplePayloadDocument payload) {
        return new SamplePayload(payload.userInput(), payload.agentOutput());
    }

    default java.util.List<AgentAssessmentDocument.AgentSamplePayloadDocument> toDocumentSamples(java.util.List<SamplePayload> payloads) {
        return payloads == null ? java.util.List.of() : payloads.stream().map(this::toDocument).toList();
    }

    default java.util.List<SamplePayload> toDomainSamples(java.util.List<AgentAssessmentDocument.AgentSamplePayloadDocument> payloads) {
        return payloads == null ? java.util.List.of() : payloads.stream().map(this::toDomain).toList();
    }

    default UUID map(String value) {
        return value == null ? null : UUID.fromString(value);
    }

    default String map(UUID value) {
        return value == null ? null : value.toString();
    }

    default AgentAssessmentDocument.AgentSamplePayloadDocument mapSamplePayload(SamplePayload payload) {
        return toDocument(payload);
    }

    default SamplePayload mapSamplePayload(AgentAssessmentDocument.AgentSamplePayloadDocument payload) {
        return toDomain(payload);
    }
}