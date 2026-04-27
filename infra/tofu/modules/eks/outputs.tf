output "cluster_name" {
  value = aws_eks_cluster.this.name
}

output "cluster_endpoint" {
  value = aws_eks_cluster.this.endpoint
}

output "cluster_ca_certificate" {
  value     = aws_eks_cluster.this.certificate_authority[0].data
  sensitive = true
}

output "node_security_group_id" {
  description = "The security group automatically created for the node group."
  value       = aws_eks_cluster.this.vpc_config[0].cluster_security_group_id
}
