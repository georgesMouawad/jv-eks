#!/usr/bin/env bash
# bootstrap.sh — run once after `tofu apply` to initialise the cluster.
# Usage: ./scripts/bootstrap.sh
#
# Prerequisites: tofu, kubectl, kubeseal, aws CLI, git — all authenticated.

set -euo pipefail

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
ENV="dev"
OVERLAY="${REPO_ROOT}/k8s/overlays/${ENV}"
CERTS_DIR="${REPO_ROOT}/k8s/certs"

require_cmd() {
  if ! command -v "$1" >/dev/null 2>&1; then
    echo "Error: required command '$1' is not installed or not in PATH." >&2
    exit 1
  fi
}

echo "==> Checking prerequisites..."
require_cmd tofu
require_cmd aws
require_cmd kubectl
require_cmd kubeseal
require_cmd git

# ── 1. Resolve Tofu outputs ───────────────────────────────────────────────────
echo "==> Reading Tofu outputs..."
cd "${REPO_ROOT}/infra/tofu"
ACCOUNT_ID=$(tofu output -raw github_actions_role_arn | cut -d: -f5)
RDS_ENDPOINT=$(tofu output -raw rds_endpoint | sed 's/:5432//')
CLUSTER_NAME=$(tofu output -raw eks_cluster_name)
AWS_REGION=$(tofu output -raw aws_region)

echo "    ACCOUNT_ID   = ${ACCOUNT_ID}"
echo "    RDS_ENDPOINT = ${RDS_ENDPOINT}"
echo "    CLUSTER_NAME = ${CLUSTER_NAME}"

# ── 1b. Configure local kubeconfig ───────────────────────────────────────────
echo "==> Updating kubeconfig..."
aws eks update-kubeconfig --name "${CLUSTER_NAME}" --region "${AWS_REGION}"

# ── 2. Patch permanent (non-sensitive) values in the overlay ──────────────────
echo "==> Patching overlay with permanent values..."
sed -i "s|<ACCOUNT_ID>|${ACCOUNT_ID}|g" \
  "${OVERLAY}/kustomization.yaml"

sed -i "s|<RDS_ENDPOINT>|${RDS_ENDPOINT}|g" \
  "${OVERLAY}/patches/auth-service-configmap.yaml" \
  "${OVERLAY}/patches/user-service-configmap.yaml"

# ── 3. Fetch and commit the Sealed Secrets public cert ───────────────────────
echo "==> Fetching Sealed Secrets public cert..."
kubeseal \
  --controller-namespace kube-system \
  --controller-name sealed-secrets \
  --fetch-cert > "${CERTS_DIR}/sealed-secrets-${ENV}.pem"

echo "    Cert written to k8s/certs/sealed-secrets-${ENV}.pem"

# ── 4. Seal secrets ───────────────────────────────────────────────────────────
echo ""
echo "==> Sealing secrets (you will be prompted for values)..."

read -rsp "Enter SPRING_DATASOURCE_PASSWORD: " DB_PASSWORD
echo ""
kubectl create secret generic db-secret \
  --namespace jv-eks \
  --from-literal=SPRING_DATASOURCE_PASSWORD="${DB_PASSWORD}" \
  --dry-run=client -o yaml | \
kubeseal --cert "${CERTS_DIR}/sealed-secrets-${ENV}.pem" --format yaml \
  > "${OVERLAY}/sealed-secrets/db-secret.yaml"
unset DB_PASSWORD
echo "    db-secret sealed."

read -rsp "Enter JWT_SECRET (base64): " JWT_SECRET
echo ""
kubectl create secret generic jwt-secret \
  --namespace jv-eks \
  --from-literal=JWT_SECRET="${JWT_SECRET}" \
  --dry-run=client -o yaml | \
kubeseal --cert "${CERTS_DIR}/sealed-secrets-${ENV}.pem" --format yaml \
  > "${OVERLAY}/sealed-secrets/jwt-secret.yaml"
unset JWT_SECRET
echo "    jwt-secret sealed."

# ── 5. Commit everything ──────────────────────────────────────────────────────
echo ""
echo "==> Committing to git..."
cd "${REPO_ROOT}"
git add \
  k8s/overlays/dev/kustomization.yaml \
  k8s/overlays/dev/patches/auth-service-configmap.yaml \
  k8s/overlays/dev/patches/user-service-configmap.yaml \
  k8s/overlays/dev/sealed-secrets/db-secret.yaml \
  k8s/overlays/dev/sealed-secrets/jwt-secret.yaml \
  k8s/certs/sealed-secrets-dev.pem

if git diff --cached --quiet; then
  echo "    No file changes to commit."
else
  git commit -m "chore: bootstrap dev environment [skip ci]"
  git push
fi

# ── 6. Bootstrap ArgoCD ───────────────────────────────────────────────────────
echo ""
echo "==> Bootstrapping ArgoCD..."
kubectl apply -f "${REPO_ROOT}/k8s/argocd/"

echo ""
echo "==> Done!"
echo "    ArgoCD is now watching the repo. Dev syncs automatically."
echo "    Get the ArgoCD admin password:"
echo "    kubectl get secret argocd-initial-admin-secret -n argocd \\"
echo "      -o jsonpath='{.data.password}' | base64 -d"
