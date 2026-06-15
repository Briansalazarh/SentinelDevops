package com.sentinel.agentops.domain;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

public record AgentMetadata(
    String agentId,
    String version,
    String targetModel,
    BigDecimal temperature,
    String systemPrompt,
    List<String> assignedTools
) {
    public AgentMetadata {
        Objects.requireNonNull(agentId, "agentId must not be null");
        Objects.requireNonNull(version, "version must not be null");
        Objects.requireNonNull(targetModel, "targetModel must not be null");
        Objects.requireNonNull(temperature, "temperature must not be null");
        Objects.requireNonNull(systemPrompt, "systemPrompt must not be null");
        assignedTools = assignedTools == null ? List.of() : List.copyOf(assignedTools);
    }
}