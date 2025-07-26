package com.sprint.deokhugam.global.storage;

import com.sprint.deokhugam.domain.book.exception.FileSizeExceededException;
import com.sprint.deokhugam.domain.book.exception.InvalidFileTypeException;
import java.io.IOException;
import java.time.Duration;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

@Slf4j
@Component
@ConditionalOnProperty(name = "deokhugam.storage.type", havingValue = "s3")
public class S3Storage {

    private final S3Client s3Client;

    private final S3Presigner s3Presigner;

    private final String bucket;

    private final long MAX_SIZE = 5 * 1024 * 1024;

    public S3Storage(S3Client s3Client,
        S3Presigner s3Presigner,
        @Value("${deokhugam.storage.s3.bucket}") String bucket) {
        this.s3Client = s3Client;
        this.s3Presigner = s3Presigner;
        this.bucket = bucket;
    }

    /**
     * S3에 이미지 업로드
     */
    public String uploadImage(MultipartFile image) throws IOException {

        // 파일 타입 검증
        String contentType = image.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new InvalidFileTypeException(contentType);
        }

        // 파일 크기 검증 (예: 5MB 제한)
        if (image.getSize() > MAX_SIZE) {
            throw new FileSizeExceededException(image.getSize(), MAX_SIZE);
        }

        // UUID 앞에서 12글자만 추출
        String shortUUID = UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        String key = "image/" + shortUUID + "_" + image.getOriginalFilename();

        // 메타 데이터 설정
        PutObjectRequest putRequest = PutObjectRequest.builder()
            .bucket(bucket)
            .key(key)
            .build();

        s3Client.putObject(putRequest, RequestBody.fromBytes(image.getBytes()));

        return key;
    }

    public void deleteImage(String key) {
        try {
            DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();

            s3Client.deleteObject(deleteRequest);
        } catch (Exception e) {
            log.warn("[S3Storage] S3에서 이미지 삭제 중 오류 발생- key: {}", key, e);
        }
    }

    public String generatePresignedUrl(String key) {
        // Presigned Url 생성
        GetObjectRequest getRequest = GetObjectRequest.builder()
            .bucket(bucket)
            .key(key)
            .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
            .signatureDuration(Duration.ofMinutes(10))
            .getObjectRequest(getRequest)
            .build();

        return s3Presigner.presignGetObject(presignRequest)
            .url()
            .toString();
    }
}
