locals {
  name_prefix = "${var.project_name}-${var.environment}"
}

data "aws_caller_identity" "current" {}

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

# ── Traefik ingress controller ────────────────────────────────────────────────
# Installed into the EKS cluster once via Helm. The helm provider authenticates
# using the same kubeconfig that the aws provider resolves via eks:DescribeCluster.
provider "helm" {
  kubernetes {
    host                   = module.eks.cluster_endpoint
    cluster_ca_certificate = base64decode(module.eks.cluster_ca_certificate)
    exec {
      api_version = "client.authentication.k8s.io/v1beta1"
      command     = "aws"
      args        = ["eks", "get-token", "--cluster-name", module.eks.cluster_name, "--region", var.aws_region]
    }
  }
}

resource "helm_release" "traefik" {
  name             = "traefik"
  repository       = "https://traefik.github.io/charts"
  chart            = "traefik"
  version          = "34.4.1"
  namespace        = "kube-system"
  create_namespace = false

  set {
    name  = "ingressClass.enabled"
    value = "true"
  }
  set {
    name  = "ingressClass.isDefaultClass"
    value = "true"
  }
  set {
    name  = "service.type"
    value = "LoadBalancer"
  }

  depends_on = [module.eks]
}

# ── Sealed Secrets controller ─────────────────────────────────────────────────
# Decrypts SealedSecret resources committed to the repo using the controller's
# private key, which never leaves the cluster.
resource "helm_release" "sealed_secrets" {
  name             = "sealed-secrets"
  repository       = "https://bitnami-labs.github.io/sealed-secrets"
  chart            = "sealed-secrets"
  version          = "2.17.3"
  namespace        = "kube-system"
  create_namespace = false

  depends_on = [module.eks]
}

# ── ArgoCD ────────────────────────────────────────────────────────────────────
# GitOps controller. After `tofu apply`, bootstrap with:
#   kubectl apply -f k8s/argocd/
# Get the initial admin password:
#   kubectl get secret argocd-initial-admin-secret -n argocd \
#     -o jsonpath='{.data.password}' | base64 -d
resource "helm_release" "argocd" {
  name             = "argocd"
  repository       = "https://argoproj.github.io/argo-helm"
  chart            = "argo-cd"
  version          = "7.8.26"
  namespace        = "argocd"
  create_namespace = true

  depends_on = [module.eks]
}
