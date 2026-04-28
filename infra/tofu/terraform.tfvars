# ── Project defaults ─────────────────────────────────────────────────────────
aws_region   = "eu-central-1"
project_name = "jv-eks"
environment  = "dev"

# ── VPC ──────────────────────────────────────────────────────────────────────
vpc_cidr             = "10.0.0.0/16"
public_subnet_cidrs  = ["10.0.1.0/24", "10.0.2.0/24"]
private_subnet_cidrs = ["10.0.11.0/24", "10.0.12.0/24"]
availability_zones   = ["eu-central-1a", "eu-central-1b"]

# ── EKS ──────────────────────────────────────────────────────────────────────
eks_kubernetes_version  = "1.33"
eks_node_instance_types = ["t3a.medium"] # multiple types improves spot availability
eks_node_desired        = 1
eks_node_min            = 1
eks_node_max            = 1

# ── RDS ──────────────────────────────────────────────────────────────────────
rds_instance_class    = "db.t3.micro"
rds_allocated_storage = 20
rds_db_name           = "appdb"
rds_username          = "postgres"
# rds_password must be supplied via TF_VAR_rds_password env var or -var flag — never commit it.

# ── ECR ──────────────────────────────────────────────────────────────────────
ecr_image_retention_count = 3
