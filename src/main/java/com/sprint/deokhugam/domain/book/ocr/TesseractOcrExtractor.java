package com.sprint.deokhugam.domain.book.ocr;

import com.sprint.deokhugam.domain.book.exception.OcrException;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;
import lombok.extern.slf4j.Slf4j;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
@Slf4j
public class TesseractOcrExtractor implements OcrExtractor {

    // ISBN 패턴 정규식
    private static final Pattern ISBN_PATTERN = Pattern.compile(
        "(?:ISBN(?:-?1[03])?:?\\s*)?(?=[-\\d\\s]{10,17}\\d)(97[89]-?\\d{1,5}-?\\d{1,7}-?\\d{1,6}-?\\d|\\d{1,5}-?\\d{1,7}-?\\d{1,6}-?\\d)"
    );

    private final Tesseract tesseract;

    public TesseractOcrExtractor() {
        this.tesseract = new Tesseract();
        initializeTesseract();
    }

    private void initializeTesseract() {
        try {
            log.info("[OCR] Tesseract 초기화 시작");

            // 시스템 환경 변수에서 경로 확인
            String tessDataPath = System.getenv("TESSDATA_PREFIX");
            log.info("[OCR] TESSDATA_PREFIX 환경 변수: {}", tessDataPath);

            if (tessDataPath != null && !tessDataPath.isEmpty()) {
                // 경로 정규화 (슬래시 통일)
                tessDataPath = tessDataPath.replace("/", "\\");
                tesseract.setDatapath(tessDataPath);
                log.info("[OCR] Tesseract 데이터 경로 설정: {}", tessDataPath);
            } else {
                // 기본 경로들을 순서대로 시도
                String[] defaultPaths = {
                    "C:\\Program Files\\Tesseract-OCR\\tessdata",
                    "C:\\Program Files (x86)\\Tesseract-OCR\\tessdata",
                    "/usr/share/tesseract-ocr/5/tessdata",
                    "/usr/share/tesseract-ocr/4.00/tessdata",
                    "/usr/local/share/tessdata",
                    "./tessdata"
                };

                boolean pathSet = false;
                for (String path : defaultPaths) {
                    java.io.File tessDataDir = new java.io.File(path);
                    if (tessDataDir.exists() && tessDataDir.isDirectory()) {
                        tesseract.setDatapath(path);
                        log.info("[OCR] Tesseract 데이터 경로 자동 설정: {}", path);
                        pathSet = true;
                        break;
                    }
                }

                if (!pathSet) {
                    log.warn("[OCR] Tesseract 데이터 경로를 찾을 수 없습니다. 기본 경로를 사용합니다.");
                }
            }

            // Tesseract 설정
            tesseract.setLanguage("eng");
            tesseract.setPageSegMode(1);
            tesseract.setOcrEngineMode(1);

            // 초기화 테스트
            log.info("[OCR] Tesseract 초기화 테스트 시작");
            BufferedImage testImage = new BufferedImage(100, 50, BufferedImage.TYPE_INT_RGB);
            tesseract.doOCR(testImage);
            log.info("[OCR] Tesseract 초기화 성공");

        } catch (Exception e) {
            log.error("[OCR] Tesseract 초기화 실패", e);
            throw new RuntimeException("Tesseract 초기화에 실패했습니다: " + e.getMessage(), e);
        }
    }

    @Override
    public String extractIsbn(MultipartFile imageFile) throws OcrException {
        try {
            log.info("[OCR] Tesseract를 사용하여 이미지에서 텍스트 추출 시작");

            // 이미지 파일 유효성 검증
            validateImageFile(imageFile);

            BufferedImage image = ImageIO.read(imageFile.getInputStream());
            if (image == null) {
                throw OcrException.clientError("이미지를 읽을 수 없습니다.");
            }

            // 이미지 전처리
            BufferedImage preprocessedImage = preprocessImage(image);

            // Tesseract OCR 실행
            String extractedText = tesseract.doOCR(preprocessedImage);
            log.info("[OCR] Tesseract에서 추출된 텍스트: {}", extractedText);

            if (extractedText == null || extractedText.trim().isEmpty()) {
                log.warn("[OCR] 텍스트를 추출할 수 없습니다.");
                return null;
            }

            // ISBN 패턴 매칭 및 추출
            String isbn = extractIsbnFromText(extractedText);
            log.info("[OCR] 추출된 ISBN: {}", isbn);
            return isbn;

        } catch (TesseractException e) {
            log.error("[OCR] OCR 처리 실패", e);
            throw OcrException.serverError("Tesseract OCR 처리 중 오류가 발생했습니다.", e);
        } catch (IOException e) {
            log.error("[OCR] 이미지 파일 읽기 실패", e);
            throw OcrException.clientError("이미지 파일을 읽을 수 없습니다.", e);
        }
    }

