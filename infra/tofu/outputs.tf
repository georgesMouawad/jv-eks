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

output "aws_region" {
  description = "AWS region where resources are deployed."
  value       = var.aws_region
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

# ── CrateSync S3 ─────────────────────────────────────────────────────────────
output "cratesync_audio_assets_bucket" {
  description = "Name of the S3 bucket for CrateSync audio assets."
  value       = module.s3.bucket_name
}

output "cratesync_audio_assets_bucket_arn" {
  description = "ARN of the S3 bucket for CrateSync audio assets."
  value       = module.s3.bucket_arn
}

# ── ElastiCache ───────────────────────────────────────────────────────────────
output "redis_endpoint" {
  description = "DNS address of the ElastiCache Redis primary node."
  value       = module.elasticache.redis_endpoint
}

output "redis_port" {
  description = "Port of the ElastiCache Redis cluster."
  value       = module.elasticache.redis_port
}

# ── IRSA ─────────────────────────────────────────────────────────────────────
output "crate_service_irsa_role_arn" {
  description = "IAM role ARN to annotate on the crate-service Kubernetes ServiceAccount."
  value       = module.irsa_crate_service.role_arn
}

output "aws_account_id" {
  description = "AWS account ID."
  value       = data.aws_caller_identity.current.account_id
}
