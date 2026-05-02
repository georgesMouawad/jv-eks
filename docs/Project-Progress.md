## Overview

MusicSync is a cloud-native microservices platform that demonstrates end-to-end DevOps engineering: infrastructure as code, containerisation, Kubernetes orchestration, GitOps CI/CD, observability, and real-time distributed features.

The system allows authenticated users to create shared "crates" (music track collections), upload audio files directly to S3 via pre-signed URLs, and receive live UI updates pushed over WebSocket when collaborators add new tracks.

---

## Architecture

### Services

| Service | Port | Responsibility |
|---|---|---|
| **api-gateway** | 8080 | Single ingress point; routes to downstream services |
| **auth-service** | 8081 | Registration, login, JWT issuance (HS256) |
| **user-service** | 8082 | User profile CRUD |
| **crate-service** | 8083 | Crate/item management, S3 pre-signed URLs, Redis Pub/Sub publish |
| **sync-service** | 8084 | WebSocket broadcast; subscribes to Redis Pub/Sub channels |
| **client** | 3000 | Next.js 15 frontend (standalone Docker image) |

### Shared Library — `server/libs/common`

A `java-library` Gradle module depended on by all backend services:

* `DatabaseCreationPostProcessor` — `BeanFactoryPostProcessor` that auto-creates the service database on first boot (catches SQLState `42P04` if it already exists).
* `JwtVerifier` — stateless JJWT 0.12.6 token parser (HS256); no Spring annotations so services instantiate it explicitly.
* `JwtAuthentication` — `AbstractAuthenticationToken` carrying `userId` (UUID) + `email`.
* `JwtAuthenticationFilter` — `OncePerRequestFilter` that populates the `SecurityContextHolder`.

### Infrastructure (AWS, eu-central-1)

* VPC with public/private subnets, Internet Gateway + NAT Gateway
* EKS cluster + managed node groups
* RDS PostgreSQL (one shared instance, one database per service, created at boot by `DatabaseCreationPostProcessor`)
* ECR repositories per service
* ElastiCache Redis cluster (used by crate-service + sync-service)
* S3 bucket (`cratesync-audio-assets`) for direct audio uploads
* IRSA role for crate-service (credential-free AWS SDK v2 access)

---

## Repository Structure

```
/infra
  /tofu                   # OpenTofu modules: vpc, eks, rds, ecr, iam, s3, elasticache, irsa
/server                   # Gradle multi-module root
  /libs
    /common               # Shared Java library (DB creation, JWT)
  /services
    /auth-service         # Spring Boot 4, port 8081
    /user-service         # Spring Boot 4, port 8082
    /api-gateway          # Spring Boot 4, port 8080
    /crate-service        # Spring Boot 4, port 8083
    /sync-service         # Spring WebFlux 4, port 8084
/client                   # Next.js 15 frontend
/k8s
  /base                   # Kustomize base (all services + monitoring)
  /overlays
    /dev                  # Dev-specific image tags + infra endpoint patches
    /prod                 # Prod-specific image tags + infra endpoint patches
/.github/workflows
  deploy.yml              # Build matrix (6 images) + GitOps manifest update
```

---

## Technology Stack

| Concern | Technology |
|---|---|
| Backend | Spring Boot 4.0.6, Java 25, Gradle 9.4.1 |
| Reactive | Spring WebFlux, Project Reactor |
| Auth | JJWT 0.12.6 (HS256), Spring Security |
| Database | PostgreSQL (RDS), Flyway migrations, Spring Data JPA |
| Cache / Pub/Sub | Redis (ElastiCache), `ReactiveStringRedisTemplate` |
| Object Storage | AWS S3 (SDK v2, pre-signed PUT URLs) |
| Frontend | Next.js 15.3.2, React 19, Tailwind CSS 4, TypeScript |
| Containers | Docker (3-stage jlink builds, eclipse-temurin:25 → alpine:3.21, non-root user) |
| Infrastructure | OpenTofu 1.11.5, AWS provider ~> 5.0 |
| Orchestration | Kubernetes (EKS), Kustomize (base + dev/prod overlays) |
| Ingress | Traefik (WebSocket-aware routing) |
| GitOps | ArgoCD (auto-sync dev, manual prod) |
| CI/CD | GitHub Actions (OIDC → ECR push, manifest commit) |
| Observability | Prometheus, Grafana, Spring Actuator (`/actuator/prometheus`) |
| Scaling | HPA (CPU + memory targets) on all services |

---

## Phase 1: Infrastructure Provisioning ✅

Provisioned with OpenTofu (`infra/tofu/`):

* VPC, subnets, gateways
* EKS cluster + node groups
* RDS PostgreSQL
* ECR repositories (one per service + client)
* ElastiCache Redis
* S3 bucket with CORS policy for direct browser uploads
* IRSA role bound to `crate-service` Kubernetes ServiceAccount

---

## Phase 2: Backend Services ✅

### Design Pattern

Hexagonal / Clean Architecture per service: `domain` → `application` → `infrastructure` → `web`.

### auth-service

* `POST /auth/register` — creates user, hashes password (BCrypt), returns JWT
* `POST /auth/login` — validates credentials, returns JWT
* JWT contains: `sub` = userId (UUID), `email`, `role`, 24 h expiry

### user-service

* `GET /users/me` — returns profile for the authenticated user
* `PUT /users/me` — updates first/last name, bio
* Reads `JwtAuthentication` from `SecurityContextHolder` (populated by `JwtAuthenticationFilter`)

