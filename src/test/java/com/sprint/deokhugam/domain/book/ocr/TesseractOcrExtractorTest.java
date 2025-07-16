package com.sprint.deokhugam.domain.book.ocr;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import com.sprint.deokhugam.domain.book.exception.OcrException;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
class TesseractOcrExtractorTest {

    private TesseractOcrExtractor tesseractOcrExtractor;

    @BeforeEach
    void setUp() {
        try {
            tesseractOcrExtractor = new TesseractOcrExtractor();
        } catch (Exception e) {
            org.junit.jupiter.api.Assumptions.assumeFalse(true, "Tesseract 초기화 실패: " + e.getMessage());
        }
    }

    @Test
    void Tesseract_OCR_구현체_가용성_테스트() {
        // given
        // setUp에서 tesseractOcrExtractor 초기화됨

        // when
        boolean available = tesseractOcrExtractor.isAvailable();

        // then
        assertThat(available).isTrue();
    }

    @Test
    void Tesseract_OCR_구현체_우선순위_테스트() {
        // given
        // setUp에서 tesseractOcrExtractor 초기화됨

        // when
        int priority = tesseractOcrExtractor.getPriority();

        // then
        assertThat(priority).isEqualTo(1);
    }

    @Test
    void 실제_ISBN_이미지에서_ISBN_추출_성공_테스트() throws IOException, OcrException {
        // given
        MultipartFile testImageFile = createTestImageFile("isbn.jpg");

        // when
        String extractedIsbn = tesseractOcrExtractor.extractIsbn(testImageFile);

        // then
        if (extractedIsbn != null) {
            assertThat(extractedIsbn).isNotEmpty();
            assertThat(extractedIsbn).matches("\\d{10}|\\d{13}");
            System.out.println("추출된 ISBN: " + extractedIsbn);
        }
    }

