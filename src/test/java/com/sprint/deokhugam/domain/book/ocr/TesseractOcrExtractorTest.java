package com.sprint.deokhugam.domain.book.ocr;

import com.sprint.deokhugam.domain.book.exception.OcrException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@DisplayName("TesseractOcrExtractor 테스트")
class TesseractOcrExtractorTest {

    private TesseractOcrExtractor tesseractOcrExtractor;

    @BeforeEach
    void setUp() {
        // given
        try {
            tesseractOcrExtractor = new TesseractOcrExtractor();
        } catch (Exception e) {
            org.junit.jupiter.api.Assumptions.assumeTrue(false, "Tesseract 초기화 실패: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Tesseract OCR 구현체 가용성 테스트")
    void testIsAvailable() {
        // given
        // setUp에서 tesseractOcrExtractor 초기화됨

        // when
        boolean available = tesseractOcrExtractor.isAvailable();

        // then
        assertThat(available).isTrue();
    }

    @Test
    @DisplayName("Tesseract OCR 구현체 우선순위 테스트")
    void testGetPriority() {
        // given
        // setUp에서 tesseractOcrExtractor 초기화됨

        // when
        int priority = tesseractOcrExtractor.getPriority();

        // then
        assertThat(priority).isEqualTo(2);
    }

    @Test
    @DisplayName("유효한 ISBN 이미지에서 ISBN 추출 성공 테스트")
    void testExtractIsbnFromValidImage() throws IOException, OcrException {
        // given
        MultipartFile testImageFile = createTestImageFile("isbn.jpg");

        // when
        String extractedIsbn = tesseractOcrExtractor.extractIsbn(testImageFile);

        // then
        assertThat(extractedIsbn).isNotNull();
        assertThat(extractedIsbn).isNotEmpty();
        assertThat(extractedIsbn).matches("\\d{10}|\\d{13}");
        System.out.println("추출된 ISBN: " + extractedIsbn);
    }

    @Test
    @DisplayName("test1.jpg 이미지에서 ISBN 추출 테스트")
    void testExtractIsbnFromTest1Image() throws IOException, OcrException {
        // given
        MultipartFile testImageFile = createTestImageFile("test1.jpg");

        // when
        String extractedIsbn = tesseractOcrExtractor.extractIsbn(testImageFile);

        // then
        System.out.println("test1.jpg 결과: " + extractedIsbn);
        if (extractedIsbn != null) {
            assertThat(extractedIsbn).matches("\\d{10}|\\d{13}");
        }
    }

    @Test
    @DisplayName("test2.jpg 이미지에서 ISBN 추출 테스트")
    void testExtractIsbnFromTest2Image() throws IOException, OcrException {
        // given
        MultipartFile testImageFile = createTestImageFile("test2.jpg");

        // when
        String extractedIsbn = tesseractOcrExtractor.extractIsbn(testImageFile);

        // then
        System.out.println("test2.jpg 결과: " + extractedIsbn);
        if (extractedIsbn != null) {
            assertThat(extractedIsbn).matches("\\d{10}|\\d{13}");
        }
    }

    @Test
    @DisplayName("test3.jpg 이미지에서 ISBN 추출 테스트")
    void testExtractIsbnFromTest3Image() throws IOException, OcrException {
        // given
        MultipartFile testImageFile = createTestImageFile("test3.jpg");

        // when
        String extractedIsbn = tesseractOcrExtractor.extractIsbn(testImageFile);

        // then
        System.out.println("test3.jpg 결과: " + extractedIsbn);
        if (extractedIsbn != null) {
            assertThat(extractedIsbn).matches("\\d{10}|\\d{13}");
        }
    }

    @Test
    @DisplayName("null 이미지 파일 예외 테스트")
    void testExtractIsbnFromNullFile() {
        // given
        MultipartFile nullFile = null;

        // when
        Throwable thrown = catchThrowable(() -> tesseractOcrExtractor.extractIsbn(nullFile));

        // then
        assertThat(thrown).isInstanceOf(OcrException.class);
        assertThat(thrown.getMessage()).contains("이미지 파일이 없습니다");
    }

    @Test
    @DisplayName("빈 이미지 파일 예외 테스트")
    void testExtractIsbnFromEmptyFile() {
        // given
        MultipartFile emptyFile = new MockMultipartFile("empty", "", "image/jpeg", new byte[0]);

        // when
        Throwable thrown = catchThrowable(() -> tesseractOcrExtractor.extractIsbn(emptyFile));

        // then
        assertThat(thrown).isInstanceOf(OcrException.class);
        assertThat(thrown.getMessage()).contains("이미지 파일이 없습니다");
    }

    @Test
    @DisplayName("지원하지 않는 파일 타입 예외 테스트")
    void testExtractIsbnFromUnsupportedFileType() {
        // given
        MultipartFile textFile = new MockMultipartFile("test", "test.txt", "text/plain", "test content".getBytes());

        // when
        Throwable thrown = catchThrowable(() -> tesseractOcrExtractor.extractIsbn(textFile));

        // then
        assertThat(thrown).isInstanceOf(OcrException.class);
        assertThat(thrown.getMessage()).contains("지원하지 않는 파일 형식입니다");
    }

    @Test
    @DisplayName("파일 크기 제한 테스트")
    void testExtractIsbnFromLargeFile() {
        // given
        byte[] largeData = new byte[11 * 1024 * 1024]; // 11MB
        MultipartFile largeFile = new MockMultipartFile("large", "large.jpg", "image/jpeg", largeData);

        // when
        Throwable thrown = catchThrowable(() -> tesseractOcrExtractor.extractIsbn(largeFile));

        // then
        assertThat(thrown).isInstanceOf(OcrException.class);
        assertThat(thrown.getMessage()).contains("이미지 파일 크기가 너무 큽니다");
    }

    @Test
    @DisplayName("잘못된 이미지 파일 예외 테스트")
    void testExtractIsbnFromCorruptedImage() {
        // given
        MultipartFile corruptedFile = new MockMultipartFile("corrupted", "corrupted.jpg", "image/jpeg", "invalid image data".getBytes());

        // when
        Throwable thrown = catchThrowable(() -> tesseractOcrExtractor.extractIsbn(corruptedFile));

        // then
        assertThat(thrown).isInstanceOf(OcrException.class);
        assertThat(thrown.getMessage()).contains("이미지를 읽을 수 없습니다");
    }

    @Test
    @DisplayName("ISBN이 없는 이미지 테스트")
    void testExtractIsbnFromImageWithoutIsbn() throws IOException, OcrException {
        // given
        MultipartFile imageWithoutIsbn = createTestImageFile("신짱구.png");

        // when
        String extractedIsbn = tesseractOcrExtractor.extractIsbn(imageWithoutIsbn);

        // then
        assertThat(extractedIsbn).isNull();
    }

    @Test
    @DisplayName("유효한 이미지 파일 검증 테스트")
    void testValidateImageFile() throws IOException {
        // given
        MultipartFile validImageFile = createTestImageFile("isbn.jpg");

        // when
        Throwable thrown = catchThrowable(() -> tesseractOcrExtractor.extractIsbn(validImageFile));

        // then
        assertThat(thrown).isNull();
    }

    @Test
    @DisplayName("ISBN 패턴 추출 테스트 - 간접 테스트")
    void testIsbnPatternExtraction() throws IOException, OcrException {
        // given
        MultipartFile testImageFile = createTestImageFile("isbn.jpg");

        // when
        String extractedIsbn = tesseractOcrExtractor.extractIsbn(testImageFile);

        // then
        if (extractedIsbn != null) {
            assertThat(extractedIsbn).matches("\\d{10}|\\d{13}");
            assertThat(extractedIsbn.length()).isIn(10, 13);
        }
    }

    /**
     * 테스트용 이미지 파일을 생성하는 헬퍼 메소드
     */
    private MultipartFile createTestImageFile(String fileName) throws IOException {
        Path imagePath = Paths.get(fileName);

        if (!Files.exists(imagePath)) {
            throw new IOException("테스트 이미지 파일이 존재하지 않습니다: " + fileName);
        }

        byte[] imageBytes = Files.readAllBytes(imagePath);
        return new MockMultipartFile(
            "image",
            fileName,
            "image/jpeg",
            imageBytes
        );
    }
}
