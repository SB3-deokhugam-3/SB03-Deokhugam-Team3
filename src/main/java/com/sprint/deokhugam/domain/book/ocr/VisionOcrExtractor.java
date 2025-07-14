package com.sprint.deokhugam.domain.book.ocr;

import com.google.cloud.vision.v1.AnnotateImageRequest;
import com.google.cloud.vision.v1.AnnotateImageResponse;
import com.google.cloud.vision.v1.BatchAnnotateImagesResponse;
import com.google.cloud.vision.v1.Feature;
import com.google.cloud.vision.v1.Image;
import com.google.cloud.vision.v1.ImageAnnotatorClient;
import com.google.cloud.vision.v1.TextAnnotation;
import com.google.protobuf.ByteString;
import com.sprint.deokhugam.domain.book.exception.OcrException;
import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
@ConditionalOnProperty(name = "ocr.vision.enabled", havingValue = "true",matchIfMissing = false)
@Slf4j
public class VisionOcrExtractor implements OcrExtractor {

    private static final Pattern ISBN_PATTERN = Pattern.compile(
        "(?:ISBN(?:-?1[03])?:?\\s*)?(?=[-\\d\\s]{10,17}\\d)(97[89]-?\\d{1,5}-?\\d{1,7}-?\\d{1,6}-?\\d|\\d{1,5}-?\\d{1,7}-?\\d{1,6}-?\\d)"
    );

    private static final int MAX_FILE_SIZE = 10 * 1024 * 1024;
    private static final List<String> SUPPORTED_CONTENT_TYPES = List.of(
        "image/jpeg","image/jpg","image/png","image/gif","image/bmp","image/webp"
    );


    @Override
    public String extractIsbn(MultipartFile imageFile) throws OcrException {
        try {
            log.info("Google Cloud Vision API를 사용하여 이미지에서 텍스트 추출 시작");

            // 이미지 파일 유혀성 검증
            validateImageFile(imageFile);

            // Google Cloud Vision API를 통한 텍스트 추출
            String extractedText = performVisionOcr(imageFile);
            log.info("Vision API에서 추출된 텍스트 : {}", extractedText);

            if (extractedText == null || extractedText.trim().isEmpty()) {
                log.warn("텍스트를 추출할 수 없습니다.");
                return null;
            }

            // ISBN 패턴 매칭 및 추출
            String isbn = extractIsbnFromText(extractedText);
            log.info("추출된 ISBN : {}", isbn);
            return isbn;
        } catch (IOException e) {
            log.error("이미지 파일 읽기 실패", e);
            throw new OcrException("이미지 파일을 읽을 수 없습니다.", e);
        } catch (Exception e) {
            log.error("Google Cloud Vision API OCR 처리 실패", e);
            throw new OcrException("Google Cloud Vision API OCR 처리 중 오류가 발생했습니다.", e);
        }
    }

    private void validateImageFile(MultipartFile imageFile) throws OcrException {
        if (imageFile == null || imageFile.isEmpty()) {
            throw new OcrException("이미지 파일이 없습니다.");
        }

        if (imageFile.getSize() > MAX_FILE_SIZE) {
            throw new OcrException("이미지 파일 크기가 너무 큽니다. (최대 10MB)");
        }

        String contentType = imageFile.getContentType();
        if (contentType == null || !SUPPORTED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new OcrException("지원하지 않는 파일 형식입니다. 지원 형식: " + SUPPORTED_CONTENT_TYPES);
        }
    }

