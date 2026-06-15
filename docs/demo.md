# Sentinel AgentOps Demo

## Requisitos previos

- Java 21
- Maven 3.9+
- Docker Desktop
- Variables de entorno reales para Cosmos y Microsoft IQ

## Arranque local

1. Copia `.env.example` a `.env` y completa los valores reales.
2. Levanta Kafka con `docker compose up -d`.
3. Crea los topics con `./scripts/create-topics.ps1`.
4. Ejecuta `mvn test`.
5. Arranca la app con `mvn spring-boot:run -Dspring-boot.run.profiles=local`.

## Flujo demo

1. Publica un evento `AgentAssessmentRequested`.
2. Kafka lo consume con ack manual inmediato.
3. Se ejecutan los hooks de validación.
4. Foundry IQ audita el meta-profile del agente.
5. Se agregan findings y se persisten en Cosmos DB.

## Observaciones

- Si Foundry IQ no responde, el pipeline continúa con un finding de degradación segura.
- Cosmos y Foundry IQ requieren credenciales reales para probar el flujo end-to-end.