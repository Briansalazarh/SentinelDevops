package com.sentinel.agentops.infrastructure.persistence.cosmos.document;

import com.azure.spring.data.cosmos.core.mapping.Container;
import com.azure.spring.data.cosmos.core.mapping.PartitionKey;
import com.sentinel.agentops.domain.AssessmentFinding;
import com.sentinel.agentops.domain.AssessmentStatus;
import lombok.Data;
import org.springframework.data.annotation.Id;

import java.time.Instant;
import java.util.List;

@Data
@Container(containerName = "agent-assessments", partitionKeyPath = "/tenantId")
public class AgentAssessmentDocument {

    @Id
    private String id;

    private String eventId;

    @PartitionKey
    private String tenantId;

    private Instant requestedAt;
    private String agentId;
    private String agentVersion;
    private String targetModel;
    private java.math.BigDecimal temperature;
    private String systemPrompt;
    private List<String> assignedTools;
    private List<AgentSamplePayloadDocument> samplePayloads;
    private List<AssessmentFinding> findings;
    private AssessmentStatus status;
    private Instant processedAt;
    private long processingDurationMillis;

    public record AgentSamplePayloadDocument(String userInput, String agentOutput) {
    }
}