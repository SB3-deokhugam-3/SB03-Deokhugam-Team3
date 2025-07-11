package com.sprint.deokhugam.domain.book.service;

import com.sprint.deokhugam.domain.book.dto.data.BookDto;
import com.sprint.deokhugam.domain.book.entity.Book;
import com.sprint.deokhugam.domain.book.mapper.BookMapper;
import com.sprint.deokhugam.domain.book.repository.BookRepository;
import com.sprint.deokhugam.global.dto.response.CursorPageResponse;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class BookServiceImpl implements BookService{

    private final BookMapper bookMapper;
    private final BookRepository bookRepository;

    @Override
    public CursorPageResponse<BookDto> getBooks(
        String keyword,
        String orderBy,
        String direction,
        String cursor,
        Instant after,
        Integer limit
    ) {
       // 기본값 설정
       String sortBy = orderBy != null ? orderBy : "title";
       String sortDirection = direction != null ? direction : "DESC";
       int pageSize = limit != null ? limit : 50;

       // 정렬 조건 생성
        Sort sort = createSort(sortBy, sortDirection);
        Pageable pageable = PageRequest.of(0,pageSize + 1, sort);

        List<Book> books;

        // 커서 기반 페이지네이션
        if (after != null) {
            books = bookRepository.findBooksWithKeywordAndCursor(keyword, after, pageable);
        } else {
            books = bookRepository.findBooksWithKeyword(keyword, pageable);
        }

        // 총 개수 조회
        long totalElements = bookRepository.countBooksWithKeyword(keyword);

        // 다음 페이지 존재 여부 확인
        boolean hasNext = books.size() > pageSize;
        if( hasNext ) {
            books = books.subList(0, pageSize);
        }

        // Next 커서 생성
        String nextCursor = null;
        Long nextIdAfter = null;

        if (hasNext && !books.isEmpty()) {
            Book lastBook = books.get(books.size() -1);
            nextCursor = lastBook.getCreatedAt().toString();
            nextIdAfter = lastBook.getCreatedAt().toEpochMilli();
        }

       return new CursorPageResponse<>(
           books.stream().map(bookMapper::toBookDto).toList(),
           nextCursor,
           nextIdAfter,
           books.size(),
           totalElements,
           hasNext
       );
    }

    private Sort createSort(String orderBy, String direction) {
        Sort.Direction sortDirection = "ASC".equals(direction) ?
            Sort.Direction.ASC : Sort.Direction.DESC;

        // 주 정렬 조건 + 생성시간 보조 정렬
        return Sort.by(sortDirection, orderBy)
            .and(Sort.by(Sort.Direction.DESC, "createdAt"));
    }
}
