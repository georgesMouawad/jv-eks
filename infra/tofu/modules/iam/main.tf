# ── GitHub Actions OIDC provider ─────────────────────────────────────────────
# There can only be one OIDC provider per URL per account.
# If one already exists, import it before running `tofu apply`:
#   tofu import module.iam.aws_iam_openid_connect_provider.github_actions \
#     arn:aws:iam::<ACCOUNT_ID>:oidc-provider/token.actions.githubusercontent.com
resource "aws_iam_openid_connect_provider" "github_actions" {
  url             = "https://token.actions.githubusercontent.com"
  client_id_list  = ["sts.amazonaws.com"]
  # AWS rotates its own thumbprint for actions.githubusercontent.com; supplying
  # the well-known value satisfies the non-empty requirement.
  thumbprint_list = ["6938fd4d98bab03faadb97b34396831e3780aea1"]

  tags = { Name = "${var.name_prefix}-github-actions-oidc" }
}

# ── Trust policy ──────────────────────────────────────────────────────────────
# Scoped to the specific repo; wildcard on ref allows all branches/tags/envs.
data "aws_iam_policy_document" "github_actions_assume_role" {
  statement {
    effect  = "Allow"
    actions = ["sts:AssumeRoleWithWebIdentity"]

    principals {
      type        = "Federated"
      identifiers = [aws_iam_openid_connect_provider.github_actions.arn]
    }

    condition {
      test     = "StringEquals"
      variable = "token.actions.githubusercontent.com:aud"
      values   = ["sts.amazonaws.com"]
    }

    condition {
      test     = "StringLike"
      variable = "token.actions.githubusercontent.com:sub"
      values   = ["repo:${var.github_org}/${var.github_repo}:*"]
    }
  }
}

# ── IAM role ──────────────────────────────────────────────────────────────────
resource "aws_iam_role" "github_actions" {
  name               = "${var.name_prefix}-github-actions-deployer"
  assume_role_policy = data.aws_iam_policy_document.github_actions_assume_role.json

  tags = { Name = "${var.name_prefix}-github-actions-deployer" }
}

# ECR: push images built in CI
resource "aws_iam_role_policy_attachment" "github_actions_ecr" {
  role       = aws_iam_role.github_actions.name
  policy_arn = "arn:aws:iam::aws:policy/AmazonEC2ContainerRegistryPowerUser"
}
