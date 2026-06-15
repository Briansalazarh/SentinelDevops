package com.sentinel.agentops.interfaces.mapper;

import com.sentinel.agentops.domain.AgentAssessment;
import com.sentinel.agentops.domain.AgentAssessmentRequested;
import com.sentinel.agentops.domain.AgentMetadata;
import com.sentinel.agentops.domain.SamplePayload;
import com.sentinel.agentops.interfaces.dto.AgentAssessmentRequestDto;
import com.sentinel.agentops.interfaces.dto.AgentAssessmentResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AgentAssessmentApiMapper {

    @Mapping(target = "samplePayloads", source = "samplePayloads")
    AgentAssessmentRequested toDomain(AgentAssessmentRequestDto dto);

    @Mapping(target = "findingsCount", expression = "java(assessment.getFindings() == null ? 0 : assessment.getFindings().size())")
    AgentAssessmentResponseDto toResponse(AgentAssessment assessment);

    default AgentMetadata toDomain(AgentAssessmentRequestDto.AgentMetadataDto dto) {
        return new AgentMetadata(
            dto.agentId(),
            dto.version(),
            dto.targetModel(),
            dto.temperature(),
            dto.systemPrompt(),
            dto.assignedTools());
    }

    default SamplePayload toDomain(AgentAssessmentRequestDto.SamplePayloadDto dto) {
        return new SamplePayload(dto.userInput(), dto.agentOutput());
    }

    default List<SamplePayload> toDomain(List<AgentAssessmentRequestDto.SamplePayloadDto> payloads) {
        return payloads.stream().map(this::toDomain).toList();
    }
}