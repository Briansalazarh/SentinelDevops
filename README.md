# Sentinel AgentOps

**Event-Driven Security Auditing & Regulatory Compliance Framework for Multi-Agent Architectures**

[![Java](https://img.shields.io/badge/Java-21-orange)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.5-green)](https://spring.io/projects/spring-boot)
[![Azure Cosmos DB](https://img.shields.io/badge/Azure%20Cosmos%20DB-NoSQL-blue)](https://azure.microsoft.com/en-us/products/cosmos-db/)
[![Apache Kafka](https://img.shields.io/badge/Apache%20Kafka-Event%20Streaming-black)](https://kafka.apache.org/)
[![Microsoft IQ](https://img.shields.io/badge/Microsoft%20IQ-Foundry-purple)](https://ai.azure.com/)
[![Testcontainers](https://img.shields.io/badge/Testcontainers-Integration%20Tests-blue)](https://testcontainers.com/)
[![License](https://img.shields.io/badge/License-MIT-yellow)](LICENSE)

> **Sentinel AgentOps** es un framework de nivel profesional para la auditoría de seguridad, gobernanza y cumplimiento normativo en arquitecturas multi-agente basadas en Inteligencia Artificial. Construido con **Spring Boot 3.3**, **Java 21**, **Apache Kafka** y **Microsoft IQ Foundry**, aplica principios de **Domain-Driven Design (DDD)** y **Arquitectura Dirigida por Eventos (EDA)** para evaluar en tiempo real riesgos de *Prompt Injection*, exposición de datos sensibles (*Data Leakage*) y violaciones de políticas institucionales.

---

## 📋 Tabla de Contenidos

1. [Problema](#problema)
2. [Solución](#solución)
3. [Decisiones de Diseño Arquitectónico](#decisiones-de-diseño-arquitectónico)
4. [Microsoft IQ Integration](#microsoft-iq-integration)
5. [Arquitectura](#arquitectura)
6. [Stack Técnico](#stack-técnico)
7. [Requisitos](#requisitos)
8. [Instalación](#instalación)
9. [Configuración](#configuración)
10. [Ejecución Local](#ejecución-local)
11. [API REST](#api-rest)
12. [Flujo del Sistema](#flujo-del-sistema)
13. [Pruebas](#pruebas)
14. [Observabilidad](#observabilidad)
15. [Estructura del Proyecto](#estructura-del-proyecto)
16. [Limitaciones y Roadmap](#limitaciones-y-roadmap)
17. [Contribuciones](#contribuciones)
18. [Licencia](#licencia)

---

## 🔴 Problema

Con la proliferación de arquitecturas multi-agente, las organizaciones requieren auditoría y cumplimiento normativo en tiempo real sobre:

- **Prompt Injection**: evaluación de intentos de jailbreak o manipulación del system prompt.
- **Data Leakage**: detección de exposición no autorizada de secretos, credenciales o PII (información de identificación personal).
- **Policy Compliance**: validación de que los agentes respeten políticas de compliance normativo y regulaciones de IA.
- **Governance**: trazabilidad completa de evaluaciones de agentes, resultados y violaciones detectadas por tenant.

Los sistemas tradicionales carecen de una capa de compliance integrada, eventos desacoplados y persistencia de hallazgos con trazas distribuidas. Esto resulta en auditorías incompletas, detección tardía de riesgos y falta de evidencia de conformidad.

---

## ✅ Solución

**Sentinel AgentOps** es una plataforma event-driven de auditoría y compliance que:

1. **Ingiere eventos** de evaluación de agentes desde Kafka o REST API.
2. **Ejecuta múltiples hooks de validación** (prompt injection, data leakage, compliance externo).
3. **Integra Microsoft IQ Foundry** como capa de inteligencia para auditoría de políticas de IA.
4. **Agrega hallazgos** de todas las validaciones en un documento unificado de dominio.
5. **Persiste en Azure Cosmos DB** con particionamiento por `tenantId` para escalabilidad masiva.
6. **Degrada de forma segura** mediante reintentos con backoff exponencial y recuperaciones ante fallos externos.
7. **Expone observabilidad** completa vía métricas Prometheus, logs estructurados y traces OpenTelemetry.

### Características Principales

| Característica | Descripción |
|---|---|
| **Event-Driven** | Consumo de eventos desde Kafka con acks manuales inmediatos y Dead Letter Topic (DLT). |
| **Multi-tenant** | Particionamiento por `tenantId` en Cosmos DB para aislamiento enterprise. |
| **Resiliente** | Retries con backoff exponencial (`Spring Retry`) y degradación segura. |
| **Compliant** | Microsoft IQ Foundry integrado, mapeo de violaciones a hallazgos estructurados. |
| **Observable** | Métricas Micrometer/Prometheus, logs Slf4j estructurados y traces OpenTelemetry. |
| **REST API** | Ingestión síncrona manual de evaluaciones. |
| **Clean Architecture & DDD** | Separación clara de responsabilidades sin acoplamiento a frameworks en la capa de dominio. |

---

## 🏛️ Decisiones de Diseño Arquitectónico

Para garantizar que **Sentinel AgentOps** funcione como un proyecto de portafolio de nivel profesional y esté **Listo para Producción**, se aplicaron los siguientes patrones y decisiones arquitectónicas:

1. **Domain-Driven Design (DDD) & Clean Architecture**:
   - **Domain Layer**: Contiene los Agregados (`AgentAssessment`), Objetos de Valor (`AssessmentFinding`, `AgentMetadata`), Enums e Interfaces de Puertos (`AgentAssessmentRepositoryPort`). No posee dependencias de Spring Boot ni infraestructura.
   - **Application Layer**: Implementa los casos de uso (`AgentAssessmentIngestionUseCase`) y orquesta el pipeline de validación.
   - **Infrastructure Layer**: Contiene adaptadores tecnológicos (Azure Cosmos DB, Apache Kafka, `RestClient` para Microsoft Foundry IQ) y mapeadores MapStruct.
   - **Interfaces Layer**: Expone endpoints REST y Listeners de eventos Kafka.

2. **Patrón Strategy (Hooks de Validación)**:
   - El pipeline de validación (`AssessmentValidationPipeline`) utiliza el patrón Strategy permitiendo registrar de forma extensible e independiente distintos hooks de análisis (`PromptInjectionAnalysisHook`, `DataLeakageEvaluationHook`, `FoundryIqComplianceHook`).

3. **Resiliencia & Degradación Segura (Safe Degradation)**:
   - La integración externa con **Microsoft Foundry IQ** aplica **Spring Retry** con la anotación `@Retryable` (3 intentos, delay inicial de 1000ms y multiplicador 2.0).
   - Ante la indisponibilidad persistente del servicio REST externo, un método `@Recover` ejecuta una degradación elegante registrando un hallazgo con severidad `MEDIUM` sin interrumpir ni hacer fallar la ingesta del evento en el pipeline.

4. **Configuración de Producción Externa y Perfiles**:
   - Todos los parámetros de conexión (Kafka, Azure Cosmos DB, Microsoft Foundry IQ) están externalizados mediante variables de entorno con fallbacks seguros para desarrollo local en `application.yml`.
   - Se proporcionan perfiles dedicados `application-dev.yml` y `application-prod.yml` que configuran garantías de producción para Kafka (`acks=all`, `retries=5`, `max.in.flight.requests.per.connection=5`, `enable.idempotence=true`) y timeouts extendidos para Cosmos DB en la nube.

5. **Entorno de Verificación Integrado (Testcontainers & Spring Integration Tests)**:
   - Inclusión de **Testcontainers** en el `pom.xml` y desarrollo de suite de pruebas de integración (`AssessmentPipelineIntegrationTest`) que valida el arranque de contexto Spring Boot y el procesamiento completo de eventos desde Kafka a través de todo el pipeline.

---

## 🔐 Microsoft IQ Integration

### ¿Por qué Foundry IQ?

Foundry IQ es la capa de inteligencia de Microsoft para auditoría de políticas y compliance en aplicaciones con IA. Sentinel AgentOps la integra como **hook de validación** dentro del pipeline de evaluación:

1. **Evaluación de Meta-Profile**: Envía el system prompt, herramientas asignadas y modelo del agente a Foundry IQ.
2. **Mapeo de Violaciones**: Detecta violaciones de policies (`ruleName`, `description`) y las traduce a hallazgos con categoría `COMPLIANCE` y severidad `HIGH`.
3. **Degradación Segura**: Si Foundry IQ falla o no responde tras 3 reintentos con backoff exponencial, el sistema regresa un hallazgo de severidad `MEDIUM` indicando que el servicio está offline, sin romper el pipeline.

### Ejemplo de Configuración

```yaml
microsoft:
  iq:
    foundry:
      endpoint: ${MICROSOFT_IQ_FOUNDRY_ENDPOINT:https://api.foundry.microsoft.com/v1}
      api-key: ${MICROSOFT_IQ_FOUNDRY_API_KEY:mock-key}
```

El hook se ejecuta automáticamente como parte de `DefaultAssessmentValidationPipeline`:

```java
// FoundryIqComplianceHook.java
POST /policies/evaluate
Body: {
  "metaProfile": {
    "systemPrompt": "You are a secure assistant.",
    "assignedTools": ["search", "calculator"],
    "model": "gpt-4.1"
  }
}
```

Respuesta esperada:
```json
{
  "isCompliant": false,
  "violations": [
    {
      "ruleName": "PII_LEAK_PREVENTION",
      "description": "System prompt allows unauthorized handling of social security numbers",
      "severity": "HIGH"
    }
  ]
}
```

---

## 🏗️ Arquitectura

### Diagrama de Capas

```
┌─────────────────────────────────────────────┐
│         Interfaces & Controllers            │
│  ├─ REST API (AgentAssessmentController)    │
│  └─ Kafka Listener (AgentAssessmentListener)│
└────────────────┬────────────────────────────┘
                 │
┌─────────────────┴────────────────────────────┐
│       Application & Use Cases                │
│  ├─ DefaultAgentAssessmentIngestionService  │
│  └─ DefaultAssessmentValidationPipeline      │
└────────────────┬────────────────────────────┘
                 │
┌─────────────────┴──────────────────────────────┐
│       Validation Hooks (Strategy Pattern)      │
│  ├─ PromptInjectionAnalysisHook               │
│  ├─ DataLeakageEvaluationHook                 │
│  └─ FoundryIqComplianceHook                   │
└────────────────┬──────────────────────────────┘
                 │
┌─────────────────┴──────────────────────────────┐
│           Domain Model                         │
│  ├─ AgentAssessment (mutable aggregate)       │
│  ├─ AssessmentFinding (value object)          │
│  ├─ AgentMetadata (value object)              │
│  └─ AssessmentStatus (enum)                   │
└────────────────┬──────────────────────────────┘
                 │
┌─────────────────┴──────────────────────────────┐
│       Infrastructure Layer                     │
│  ├─ Kafka Configuration & Listeners           │
│  ├─ Cosmos DB Configuration & Repository      │
│  ├─ Mappers (MapStruct)                       │
│  └─ External Service Clients (RestClient)     │
└─────────────────────────────────────────────────┘
```

### Flujo de Procesamiento

```
Event (Kafka/REST)
    ↓
AgentAssessmentRequested (Domain Event)
    ↓
Validation Pipeline
    ├─ PromptInjectionAnalysisHook
    ├─ DataLeakageEvaluationHook
    └─ FoundryIqComplianceHook (Microsoft IQ with Retries)
    ↓
Aggregate Findings
    ↓
Determine AssessmentStatus
    ↓
Persist to Cosmos DB
    ↓
Observable (Metrics, Logs, Traces)
```

---

## 🛠️ Stack Técnico

| Componente | Tecnología | Versión |
|---|---|---|
| **Runtime** | Java | 21 |
| **Framework** | Spring Boot | 3.3.5 |
| **Resiliencia** | Spring Retry + Spring AOP | 3.3.5 |
| **Build** | Maven | 3.9+ |
| **Event Streaming** | Apache Kafka | 3.7 |
| **Database** | Azure Cosmos DB | NoSQL |
| **Mapping** | MapStruct | 1.6.3 |
| **Logging** | SLF4J + Logback | - |
| **Tracing** | OpenTelemetry | Latest |
| **Metrics** | Micrometer | Latest |
| **External IQ** | Microsoft Foundry IQ | Latest |
| **Testing** | JUnit 5, Mockito, Spring Test, Testcontainers | Latest |
| **Container** | Docker | Latest |

---

## 📋 Requisitos

### Obligatorios

- **Java 21** o superior
- **Maven 3.9+**
- **Docker Desktop** (para Kafka local o Testcontainers)

---

## ⚙️ Configuración

### 1. Variables de Entorno

Puedes configurar el archivo `.env` o definir las variables directamente en tu entorno:

```env
# Kafka
KAFKA_BOOTSTRAP_SERVERS=localhost:9092
KAFKA_GROUP_ID=sentinel-agentops-assessments
KAFKA_CONCURRENCY=8

# Azure Cosmos DB
COSMOS_ENDPOINT=https://YOUR-COSMOS-ACCOUNT.documents.azure.com:443/
COSMOS_KEY=YOUR_COSMOS_PRIMARY_KEY
COSMOS_DATABASE=sentinel-agentops
COSMOS_CONTAINER=agent-assessments

# Microsoft IQ Foundry
MICROSOFT_IQ_FOUNDRY_ENDPOINT=https://api.foundry.microsoft.com/v1
MICROSOFT_IQ_FOUNDRY_API_KEY=YOUR_FOUNDRY_API_KEY
```

### 2. Perfiles de Spring Boot

- `local` (por defecto): Desarrollo local con fallbacks seguros.
- `dev`: Entorno de desarrollo conectado a servicios dev.
- `prod`: Entorno de producción con máximas garantías de idempotencia en Kafka y timeouts de nube.

Para ejecutar en perfil de producción:

```bash
java -jar target/agentops-0.1.0-SNAPSHOT.jar --spring.profiles.active=prod
```

---

## 📡 API REST

### Endpoint de Ingestión Manual

**POST** `/api/v1/assessments`

```bash
curl -X POST http://localhost:8080/api/v1/assessments \
  -H "Content-Type: application/json" \
  -d @samples/agent-assessment-request.json
```

---

## ✅ Pruebas

### Ejecutar Pruebas Automatizadas (Unitarias e Integración)

```bash
mvn test
```

Incluye:
- `FoundryIqComplianceHookTest`: Validación de reintentos y degradación segura con Spring Retry.
- `AssessmentPipelineIntegrationTest`: Prueba de integración end-to-end simulando la ingesta de eventos desde Kafka a través de todo el pipeline.

---

## 📄 Licencia

Este proyecto está bajo licencia **MIT**. Ver [`LICENSE`](LICENSE) para detalles.

---

**Versión**: 0.1.0-SNAPSHOT  
**Estado**: Listo para Producción / Portfolio Grade