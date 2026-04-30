# ── Global ───────────────────────────────────────────────────────────────────
variable "aws_region" {
  description = "AWS region to deploy into."
  type        = string
  default     = "eu-central-1"
}

variable "project_name" {
  description = "Short name used to prefix all resources."
  type        = string
  default     = "jv-eks"
}

variable "environment" {
  description = "Deployment environment (dev | staging | prod)."
  type        = string
  default     = "dev"
}

# ── VPC ──────────────────────────────────────────────────────────────────────
variable "vpc_cidr" {
  description = "CIDR block for the VPC."
  type        = string
  default     = "10.0.0.0/16"
}

variable "public_subnet_cidrs" {
  description = "CIDR blocks for public subnets (one per AZ)."
  type        = list(string)
  default     = ["10.0.1.0/24", "10.0.2.0/24"]
}

variable "private_subnet_cidrs" {
  description = "CIDR blocks for private subnets (one per AZ)."
  type        = list(string)
  default     = ["10.0.11.0/24", "10.0.12.0/24"]
}

variable "availability_zones" {
  description = "Availability zones to use."
  type        = list(string)
  default     = ["eu-central-1a", "eu-central-1b"]
}

# ── EKS ──────────────────────────────────────────────────────────────────────
variable "eks_kubernetes_version" {
  description = "Kubernetes version for the EKS control plane."
  type        = string
  default     = "1.33"
}

variable "eks_node_instance_types" {
  description = "EC2 instance types for EKS managed node group."
  type        = list(string)
  default     = ["t3.medium"]
}

variable "eks_node_desired" {
  description = "Desired number of worker nodes."
  type        = number
  default     = 1
}

variable "eks_node_min" {
  description = "Minimum number of worker nodes."
  type        = number
  default     = 1
}

variable "eks_node_max" {
  description = "Maximum number of worker nodes."
  type        = number
  default     = 1
}

variable "github_org" {
  description = "GitHub organisation or username that owns the repository."
  type        = string
}

variable "github_repo" {
  description = "GitHub repository name (without the org prefix)."
  type        = string
}

# ── RDS ──────────────────────────────────────────────────────────────────────
variable "rds_instance_class" {
  description = "RDS instance class."
  type        = string
  default     = "db.t3.micro"
}

variable "rds_allocated_storage" {
  description = "Allocated storage for RDS in GB."
  type        = number
  default     = 20
}

variable "rds_db_name" {
  description = "Initial database name created by RDS."
  type        = string
  default     = "appdb"
}

variable "rds_username" {
  description = "Master username for the RDS instance."
  type        = string
  default     = "postgres"
}

variable "rds_password" {
  description = "Master password for the RDS instance."
  type        = string
  sensitive   = true
}

# ── Grafana ───────────────────────────────────────────────────────────────────
variable "grafana_admin_password" {
  description = "Admin password for the Grafana dashboard."
  type        = string
  sensitive   = true
}

# ── ECR ──────────────────────────────────────────────────────────────────────
variable "ecr_image_retention_count" {
  description = "Number of images to keep per ECR repository."
  type        = number
  default     = 10
}

# ── CrateSync S3 ─────────────────────────────────────────────────────────────
variable "cratesync_cors_allowed_origins" {
  description = "Origins allowed to PUT/GET audio assets via pre-signed URLs (e.g. Next.js frontend URL)."
  type        = list(string)
  default     = ["http://localhost:3000"]
}

# ── ElastiCache ───────────────────────────────────────────────────────────────
variable "elasticache_node_type" {
  description = "ElastiCache node type for the Redis cluster."
  type        = string
  default     = "cache.t3.micro"
}
