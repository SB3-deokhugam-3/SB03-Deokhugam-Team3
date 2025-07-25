package com.sprint.deokhugam.domain.book.controller.api;

import com.sprint.deokhugam.domain.api.dto.NaverBookDto;
import com.sprint.deokhugam.domain.book.dto.data.BookDto;
import com.sprint.deokhugam.domain.book.dto.request.BookCreateRequest;
import com.sprint.deokhugam.domain.book.dto.request.BookUpdateRequest;
import com.sprint.deokhugam.domain.book.exception.OcrException;
import com.sprint.deokhugam.domain.popularbook.dto.data.PopularBookDto;
import com.sprint.deokhugam.domain.popularbook.dto.request.PopularBookGetRequest;
import com.sprint.deokhugam.global.dto.response.CursorPageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.io.IOException;
import java.util.UUID;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;


@Tag(name = "도서 관리", description = "도서 관련 API")
public interface BookApi {

    @Operation(summary = "도서 등록", description = "새로운 도서를 등록합니다.")
    @ApiResponses({
        @ApiResponse(
            responseCode = "201",
            description = "도서 등록 성공",
            content = @Content(
                mediaType = "*/*",
                schema = @Schema(implementation = BookDto.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "잘못된 요청(입력값 검증 실패, ISBN 형식 오류 등)",
            content = @Content(
                mediaType = "*/*",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "409",
            description = "ISBN 중복",
            content = @Content(
                mediaType = "*/*",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "서버 내부 오류",
            content = @Content(
                mediaType = "*/*",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        )
    })
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseEntity<BookDto> create(
        @Parameter(description = "도서 정보")
        @Valid @RequestPart("bookData") BookCreateRequest bookData,

        @Parameter(description = "도서 썸네일 이미지",
            schema = @Schema(type = "string", format = "binary"))
        @RequestPart(value = "thumbnailImage", required = false) MultipartFile thumbnailImage)
        throws IOException;

    @Operation(summary = "도서 목록 조회", description = "검색 조건에 맞는 도서 목록을 조회합니다.")
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "도서 목록 조회 성공",
            content = @Content(
                mediaType = "*/*",
                array = @ArraySchema(schema = @Schema(implementation = BookDto.class))
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "잘못된 요청(정렬 기준 오류, 페이지네이션 파라미터 오류 등)",
            content = @Content(
                mediaType = "*/*",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "서버 내부 오류",
            content = @Content(
                mediaType = "*/*",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        )
    })
    @GetMapping
    ResponseEntity<CursorPageResponse<BookDto>> getBooks(
        @Parameter(description = "도서 제목| 저자 | ISBN", example = "자바의 정석")
        @RequestParam(required = false) String keyword,

        @Parameter(description = "정렬 기준(title| publishedDate | rating | reviewCount",
            example = "title")
        @RequestParam(defaultValue = "title") String orderBy,

        @Parameter(description = "정렬 방향 (ASC or DESC)",
            example = "DESC")
        @RequestParam(defaultValue = "DESC") String direction,

        @Parameter(description = "커서 페이지네이션 커서")
        @RequestParam(required = false) String cursor,

        @Parameter(description = "보조 커서(createdAt)")
        @RequestParam(required = false) Long after,

        @Parameter(description = "페이지 크기", example = "50")
        @RequestParam(defaultValue = "50") Integer limit
    );

    @Operation(summary = "도서 정보 상세 조회", description = "도서 상세 정보 조회")
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "도서 정보 조회 성공",
            content = @Content(
                mediaType = "*/*",
                schema = @Schema(implementation = BookDto.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "도서 정보 없음",
            content = @Content(
                mediaType = "*/*",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "서버 내부 오류",
            content = @Content(
                mediaType = "*/*",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        )
    })
    @GetMapping("/{bookId}")
    ResponseEntity<BookDto> getBook(
        @Parameter(description = "도서 ID", example = "9a4d6e2b-8f53-4c8c-a7c7-21d5079b8b37")
        @PathVariable UUID bookId);

    @Operation(summary = "ISBN으로 도서 정보 조회",
        description = "Naver API를 통해 ISBN으로 도서 정보를 조회합니다.")
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "도서 정보 조회 성공",
            content = @Content(
                mediaType = "*/*",
                schema = @Schema(implementation = NaverBookDto.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "잘못된 ISBN 형식",
            content = @Content(
                mediaType = "*/*",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "도서 정보 없음",
            content = @Content(
                mediaType = "*/*",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "서버 내부 오류",
            content = @Content(
                mediaType = "*/*",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        )
    })
    @GetMapping("/info")
    ResponseEntity<NaverBookDto> getBookInfoByIsbn(
        @Parameter(description = "ISBN 번호", example = "9788968481901")
        @RequestParam String isbn);

    @Operation(summary = "도서 정보 수정", description = "도서 정보를 수정합니다.")
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "도서 정보 수정 성공",
            content = @Content(
                mediaType = "*/*",
                schema = @Schema(implementation = BookDto.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "잘못된 요청(입력값 검증 실패, ISBN 형식 오류 등)",
            content = @Content(
                mediaType = "*/*",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "도서 정보 없음",
            content = @Content(
                mediaType = "*/*",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "409",
            description = "ISBN 중복",
            content = @Content(
                mediaType = "*/*",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "서버 내부 오류",
            content = @Content(
                mediaType = "*/*",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        )
    })
    @PatchMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE, path = "/{bookId}")
    ResponseEntity<BookDto> update(
        @Parameter(description = "도서 ID", example = "9a4d6e2b-8f53-4c8c-a7c7-21d5079b8b37")
        @PathVariable UUID bookId,

        @Parameter(description = "수정할 도서 정보")
        @Valid @RequestPart("bookData") BookUpdateRequest bookData,

        @Parameter(description = "수정할 도서 썸네일 이미지")
        @RequestPart(value = "thumbnailImage", required = false) MultipartFile thumbnailImage
    ) throws IOException;

    @Operation(summary = "이미지 기반 ISBN 인식",
        description = "도서 이미지를 통해 ISBN을 인식합니다.")
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "ISBN 인식 성공",
            content = @Content(
                mediaType = "*/*",
                schema = @Schema(implementation = String.class),
                examples = {
                    @ExampleObject(
                        name = "성공 예시",
                        summary = "ISBN 번호 예시",
                        value = "9788968481901"
                    )
                }
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "잘못된 이미지 형식 또는 OCR 인식 실패",
            content = @Content(
                mediaType = "*/*",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "서버 내부 오류",
            content = @Content(
                mediaType = "*/*",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        )
    })
    @PostMapping("/isbn/ocr")
    ResponseEntity<String> extractIsbnFromImage(
        @Parameter(description = "도서 이미지",
            schema = @Schema(type = "string", format = "binary"))
        @RequestPart(value = "image") MultipartFile image
    ) throws OcrException;

    @Operation(summary = "도서 논리 삭제",
        description = "도서를 논리적으로 삭제합니다.")
    @ApiResponses({
        @ApiResponse(
            responseCode = "204",
            description = "도서 삭제 성공"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "도서 정보 없음"
        ),
        @ApiResponse(
            responseCode = "500",
            description = "서버 내부 오류"
        )
    })
    @DeleteMapping("/{bookId}")
    ResponseEntity<Void> delete(
        @Parameter(description = "도서 ID",
            example = "9a4d6e2b-8f53-4c8c-a7c7-21d5079b8b37")
        @PathVariable UUID bookId);

    @Operation(summary = "도서 물리 삭제",
        description = "도서를 물리적으로 삭제합니다.")
    @ApiResponses({
        @ApiResponse(
            responseCode = "204",
            description = "도서 삭제 성공"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "도서 정보 없음"
        ),
        @ApiResponse(
            responseCode = "500",
            description = "서버 내부 오류"
        )
    })
    @DeleteMapping("/{bookId}/hard")
    ResponseEntity<Void> hardDelete(
        @Parameter(description = "도서 ID",
            example = "9a4d6e2b-8f53-4c8c-a7c7-21d5079b8b37")
        @PathVariable UUID bookId);

    @Operation(summary = "인기 도서 목록 조회",
        description = "기간별 인기 도서 목록을 조회합니다.")
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "인기 도서 목록 조회 성공",
            content = @Content(
                mediaType = "*/*",
                array = @ArraySchema(schema = @Schema(implementation = PopularBookDto.class))
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "잘못된 요청(랭킹 기간 오류, 정렬 방향 오류 등)",
            content = @Content(
                mediaType = "*/*",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "서버 내부 오류",
            content = @Content(
                mediaType = "*/*",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        )
    })
    @GetMapping("/popular")
    ResponseEntity<CursorPageResponse<PopularBookDto>> getPopularBooks(
        @Parameter(
            description = """
                인기 도서 목록 조회용 DTO<br>
                - **period**: 랭킹 기간 (DAILY, WEEKLY, MONTHLY, ALL_TIME, 기본값: DAILY)<br>
                - **direction**: 정렬 방향 (ASC, DESC, 기본값: ASC)<br>
                - **cursor**: 커서 페이지네이션을 위한 커서 값 (인기 도서 순위)<br>
                - **after**: createdAt 기준 보조 커서<br>
                - **limit**: 페이지 크기 (기본값: 50)
                """
        )
        @ModelAttribute PopularBookGetRequest request);
}