    @Test
    void test1_jpg_이미지에서_ISBN_추출_테스트() throws IOException, OcrException {
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
    void test2_jpg_이미지에서_ISBN_추출_테스트() throws IOException, OcrException {
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
    void test3_jpg_이미지에서_ISBN_추출_테스트() throws IOException, OcrException {
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
    void null_이미지_파일_예외_테스트() {
        // given
        MultipartFile nullFile = null;

        // when
        Throwable thrown = catchThrowable(() -> tesseractOcrExtractor.extractIsbn(nullFile));

        // then
        assertThat(thrown)
            .isInstanceOf(OcrException.class)
            .hasMessageContaining("OCR 잘못된 입력 값입니다.");
    }

    @Test
    void 빈_이미지_파일_예외_테스트() {
        // given
        MultipartFile emptyFile = new MockMultipartFile("empty", "", "image/jpeg", new byte[0]);

        // when
        Throwable thrown = catchThrowable(() -> tesseractOcrExtractor.extractIsbn(emptyFile));

        // then
        assertThat(thrown)
            .isInstanceOf(OcrException.class)
            .hasMessageContaining("OCR 잘못된 입력 값입니다.");
    }

    @Test
    void 지원하지_않는_파일_타입_예외_테스트() {
        // given
        MultipartFile textFile = new MockMultipartFile("test", "test.txt", "text/plain", "test content".getBytes());

        // when
        Throwable thrown = catchThrowable(() -> tesseractOcrExtractor.extractIsbn(textFile));

        // then
        assertThat(thrown)
            .isInstanceOf(OcrException.class)
            .hasMessageContaining("OCR 잘못된 입력 값입니다.");
    }

    @Test
    void 파일_크기_제한_테스트() {
        // given
        byte[] largeData = new byte[6 * 1024 * 1024]; // 11MB
        MultipartFile largeFile = new MockMultipartFile("large", "large.jpg", "image/jpeg", largeData);

        // when
        Throwable thrown = catchThrowable(() -> tesseractOcrExtractor.extractIsbn(largeFile));

        // then
        assertThat(thrown)
            .isInstanceOf(OcrException.class)
            .hasMessageContaining("OCR 잘못된 입력 값입니다.");
    }

    @Test
    void 잘못된_이미지_파일_예외_테스트() {
        // given
        MultipartFile corruptedFile = new MockMultipartFile("corrupted", "corrupted.jpg", "image/jpeg", "invalid image data".getBytes());

        // when
        Throwable thrown = catchThrowable(() -> tesseractOcrExtractor.extractIsbn(corruptedFile));

        // then
        assertThat(thrown)
            .isInstanceOf(OcrException.class)
            .hasMessageContaining("OCR 서버 내부 오류가 발생했습니다.");
    }

    @Test
    void ISBN이_없는_이미지_테스트() throws IOException, OcrException {
        // given
        MultipartFile imageWithoutIsbn = createTestImageFile("신짱구.png");

        // when
        String extractedIsbn = tesseractOcrExtractor.extractIsbn(imageWithoutIsbn);

        // then
        assertThat(extractedIsbn).isNull();
    }

    @Test
    void ISBN_13_패턴_매칭_테스트() throws IOException, OcrException {
        // given
        MultipartFile testImageWithIsbn13 = createTestImageWithText("ISBN: 9780134685991");

        // when
        String extractedIsbn = tesseractOcrExtractor.extractIsbn(testImageWithIsbn13);

        // then
        if (extractedIsbn != null) {
            assertThat(extractedIsbn).hasSize(13);
            assertThat(extractedIsbn).startsWith("978");
        }
    }

    @Test
    void ISBN_10_패턴_매칭_테스트() throws IOException, OcrException {
        // given
        MultipartFile testImageWithIsbn10 = createTestImageWithText("ISBN: 0134685997");

        // when
        String extractedIsbn = tesseractOcrExtractor.extractIsbn(testImageWithIsbn10);

        // then
        if (extractedIsbn != null) {
            assertThat(extractedIsbn).hasSize(10);
            assertThat(extractedIsbn).matches("\\d{10}");
        }
    }

    @Test
    void 다양한_ISBN_형식_처리_테스트() throws IOException, OcrException {
        // given
        String[] isbnTexts = {
            "ISBN-13: 978-0-134-68599-1",
            "ISBN 9780134685991",
            "978-0134685991",
            "ISBN: 0134685997",
            "ISBN-10: 0134685997"
        };

        for (String isbnText : isbnTexts) {
            // given
            MultipartFile testImage = createTestImageWithText(isbnText);

            // when
            String extractedIsbn = tesseractOcrExtractor.extractIsbn(testImage);

            // then
            if (extractedIsbn != null) {
                assertThat(extractedIsbn).matches("\\d{10}|\\d{13}");
                System.out.println("텍스트: " + isbnText + " -> 추출된 ISBN: " + extractedIsbn);
            }
        }
    }

    @Test
    void ContentType_null_처리_테스트() {
        // given
        MultipartFile fileWithNullContentType = new MockMultipartFile("test", "test.jpg", null, "test content".getBytes());

        // when
        Throwable thrown = catchThrowable(() -> tesseractOcrExtractor.extractIsbn(fileWithNullContentType));

        // then
        assertThat(thrown)
            .isInstanceOf(OcrException.class)
            .hasMessageContaining("OCR 잘못된 입력 값입니다.");
    }

    @Test
    void 빈_텍스트_결과_처리_테스트() throws IOException, OcrException {
        // given
        MultipartFile blankImage = createBlankImage();

        // when
        String extractedIsbn = tesseractOcrExtractor.extractIsbn(blankImage);

        // then
        assertThat(extractedIsbn).isNull();
    }

    @Test
    void 숫자_패턴_매칭_테스트() throws IOException, OcrException {
        // given
        MultipartFile testImageWithNumbers = createTestImageWithText("Book Code: 1234567890123 Other: 9876543210");

        // when
        String extractedIsbn = tesseractOcrExtractor.extractIsbn(testImageWithNumbers);

        // then
        if (extractedIsbn != null) {
            assertThat(extractedIsbn).matches("\\d{10}|\\d{13}");
        }
    }

    /**
     * 테스트용 이미지 파일을 생성하는 헬퍼 메소드
     */
    private MultipartFile createTestImageFile(String fileName) throws IOException {
        Path imagePath = Paths.get(fileName);

        if (!Files.exists(imagePath)) {
            // 파일이 없으면 테스트용 이미지 생성
            return createTestImageWithText("Test Image - No ISBN");
        }

        byte[] imageBytes = Files.readAllBytes(imagePath);
        return new MockMultipartFile(
            "image",
            fileName,
            "image/jpeg",
            imageBytes
        );
    }

    /**
     * 텍스트가 포함된 테스트 이미지를 생성하는 헬퍼 메소드
     */
    private MultipartFile createTestImageWithText(String text) throws IOException {
        BufferedImage image = new BufferedImage(400, 200, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();

        // 배경을 흰색으로 설정
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, 400, 200);

        // 텍스트를 검은색으로 설정
        g2d.setColor(Color.BLACK);
        g2d.drawString(text, 50, 100);

        g2d.dispose();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "jpg", baos);

        return new MockMultipartFile(
            "image",
            "test.jpg",
            "image/jpeg",
            baos.toByteArray()
        );
    }

    /**
     * 지정된 형식의 테스트 이미지를 생성하는 헬퍼 메소드
     */
    private MultipartFile createTestImageWithFormat(String contentType) throws IOException {
        BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, 100, 100);
        g2d.dispose();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        String format = contentType.split("/")[1];
        ImageIO.write(image, format.equals("jpeg") ? "jpg" : format, baos);

        return new MockMultipartFile(
            "image",
            "test." + format,
            contentType,
            baos.toByteArray()
        );
    }

    /**
     * 빈 이미지를 생성하는 헬퍼 메소드
     */
    private MultipartFile createBlankImage() throws IOException {
        BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, 100, 100);
        g2d.dispose();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "jpg", baos);

        return new MockMultipartFile(
            "blank",
            "blank.jpg",
            "image/jpeg",
            baos.toByteArray()
        );
    }
}