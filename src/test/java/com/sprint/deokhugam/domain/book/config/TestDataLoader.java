package com.sprint.deokhugam.domain.book.config;

import com.sprint.deokhugam.domain.book.entity.Book;
import com.sprint.deokhugam.domain.book.repository.BookRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;
import java.util.List;

@Configuration
public class TestDataLoader {

    private final BookRepository bookRepository;

    public TestDataLoader(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    // 애플리케이션 시작 시 더미 데이터 로드
    @PostConstruct
    public void loadDummyBooks() {
        bookRepository.deleteAll(); // 초기화
        bookRepository.saveAll(List.of(
            Book.builder()
                .title("Spring Boot Guide")
                .author("박스프링")
                .description("스프링 부트 정석 가이드")
                .publisher("웹 출판사")
                .publishedDate(LocalDate.of(2023, 6, 1))
                .isbn("9788987654321")
                .rating(4.5)
                .reviewCount(3L)
                .isDeleted(false)
                .build(),
            Book.builder()
                .title("Modern Java")
                .author("김자바")
                .description("자바 2025 가이드")
                .publisher("자바 전문가 출판사")
                .publishedDate(LocalDate.of(2022, 12, 15))
                .isbn("9788123456789")
                .rating(4.8)
                .reviewCount(10L)
                .isDeleted(false)
                .build()
        ));
    }
}