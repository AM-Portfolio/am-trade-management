# AM Trade Management: Troubleshooting & Learning Summary

This document summarizes the technical challenges resolved during the stabilization of the `am-trade-management` repository and the deployment to the Pre-production Kubernetes cluster.

---

## 1. Logging & Annotation Processor Stabilization
**The Issue:** The CI/CD build was occasionally failing to find the `log` variable, even though `@Slf4j` was present.
**The Solution:** Replaced Lombok's `@Slf4j` with explicit SLF4J logger definitions:
`private static final Logger log = LoggerFactory.getLogger(YourClass.class);`
**Learning:** While Lombok is convenient, annotation processing can sometimes be flaky in complex multi-module Maven builds or specific CI/CD environments. Using explicit loggers is more "bulletproof" for production-grade code.

## 2. Java Syntax & Git Conflicts
**The Issue:** Build failures caused by residual `<<<<<<< HEAD` merge markers and a duplicate `recalculatePortfolio` method in `PortfolioSummaryController`.
**The Solution:** Manually audited the files, removed the corrupted Git markers, and deleted the duplicate method declaration.
**Learning:** Always double-check your files after a complex `git merge` or `git rebase`. Compilation errors like "Duplicate method" are a red flag that a merge wasn't cleaned up properly.

## 3. Docker Build: Private Registry Authentication
**The Issue:** The Docker build failed with `401 Unauthorized` because it couldn't download the `am-observability-lib` from GitHub Packages.
**The Solution:** Updated the `Dockerfile` to:
1. Accept a `GITHUB_PACKAGES_TOKEN` as a build argument.
2. Copy the local `settings.xml` into the build container.
3. Pass the token into the Maven command.
**Learning:** A `Dockerfile` is an isolated environment. Even if your PC or the GitHub Runner has access to a private registry, you must explicitly "pass the keys" into the Docker build process if Maven needs to download private dependencies *inside* the container.

## 4. Kubernetes: The UID 1000 Permission Conflict
**The Issue:** The pod was stuck in `CrashLoopBackOff` with `java.io.FileNotFoundException: ... (Permission denied)`.
**The Solution:** Updated the `Dockerfile` to create the `spring` user with **UID 1000** and **GID 1000**.
**The Concept:** 
- In Docker, a non-root user usually gets UID 100 or 101.
- In your Kubernetes Helm chart, a `securityContext` enforced `runAsUser: 1000`.
- Because the process was running as **User 1000**, it was blocked from writing to a folder owned by **User 100**.
**Learning:** Always align your Docker `USER` ID with your Kubernetes `securityContext.runAsUser`. If they don't match, you will almost always hit permission issues with files and logs.

## 5. Observability: DNS & Service Discovery
**The Issue:** `UnknownHostException: grafana-alloy.monitoring.svc.cluster.local`.
**The Solution:** Changed the `OTEL_EXPORTER_OTLP_TRACES_ENDPOINT` in `values.yaml` to point to `otel-collector` instead of `grafana-alloy`.
**Learning:** In Kubernetes, services talk to each other using internal DNS names (`service-name.namespace.svc.cluster.local`). If your configuration points to a service name that doesn't exist in that specific cluster, the network request will fail immediately with a "Name does not resolve" error.

---
*Note: The code is now stable, the builds are passing, and the permissions are aligned. Final step is to push the `values.yaml` fix to see traces in Tempo.*
