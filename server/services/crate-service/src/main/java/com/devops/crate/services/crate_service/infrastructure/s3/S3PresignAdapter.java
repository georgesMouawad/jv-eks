package com.devops.crate.services.crate_service.infrastructure.s3;

import com.devops.crate.services.crate_service.application.port.out.PresignPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;
import java.util.UUID;

@Component
public class S3PresignAdapter implements PresignPort {

    private final S3Presigner presigner;
    private final String bucketName;

    public S3PresignAdapter(@Value("${aws.s3.bucket-name}") String bucketName,
            @Value("${aws.region}") String region) {
        this.bucketName = bucketName;
        // DefaultCredentialsProvider picks up IRSA credentials automatically when
        // running on EKS (AWS_WEB_IDENTITY_TOKEN_FILE + AWS_ROLE_ARN env vars).
        this.presigner = S3Presigner.builder()
                .region(Region.of(region))
                .build();
    }

    @Override
    public String generatePresignedPutUrl(String s3Key, long expiresInSeconds) {
        PutObjectRequest putRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(s3Key)
                .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofSeconds(expiresInSeconds))
                .putObjectRequest(putRequest)
                .build();

        PresignedPutObjectRequest presigned = presigner.presignPutObject(presignRequest);
        return presigned.url().toString();
    }

    @Override
    public String generateS3Key(UUID crateId, String trackName) {
        // Sanitize the track name so it is safe as an S3 key segment
        String sanitized = trackName.replaceAll("[^a-zA-Z0-9._-]", "_");
        return "crates/" + crateId + "/" + UUID.randomUUID() + "_" + sanitized;
    }
}
