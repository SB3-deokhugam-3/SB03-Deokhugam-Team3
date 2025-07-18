package com.sprint.deokhugam.fixture;

import com.sprint.deokhugam.domain.book.dto.data.BookDto;
import com.sprint.deokhugam.domain.book.dto.request.BookCreateRequest;
import com.sprint.deokhugam.domain.book.dto.request.BookUpdateRequest;
import com.sprint.deokhugam.domain.book.entity.Book;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.test.util.ReflectionTestUtils;

public class BookFixture {

    public static Book createBookEntity(String title, String author, String description, String publisher,
        LocalDate publishedDate, String isbn, String thumbnailUrl, Double rating, Long reviewCount) {
        return Book.builder()
            .title(title)
            .author(author)
            .description(description)
            .publisher(publisher)
            .publishedDate(publishedDate)
            .isbn(isbn)
            .thumbnailUrl(thumbnailUrl)
            .rating(rating)
            .reviewCount(reviewCount)
            .isDeleted(false)
            .build();
    }

    public static BookCreateRequest createRequest(String title, String author, String description,
        String publisher, LocalDate publishedDate, String isbn) {
        return BookCreateRequest.builder()
            .title(title)
            .author(author)
            .description(description)
            .publisher(publisher)
            .publishedDate(publishedDate)
            .isbn(isbn)
            .build();
    }

    public static BookDto createBookDto(UUID id, String title, String author, String description,
        String publisher, LocalDate publishedDate, String isbn, String thumbnailUrl,
        Long reviewCount, Double rating, Instant createdAt, Instant updatedAt) {
        return BookDto.builder()
            .id(id)
            .title(title)
            .author(author)
            .description(description)
            .publisher(publisher)
            .publishedDate(publishedDate)
            .isbn(isbn)
            .thumbnailUrl(thumbnailUrl)
            .reviewCount(reviewCount)
            .rating(rating)
            .createdAt(createdAt)
            .updatedAt(updatedAt)
            .build();
    }

    public static BookUpdateRequest createUpdateRequest(String title, String author, String description,
        String publisher, LocalDate publishedDate) {

        return BookUpdateRequest.builder()
            .title(title)
            .author(author)
            .description(description)
            .publisher(publisher)
            .publishedDate(publishedDate)
            .build();
    }

    public static List<Book> createTestBooks() {
        Instant now = Instant.now();

        // 첫 번째 Book 생성 및 createdAt 설정
        Book book1 = createBookEntity("Super Hot Day", "김현기", "덥다 더워 덥다 더워",
            "3팀", LocalDate.of(2023, 1, 1), "9788123456789", null,
            4.5, 100L);

        // ReflectionTestUtils를 사용하여 BaseEntity의 필드 설정
        ReflectionTestUtils.setField(book1, "id", UUID.randomUUID());
        ReflectionTestUtils.setField(book1, "createdAt", now.minusSeconds(3600));
        ReflectionTestUtils.setField(book1, "updatedAt", now.minusSeconds(1800));

        // 두 번째 Book 생성 및 createdAt 설정
        Book book2 = createBookEntity("Spring Boot Guide", "박스프링", "스프링 부트 가이드",
            "웹출판사", LocalDate.of(2023, 6, 1), "9788987654321", null,
            4.8, 200L);

        ReflectionTestUtils.setField(book2, "id", UUID.randomUUID());
        ReflectionTestUtils.setField(book2, "createdAt", now.minusSeconds(7200));
        ReflectionTestUtils.setField(book2, "updatedAt", now.minusSeconds(3600));

        // 세 번째 Book 생성 및 createdAt 설정
        Book book3 = createBookEntity("Database Design", "이데이터", "데이터베이스 설계",
            "DB출판사", LocalDate.of(2023, 3, 1), "9788555666777", null,
            4.2, 50L);

        ReflectionTestUtils.setField(book3, "id", UUID.randomUUID());
        ReflectionTestUtils.setField(book3, "createdAt", now.minusSeconds(10800));
        ReflectionTestUtils.setField(book3, "updatedAt", now.minusSeconds(5400));

        return List.of(book1, book2, book3);
    }

    public static List<BookDto> createTestBookDtos() {
        Instant now = Instant.now();
        return List.of(
            createBookDto(UUID.randomUUID(), "Super Hot Day", "김현기", "덥다 더워 덥다 더워",
                "3팀", LocalDate.of(2023, 1, 1), "9788123456789",
                null, 100L, 4.5, now.minusSeconds(3600),
                now.minusSeconds(1800)),
            createBookDto(UUID.randomUUID(), "Spring Boot Guide", "박스프링", "스프링 부트 가이드",
                "웹출판사", LocalDate.of(2023, 6, 1), "9788987654321",
                null, 200L, 4.8, now.minusSeconds(7200),
                now.minusSeconds(3600)),
            createBookDto(UUID.randomUUID(), "Database Design", "이데이터", "데이터베이스 설계",
                "DB출판사", LocalDate.of(2023, 3, 1), "9788555666777",
                null, 50L, 4.2, now.minusSeconds(10800),
                now.minusSeconds(5400))
        );
    }
}
