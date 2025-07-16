package com.sprint.deokhugam.domain.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.deokhugam.domain.api.dto.NaverBookDto;
import com.sprint.deokhugam.domain.api.dto.NaverBookItem;
import com.sprint.deokhugam.domain.book.exception.BookInfoNotFoundException;
import com.sprint.deokhugam.domain.book.exception.InvalidIsbnException;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Base64;
import java.util.Base64.Encoder;
import java.util.List;
import javax.imageio.ImageIO;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Component
@AllArgsConstructor
public class NaverBookInfoProvider implements BookInfoProvider {

    private final WebClient naverApiClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public NaverBookDto fetchInfoByIsbn(String isbn) {
        log.debug("[BookInfoProvider] 도서 정보 조회 요청- isbn: {}", isbn);

        if (!isbn.matches("\\d{10}|\\d{13}")) {
            throw new InvalidIsbnException(isbn);
        }

        NaverBookItem item = parseItemsFromApi(isbn);

        log.info("[BookInfoProvider] 도서 정보 조회 성공: item: {}", item);

        String author = item.author().replace("^", ",");
        LocalDate publishedDate;
        try {
            publishedDate = LocalDate.parse(item.pubDate(), DateTimeFormatter.ofPattern("yyyyMMdd"));
        } catch (DateTimeParseException e) {
            log.warn("[BookInfoProvider] pubDate 파싱 실패: {}", item.pubDate(), e);
            publishedDate = null;
        }
        String thumbnailImage = imageToBase64(item.image());

        return NaverBookDto.builder()
            .title(item.title())
            .author(author)
            .description(item.description())
            .publisher(item.publisher())
            .publishedDate(publishedDate)
            .isbn(item.isbn())
            .thumbnailImage(thumbnailImage)
            .build();
    }

    private NaverBookItem parseItemsFromApi(String isbn) {
        try {
            String response = naverApiClient.get()
                .uri(uriBuilder -> uriBuilder
                    .path("/book_adv.json")
                    .queryParam("d_isbn", isbn)
                    .build())
                .retrieve()
                .bodyToMono(String.class)
                .block();  // 실제 HTTP 호출 발생

            JsonNode root = objectMapper.readTree(response);

            // items 배열만 꺼내기
            JsonNode itemNode = root.path("items");

            // itemNode -> List<NaverBookItem>
            List<NaverBookItem> items = objectMapper.readerForListOf(NaverBookItem.class)
                .readValue(itemNode);

            // ISBN에 대한 도서 정보가 없는 경우
            if (items.isEmpty()) {
                throw new BookInfoNotFoundException(isbn);
            }

            return items.get(0);
        } catch (BookInfoNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("[BookInfoProvider] Naver API 통신 중 오류 발생- isbn: {} message: {}", isbn,
                e.getMessage());
            throw new RuntimeException("Naver API 통신 실패", e);
        }
    }

    protected String imageToBase64(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            return "";
        }

        try {
            URL url = new URL(imageUrl);
            BufferedImage thumbnailImage = ImageIO.read(url);
            if (thumbnailImage == null) {
                log.warn("[BookInfoProvider] 이미지를 읽을 수 없음: {}", imageUrl);
                return "";
            }

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            // URL에서 확장자 추출 또는 기본값 사용
            String format = imageUrl.substring(imageUrl.lastIndexOf('.') + 1).toLowerCase();
            if (!format.matches("jpg|jpeg|png|gif")) {
                format = "jpg";
            }
            ImageIO.write(thumbnailImage, format, bos);
            Encoder encoder = Base64.getEncoder();

            return encoder.encodeToString(bos.toByteArray());
        } catch (Exception e) {
            log.error("파일 변환 중 에러 발생- e: {}", e.getMessage());
            throw new RuntimeException("파일 변환 중 에러 발생", e);
        }
    }
}
