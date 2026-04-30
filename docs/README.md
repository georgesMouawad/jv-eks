# Production-Grade DevOps Platform

A microservices platform on AWS EKS demonstrating end-to-end DevOps practices: infrastructure as code, containerisation, GitOps CI/CD, observability, and autoscaling.

---

## Table of Contents

1. [Architecture](#architecture)
2. [Repository Structure](#repository-structure)
3. [Deployment Flow](#deployment-flow)
4. [Infrastructure](#infrastructure)
5. [Kubernetes](#kubernetes)
6. [CI/CD](#cicd)
7. [Observability](#observability)
8. [Scaling & Resilience](#scaling--resilience)
9. [Secrets Management](#secrets-management)
10. [Tradeoffs & Decisions](#tradeoffs--decisions)
11. [Failure Handling Strategies](#failure-handling-strategies)
12. [Runbooks](#runbooks)

---

## Architecture

Three Spring Boot services behind a Spring Cloud Gateway, deployed on EKS, backed by RDS PostgreSQL.

```
Internet → Traefik NLB → API Gateway (8080)
                             ├── /api/auth/**  → Auth Service  (8081) → RDS auth_db
                             └── /api/users/** → User Service  (8082) → RDS user_db
```

**Diagrams**

| Diagram                                           | Format           | Description                       |
| ------------------------------------------------- | ---------------- | --------------------------------- |
| [architecture.puml](diagrams/architecture.puml)   | PlantUML         | Component + traffic flow overview |
| [infrastructure.d2](diagrams/infrastructure.d2)   | D2 / Terrastruct | AWS resource topology             |
| [deployment-flow.d2](diagrams/deployment-flow.d2) | D2 / Terrastruct | GitOps CI/CD pipeline sequence    |

Render PlantUML: `plantuml docs/diagrams/architecture.puml`  
Render D2: `d2 docs/diagrams/infrastructure.d2 infrastructure.svg`

---

## Repository Structure

```
.
├── .github/workflows/
│   ├── ci.yml              # PR checks — runs tests
│   └── deploy.yml          # main branch — build, push, update tags
├── docs/
│   └── diagrams/           # PlantUML + D2 source diagrams
├── infra/tofu/             # OpenTofu (Terraform-compatible) IaC
│   ├── main.tf             # Helm releases (Traefik, ArgoCD, Sealed Secrets,
│   │                       #   Prometheus stack, Metrics Server)
│   ├── modules/
│   │   ├── vpc/            # VPC, subnets, IGW, NAT
│   │   ├── eks/            # Cluster, node group, access entries
│   │   ├── rds/            # PostgreSQL 16, encrypted gp3
│   │   ├── ecr/            # 3 repos + lifecycle policies
│   │   └── iam/            # OIDC provider + GitHub Actions role
│   └── terraform.tfvars
├── k8s/
│   ├── base/               # Environment-agnostic manifests
│   │   ├── auth-service/   # Deployment, Service, ConfigMap, HPA
│   │   ├── user-service/
│   │   ├── api-gateway/
│   │   └── monitoring/     # ServiceMonitor resources
│   ├── overlays/
│   │   ├── dev/            # Image tags, RDS endpoint, SealedSecrets
│   │   └── prod/
│   ├── argocd/             # Application resources (app-dev.yaml, app-prod.yaml)
│   └── certs/              # Sealed Secrets public certs (safe to commit)
├── scripts/
│   └── bootstrap.sh        # One-time post-`tofu apply` initialisation
└── server/                 # Gradle multi-module Spring Boot project
    └── services/
        ├── auth-service/
        ├── user-service/
        └── api-gateway/
```

---

## Deployment Flow

1. **Developer** pushes a feature branch → PR opened.
2. **`ci.yml`** runs tests on every PR and non-main push.
3. PR merged to **`main`** triggers **`deploy.yml`**.
4. **`build-and-push`** job (matrix: 3 services in parallel):
    - Authenticates to AWS via **GitHub Actions OIDC** (no long-lived keys).
    - Builds a 3-stage Docker image (jlink custom JRE → alpine runtime).
    - Pushes `image:SHORT_SHA` + `image:latest` to **ECR**.
5. **`update-manifests`** job commits `newTag: SHORT_SHA` into both overlay `kustomization.yaml` files with `[skip ci]` to avoid loops.
6. **ArgoCD** detects the commit (polls every 3 min). Dev syncs automatically; prod requires a manual sync in the ArgoCD UI.
7. ArgoCD applies resources in **sync-wave order**:
    - Wave `-1`: SealedSecrets → Sealed Secrets controller decrypts → real Secrets created.
    - Wave `0`: Deployments, Services, Ingress, HPAs applied.
8. Kubernetes performs a **rolling update** — zero downtime.

---

## Infrastructure

All infrastructure is provisioned with **OpenTofu** (`infra/tofu/`). Sensitive variables (`rds_password`, `grafana_admin_password`) are never committed — supplied via `TF_VAR_*` environment variables.

### AWS Resources

| Resource             | Detail                                                                                          |
| -------------------- | ----------------------------------------------------------------------------------------------- |
| **VPC**              | `10.0.0.0/16`, 2 public + 2 private subnets across `eu-central-1a/b`                            |
| **Internet Gateway** | Public subnet egress                                                                            |
| **NAT Gateway**      | Single AZ (cost-optimised) — private subnet egress                                              |
| **EKS**              | K8s 1.33, `t3a.medium` SPOT nodes, 1–2 nodes, `API_AND_CONFIG_MAP` auth mode                    |
| **RDS**              | PostgreSQL 16, `db.t3.micro`, 20 GB gp3, encrypted at rest, private subnet                      |
| **ECR**              | 3 repositories, retain last 3 images, untagged cleaned after 1 day                              |
| **IAM**              | OIDC provider for `token.actions.githubusercontent.com`, scoped to `georgesMouawad/jv-eks` repo |

### Helm Releases (managed by Tofu)

| Release               | Chart                                               | Namespace   | Purpose                             |
| --------------------- | --------------------------------------------------- | ----------- | ----------------------------------- |
| traefik               | `traefik/traefik` 34.4.1                            | kube-system | Ingress + NLB                       |
| sealed-secrets        | `bitnami/sealed-secrets` 2.17.3                     | kube-system | Secret decryption controller        |
| argocd                | `argoproj/argo-cd` 7.8.26                           | argocd      | GitOps                              |
| kube-prometheus-stack | `prometheus-community/kube-prometheus-stack` 84.4.0 | monitoring  | Prometheus + Grafana + Alertmanager |
| metrics-server        | `metrics-server/metrics-server` 3.12.2              | kube-system | HPA CPU/memory metrics source       |

---

## Kubernetes

### Base manifests (`k8s/base/`)

Each service has: `Deployment`, `Service` (ClusterIP), `ConfigMap`, `HPA`.  
API Gateway additionally has an `Ingress` (Traefik).  
`monitoring/` contains `ServiceMonitor` resources for Prometheus scraping.

### Overlays (`k8s/overlays/dev/`)

- **Image overrides**: ECR URIs + `newTag` (updated by CI)
- **ConfigMap patches**: RDS endpoint, datasource URL (set by `bootstrap.sh`)
- **SealedSecrets**: `db-secret`, `jwt-secret` (encrypted, safe to commit)
- **Ingress patch**: host `api.jv-eks.dev`

### Init Containers

Auth-service and user-service use an init container (`postgres:15-alpine`) that:

1. Polls `pg_isready` until RDS is reachable.
2. Creates the database (`auth_db` / `user_db`) if it doesn't exist.

---

## CI/CD

### GitHub Actions Workflows

| Workflow     | Trigger                             | Jobs                                          |
| ------------ | ----------------------------------- | --------------------------------------------- |
| `ci.yml`     | PR, push to non-main                | `test`                                        |
| `deploy.yml` | Push to `main`, `workflow_dispatch` | `build-and-push` (matrix), `update-manifests` |

### OIDC Authentication

No AWS access keys stored in GitHub. The workflow assumes `jv-eks-dev-github-actions-deployer` via OIDC token exchange. The trust policy is scoped to `repo:georgesMouawad/jv-eks:*`.

---

## Observability

### Metrics pipeline

```
Spring Boot Actuator (/actuator/prometheus)
  → ServiceMonitor (scrape config)
    → Prometheus (30s interval)
      → Grafana (PromQL dashboards)
```

### Key metrics available

- `jvm_memory_used_bytes` — heap / non-heap per service
- `process_cpu_usage` — CPU utilisation
- `http_server_requests_seconds_*` — request rate, latency (p50/p99), error rate
- Kubernetes node / pod CPU + memory from `kube-state-metrics` (bundled)

### Accessing Grafana

```bash
kubectl port-forward svc/kube-prometheus-stack-grafana -n monitoring 3000:80
# http://localhost:3000  |  admin / <TF_VAR_grafana_admin_password>
```

Or via ingress: `http://grafana.jv-eks.dev` (add Traefik LB IP to hosts file).

---

## Scaling & Resilience

### Horizontal Pod Autoscaler

| Service      | Min | Max | Scale-up trigger          |
| ------------ | --- | --- | ------------------------- |
| auth-service | 1   | 4   | CPU > 70% OR memory > 80% |
| user-service | 1   | 4   | CPU > 70% OR memory > 80% |
| api-gateway  | 1   | 3   | CPU > 70% OR memory > 80% |

Scale-down stabilisation window: 120 s (prevents thrashing).

### Resource limits

| Service      | CPU request | CPU limit | Memory request | Memory limit |
| ------------ | ----------- | --------- | -------------- | ------------ |
| auth-service | 128m        | 500m      | 256Mi          | 512Mi        |
| user-service | 128m        | 500m      | 256Mi          | 512Mi        |
| api-gateway  | 64m         | 250m      | 128Mi          | 256Mi        |

### Health probes

All services expose `/actuator/health/liveness` and `/actuator/health/readiness` used by Kubernetes liveness and readiness probes.

---

## Secrets Management

Secrets are managed with **Bitnami Sealed Secrets** — never stored in plaintext, safe to commit.

### Lifecycle

1. **One-time**: `bootstrap.sh` fetches the controller's public cert → commits to `k8s/certs/sealed-secrets-dev.pem`.
2. **Sealing**: `kubectl create secret ... --dry-run | kubeseal --cert k8s/certs/sealed-secrets-dev.pem` → commit `SealedSecret` YAML.
3. **In cluster**: Controller decrypts using its private key (never leaves the cluster) → creates a standard `Secret`.
4. **Key loss** (after `tofu destroy`): Re-run `bootstrap.sh` — fetches new cert, re-seals, overwrites old YAMLs.

### Sync ordering

SealedSecrets carry `argocd.argoproj.io/sync-wave: "-1"` — ArgoCD applies and waits for them to be healthy before applying Deployments (wave `0`). This guarantees Secrets exist before pods start.

---

## Tradeoffs & Decisions

| Decision          | Chosen                | Alternative               | Reason                                                              |
| ----------------- | --------------------- | ------------------------- | ------------------------------------------------------------------- |
| IaC tool          | OpenTofu              | Terraform                 | Open-source, drop-in compatible, no licensing concern               |
| Ingress           | Traefik               | NGINX / ALB Ingress       | Simpler config, native Helm chart, good Kubernetes integration      |
| GitOps            | ArgoCD                | Flux                      | Richer UI, sync-wave support, wide adoption                         |
| Secrets           | Sealed Secrets        | External Secrets Operator | No external secret store dependency; ciphertext lives in Git        |
| CI authentication | OIDC                  | IAM access keys           | No long-lived credentials; keyless by default                       |
| NAT Gateway       | Single AZ             | Multi-AZ                  | Cost optimisation for dev; production should use per-AZ NAT         |
| RDS               | Single-AZ, no replica | Multi-AZ RDS              | Cost; `multi_az = true` is a 1-line change for prod                 |
| Node capacity     | SPOT `t3a.medium`     | On-demand                 | ~70% cost saving; acceptable interruption risk for dev              |
| Image build       | 3-stage jlink         | Fat JAR                   | ~60 MB runtime image vs ~250 MB; custom JRE excludes unused modules |
| Metrics           | Actuator + Prometheus | CloudWatch                | Vendor-neutral; rich JVM + HTTP metrics out of the box              |

---

## Failure Handling Strategies

### Pod crashes → auto-recovery

Kubernetes restarts crashed containers automatically via the default `restartPolicy: Always`. Liveness probes detect hung processes and trigger restarts before manual intervention is needed.

**Verify:**

```bash
kubectl delete pod <pod-name> -n jv-eks
kubectl get pods -n jv-eks -w  # replacement starts immediately
```

### Failed deployments → rollback

Rolling update strategy ensures at least one healthy replica stays up. If new pods fail readiness probes, the rollout stalls and the old replica set remains active.

```bash
# Manual rollback to previous revision
kubectl rollout undo deployment/auth-service -n jv-eks
kubectl rollout status deployment/auth-service -n jv-eks
```

In GitOps model: revert the `newTag` commit in Git — ArgoCD will sync back to the previous image.

### Database outage → degraded behavior

If RDS becomes unreachable:

- Spring Boot's `/actuator/health/readiness` returns `DOWN` (datasource health check fails).
- Kubernetes marks the pod as not-ready → Traefik stops routing to it.
- Traffic is dropped cleanly rather than hitting a pod that returns 500s.
- Once RDS recovers, the readiness probe passes and the pod re-enters rotation automatically.

### Sealed Secrets controller restart

SealedSecrets are decrypted once into standard Secrets. If the controller restarts, existing Secrets remain intact — the controller only acts on create/update of `SealedSecret` resources.

### Node failure / SPOT interruption

The node group has `min = 1`, `max = 2`. On SPOT interruption, EKS replaces the node. With HPA `minReplicas = 1`, a pod will be rescheduled onto the replacement node. For zero-downtime, set `minReplicas = 2` in production.

---

## Runbooks

### Provision from scratch

```bash
# 1. Provision AWS infrastructure
cd infra/tofu
export TF_VAR_rds_password="..."
export TF_VAR_grafana_admin_password="..."
tofu init && tofu apply

# 2. One-time cluster bootstrap
cd ../..
./scripts/bootstrap.sh
# → updates kubeconfig, patches overlays, seals secrets, bootstraps ArgoCD

# 3. Trigger first deploy
git commit --allow-empty -m "chore: initial deploy" && git push
```

### Rotate a secret

```bash
kubectl create secret generic db-secret --namespace jv-eks \
  --from-literal=SPRING_DATASOURCE_PASSWORD=newpassword \
  --dry-run=client -o yaml | \
kubeseal --cert k8s/certs/sealed-secrets-dev.pem --format yaml \
  > k8s/overlays/dev/sealed-secrets/db-secret.yaml
git commit -am "chore: rotate db-secret" && git push
```

### Tear down

```bash
cd infra/tofu
tofu plan -destroy -out destroy.tfplan
tofu apply destroy.tfplan

# Clean up local kubeconfig
kubectl config delete-context <eks-context>
```
