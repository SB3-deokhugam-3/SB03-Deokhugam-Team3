package com.sprint.deokhugam.global.storage;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.sprint.deokhugam.domain.book.exception.FileSizeExceededException;
import com.sprint.deokhugam.domain.book.exception.InvalidFileTypeException;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

@ExtendWith(MockitoExtension.class)
class S3StorageTest {

    @Mock
    private S3Client s3Client;

    @Mock
    private S3Presigner s3Presigner;

    private S3Storage s3Storage;

    @BeforeEach
    void setUp() {
        s3Client = mock(S3Client.class);
        s3Presigner = mock(S3Presigner.class);
        s3Storage = new S3Storage(s3Client, s3Presigner, "test-bucket");
    }

    @Test
    void 이미지를_업로드_하면_키를_반환해야_한다() throws IOException {

        // given
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "test.jpg",
            "image/jpeg",
            "test-image-content".getBytes()
        );

        // when
        String key = s3Storage.uploadImage(file);

        // then
        ArgumentCaptor<PutObjectRequest> requestCaptor = ArgumentCaptor.forClass(PutObjectRequest.class);
        verify(s3Client).putObject(requestCaptor.capture(), any(RequestBody.class));
        PutObjectRequest capturedRequest = requestCaptor.getValue();
        assertThat(capturedRequest.bucket()).isEqualTo("test-bucket");
        assertThat(capturedRequest.key()).startsWith("image/");
        assertThat(key).isEqualTo(capturedRequest.key());
    }

    @Test
    void 파일_타입이_image가_아닌_경우_업로드에_실패한다() {

        // given
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "test.txt",
            "text/plain",
            "text-content".getBytes()
        );

        // when
        Throwable thrown = catchThrowable(() -> s3Storage.uploadImage(file));

        // then
        assertThat(thrown)
            .isInstanceOf(InvalidFileTypeException.class);
    }

    @Test
    void 파일_크기가_5MB_보다_큰_경우_업로드에_실패한다() {

        // given
        byte[] largeContent = new byte[6 * 1024 * 1024]; // 6MB
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "large.jpg",
            "image/jpeg",
            largeContent
        );

        Throwable thrown = catchThrowable(() -> s3Storage.uploadImage(file));

        // then
        assertThat(thrown)
            .isInstanceOf(FileSizeExceededException.class);
    }

    @Test
    void presigned_url_생성_테스트() {

        // given
        String key = "image/test.jpg";
        URL fakeUrl = mock(URL.class);
        PresignedGetObjectRequest presignedRequest = mock(PresignedGetObjectRequest.class);

        given(fakeUrl.toString()).willReturn("https://fake-presigned-url.com/image/test.jpg");
        given(presignedRequest.url()).willReturn(fakeUrl);
        given(s3Presigner.presignGetObject(any(GetObjectPresignRequest.class)))
            .willReturn(presignedRequest);

        // when
        String url = s3Storage.generatePresignedUrl(key);

        // then
        assertThat(url).isEqualTo("https://fake-presigned-url.com/image/test.jpg");
        ArgumentCaptor<GetObjectPresignRequest> captor = ArgumentCaptor.forClass(GetObjectPresignRequest.class);
        verify(s3Presigner).presignGetObject(captor.capture());
        assertThat(captor.getValue().signatureDuration()).isEqualTo(Duration.ofMinutes(10));
    }

    @Test
    void 로그_파일_업로드_테스트() {

        // given
        File file = new File("test.log");

        // when
        String key = s3Storage.uploadFile(file);

        // then
        ArgumentCaptor<PutObjectRequest> requestCaptor = ArgumentCaptor.forClass(PutObjectRequest.class);
        verify(s3Client).putObject(requestCaptor.capture(), any(RequestBody.class));
        PutObjectRequest capturedRequest = requestCaptor.getValue();
        assertThat(capturedRequest.bucket()).isEqualTo("test-bucket");
        assertThat(capturedRequest.key()).startsWith("logs/");
        assertThat(key).isEqualTo(capturedRequest.key());
    }

    @Test
    void 로그_파일이_아닌_경우_업로드에_실패한다() {

        // given
        File file = new File("test.txt");

        // when
        Throwable thrown = catchThrowable(() -> s3Storage.uploadFile(file));

        // then
        assertThat(thrown)
            .isInstanceOf(InvalidFileTypeException.class);
    }
}