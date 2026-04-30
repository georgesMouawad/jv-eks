# ── IRSA for crate-service ────────────────────────────────────────────────────
# IRSA (IAM Roles for Service Accounts) lets a Kubernetes pod assume an AWS IAM
# role without any static credentials. The mechanism is identical to the GitHub
# Actions OIDC setup in modules/iam — the only difference is who issues the
# token: GitHub's servers for CI, and the EKS cluster itself for pods.
#
# Full runtime flow (happens automatically once the resources below exist):
#
#   1. Kubernetes mounts a short-lived OIDC JWT into the pod at:
#        /var/run/secrets/eks.amazonaws.com/serviceaccount/token
#      EKS rotates this token every ~24 h with no action required.
#
#   2. The AWS SDK inside the pod detects the token via the
#      AWS_WEB_IDENTITY_TOKEN_FILE env var (injected by EKS automatically when
#      the ServiceAccount carries the role-arn annotation).
#
#   3. The SDK calls sts:AssumeRoleWithWebIdentity, presenting the token.
#
#   4. AWS STS validates the token's signature against the OIDC provider
#      registered below (step "OIDC provider"), then evaluates the two
#      conditions in the trust policy (step "Trust policy"):
#        - sub  must equal "system:serviceaccount:<namespace>:<sa-name>"
#          → ensures only THIS specific ServiceAccount can assume the role,
#            not any other pod in the cluster.
#        - aud  must equal "sts.amazonaws.com"
#          → rejects tokens issued for other audiences (e.g. the k8s API).
#
#   5. On success STS returns temporary credentials (15 min TTL by default).
#      The SDK caches and auto-refreshes them — the pod never sees long-lived keys.
#
# The three Tofu resources that make this possible:
#   aws_iam_openid_connect_provider  → tells AWS to trust tokens from this cluster
#   aws_iam_role (+ trust policy)    → the role the pod will assume, locked to
#                                      one specific ServiceAccount
#   aws_iam_policy + attachment      → what the role is actually allowed to do

# Derive the OIDC provider host (strip the https:// prefix).
# IAM condition keys use the bare host, e.g.:
#   oidc.eks.eu-central-1.amazonaws.com/id/EXAMPLE:sub
locals {
  oidc_host = trimprefix(var.oidc_issuer_url, "https://")
}

# ── Step 1: Register the cluster as a trusted OIDC provider with AWS IAM ──────
# This is the same concept as registering token.actions.githubusercontent.com
# for GitHub Actions — it tells AWS STS: "tokens signed by this issuer are
# trustworthy; use their claims to evaluate role trust policies."
# The TLS thumbprint is fetched dynamically so it stays correct if AWS rotates
# the certificate on the OIDC endpoint.
data "tls_certificate" "eks" {
  url = var.oidc_issuer_url
}

resource "aws_iam_openid_connect_provider" "eks" {
  url             = var.oidc_issuer_url
  client_id_list  = ["sts.amazonaws.com"] # the only audience we ever want to accept
  thumbprint_list = [data.tls_certificate.eks.certificates[0].sha1_fingerprint]
}

# ── Step 2: Define what the role is allowed to do (the permission boundary) ───
# Least-privilege: only PutObject / GetObject on objects inside the bucket.
# The bucket itself cannot be listed, deleted, or have its policy changed.
data "aws_iam_policy_document" "crate_service_s3" {
  statement {
    sid    = "CrateServiceAudioAssets"
    effect = "Allow"
    actions = [
      "s3:PutObject",
      "s3:GetObject",
    ]
    resources = ["${var.audio_assets_bucket_arn}/*"] # objects only, not the bucket ARN itself
  }
}

resource "aws_iam_policy" "crate_service_s3" {
  name        = "${var.name_prefix}-crate-service-s3-policy"
  description = "Allows crate-service to read/write audio assets in S3."
  policy      = data.aws_iam_policy_document.crate_service_s3.json
}

# ── Step 3: Define who can assume the role (the trust policy) ─────────────────
# The principal is the OIDC provider registered above (Federated = OIDC, not
# an AWS service or IAM user).  The two StringEquals conditions are the
# critical security gate:
#   :sub  pins the role to exactly one Kubernetes ServiceAccount.
#   :aud  ensures the token was issued for STS, not for the k8s API server.
# Without these conditions ANY pod in the cluster could assume this role.
data "aws_iam_policy_document" "crate_service_assume_role" {
  statement {
    effect  = "Allow"
    actions = ["sts:AssumeRoleWithWebIdentity"]

    principals {
      type        = "Federated"
      identifiers = [aws_iam_openid_connect_provider.eks.arn]
    }

    # Only the crate-service ServiceAccount in the jv-eks namespace may assume this role.
    condition {
      test     = "StringEquals"
      variable = "${local.oidc_host}:sub"
      values   = ["system:serviceaccount:${var.k8s_namespace}:${var.k8s_service_account_name}"]
    }

    # Reject tokens issued for any audience other than STS.
    condition {
      test     = "StringEquals"
      variable = "${local.oidc_host}:aud"
      values   = ["sts.amazonaws.com"]
    }
  }
}

# ── Step 4: Create the role and attach the permission policy ──────────────────
# After `tofu apply`, annotate the Kubernetes ServiceAccount with:
#   eks.amazonaws.com/role-arn: <output.role_arn>
# EKS will then automatically inject AWS_WEB_IDENTITY_TOKEN_FILE and
# AWS_ROLE_ARN env vars into every pod that uses that ServiceAccount,
# triggering the SDK to perform the token exchange described above.
resource "aws_iam_role" "crate_service" {
  name               = "${var.name_prefix}-crate-service-irsa-role"
  assume_role_policy = data.aws_iam_policy_document.crate_service_assume_role.json
}

resource "aws_iam_role_policy_attachment" "crate_service_s3" {
  role       = aws_iam_role.crate_service.name
  policy_arn = aws_iam_policy.crate_service_s3.arn
}
