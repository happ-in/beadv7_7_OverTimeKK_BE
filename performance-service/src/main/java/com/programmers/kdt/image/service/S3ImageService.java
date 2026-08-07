package com.programmers.kdt.image.service;

import com.programmers.kdt.image.dto.ImgUploadUrlResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3ImageService {

    private static final Duration UPLOAD_URL_EXPIRED = Duration.ofMinutes(5);
    private static final Duration ALLOWED_GET_URL_EXPIRED = Duration.ofMinutes(30);

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/jpg"
    );

    private final S3Presigner s3Presigner;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Value("${cloud.aws.s3.image-prefix}")
    private String imagePrefix;

    public ImgUploadUrlResponse createUploadUrl(
            String originalFileName,
            String contentType
    ) {
        validateContentType(contentType);

        String extension = extractExtension(originalFileName);
        String objectKey = imagePrefix + UUID.randomUUID() + extension;

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(objectKey)
                .contentType(contentType)
                .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(UPLOAD_URL_EXPIRED)
                .putObjectRequest(putObjectRequest)
                .build();

        PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(presignRequest);

        return new ImgUploadUrlResponse(
                objectKey,
                presignedRequest.url().toString()
        );
    }

    private void validateContentType(String contentType) {
        if (!ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new IllegalArgumentException(
                    "지원하지 않는 이미지 형식입니다."
            );
        }
    }

    private String extractExtension(String originalFileName) {
        if (originalFileName == null) {
            return "";
        }

        int dotIndex = originalFileName.lastIndexOf(".");

        if (dotIndex < 0) {
            return "";
        }

        return originalFileName
                .substring(dotIndex)
                .toLowerCase();
    }

    public String getPresignedGetUrl(String objectKey) {

        if (objectKey == null || objectKey.isEmpty()) {
            return "";
        }

        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucket)
                .key(objectKey)
                .build();

        GetObjectPresignRequest presignRequest =
                GetObjectPresignRequest.builder()
                        .signatureDuration(ALLOWED_GET_URL_EXPIRED)
                        .getObjectRequest(getObjectRequest)
                        .build();

        PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(presignRequest);

        return presignedRequest.url().toString();
    }

}