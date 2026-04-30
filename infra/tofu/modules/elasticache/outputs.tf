output "redis_endpoint" {
  description = "DNS address of the Redis primary node."
  value       = aws_elasticache_cluster.redis.cache_nodes[0].address
}

output "redis_port" {
  description = "Port of the Redis cluster."
  value       = aws_elasticache_cluster.redis.cache_nodes[0].port
}
