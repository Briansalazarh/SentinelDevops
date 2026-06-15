package com.sentinel.agentops.domain;

import java.util.Objects;

public record SamplePayload(
    String userInput,
    String agentOutput
) {
    public SamplePayload {
        Objects.requireNonNull(userInput, "userInput must not be null");
        Objects.requireNonNull(agentOutput, "agentOutput must not be null");
    }
}