package com.sentinel.agentops.interfaces.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record AgentAssessmentRequestDto(
    @NotNull UUID eventId,
    @NotNull Instant timestamp,
    @NotBlank String tenantId,
    @NotNull @Valid AgentMetadataDto agentMetadata,
    @NotEmpty @Valid List<SamplePayloadDto> samplePayloads
) {
    public record AgentMetadataDto(
        @NotBlank String agentId,
        @NotBlank String version,
        @NotBlank String targetModel,
        @NotNull BigDecimal temperature,
        @NotBlank String systemPrompt,
        @NotEmpty List<@NotBlank String> assignedTools
    ) {
    }

    public record SamplePayloadDto(
        @NotBlank String userInput,
        @NotBlank String agentOutput
    ) {
    }
}