locals {
  name_prefix = "${var.project_name}-${var.environment}"
}

# ── VPC ──────────────────────────────────────────────────────────────────────
module "vpc" {
  source = "./modules/vpc"

  name_prefix          = local.name_prefix
  vpc_cidr             = var.vpc_cidr
  public_subnet_cidrs  = var.public_subnet_cidrs
  private_subnet_cidrs = var.private_subnet_cidrs
  availability_zones   = var.availability_zones
}

# ── IAM (GitHub Actions OIDC + deployer role) ────────────────────────────────
module "iam" {
  source = "./modules/iam"

  name_prefix = local.name_prefix
  github_org  = var.github_org
  github_repo = var.github_repo
}

# ── EKS ──────────────────────────────────────────────────────────────────────
module "eks" {
  source = "./modules/eks"

  name_prefix             = local.name_prefix
  kubernetes_version      = var.eks_kubernetes_version
  private_subnet_ids      = module.vpc.private_subnet_ids
  node_instance_types     = var.eks_node_instance_types
  node_desired            = var.eks_node_desired
  node_min                = var.eks_node_min
  node_max                = var.eks_node_max
  github_actions_role_arn = module.iam.github_actions_role_arn
}

# ── RDS ──────────────────────────────────────────────────────────────────────
module "rds" {
  source = "./modules/rds"

  name_prefix        = local.name_prefix
  vpc_id             = module.vpc.vpc_id
  private_subnet_ids = module.vpc.private_subnet_ids
  eks_node_sg_id     = module.eks.node_security_group_id
  instance_class     = var.rds_instance_class
  allocated_storage  = var.rds_allocated_storage
  db_name            = var.rds_db_name
  username           = var.rds_username
  password           = var.rds_password
}

# ── ECR ──────────────────────────────────────────────────────────────────────
module "ecr" {
  source = "./modules/ecr"

  name_prefix           = local.name_prefix
  service_names         = ["auth-service", "user-service", "api-gateway"]
  image_retention_count = var.ecr_image_retention_count
}
