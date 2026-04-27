variable "name_prefix" {
  type = string
}

variable "service_names" {
  type        = list(string)
  description = "List of service names — one ECR repository is created per entry."
}

variable "image_retention_count" {
  type        = number
  description = "Number of tagged images to keep per repository."
}
