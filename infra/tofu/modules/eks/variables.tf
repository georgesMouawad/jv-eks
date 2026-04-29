variable "name_prefix" {
  type = string
}

variable "kubernetes_version" {
  type = string
}

variable "private_subnet_ids" {
  type = list(string)
}

variable "node_instance_types" {
  type = list(string)
}

variable "node_desired" {
  type = number
}

variable "node_min" {
  type = number
}

variable "node_max" {
  type = number
}

variable "github_actions_role_arn" {
  description = "ARN of the IAM role assumed by GitHub Actions for EKS deployments."
  type        = string
}
