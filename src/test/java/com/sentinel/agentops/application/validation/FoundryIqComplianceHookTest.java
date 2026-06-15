package com.sentinel.agentops.application.validation;

import com.sentinel.agentops.domain.AgentAssessmentRequested;
import com.sentinel.agentops.domain.AgentMetadata;
import com.sentinel.agentops.domain.AssessmentCategory;
import com.sentinel.agentops.domain.AssessmentFinding;
import com.sentinel.agentops.domain.AssessmentSeverity;
import com.sentinel.agentops.domain.SamplePayload;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.client.MockRestServiceServer;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@RestClientTest(FoundryIqComplianceHook.class)
@TestPropertySource(properties = {
    "microsoft.iq.foundry.endpoint=http://foundry.test/v1",
    "microsoft.iq.foundry.api-key=test-key"
})
class FoundryIqComplianceHookTest {

    private static final Logger log = LoggerFactory.getLogger(FoundryIqComplianceHookTest.class);

    @Autowired
    private FoundryIqComplianceHook hook;

    @Autowired
    private MockRestServiceServer server;

    @Test
    void analyze_debeRetornarVacio_cuandoNoHayViolaciones() {
        log.info("Ejecutando escenario 1: validacion exitosa sin violaciones");

        server.expect(requestTo("http://foundry.test/v1/policies/evaluate"))
            .andExpect(method(HttpMethod.POST))
            .andRespond(withSuccess("{\"isCompliant\": true, \"violations\": []}", MediaType.APPLICATION_JSON));

        List<AssessmentFinding> findings = hook.analyze(buildRequest());

        assertThat(findings).isEmpty();
        server.verify();
    }

    @Test
    void analyze_debeRetornarFindingHighCompliance_cuandoHayViolaciones() {
        log.info("Ejecutando escenario 2: violaciones de cumplimiento activas");

        String response = """
            {
              "isCompliant": false,
              "violations": [
                {
                  "ruleName": "PII_LEAK_PREVENTION",
                  "description": "System prompt allows unauthorized handling of social security numbers"
                }
              ]
            }
            """;

        server.expect(requestTo("http://foundry.test/v1/policies/evaluate"))
            .andExpect(method(HttpMethod.POST))
            .andRespond(withSuccess(response, MediaType.APPLICATION_JSON));

        List<AssessmentFinding> findings = hook.analyze(buildRequest());

        assertThat(findings).hasSize(1);
        AssessmentFinding finding = findings.getFirst();
        assertThat(finding.category()).isEqualTo(AssessmentCategory.COMPLIANCE);
        assertThat(finding.severity()).isEqualTo(AssessmentSeverity.HIGH);
        assertThat(finding.detail())
            .contains("PII_LEAK_PREVENTION")
            .contains("social security numbers");

        server.verify();
    }

    @Test
    void analyze_debeDegradarGracefully_cuandoFoundryIqFalla() {
        log.info("Ejecutando escenario 3: degradacion elegante ante falla de red");

        server.expect(requestTo("http://foundry.test/v1/policies/evaluate"))
            .andExpect(method(HttpMethod.POST))
            .andRespond(withServerError());

        List<AssessmentFinding> findings = hook.analyze(buildRequest());

        assertThat(findings).hasSize(1);
        AssessmentFinding fallback = findings.getFirst();
        assertThat(fallback.category()).isEqualTo(AssessmentCategory.COMPLIANCE);
        assertThat(fallback.severity()).isEqualTo(AssessmentSeverity.MEDIUM);
        assertThat(fallback.detail().toLowerCase())
            .contains("offline")
            .contains("unreachable");

        server.verify();
    }

    private AgentAssessmentRequested buildRequest() {
        AgentMetadata metadata = new AgentMetadata(
            "agent-001",
            "1.0.0",
            "gpt-4.1",
            BigDecimal.valueOf(0.2),
            "You are a secure assistant.",
            List.of("search", "calculator")
        );

        return new AgentAssessmentRequested(
            UUID.randomUUID(),
            Instant.now(),
            "tenant-a",
            metadata,
            List.of(new SamplePayload("hola", "respuesta"))
        );
    }
}