    private String performVisionOcr(MultipartFile imageFile) throws IOException {
        log.info("Google Cloud Vision API 호출 - 파일명 : {}, 크기 : {}", imageFile.getOriginalFilename(), imageFile.getSize());

        // Google Cloud Vision API 클라이언트 초기화
        try (ImageAnnotatorClient vision = ImageAnnotatorClient.create()) {

            // 이미지 파일을 ByteString으로 변환
            ByteString imgBytes = ByteString.copyFrom(imageFile.getBytes());

            // 이미지 객체 생성
            Image img = Image.newBuilder().setContent(imgBytes).build();

            // 텍스트 감지 기능 설정
            Feature feat = Feature.newBuilder()
                .setType(Feature.Type.TEXT_DETECTION)
                .build();

            // 이미지 분석 요청 생성
            AnnotateImageRequest request = AnnotateImageRequest.newBuilder()
                .addFeatures(feat)
                .setImage(img)
                .build();

            // 배치 요청 실행
            BatchAnnotateImagesResponse response = vision.batchAnnotateImages(List.of(request));

            // 응답 처리
            List<AnnotateImageResponse> responses = response.getResponsesList();

            if (responses.isEmpty()) {
                log.warn("Vision API에서 응답을 받지 못했습니다.");
                return null;
            }

            AnnotateImageResponse res = responses.get(0);

            // 오류 check
            if (res.hasError()) {
                log.error("Vision API 오류 : {}", res.getError().getMessage());
                throw new RuntimeException("Vision API 오류  : " + res.getError().getMessage());
            }

            // 텍스트 추출
            TextAnnotation textAnnotation = res.getFullTextAnnotation();
            if (textAnnotation != null) {
                String extractedText = textAnnotation.getText();
                log.debug("추출된 전체 텍스트: {} ", extractedText);
                return extractedText;
            }

            log.warn("Vision API에서 텍스트를 감지하지 못했습니다.");
            return null;
        } catch (Exception e) {
            log.error("Google Cloud Vision API 호출 중 오류 발생", e);
            throw new IOException("Google Cloud Vision API 호출 실패",e);
        }
    }

    private String extractIsbnFromText(String text) {
        if (text == null || text.trim().isEmpty()) {
            return null;
        }

        // 텍스트 정규화 ( 개행 문자를 공백으로 변환, 여러 공백을 하나로 통합 )
        String normalizedText = text.replaceAll("\\s+", " ");

        log.debug("정규화된 텍스트 : {}", normalizedText);

        // ISBN 패턴 매칭
        Matcher matcher = ISBN_PATTERN.matcher(normalizedText);
        if (matcher.find()) {
            String rawIsbn = matcher.group();
            String isbn = rawIsbn.replaceAll("[^\\d]","");

            // ISBN-10 or ISBN-13 길이 검증
            if(isbn.length() == 10 || isbn.length() == 13) {
                log.info("유효한 ISBN 패턴 발견 : {} ( 원본 : {} )", isbn, rawIsbn);
                return isbn;
            }
        }

        // 추가적인 ISBN 패턴 검색 ( 숫자만으로 구성된 10자리 또는 13자리 )
        Pattern numericPattern = Pattern.compile("\\b(\\d{10}|\\d{13})\\b");
        Matcher numericMatcher = numericPattern.matcher(normalizedText);

        while (numericMatcher.find()) {
            String candidate = numericMatcher.group();
            if (candidate.length() == 10 || candidate.length() == 13) {
                // 간단한 ISBN 체크섬 검증
                if (isValidIsbnFormat(candidate)) {
                    log.info("숫자 패턴에서 유효한 ISBN 발견 : {} ", candidate);
                    return candidate;
                }
            }
        }

        log.warn("유효한 ISBN을 찾을 수 없습니다. 텍스트: {}", text);
        return null;
    }

    private boolean isValidIsbnFormat(String isbn) {
        // 기본적인 ISBN 형식 검증
        if (isbn.length() == 13) {
            return isbn.startsWith("978") || isbn.startsWith("979");
            // ISBN-13은 978 or 979로 시작해야함!
        } else if (isbn.length() == 10) {
            // ISBN-10은 모두 숫자이거나 마지막이 X일 수 있다.
            return isbn.matches("\\d{9}[\\dX]");
        }
        return false;
    }

    @Override
    public boolean isAvailable() {
        try {
            log.info("Google Cloud Vision API 가용성을 확인");

            // Vision API 클라이언트 생성 테스트
            try (ImageAnnotatorClient vision = ImageAnnotatorClient.create()) {
                // 클라이언트가 정상적으로 생성되면 사용 가능
                log.info("Google Cloud Vision API 클라이언트 생성 성공");
                return true;
            }

        } catch (Exception e) {
            log.warn("Google Cloud Vision API를 사용할 수 없습니다. : {}", e.getMessage());
            return false;
        }
    }

    @Override
    public int getPriority() {
        // 가장 높은 우선 순위 ( Vision API가 일반적으로 더 정확함 )
        return 1;
    }
}
