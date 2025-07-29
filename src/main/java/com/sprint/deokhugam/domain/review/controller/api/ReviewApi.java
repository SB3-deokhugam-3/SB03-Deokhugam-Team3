package com.sprint.deokhugam.domain.review.controller.api;

import com.sprint.deokhugam.domain.popularreview.dto.data.PopularReviewDto;
import com.sprint.deokhugam.domain.review.dto.data.ReviewDto;
import com.sprint.deokhugam.domain.review.dto.request.ReviewCreateRequest;
import com.sprint.deokhugam.domain.review.dto.request.ReviewGetRequest;
import com.sprint.deokhugam.domain.review.dto.request.ReviewUpdateRequest;
import com.sprint.deokhugam.global.dto.response.CursorPageResponse;
import com.sprint.deokhugam.global.enums.PeriodType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.time.Instant;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "리뷰 관리")
public interface ReviewApi {

    @GetMapping
    @Operation(
        summary = "리뷰 목록 조회",
        description = "검색 조건에 맞는 리뷰 목록을 조회합니다.",
        operationId = "searchReviews",
        security = @SecurityRequirement(name = "CustomHeaderAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", description = "리뷰 목록 조회 성공",
            content = @Content(
                mediaType = "*/*",
                schema = @Schema(implementation = ReviewDto.class))
        ),
        @ApiResponse(
            responseCode = "400", description = "잘못된 요청 (정렬 방향 오류, 페이지네이션 파라미터 오류, 리뷰 ID 누락)",
            content = @Content(
                mediaType = "*/*",
                schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "500", description = "서버 내부 오류",
            content = @Content(
                mediaType = "*/*",
                schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    ResponseEntity<CursorPageResponse<ReviewDto>> findAll(
        @Parameter(
            description = """
                인기 리뷰 목록 조회용 DTO<br>
                - **period**: 랭킹 기간 (DAILY, WEEKLY, MONTHLY, ALL_TIME, 기본값: DAILY)<br>
                - **direction**: 정렬 방향 (ASC, DESC, 기본값: ASC)<br>
                - **cursor**: 커서 페이지네이션을 위한 커서<br>
                - **after**: createdAt 기준 보조 커서<br>
                - **limit**: 페이지 크기 (기본값: 50)
                """
        )
        @ModelAttribute @Valid ReviewGetRequest reviewGetRequest,
        @RequestHeader("Deokhugam-Request-User-ID") UUID requestUserId
    );

    @PostMapping
    @Operation(
        summary = "리뷰 등록",
        description = "새로운 리뷰를 등록합니다.",
        operationId = "createReview",
        security = @SecurityRequirement(name = "CustomHeaderAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201", description = "리뷰 등록 성공",
            content = @Content(
                mediaType = "*/*",
                schema = @Schema(implementation = ReviewDto.class))
        ),
        @ApiResponse(
            responseCode = "409", description = "이미 작성된 리뷰 존재",
            content = @Content(
                mediaType = "*/*",
                schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "400", description = "잘못된 요청 (입력값 검증 실패)",
            content = @Content(
                mediaType = "*/*",
                schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "404", description = "리뷰 정보 없음",
            content = @Content(
                mediaType = "*/*",
                schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "500", description = "서버 내부 오류",
            content = @Content(
                mediaType = "*/*",
                schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    ResponseEntity<ReviewDto> createReview(@RequestBody @Valid ReviewCreateRequest request);


    @GetMapping("/{reviewId}")
    @Operation(
        summary = "리뷰 상세 정보 조회",
        description = "리뷰 ID로 상세 정보를 조회합니다.",
        operationId = "getReview",
        security = @SecurityRequirement(name = "CustomHeaderAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", description = "리뷰 정보 조회 성공",
            content = @Content(
                mediaType = "*/*",
                schema = @Schema(implementation = ReviewDto.class))
        ),
        @ApiResponse(
            responseCode = "400", description = "잘못된 요청 (요청자 ID 누락)",
            content = @Content(
                mediaType = "*/*",
                schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "404", description = "리뷰 정보 없음",
            content = @Content(
                mediaType = "*/*",
                schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "500", description = "서버 내부 오류",
            content = @Content(
                mediaType = "*/*",
                schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    ResponseEntity<ReviewDto> getReview(
        @PathVariable @Parameter(description = "리뷰 ID", required = true) UUID reviewId,
        @RequestHeader("Deokhugam-Request-User-ID")
        @Parameter(description = "요청자 ID", required = true) UUID requestUserId
    );

    @DeleteMapping("/{reviewId}")
    @Operation(
        summary = "리뷰 논리 삭제",
        description = "본인이 작성한 리뷰을 논리적으로 삭제합니다.",
        operationId = "deleteReview",
        security = @SecurityRequirement(name = "CustomHeaderAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "리뷰 삭제 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 (요청자 ID 누락)"),
        @ApiResponse(responseCode = "404", description = "리뷰 정보 없음"),
        @ApiResponse(responseCode = "403", description = "리뷰 삭제 권한 없음"),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    ResponseEntity<Void> deleteReview(
        @PathVariable @Parameter(description = "리뷰 ID", required = true) UUID reviewId,
        @RequestHeader("Deokhugam-Request-User-ID")
        @Parameter(description = "요청자 ID", required = true) UUID requestUserId
    );

    @DeleteMapping("/{reviewId}/hard")
    @Operation(
        summary = "리뷰 물리 삭제",
        description = "본인이 작성한 리뷰을 물리적으로 삭제합니다.",
        operationId = "permanentDeleteReview",
        security = @SecurityRequirement(name = "CustomHeaderAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "리뷰 삭제 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 (요청자 ID 누락)"),
        @ApiResponse(responseCode = "404", description = "리뷰 정보 없음"),
        @ApiResponse(responseCode = "403", description = "리뷰 삭제 권한 없음"),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    ResponseEntity<Void> hardDeleteReview(
        @PathVariable @Parameter(description = "리뷰 ID", required = true) UUID reviewId,
        @RequestHeader("Deokhugam-Request-User-ID")
        @Parameter(description = "요청자 ID", required = true) UUID requestUserId
    );

    @PatchMapping("/{reviewId}")
    @Operation(
        summary = "리뷰 수정",
        description = "본인이 작성한 리뷰을 수정합니다.",
        operationId = "updateReview",
        security = @SecurityRequirement(name = "CustomHeaderAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", description = "리뷰 수정 성공",
            content = @Content(
                mediaType = "*/*",
                schema = @Schema(implementation = ReviewDto.class))
        ),
        @ApiResponse(
            responseCode = "400", description = "잘못된 요청 (입력값 검증 실패, 요청자 ID 누락)",
            content = @Content(
                mediaType = "*/*",
                schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "404", description = "리뷰 정보 없음",
            content = @Content(
                mediaType = "*/*",
                schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "403", description = "리뷰 수정 권한 없음",
            content = @Content(
                mediaType = "*/*",
                schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "500", description = "서버 내부 오류",
            content = @Content(
                mediaType = "*/*",
                schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    ResponseEntity<ReviewDto> updateReview(
        @PathVariable @Parameter(description = "리뷰 ID", required = true) UUID reviewId,
        @Parameter(description = "요청자 ID", required = true)
        @RequestHeader("Deokhugam-Request-User-ID") UUID requestUserId,
        @RequestBody @Valid ReviewUpdateRequest request
    );

    @GetMapping("/popular")
    @Operation(
        summary = "인기 리뷰 목록 조회",
        description = "기간별 인기 리뷰 목록을 조회합니다",
        operationId = "getPopularReviews"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", description = "인기 리뷰 목록 조회 성공",
            content = @Content(
                mediaType = "*/*",
                schema = @Schema(implementation = PopularReviewDto.class))
        ),
        @ApiResponse(
            responseCode = "400", description = "잘못된 요청 (랭킹 기간 오류, 정렬 방향 오류)",
            content = @Content(
                mediaType = "*/*",
                schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "500", description = "서버 내부 오류",
            content = @Content(
                mediaType = "*/*",
                schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    ResponseEntity<CursorPageResponse<PopularReviewDto>> getPopularReviews(
        @Parameter(description = "랭킹 기간")
        @RequestParam(defaultValue = "DAILY") PeriodType period,

        @Parameter(description = "정렬 방향")
        @RequestParam(defaultValue = "ASC") String direction,

        @Parameter(description = "커서 페이지네이션 커서")
        @RequestParam(required = false) String cursor,

        @Parameter(description = "보조 커서(createdAt)")
        @RequestParam(required = false) Instant after,

        @Parameter(description = "페이지 크기")
        @RequestParam(defaultValue = "50") @Min(1) @Max(100) int limit
    );
}
