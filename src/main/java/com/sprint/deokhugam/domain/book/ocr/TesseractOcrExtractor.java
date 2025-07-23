package com.sprint.deokhugam.domain.book.ocr;

import com.sprint.deokhugam.domain.book.exception.OcrException;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
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

    // 더 포괄적인 ISBN 패턴으로 개선
    private static final Pattern ISBN_PATTERN = Pattern.compile(
        "(?i)(?:ISBN(?:[-\\s]?1[03])?[:\\s]*)?(?=[-\\d\\s]{10,17}\\d)(97[89][-\\s]?\\d{1,5}[-\\s]?\\d{1,7}[-\\s]?\\d{1,6}[-\\s]?\\d|\\d{1,5}[-\\s]?\\d{1,7}[-\\s]?\\d{1,6}[-\\s]?\\d)"
    );

    // ISBN 키워드 근처 텍스트를 우선 분석하기 위한 패턴
    private static final Pattern ISBN_KEYWORD_PATTERN = Pattern.compile(
        "(?i)(ISBN|International\\s+Standard\\s+Book\\s+Number|도서번호|서지번호|Book\\s+Number)"
    );

    // OCR에서 자주 잘못 인식되는 문자들 매핑
    private static final String[][] OCR_CORRECTIONS = {
        {"O", "0"}, {"o", "0"}, {"I", "1"}, {"l", "1"}, {"S", "5"}, {"s", "5"},
        {"Z", "2"}, {"z", "2"}, {"B", "8"}, {"G", "6"}, {"g", "6"}, {"q", "9"}
    };

    // 다양한 페이지 분할 모드 시도 순서 (ISBN에 효과적인 순서로 배열)
    private static final int[] PAGE_SEG_MODES = {6, 8, 7, 3, 1};

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
                tessDataPath = Paths.get(tessDataPath).normalize().toString();
                tesseract.setDatapath(tessDataPath);
                log.info("[OCR] Tesseract 데이터 경로 설정: {}", tessDataPath);
            } else {
                String[] defaultPaths = {
                    "/opt/homebrew/share/tessdata",
                    "/opt/local/share/tessdata",
                    "/usr/local/share/tessdata",
                    "/usr/share/tesseract-ocr/5/tessdata",
                    "/usr/share/tesseract-ocr/4.00/tessdata",
                    "C:\\Program Files\\Tesseract-OCR\\tessdata",
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
        validateImageFile(imageFile);

        try {
            BufferedImage image = ImageIO.read(imageFile.getInputStream());
            if (image == null) {
                throw OcrException.clientError("이미지를 읽을 수 없습니다.");
            }

            log.info("[OCR] 이미지 처리 시작 - 크기: {}x{}, 파일크기: {}KB",
                image.getWidth(), image.getHeight(), imageFile.getSize() / 1024);

            // 1단계: 원본 이미지로 기본 모드 시도
            log.debug("[OCR] 1단계: 원본 이미지로 OCR 시도");
            String isbn = tryOcrWithOriginalImage(image);
            if (isbn != null) {
                log.info("[OCR] 원본 이미지로 ISBN 발견: {}", isbn);
                return isbn;
            }

            // 2단계: 이미지 크기 최적화 후 시도
            log.debug("[OCR] 2단계: 이미지 크기 최적화 후 시도");
            BufferedImage resizedImage = resizeImageForOcr(image);
            isbn = tryOcrWithMultipleModes(resizedImage);
            if (isbn != null) {
                log.info("[OCR] 크기 최적화 후 ISBN 발견: {}", isbn);
                return isbn;
            }

            // 3단계: 그레이스케일 변환 후 시도
            log.debug("[OCR] 3단계: 그레이스케일 변환 후 시도");
            BufferedImage grayImage = convertToGrayscale(resizedImage);
            isbn = tryOcrWithMultipleModes(grayImage);
            if (isbn != null) {
                log.info("[OCR] 그레이스케일 변환 후 ISBN 발견: {}", isbn);
                return isbn;
            }

            // 4단계: 노이즈 제거 후 시도
            log.debug("[OCR] 4단계: 노이즈 제거 후 시도");
            BufferedImage denoisedImage = removeNoise(grayImage);
            isbn = tryOcrWithMultipleModes(denoisedImage);
            if (isbn != null) {
                log.info("[OCR] 노이즈 제거 후 ISBN 발견: {}", isbn);
                return isbn;
            }

            log.warn("[OCR] 모든 전처리 방법으로도 ISBN을 찾을 수 없습니다.");
            return null;

        } catch (TesseractException e) {
            log.error("[OCR] OCR 처리 실패", e);
            throw OcrException.serverError("Tesseract OCR 처리 중 오류가 발생했습니다.", e);
        } catch (IOException e) {
            log.error("[OCR] 이미지 파일 읽기 실패", e);
            throw OcrException.clientError("이미지 파일을 읽을 수 없습니다.", e);
        } catch (RuntimeException e) {
            log.error("[OCR] 런타임 예외 발생", e);
            throw OcrException.serverError("OCR 처리 중 예상치 못한 오류가 발생했습니다.", e);
        } catch (Exception e) {
            log.error("[OCR] 일반 예외 발생", e);
            throw OcrException.serverError("OCR 처리 중 시스템 오류가 발생했습니다.", e);
        }
    }

    private void validateImageFile(MultipartFile imageFile) throws OcrException {
        if (imageFile == null || imageFile.isEmpty()) {
            throw OcrException.clientError("이미지 파일이 없습니다.");
        }

        if (imageFile.getSize() > 5 * 1024 * 1024) {
            throw OcrException.clientError("이미지 파일 크기가 너무 큽니다. (최대 5MB)");
        }

        String contentType = imageFile.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw OcrException.clientError("지원하지 않는 파일 형식입니다.");
        }

        List<String> supportedTypes = List.of("image/jpeg", "image/jpg", "image/png", "image/gif", "image/bmp", "image/webp");
        if (!supportedTypes.contains(contentType.toLowerCase())) {
            throw OcrException.clientError("지원되지 않는 이미지 형식입니다. (지원 형식: JPEG, PNG, GIF, BMP, WEBP)");
        }
    }

    private String tryOcrWithOriginalImage(BufferedImage image) throws TesseractException {
        tesseract.setPageSegMode(1); // 기본 모드
        String rawText = tesseract.doOCR(image);
        return extractIsbnFromText(rawText);
    }

    /**
     *  여러 페이지 분할 모드로 시도
     *  */
    private String tryOcrWithMultipleModes(BufferedImage image) throws TesseractException {
        for (int mode : PAGE_SEG_MODES) {
            try {
                log.debug("[OCR] 페이지 분할 모드 {} 시도", mode);
                tesseract.setPageSegMode(mode);
                String rawText = tesseract.doOCR(image);
                String isbn = extractIsbnFromText(rawText);

                if (isbn != null) {
                    log.info("[OCR] 페이지 분할 모드 {}로 ISBN 발견: {}", mode, isbn);
                    return isbn;
                }
            } catch (TesseractException e) {
                log.debug("[OCR] 페이지 분할 모드 {} 실패: {}", mode, e.getMessage());
                // 다음 모드로 계속 시도
            }
        }
        return null;
    }

    private String extractIsbnFromText(String text) {
        if (text == null || text.trim().isEmpty()) {
            return null;
        }

        // 1차 정규화: 기본 정리
        String normalizedText = text.replaceAll("\\s+", " ")
            .replaceAll("[^\\w\\s\\d\\-:()]", " ")
            .trim();

        log.debug("[OCR] 1차 정규화된 텍스트: {}", normalizedText);

        // 1단계: ISBN 키워드 근처 텍스트를 우선적으로 검색
        String focusedText = extractTextAroundIsbnKeyword(normalizedText);
        if (focusedText != null) {
            String isbn = findIsbnInText(focusedText);
            if (isbn != null) {
                return isbn;
            }
        }

        // 2단계: 전체 텍스트에서 ISBN 검색
        String isbn = findIsbnInText(normalizedText);
        if (isbn != null) {
            return isbn;
        }

        // 3단계: OCR 오인식 문자 보정 후 재검색
        log.debug("[OCR] OCR 오인식 문자 보정 시도");
        String correctedText = correctOcrErrors(normalizedText);
        if (!correctedText.equals(normalizedText)) {
            log.debug("[OCR] 문자 보정된 텍스트: {}", correctedText);
            return findIsbnInText(correctedText);
        }

        return null;
    }

    private String extractTextAroundIsbnKeyword(String text) {
        Matcher keywordMatcher = ISBN_KEYWORD_PATTERN.matcher(text);
        if (keywordMatcher.find()) {
            int start = Math.max(0, keywordMatcher.start() - 20);
            int end = Math.min(text.length(), keywordMatcher.end() + 50);
            String focusedText = text.substring(start, end);
            log.debug("[OCR] ISBN 키워드 근처 텍스트: {}", focusedText);
            return focusedText;
        }
        return null;
    }

    private String findIsbnInText(String text) {
        // ISBN 패턴 매칭 개선
        Matcher matcher = ISBN_PATTERN.matcher(text);
        if (matcher.find()) {
            String rawIsbn = matcher.group();
            String isbn = cleanIsbn(rawIsbn);

            if (isbn != null && isValidIsbnFormat(isbn)) {
                log.info("[OCR] 유효한 ISBN 패턴 발견: {} (원본: {})", isbn, rawIsbn);
                return isbn;
            }
        }

        Pattern numericPattern = Pattern.compile("\\b(\\d{10}|\\d{13})\\b");
        Matcher numericMatcher = numericPattern.matcher(text);

        while (numericMatcher.find()) {
            String candidate = numericMatcher.group();
            if (isValidIsbnFormat(candidate)) {
                log.info("[OCR] 숫자 패턴에서 유효한 ISBN 발견: {}", candidate);
                return candidate;
            }
        }

        return null;
    }

    private String cleanIsbn(String rawIsbn) {
        if (rawIsbn == null) return null;

        String cleaned = rawIsbn.replaceAll("(?i)isbn[-\\s:]*", "")
            .replaceAll("[^\\d]", "");

        return (cleaned.length() >= 10 && cleaned.length() <= 13) ? cleaned : null;
    }

    private boolean isValidIsbnFormat(String isbn) {
        if (isbn == null) return false;

        if (isbn.length() == 13) {
            return isbn.startsWith("978") || isbn.startsWith("979");
        } else if (isbn.length() == 10) {
            return isbn.matches("\\d{9}[\\dX]");
        }
        return false;
    }

    /**
     * OCR 오인식 문자 보정 개선
     * */
    private String correctOcrErrors(String text) {
        String corrected = text;

        // ISBN 숫자 부분에서만 보정 적용
        Pattern isbnNumberPattern = Pattern.compile("\\b\\d{9,13}\\b");
        Matcher matcher = isbnNumberPattern.matcher(text);

        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String number = matcher.group();
            String correctedNumber = number;

            // OCR 오인식 보정 적용
            for (String[] correction : OCR_CORRECTIONS) {
                correctedNumber = correctedNumber.replace(correction[0], correction[1]);
            }

            if (!number.equals(correctedNumber)) {
                log.debug("[OCR] 문자 보정: {} -> {}", number, correctedNumber);
            }

            matcher.appendReplacement(sb, correctedNumber);
        }
        matcher.appendTail(sb);

        return sb.toString();
    }

    /**
     * 기존 이미지 크기 최적화
     * */
    private BufferedImage resizeImageForOcr(BufferedImage image) {
        try {
            int width = image.getWidth();
            int height = image.getHeight();

            // OCR에 적합한 크기 계산 ( 너무 작으면 확대, 너무 크면 축소 )
            int targetWidth, targetHeight;

            if (width < 800 || height < 600) {
                // 작은 이미지는 2배 확대
                targetWidth = width * 2;
                targetHeight = height * 2;
                log.debug("[OCR] 이미지 확대: {}x{} -> {}x{}", width, height, targetWidth, targetHeight);
            } else if (width > 2000 || height > 2000) {
                // 큰 이미지는 축소
                double scale = Math.min(2000.0 / width, 2000.0 / height);
                targetWidth = (int) (width * scale);
                targetHeight = (int) (height * scale);
                log.debug("[OCR] 이미지 축소: {}x{} -> {}x{}", width, height, targetWidth, targetHeight);
            } else {
                // 적정 크기는 그대로 유지
                log.debug("[OCR] 이미지 크기 적정함: {}x{}", width, height);
                return image;
            }

            BufferedImage resized = new BufferedImage(targetWidth, targetHeight, image.getType());
            Graphics2D g2d = resized.createGraphics();

            // 고품질 리사이징 설정
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2d.drawImage(image, 0, 0, targetWidth, targetHeight, null);
            g2d.dispose();

            return resized;

        } catch (Exception e) {
            log.warn("[OCR] 이미지 리사이징 실패, 원본 이미지 사용: {}", e.getMessage());
            return image;
        }
    }

    private BufferedImage convertToGrayscale(BufferedImage image) {
        try {
            log.debug("[OCR] 그레이스케일 변환 시작");

            BufferedImage grayImage = new BufferedImage(
                image.getWidth(), image.getHeight(), BufferedImage.TYPE_BYTE_GRAY);

            Graphics2D g2d = grayImage.createGraphics();
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2d.drawImage(image, 0, 0, null);
            g2d.dispose();

            log.debug("[OCR] 그레이스케일 변환 완료");
            return grayImage;

        } catch (Exception e) {
            log.warn("[OCR] 그레이스케일 변환 실패, 원본 이미지 사용: {}", e.getMessage());
            return image;
        }
    }

    // 노이즈 제거 ( 안전하고 빠른 방법 )
    private BufferedImage removeNoise(BufferedImage image) {
        try {
            log.debug("[OCR] 노이즈 제거 시작");

            int width = image.getWidth();
            int height = image.getHeight();
            BufferedImage cleaned = new BufferedImage(width, height, image.getType());

            // 간단한 미디언 필터 적용 ( 3x3 윈도우 )
            for (int y = 1; y < height - 1; y++) {
                for (int x = 1; x < width - 1; x++) {
                    int[] pixels = new int[9];
                    int index = 0;

                    // 3x3 윈도우의 픽셀들을 수집
                    for (int dy = -1; dy <= 1; dy++) {
                        for (int dx = -1; dx <= 1; dx++) {
                            Color color = new Color(image.getRGB(x + dx, y + dy));
                            pixels[index++] = color.getRed(); // 그레이스케일이므로 R값만 사용
                        }
                    }

                    // 중간값 찾기 ( 간단한 정렬 )
                    java.util.Arrays.sort(pixels);
                    int median = pixels[4]; // 9개 중 5번째가 중간값

                    Color medianColor = new Color(median, median, median);
                    cleaned.setRGB(x, y, medianColor.getRGB());
                }
            }

            // 테두리 픽셀은 원본 그대로 복사
            Graphics2D g2d = cleaned.createGraphics();
            g2d.drawImage(image, 0, 0, null);
            g2d.dispose();

            log.debug("[OCR] 노이즈 제거 완료");
            return cleaned;

        } catch (Exception e) {
            log.warn("[OCR] 노이즈 제거 실패, 원본 이미지 사용: {}", e.getMessage());
            return image;
        }
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
        return 1;
    }
}