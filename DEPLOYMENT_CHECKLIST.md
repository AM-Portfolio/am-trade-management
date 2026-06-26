# Deployment Checklist

Before deploying `am-trade-management` to your production or Kubernetes environment, you **MUST UNDO** the local bypasses we made in `am-trade-app/src/main/resources/application.yml`. 

If you do not revert these, your application will attempt to connect to `localhost:9092` in the cluster and will fail to start.

## 1. Revert Kafka Bootstrap Servers
**File:** `am-trade-app/src/main/resources/application.yml`
**Line:** ~50

**Change from (Our Local Fix):**
```yaml
  kafka:
    bootstrap-servers: ${KAFKA_BOOTSTRAP_SERVERS:localhost:9092}
```
**Back to (Original):**
```yaml
  kafka:
    bootstrap-servers: ${KAFKA_BOOTSTRAP_SERVERS:kafka.dev.svc.cluster.local:9092}
```

## 2. Revert Kafka Security Protocol
**File:** `am-trade-app/src/main/resources/application.yml`
**Line:** ~52

**Change from (Our Local Fix):**
```yaml
    properties:
      security.protocol: ${KAFKA_SECURITY_PROTOCOL:PLAINTEXT}
      sasl.mechanism: ${KAFKA_SASL_MECHANISM:PLAIN}
      sasl.jaas.config: ${KAFKA_SASL_JAAS_CONFIG:}
```
**Back to (Original):**
```yaml
    properties:
      security.protocol: ${KAFKA_SECURITY_PROTOCOL:SASL_PLAINTEXT}
      sasl.mechanism: ${KAFKA_SASL_MECHANISM:SCRAM-SHA-256}
      sasl.jaas.config: ${KAFKA_SASL_JAAS_CONFIG:org.apache.kafka.common.security.scram.ScramLoginModule required username="${KAFKA_SASL_USERNAME:kafkaUser}" password="${KAFKA_SASL_PASSWORD}";}
```

## 3. Revert MongoDB URI Fallback
**File:** `am-trade-app/src/main/resources/application.yml`
**Line:** ~40

**Change from (Our Local Fix):**
```yaml
  data:
    mongodb:
      uri: ${MONGODB_URI:mongodb://localhost:27017/trade}
```
**Back to (Original):**
```yaml
  data:
    mongodb:
      uri: ${MONGODB_URI}
```

## 4. Revert Local Mock Authentication
**File:** `am-trade-app/src/main/resources/application-security.yml`
**Line:** ~4

**Change from (Our Local Fix):**
```yaml
  security:
    enabled: true
    local-mock:
      enabled: true
      user-id: "local-dev-user"
    public-paths:
      - "/actuator/**"
```
**Back to (Original):**
```yaml
  security:
    enabled: true
    public-paths:
      - "/actuator/**"
```

---
*Note for the future: To avoid having to manually change files back and forth, you should run your application locally using the `local` Spring Boot profile (`mvn spring-boot:run -Dspring-boot.run.profiles=local`), which will automatically load `application-local.yml` and override the settings without touching the main `application.yml` file!*

