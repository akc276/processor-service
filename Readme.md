# Processor Service Microservice

Kotlin Spring Boot microservice responsible for consuming request events from `consumer-api-gateway`, processing event payloads, and publishing response events back to `consumer-api-gateway` via Azure Event Hubs local simulator (Kafka protocol).

---

## Git Repositories
- **Processor Service Repo:** [https://github.com/akc276/processor-service.git](https://github.com/akc276/processor-service.git)
- **Consumer API Gateway Repo:** [https://github.com/akc276/customer-api-gateway.git](https://github.com/akc276/customer-api-gateway.git)

```zsh
# Clone Processor Service
git clone https://github.com/akc276/processor-service.git
```

---

## Technical Specifications
- **Language:** Kotlin 2.3.21 / Java 21
- **Framework:** Spring Boot 4.1.0 / Spring Kafka
- **Messaging:** Azure Event Hubs Local Simulator (`SASL_PLAINTEXT:9092`)
- **Port:** `8081`

---

## Distributed Trace Correlation
- Consumes from topic: `gateway-requests` (Consumer group: `processor-cg`)
- Extracts trace header: `X-Correlation-ID` or `traceId`
- Populates SLF4J MDC: `MDC.put("correlationId", traceId)`
- Produces to topic: `service-responses` with attached header `X-Correlation-ID: <traceId>`

---

## Local Build & Execution

### Build Jar via Gradle
```zsh
./gradlew build
```

### Run Tests
```zsh
./gradlew test
```

### Build Docker Image
```zsh
docker build -t processor-service:local .
```

---

## Deployment & ArgoCD Setup

For complete instructions on running Docker Compose, k3d Kubernetes, Helm 3 charts, and ArgoCD GitOps deployment for both microservices, please refer to the [Consumer API Gateway README](../consumer-api-gateway/Readme.md).

