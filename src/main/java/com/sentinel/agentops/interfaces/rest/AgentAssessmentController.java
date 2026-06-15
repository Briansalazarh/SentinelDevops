package com.sentinel.agentops.interfaces.rest;

import com.sentinel.agentops.application.port.AgentAssessmentIngestionUseCase;
import com.sentinel.agentops.domain.AgentAssessment;
import com.sentinel.agentops.interfaces.dto.AgentAssessmentRequestDto;
import com.sentinel.agentops.interfaces.dto.AgentAssessmentResponseDto;
import com.sentinel.agentops.interfaces.mapper.AgentAssessmentApiMapper;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/assessments")
public class AgentAssessmentController {

    private final AgentAssessmentIngestionUseCase ingestionUseCase;
    private final AgentAssessmentApiMapper mapper;

    public AgentAssessmentController(AgentAssessmentIngestionUseCase ingestionUseCase, AgentAssessmentApiMapper mapper) {
        this.ingestionUseCase = ingestionUseCase;
        this.mapper = mapper;
    }

    @PostMapping
    public ResponseEntity<AgentAssessmentResponseDto> ingest(@Valid @RequestBody AgentAssessmentRequestDto request) {
        AgentAssessment assessment = ingestionUseCase.ingest(mapper.toDomain(request));
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(mapper.toResponse(assessment));
    }
}