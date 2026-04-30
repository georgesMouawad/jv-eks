# ── S3 bucket for CrateSync audio assets ─────────────────────────────────────

resource "aws_s3_bucket" "audio_assets" {
  bucket = var.bucket_name

  tags = { Name = var.bucket_name }
}

# Block all public access — objects are accessed only via pre-signed URLs
resource "aws_s3_bucket_public_access_block" "audio_assets" {
  bucket = aws_s3_bucket.audio_assets.id

  block_public_acls       = true
  block_public_policy     = true
  ignore_public_acls      = true
  restrict_public_buckets = true
}

# Encryption at rest
resource "aws_s3_bucket_server_side_encryption_configuration" "audio_assets" {
  bucket = aws_s3_bucket.audio_assets.id

  rule {
    apply_server_side_encryption_by_default {
      sse_algorithm = "AES256"
    }
  }
}

# CORS — allows the Next.js frontend to PUT directly to S3 via pre-signed URLs
resource "aws_s3_bucket_cors_configuration" "audio_assets" {
  bucket = aws_s3_bucket.audio_assets.id

  cors_rule {
    allowed_headers = ["*"]
    allowed_methods = ["PUT", "GET"]
    allowed_origins = var.cors_allowed_origins
    expose_headers  = ["ETag"]
    max_age_seconds = 3600
  }
}

# Lifecycle rule — abort incomplete multipart uploads after 1 day
resource "aws_s3_bucket_lifecycle_configuration" "audio_assets" {
  bucket = aws_s3_bucket.audio_assets.id

  rule {
    id     = "abort-incomplete-multipart"
    status = "Enabled"

    abort_incomplete_multipart_upload {
      days_after_initiation = 1
    }
  }
}
