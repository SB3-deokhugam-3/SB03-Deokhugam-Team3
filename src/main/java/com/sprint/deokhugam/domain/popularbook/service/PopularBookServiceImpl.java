package com.sprint.deokhugam.domain.popularbook.service;

import com.sprint.deokhugam.domain.book.storage.s3.S3Storage;
import com.sprint.deokhugam.domain.popularbook.dto.data.PopularBookDto;
import com.sprint.deokhugam.domain.popularbook.dto.request.PopularBookGetRequest;
import com.sprint.deokhugam.domain.popularbook.entity.PopularBook;
import com.sprint.deokhugam.domain.popularbook.exception.InvalidSortDirectionException;
import com.sprint.deokhugam.domain.popularbook.repository.PopularBookRepository;
import com.sprint.deokhugam.global.dto.response.CursorPageResponse;
import com.sprint.deokhugam.global.period.PeriodType;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PopularBookServiceImpl implements PopularBookService {

    private final PopularBookRepository popularBookRepository;
    private final S3Storage s3Storage;

    private static final Set<String> VALID_SORT_DIRECTION = Set.of("ASC", "DESC");

    /**
     * 요청된 기간과 정렬 기준에 따라 인기 도서 목록을 조회합니다.
     *
     * <p>지원하는 기간 유형(PeriodType)에는 DAILY, WEEKLY, MONTHLY, ALL_TIME이 있으며,
     * 정렬 기준은 DESC(내림차순), ASC(오름차순)를 지원합니다.
     * 커서 기반 페이지네이션 방식으로, cursor와 after 값을 기준으로 데이터를 조회합니다.</p>
     *
     * @param request 인기 도서 조회 요청 객체
     *                - period: 기간 유형 (예: DAILY, WEEKLY)
     *                - direction: 정렬 방향 (예: DESC)
     *                - cursor: 커서 기반 페이지네이션 기준 값
     *                - after: 특정 시점 이후 데이터를 조회할 때 사용
     *                - limit: 조회할 최대 도서 수
     * @return 인기 도서 목록과 페이지네이션 정보를 포함하는 {@link CursorPageResponse}
     */
    @Override
    @Transactional(readOnly = true)
    public CursorPageResponse<PopularBookDto> getPopularBooks(PopularBookGetRequest request) {
        // asc, desc 등 소문자로 입력했을 때도 유효하도록
        String sortDirection = request.direction().toUpperCase();
        PeriodType period = request.period();
        int limit = request.limit();

        if (!VALID_SORT_DIRECTION.contains(sortDirection)) {
            throw new InvalidSortDirectionException(sortDirection);
        }

        List<PopularBookDto> popularBooks = popularBookRepository.findAllByRequest(request);

        // cursor
        boolean hasNext = popularBooks.size() > limit;

        String nextCursor;
        String nextAfter;

        if (hasNext) {
            popularBooks = popularBooks.subList(0, limit);
            PopularBookDto lastBook = popularBooks.get(limit - 1);
            nextCursor = lastBook.getRank().toString();
            nextAfter = lastBook.getCreatedAt().toString();
        } else {
            nextCursor = null;
            nextAfter = null;
        }

        List<PopularBookDto> popularBookDtos = popularBooks.stream()
            .peek(dto -> {
                if (dto.getThumbnailUrl() != null) {
                    String thumbnailUrl = s3Storage.generatePresignedUrl(dto.getThumbnailUrl());
                    dto.updateThumbnailUrl(thumbnailUrl);
                }
            })
            .toList();

        long totalElements = popularBookRepository.countByPeriod(period);

        return new CursorPageResponse<>(popularBookDtos, nextCursor, nextAfter,
            limit, totalElements, hasNext);
    }
}
