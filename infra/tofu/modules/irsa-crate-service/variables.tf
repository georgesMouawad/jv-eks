variable "name_prefix" {
  description = "Prefix for resource names."
  type        = string
}

variable "oidc_issuer_url" {
  description = "OIDC issuer URL of the EKS cluster (e.g. https://oidc.eks.eu-central-1.amazonaws.com/id/...)."
  type        = string
}

variable "audio_assets_bucket_arn" {
  description = "ARN of the S3 audio-assets bucket."
  type        = string
}

variable "k8s_namespace" {
  description = "Kubernetes namespace where crate-service runs."
  type        = string
  default     = "jv-eks"
}

variable "k8s_service_account_name" {
  description = "Name of the Kubernetes ServiceAccount used by crate-service."
  type        = string
  default     = "crate-service"
}
