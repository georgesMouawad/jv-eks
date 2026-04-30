output "role_arn" {
  description = "ARN of the IAM role to annotate on the crate-service Kubernetes ServiceAccount."
  value       = aws_iam_role.crate_service.arn
}

output "oidc_provider_arn" {
  description = "ARN of the EKS OIDC provider."
  value       = aws_iam_openid_connect_provider.eks.arn
}
