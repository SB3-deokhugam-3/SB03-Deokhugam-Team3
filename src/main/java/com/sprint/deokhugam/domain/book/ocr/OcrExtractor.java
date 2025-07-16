package com.sprint.deokhugam.domain.book.ocr;

import com.sprint.deokhugam.domain.book.exception.OcrException;
import org.springframework.web.multipart.MultipartFile;

public interface OcrExtractor {
    /**
     * 이미지에서 ISBN을 추출
     * @param imageFile 이미지 파일
     * @return 추출된 ISBN
     * @throws OcrException OCR 처리 중 오류 발생 시
     *  */
    String extractIsbn(MultipartFile imageFile) throws OcrException;

    /**
     * 해당 OCR 구현체가  사용 가능한지 확인
     * @return 사용 가능 여부
     * */
    boolean isAvailable();

    /**
     * OCR 구현체의 우선순위를 반환
     * 낮은 값일수록 높은 우선순위
     * @return 우선순위
     * */
    int getPriority();
}
