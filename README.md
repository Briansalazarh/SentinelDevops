# Sentinel AgentOps

**Event-Driven Security Auditing & Compliance Foundation for Multi-Agent Architectures**

[![Java](https://img.shields.io/badge/Java-21-orange)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.5-green)](https://spring.io/projects/spring-boot)
[![Azure Cosmos DB](https://img.shields.io/badge/Azure%20Cosmos%20DB-NoSQL-blue)](https://azure.microsoft.com/en-us/products/cosmos-db/)
[![Apache Kafka](https://img.shields.io/badge/Apache%20Kafka-Event%20Streaming-black)](https://kafka.apache.org/)
[![Microsoft IQ](https://img.shields.io/badge/Microsoft%20IQ-Foundry-purple)](https://ai.azure.com/)
[![License](https://img.shields.io/badge/License-MIT-yellow)](LICENSE)

---

## 📋 Tabla de Contenidos

1. [Problema](#problema)
2. [Solución](#solución)
3. [Microsoft IQ Integration](#microsoft-iq-integration)
4. [Arquitectura](#arquitectura)
5. [Stack Técnico](#stack-técnico)
6. [Requisitos](#requisitos)
7. [Instalación](#instalación)
8. [Configuración](#configuración)
9. [Ejecución Local](#ejecución-local)
10. [API REST](#api-rest)
11. [Flujo del Sistema](#flujo-del-sistema)
12. [Pruebas](#pruebas)
13. [Observabilidad](#observabilidad)
14. [Estructura del Proyecto](#estructura-del-proyecto)
15. [Limitaciones y Roadmap](#limitaciones-y-roadmap)
16. [Contribuciones](#contribuciones)
17. [Licencia](#licencia)

---

## 🔴 Problema

Con la proliferación de arquitecturas multi-agente, organizaciones requieren auditoría y compliance en tiempo real sobre:

- **Prompt Injection**: evaluación de intentos de jailbreak o manipulación del sistema prompt.
- **Data Leakage**: detección de exposición no autorizada de secretos, credenciales o PII.
- **Policy Compliance**: validación de que los agentes respeten políticas de compliance y cumplimiento normativo.
- **Governance**: trazabilidad completa de evaluaciones de agentes, resultados y violaciones detectadas.

Los sistemas tradicionales carecen de una capa de compliance integrada, eventos desacoplados y persistencia de hallazgos con traces distribuidas. Esto resulta en auditorías incompletas, detección tardía de riesgos y falta de evidencia de conformidad.

---

## ✅ Solución

**Sentinel AgentOps** es una plataforma event-driven de auditoría y compliance que:

1. **Ingiere eventos** de evaluación de agentes desde Kafka o REST.
2. **Ejecuta múltiples hooks de validación** en paralelo (prompt injection, data leakage, compliance externo).
3. **Integra Microsoft IQ Foundry** como capa de inteligencia para auditoría de policies.
4. **Agrega hallazgos** de todas las validaciones en un documento unificado.
5. **Persiste en Azure Cosmos DB** con particionamiento por tenant para escalabilidad.
6. **Degrada de forma segura** si servicios externos no responden, sin romper el pipeline.
7. **Expone observabilidad** completa vía metrics, logs y traces distribuidas con OpenTelemetry.

### Características Principales

| Característica | Descripción |
|---|---|
| **Event-Driven** | Consumo de eventos desde Kafka con ack manual inmediato y DLT. |
| **Multi-tenant** | Particionamiento por `tenantId` en Cosmos DB. |
| **Resiliente** | Retries con backoff exponencial, degradación segura. |
| **Compliant** | Microsoft IQ Foundry integrado, mappeo de violaciones a hallazgos estructurados. |
| **Observable** | Métricas, logs estructurados, traces distribuidas. |
| **REST API** | Ingestión manual de evaluaciones sin Kafka. |
| **Thread-safe** | Agregación concurrente de findings con `CopyOnWriteArrayList`. |

---

## 🔐 Microsoft IQ Integration

### ¿Por qué Foundry IQ?

Foundry IQ es la capa de inteligencia de Microsoft para auditoría de políticas y compliance en aplicaciones con IA. Sentinel AgentOps la integra como **hook de validación** dentro del pipeline de evaluación:

1. **Evaluación de Meta-Profile**: Envía el system prompt, herramientas asignadas y modelo del agente a Foundry IQ.
2. **Mapeo de Violaciones**: Detecta violaciones de policies (`ruleName`, `description`) y las traduce a hallazgos con categoría `COMPLIANCE` y severidad `HIGH`.
3. **Degradación Segura**: Si Foundry IQ falla o no responde, el sistema regresa un hallazgo de severidad `MEDIUM` indicando que el servicio está offline, sin romper el pipeline.

### Ejemplo de Integración

```yaml
microsoft:
  iq:
    foundry:
      endpoint: https://api.foundry.microsoft.com/v1
      api-key: YOUR_FOUNDRY_API_KEY
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
└────────────────┬────────────────────────────────┘
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
    └─ FoundryIqComplianceHook (Microsoft IQ)
    ↓
Aggregate Findings
    ↓
Determine AssessmentStatus
    ↓
Persist to Cosmos DB
    ↓
Observable (Metrics, Logs, Traces)
```

### Patrón DDD (Domain-Driven Design)

- **Domain Layer**: Entities, Value Objects, Aggregates sin dependencia de frameworks.
- **Application Layer**: Use cases y orchestration.
- **Infrastructure Layer**: Kafka, Cosmos, REST clients.
- **Interfaces Layer**: REST controllers, Kafka listeners.

---

## 🛠️ Stack Técnico

| Componente | Tecnología | Versión |
|---|---|---|
| **Runtime** | Java | 21 |
| **Framework** | Spring Boot | 3.3.5 |
| **Build** | Maven | 3.9+ |
| **Event Streaming** | Apache Kafka | 3.7 |
| **Database** | Azure Cosmos DB | NoSQL |
| **Mapping** | MapStruct | 1.6.3 |
| **Logging** | SLF4J + Logback | - |
| **Tracing** | OpenTelemetry | Latest |
| **Metrics** | Micrometer | Latest |
| **External IQ** | Microsoft Foundry IQ | Latest |
| **Testing** | JUnit 5, Mockito, Spring Test | - |
| **Container** | Docker | Latest |

---

## 📋 Requisitos

### Obligatorios

- **Java 21** o superior
- **Maven 3.9+**
- **Docker Desktop** (para Kafka local)

### Para Ejecución End-to-End

- Cuenta de **Microsoft Azure** con:
  - Recurso **Azure Cosmos DB** (NoSQL) creado
  - Credenciales de Cosmos (endpoint, key)
  - Proyecto en **Azure AI Foundry** con acceso a Foundry IQ
  - API Key de Foundry IQ

---

## 💾 Instalación

### 1. Clonar el Repositorio

```bash
git clone https://github.com/tu-usuario/sentinel-agentops.git
cd sentinel-agentops
```

### 2. Compilar el Proyecto

```bash
mvn clean install
```

Esto descargará todas las dependencias y compilará el proyecto. Deberías ver:

```
[INFO] BUILD SUCCESS
```

### 3. Verificar Estructura

```bash
tree src/main -L 3
```

---

## ⚙️ Configuración

### 1. Archivo de Variables de Entorno

Copia el template de ejemplo y completa con tus credenciales reales:

```bash
cp .env.example .env
```

Abre el archivo `.env` y reemplaza los placeholders:

```env
# Kafka Local
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

### 2. Configuración Local de Spring Boot

El archivo [`src/main/resources/application-local.yml`](src/main/resources/application-local.yml) ya contiene la configuración parametrizada. Se activa con el perfil `local`:

```bash
export SPRING_PROFILES_ACTIVE=local
```

### 3. Docker Compose para Kafka

El archivo [`docker-compose.yml`](docker-compose.yml) levanta Kafka en un contenedor:

```yaml
services:
  kafka:
    image: bitnami/kafka:3.7
    ports:
      - "9092:9092"
    environment:
      - KAFKA_CFG_NODE_ID=0
      - KAFKA_CFG_PROCESS_ROLES=controller,broker
      # ... más configuración
```

---

## 🚀 Ejecución Local

### Paso 1: Levantar Kafka Localmente

```bash
docker compose up -d
```

Verifica que Kafka está listo:

```bash
docker compose logs kafka | grep "started"
```

### Paso 2: Crear Topics

```powershell
# En PowerShell
.\scripts\create-topics.ps1
```

O manualmente:

```bash
docker exec sentinel-kafka /opt/bitnami/kafka/bin/kafka-topics.sh \
  --bootstrap-server localhost:9092 \
  --create --if-not-exists \
  --topic sentinel.agents.assessments \
  --partitions 6 --replication-factor 1
```

### Paso 3: Ejecutar Pruebas

```bash
mvn test
```

Deberías ver 3 tests exitosos:
- `FoundryIqComplianceHookTest::analyze_debeRetornarVacio_cuandoNoHayViolaciones`
- `FoundryIqComplianceHookTest::analyze_debeRetornarFindingHighCompliance_cuandoHayViolaciones`
- `FoundryIqComplianceHookTest::analyze_debeDegradarGracefully_cuandoFoundryIqFalla`

### Paso 4: Arrancar la Aplicación

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

O si prefieres compilar y ejecutar el JAR:

```bash
mvn package
java -jar target/agentops-0.1.0-SNAPSHOT.jar --spring.profiles.active=local
```

Espera a que veas:

```
... [main] c.s.a.SentinelAgentOpsApplication : Started SentinelAgentOpsApplication
```

---

## 📡 API REST

### Endpoint de Ingestión Manual

**POST** `/api/v1/assessments`

Ingiere un evento de evaluación de agente sin Kafka.

#### Request

```bash
curl -X POST http://localhost:8080/api/v1/assessments \
  -H "Content-Type: application/json" \
  -d @samples/agent-assessment-request.json
```

#### Payload de Ejemplo

Ver [`samples/agent-assessment-request.json`](samples/agent-assessment-request.json):

```json
{
  "eventId": "11111111-1111-1111-1111-111111111111",
  "timestamp": "2026-06-14T12:00:00Z",
  "tenantId": "tenant-demo",
  "agentMetadata": {
    "agentId": "agent-ops-001",
    "version": "1.0.0",
    "targetModel": "gpt-4.1",
    "temperature": 0.2,
    "systemPrompt": "You are a secure assistant.",
    "assignedTools": ["search", "calculator"]
  },
  "samplePayloads": [
    {
      "userInput": "hola",
      "agentOutput": "respuesta"
    }
  ]
}
```

#### Response (200 OK)

```json
{
  "eventId": "11111111-1111-1111-1111-111111111111",
  "tenantId": "tenant-demo",
  "agentMetadata": {
    "agentId": "agent-ops-001",
    "version": "1.0.0",
    "targetModel": "gpt-4.1",
    "temperature": 0.2,
    "systemPrompt": "You are a secure assistant.",
    "assignedTools": ["search", "calculator"]
  },
  "findings": [
    {
      "category": "COMPLIANCE",
      "severity": "HIGH",
      "detail": "PII_LEAK_PREVENTION: System prompt allows unauthorized handling of social security numbers",
      "source": "FoundryIqComplianceHook",
      "detectedAt": "2026-06-14T12:00:01Z"
    }
  ],
  "status": "APPROVED",
  "processedAt": "2026-06-14T12:00:01Z",
  "processingDurationMillis": 245
}
```

### Health Check

**GET** `/actuator/health`

```json
{
  "status": "UP"
}
```

### Métricas Prometheus

**GET** `/actuator/prometheus`

Incluye:
- `sentinel.agent_assessments.ingested_total`
- `sentinel.agent_assessments.failed_total`
- `sentinel.agent_assessments.processing.latency`

---

## 🔄 Flujo del Sistema

### Flujo Kafka (Event-Driven)

```
1. Producer envía AgentAssessmentRequested a topic "sentinel.agents.assessments"
   ↓
2. AgentAssessmentRequestedListener consume el evento (manual_immediate ack)
   ↓
3. DefaultAgentAssessmentIngestionService.ingest() orquesta:
   a) Crear AgentAssessment desde request
   b) Ejecutar DefaultAssessmentValidationPipeline
   c) Mapear violaciones a AssessmentFinding
   d) Determinar AssessmentStatus
   e) Persistir a Cosmos DB
   ↓
4. Si éxito: commit manual en Kafka
   Si error: enviar a DLT "sentinel.agents.assessments.DLT"
   ↓
5. Observable: logs, métricas, traces
```

### Flujo REST (Manual)

```
1. Cliente HTTP POST a /api/v1/assessments
   ↓
2. AgentAssessmentController.submitAssessment()
   ↓
3. Mismo flujo de orquestación (pasos 3-5 de Kafka)
   ↓
4. Response JSON con resultado
```

---

## ✅ Pruebas

### Ejecutar Todas las Pruebas

```bash
mvn test
```

### Ejecutar Solo Foundry IQ Hook Tests

```bash
mvn -Dtest=FoundryIqComplianceHookTest test
```

### Coverage

```bash
mvn test jacoco:report
# Ver target/site/jacoco/index.html
```

### Escenarios Cubiertos

1. **Foundry IQ - Éxito sin violaciones**: respuesta válida sin hallazgos.
2. **Foundry IQ - Violaciones activas**: detección e ingesta correcta.
3. **Foundry IQ - Degradación segura**: fallo de red sin romper el pipeline.
4. **Prompt Injection**: detección de patrones de jailbreak.
5. **Data Leakage**: detección de secretos, emails, PII.
6. **Persistencia Cosmos**: serialización y almacenamiento de findings.

---

## 📊 Observabilidad

### Logs

Estructurados con Slf4j + Logback, incluyen:
- Request/response de Foundry IQ
- Parsing de eventos Kafka
- Timing de persistencia en Cosmos
- Stack traces de degradación segura

Nivel recomendado para desarrollo:

```yaml
logging:
  level:
    root: INFO
    com.sentinel.agentops: DEBUG
```

### Métricas

Expuestas en `/actuator/prometheus`:

```
# Evaluaciones ingeridas totales
sentinel.agent_assessments.ingested_total{...} 42

# Evaluaciones fallidas
sentinel.agent_assessments.failed_total{...} 1

# Latencia de procesamiento (ms)
sentinel.agent_assessments.processing.latency{status="APPROVED"} histogram
```

### Traces Distribuidas

Configuradas con OpenTelemetry e OTLP. Para activar:

```yaml
management:
  tracing:
    sampling:
      probability: 1.0
```

Exportar a Jaeger, Zipkin, etc.

---

## 📁 Estructura del Proyecto

```
sentinel-agentops/
├── src/
│   ├── main/
│   │   ├── java/com/sentinel/agentops/
│   │   │   ├── SentinelAgentOpsApplication.java
│   │   │   ├── domain/
│   │   │   │   ├── AgentAssessment.java
│   │   │   │   ├── AgentAssessmentRequested.java
│   │   │   │   ├── AssessmentFinding.java
│   │   │   │   ├── AssessmentCategory.java
│   │   │   │   ├── AssessmentSeverity.java
│   │   │   │   ├── AssessmentStatus.java
│   │   │   │   ├── AgentMetadata.java
│   │   │   │   ├── SamplePayload.java
│   │   │   │   └── port/
│   │   │   │       └── AgentAssessmentRepositoryPort.java
│   │   │   ├── application/
│   │   │   │   ├── service/
│   │   │   │   │   └── DefaultAgentAssessmentIngestionService.java
│   │   │   │   ├── validation/
│   │   │   │   │   ├── AssessmentValidationHook.java
│   │   │   │   │   ├── AssessmentValidationPipeline.java
│   │   │   │   │   ├── DefaultAssessmentValidationPipeline.java
│   │   │   │   │   ├── PromptInjectionAnalysisHook.java
│   │   │   │   │   ├── DataLeakageEvaluationHook.java
│   │   │   │   │   └── FoundryIqComplianceHook.java (Microsoft IQ)
│   │   │   │   └── port/
│   │   │   │       └── AgentAssessmentIngestionUseCase.java
│   │   │   ├── infrastructure/
│   │   │   │   ├── config/
│   │   │   │   │   ├── SentinelKafkaProperties.java
│   │   │   │   │   └── SentinelCosmosProperties.java
│   │   │   │   ├── messaging/kafka/
│   │   │   │   │   ├── KafkaConfiguration.java
│   │   │   │   │   ├── AgentAssessmentRequestedListener.java
│   │   │   │   │   └── AgentAssessmentRequestedJsonDeserializer.java
│   │   │   │   └── persistence/cosmos/
│   │   │   │       ├── CosmosConfiguration.java
│   │   │   │       ├── adapter/
│   │   │   │       │   └── CosmosAgentAssessmentRepositoryAdapter.java
│   │   │   │       ├── document/
│   │   │   │       │   └── AgentAssessmentDocument.java
│   │   │   │       ├── mapper/
│   │   │   │       │   └── AgentAssessmentDocumentMapper.java
│   │   │   │       └── repository/
│   │   │   │           └── AgentAssessmentRepository.java
│   │   │   └── interfaces/
│   │   │       ├── rest/
│   │   │       │   ├── AgentAssessmentController.java
│   │   │       │   └── dto/
│   │   │       │       ├── AgentAssessmentRequestDto.java
│   │   │       │       └── AgentAssessmentResponseDto.java
│   │   │       └── mapper/
│   │   │           └── AgentAssessmentApiMapper.java
│   │   └── resources/
│   │       ├── application.yml
│   │       └── application-local.yml
│   └── test/
│       └── java/com/sentinel/agentops/
│           └── application/validation/
│               └── FoundryIqComplianceHookTest.java
├── scripts/
│   ├── create-topics.ps1
│   └── verify-local.ps1
├── docs/
│   └── demo.md
├── samples/
│   └── agent-assessment-request.json
├── pom.xml
├── docker-compose.yml
├── .env.example
├── .gitignore
└── README.md
```

---

## 🔒 Limitaciones y Roadmap

### Limitaciones Actuales

1. **Cosmos DB**: Requiere credenciales reales. No hay soporte para emulator local con Spring Data Cosmos.
2. **Foundry IQ**: Requiere acceso a Azure AI Foundry y API key activa.
3. **Kafka**: Se proporciona localmente. Para producción, requiere cluster externo o Event Hubs.
4. **Volumen de Findings**: Optimizado para hasta 10k evaluaciones/minuto. Para mayor volumen, considerar sharding.
5. **Retries**: Backoff exponencial con máximo 5s. Para casos de uso ultra-críticos, revisar delays.

### Roadmap Futuro

- [ ] Soporte para Fabric IQ y Work IQ como alternativas a Foundry IQ.
- [ ] Caché local de policies de Foundry IQ para reducir latencia.
- [ ] Batch processing de evaluaciones.
- [ ] Dashboard de compliance real-time.
- [ ] Webhooks para notificaciones de violaciones críticas.
- [ ] Integración con Azure Service Bus para DLQ distribuido.
- [ ] Soporte para múltiples tenants con isolation completo.
- [ ] Exportación de audit trail a Azure Purview.

---

## 🤝 Contribuciones

Las contribuciones son bienvenidas. Para reportar bugs o sugerir features:

1. Abre un GitHub Issue.
2. Describe el problema o feature de forma clara.
3. Si es un PR, asegúrate de:
   - Ejecutar `mvn test`.
   - Mantener el mismo estilo de código.
   - Incluir pruebas para nueva lógica.

---

## 📄 Licencia

Este proyecto está bajo licencia **MIT**. Ver [`LICENSE`](LICENSE) para detalles.

---

## 📞 Soporte & Documentación Adicional

- **Documentación de Demo**: Ver [`docs/demo.md`](docs/demo.md)
- **Variables de Entorno**: Ver [`.env.example`](.env.example)
- **Payload de Ejemplo**: Ver [`samples/agent-assessment-request.json`](samples/agent-assessment-request.json)
- **Logs Locales**: Ver `target/logs/` (si está configurado)
- **Métricas**: http://localhost:8080/actuator/prometheus

---

## 🎯 Resumen Ejecutivo

**Sentinel AgentOps** es una solución event-driven, resiliente y compliant para auditoría de agentes multi-tenant. Con **Microsoft IQ Foundry** integrado, proporciona inteligencia de compliance en tiempo real, degradación segura ante fallos y persistencia de hallazgos en Azure Cosmos DB. Diseñado para hackathones, puede ser escalado a producción con mínimas modificaciones.

**Casos de uso**:
- Auditoría de agentes LLM en producción.
- Compliance con regulaciones de IA (EU AI Act, etc.).
- Detección de prompt injection y data leakage.
- Governance de multi-tenants en SaaS.

---

**Versión**: 0.1.0-SNAPSHOT  
**Última actualización**: 2026-06-14  
**Autor**: Brian Salazar