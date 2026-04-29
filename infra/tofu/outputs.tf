# ── VPC ──────────────────────────────────────────────────────────────────────
output "vpc_id" {
  description = "ID of the created VPC."
  value       = module.vpc.vpc_id
}

output "public_subnet_ids" {
  description = "IDs of the public subnets."
  value       = module.vpc.public_subnet_ids
}

output "private_subnet_ids" {
  description = "IDs of the private subnets."
  value       = module.vpc.private_subnet_ids
}

# ── EKS ──────────────────────────────────────────────────────────────────────
output "eks_cluster_name" {
  description = "Name of the EKS cluster."
  value       = module.eks.cluster_name
}

output "eks_cluster_endpoint" {
  description = "API server endpoint of the EKS cluster."
  value       = module.eks.cluster_endpoint
}

output "eks_kubeconfig_command" {
  description = "AWS CLI command to update local kubeconfig."
  value       = "aws eks update-kubeconfig --name ${module.eks.cluster_name} --region ${var.aws_region}"
}

# ── RDS ──────────────────────────────────────────────────────────────────────
output "rds_endpoint" {
  description = "Connection endpoint for the RDS instance."
  value       = module.rds.endpoint
}

output "rds_db_name" {
  description = "Database name on the RDS instance."
  value       = module.rds.db_name
}

# ── ECR ──────────────────────────────────────────────────────────────────────
output "ecr_repository_urls" {
  description = "Map of service name to ECR repository URL."
  value       = module.ecr.repository_urls
}

# ── IAM ──────────────────────────────────────────────────────────────────────
output "github_actions_role_arn" {
  description = "ARN of the GitHub Actions deployer role. Set this as the AWS_ROLE_ARN secret in GitHub."
  value       = module.iam.github_actions_role_arn
}