    private void validateImageFile(MultipartFile imageFile) throws OcrException {
        if (imageFile == null || imageFile.isEmpty()) {
            throw OcrException.clientError("이미지 파일이 없습니다.");
        }

        // 파일 크기 검증 (10MB 제한)
        if (imageFile.getSize() > 10 * 1024 * 1024) {
            throw OcrException.clientError("이미지 파일 크기가 너무 큽니다. (최대 10MB)");
        }

        // 파일 타입 검증
        String contentType = imageFile.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw OcrException.clientError("지원하지 않는 파일 형식입니다.");
        }
    }

    private String extractIsbnFromText(String text) {
        if (text == null || text.trim().isEmpty()) {
            return null;
        }

        // 텍스트 정규화 (개행 문자를 공백으로 변환, 여러 공백을 하나로 통합)
        String normalizedText = text.replaceAll("\\s+", " ");

        log.debug("[OCR] 정규화된 텍스트: {}", normalizedText);

        // ISBN 패턴 매칭
        Matcher matcher = ISBN_PATTERN.matcher(normalizedText);
        if (matcher.find()) {
            String rawIsbn = matcher.group();
            String isbn = rawIsbn.replaceAll("[^\\d]", "");

            // ISBN-10 or ISBN-13 길이 검증
            if (isbn.length() == 10 || isbn.length() == 13) {
                log.info("[OCR] 유효한 ISBN 패턴 발견: {} (원본: {})", isbn, rawIsbn);
                return isbn;
            }
        }

        // 추가적인 ISBN 패턴 검색 (숫자만으로 구성된 10자리 or 13자리)
        Pattern numericPattern = Pattern.compile("\\b(\\d{10}|\\d{13})\\b");
        Matcher numericMatcher = numericPattern.matcher(normalizedText);

        while (numericMatcher.find()) {
            String candidate = numericMatcher.group();
            if (candidate.length() == 10 || candidate.length() == 13) {
                // 간단한 ISBN 체크섬 검증
                if (isValidIsbnFormat(candidate)) {
                    log.info("[OCR] 숫자 패턴에서 유효한 ISBN 발견: {}", candidate);
                    return candidate;
                }
            }
        }

        log.warn("[OCR] 유효한 ISBN을 찾을 수 없습니다. 텍스트: {}", text);
        return null;
    }

    private boolean isValidIsbnFormat(String isbn) {
        // 기본적인 ISBN 형식 검증
        if (isbn.length() == 13) {
            // ISBN-13은 978 또는 979로 시작해야 함
            return isbn.startsWith("978") || isbn.startsWith("979");
        } else if (isbn.length() == 10) {
            // ISBN-10은 모든 문자가 숫자이거나 마지막이 X일 수 있음
            return isbn.matches("\\d{9}[\\dX]");
        }
        return false;
    }

    private BufferedImage preprocessImage(BufferedImage image) {
        // 이미지 전처리 로직 (크기 조정, 노이즈 제거 등)
        // 여기서는 단순하게 원본 이미지를 반환
        // 필요에 따라 이미지 품질 향상 로직을 여기에 추가 가능
        return image;
    }

    @Override
    public boolean isAvailable() {
        try {
            BufferedImage testImage = new BufferedImage(100, 50, BufferedImage.TYPE_INT_RGB);
            tesseract.doOCR(testImage);
            return true;
        } catch (Exception e) {
            log.warn("[OCR] Tesseract를 사용할 수 없습니다: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public int getPriority() {
        return 1; // 유일한 OCR 엔진이므로 우선순위 1
    }
}