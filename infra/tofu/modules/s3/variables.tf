variable "bucket_name" {
  description = "Name of the S3 bucket for audio assets."
  type        = string
}

variable "cors_allowed_origins" {
  description = "List of origins allowed to PUT/GET via pre-signed URLs."
  type        = list(string)
}
