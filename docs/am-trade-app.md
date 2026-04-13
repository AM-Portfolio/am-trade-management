# Module Analysis: `am-trade-app`

**Role**: Execution Core & Bootstrap Module

## 1. What is this module doing?
The `am-trade-app` module is the **Brain** and **Entry Point** of the entire application. In a Modular Monolith architecture, it’s best practice to keep framework boilerplate separated from business logic. This module contains almost no actual code; instead, its entire job is to:
1. Gather all the other sub-modules together.
2. Provide the configuration files (`yml`).
3. Boot up the Spring Framework.
4. Package everything into a single executable Fat JAR file that can be deployed to a server or Kubernetes cluster.

---

## 2. Dependencies & Sub-Modules (`pom.xml`)
If you look at the `pom.xml` of this module, it doesn't just depend on Spring. It acts as the anchor linking the entire project. It explicitly imports:
*   `am-trade-api` (For the REST Controllers)
*   `am-trade-dashboard`
*   `am-trade-persistence`
*   `am-trade-kafka`
*   `am-trade-exceptions`
*   `am-trade-analytics`

It also imports `spring-boot-starter-web` which provides the embedded Tomcat server needed to actually host the endpoints on port `8080`. The file utilizes the `spring-boot-maven-plugin` to run a `repackage` goal, taking all these scattered parts and wrapping them up for deployment.

---

## 3. The Code (`TradeManagementApplication.java`)
There is only one Java file in this entire folder:
`src/main/java/am/trade/app/TradeManagementApplication.java`

**Core Annotations powering the application:**
*   `@SpringBootApplication(scanBasePackages = {"am.trade"})`: This explicitly tells Spring Boot, "Do not just look in this folder. Look in **every** folder that starts with `am.trade` across the entire project."
*   `@Import({ ...AutoConfiguration.class })`: This is a professional pattern. Rather than hoping Spring randomly finds the sub-modules, it explicitly imports the Configuration classes of Kafka, API, Analytics, etc., guaranteeing they initialize in the correct order.
*   `@EnableMongoRepositories(...)`: Forces the startup sequence to connect to the database securely.

---

## 4. Endpoints
> [!NOTE]
> **This module contains zero business endpoints.** 
> All `/api/v1/...` REST paths are inherited dynamically from the `am-trade-api` module.

However, because of the Actuator configuration, this module *does* expose infrastructure endpoints:
*   `GET /actuator/health` (Used by Kubernetes to check if the app crashed)
*   `GET /actuator/info` 
*   `GET /actuator/prometheus` (Exposes statistics for Grafana scraping)

---

## 5. Configurations (`src/main/resources/...`)
This module stores the `application.yml` and environment-specific configs (`application-preprod.yml`, `application-prod.yml`). 

**What is configured here?**
1.  **MongoDB Connections**: It maps the URI down to `mongodb.dev.svc.cluster.local:27017` utilizing an auth source.
2.  **Kafka Protocols**: It sets up `SASL_PLAINTEXT` and the `PLAIN` login module (username/password) requiring authentication to publish trade metrics. It points to `kafka.dev.svc.cluster.local:9092`.
3.  **Circuit Breakers & Retries**: Configures safety mechanisms (e.g., if a service fails, it retries 3 times with a 2.0 backoff multiplier). This ensures stability.
4.  **Logging**: The root log level is set to `INFO`, suppressing unnecessary debugging noise in production.

## Summary for Developers
If you need to change business logic, add an API route, or calculate a new metric, **DO NOT edit this module.** 
You should only touch `am-trade-app` if you need to:
*   Change a database password or Kafka connection string.
*   Add a new environment (e.g., creating `application-staging.yml`).
*   Create an entirely new root module that needs to be imported into the build cycle.