### crate-service

* `POST /api/crates` — creates a crate owned by the caller
* `GET /api/crates/{id}` — returns crate metadata + item list
* `GET /api/crates/{id}/upload-url` — returns a 15-minute pre-signed S3 PUT URL
* `POST /api/crates/{id}/items/confirm` — saves item metadata after S3 upload; publishes to Redis `crate-updates-{id}`

### sync-service

* `GET /ws/sync/{crateId}` (WebSocket upgrade) — subscribes the session to Redis channel; broadcasts payload to all connected clients on each Pub/Sub message

### Database Migrations

Each service has `src/main/resources/db/migration/V1__init.sql` (Flyway). `ddl-auto: none`. `DatabaseCreationPostProcessor` ensures the database exists before Flyway runs.

---

## Phase 3: Frontend ✅

Next.js 15 app (`client/`) with TypeScript and Tailwind CSS 4.

| Route | Purpose |
|---|---|
| `/login` | Email + password form |
| `/register` | Username + email + password form |
| `/crates` | Create crate, open by UUID, recent crate list |
| `/crates/[id]` | Track list, file upload (pre-signed S3 flow), live sync via WebSocket |

**Upload flow:** Request pre-signed URL → `PUT` file directly to S3 (no auth header) → call confirm endpoint → UI refreshes.

**Live sync:** `useCrateSync` hook connects to `${WS_URL}/ws/sync/{crateId}`, fires `onUpdate()` on every message, reconnects with exponential back-off (1 s → 30 s cap).

Docker image: 3-stage `node:22-alpine` build, `output: standalone`, non-root user.

---

## Phase 4: Kubernetes Deployment ✅

### Kustomize Layout

```
k8s/base/
  namespace.yaml
  auth-service/           deployment, service, configmap, hpa, kustomization
  user-service/           deployment, service, configmap, hpa, kustomization
  api-gateway/            deployment, service, configmap, ingress, hpa, kustomization
  crate-service/          deployment, service, configmap, serviceaccount, hpa, kustomization
  sync-service/           deployment, service, configmap, hpa, kustomization
  monitoring/             ServiceMonitor per service
k8s/overlays/
  dev/patches/            image tags + env-specific ConfigMap values + IRSA ARN
  prod/patches/           same for prod
```

### Ingress Routing (Traefik)

* `/ws/sync/*` → sync-service:8084 (listed before catch-all; Traefik passes `Upgrade` headers natively)
* `/api/crates/*` → crate-service:8083
* `/` → api-gateway:8080

### GitOps

ArgoCD watches `k8s/overlays/dev` and `k8s/overlays/prod`. Dev has automated sync; prod requires manual approval in the ArgoCD UI.

---

## Phase 5: CI/CD Pipeline ✅

`.github/workflows/deploy.yml` — triggered on push to `main` or manual dispatch.

**Build matrix (6 images in parallel):**

| Image | Context | Dockerfile |
|---|---|---|
| auth-service | `server/` | `server/services/auth-service/Dockerfile` |
| user-service | `server/` | `server/services/user-service/Dockerfile` |
| api-gateway | `server/` | `server/services/api-gateway/Dockerfile` |
| crate-service | `server/` | `server/services/crate-service/Dockerfile` |
| sync-service | `server/` | `server/services/sync-service/Dockerfile` |
| client | `client/` | `client/Dockerfile` |

Each image is pushed to ECR with the short SHA tag and `:latest`. The `update-manifests` job then `sed`-replaces `newTag` in both overlay kustomizations and commits back to `main` (ArgoCD detects the change and syncs). Uses OIDC for AWS authentication — no long-lived credentials stored.

---

## Phase 6: Observability ✅

* Every service exposes `/actuator/prometheus`.
* A `ServiceMonitor` CRD per service tells the Prometheus Operator to scrape the endpoint every 30 s.
* Grafana dashboards visualise request rate, JVM heap, HikariCP pool utilisation, Redis connections.

---

## Phase 7: Scaling & Resilience ✅

* `HorizontalPodAutoscaler` on every service (min 1, max 4–6 replicas, CPU 70 % / memory 80 % targets).
* Pod disruption budgets ensure at least one replica stays available during rolling updates.
* Liveness + readiness probes via `/actuator/health`.
* `DatabaseCreationPostProcessor` makes cold-start safe on any new environment.

---

## Key Design Decisions

| Decision | Rationale |
|---|---|
| `libs/common` shared library | Eliminates copy-paste of JWT + DB-creation code across services; `java-library` exposes API deps transitively |
| IRSA over static credentials | Credential-free S3 access; least-privilege per pod |
| S3 pre-signed URL (client-side upload) | Avoids routing large binary files through the backend; reduces bandwidth cost |
| Redis Pub/Sub (not Kafka) | Sufficient for low-latency fan-out at this scale; avoids operational overhead of a Kafka cluster |
| Flyway + `ddl-auto: none` | Reproducible, auditable schema evolution; prevents accidental table drops |
| Standalone Next.js output | Minimal Docker image; no extra Node server process |
| GitOps (ArgoCD) over `kubectl apply` in CI | Cluster state is always reconcilable from Git; rollback = revert commit |

---

## Future Enhancements

* Blue/Green or Canary deployments (Argo Rollouts)
* Helm charts for reusable packaging
* Service mesh (Istio) for mTLS + advanced traffic management
* Async event sourcing (SQS/SNS) for durable crate-update delivery
* End-to-end test suite (Playwright for frontend, Testcontainers for backend)

