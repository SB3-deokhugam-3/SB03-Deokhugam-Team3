package com.sprint.deokhugam.domain.user.controller.api;

import com.sprint.deokhugam.domain.popularreview.dto.data.PopularReviewDto;
import com.sprint.deokhugam.domain.user.dto.data.UserDto;
import com.sprint.deokhugam.domain.user.dto.request.UserCreateRequest;
import com.sprint.deokhugam.domain.user.dto.request.UserLoginRequest;
import com.sprint.deokhugam.domain.user.dto.request.UserUpdateRequest;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "사용자 관리")
public interface UserApi {

    @PostMapping
    @Operation(
        summary = "회원가입",
        description = "새로운 사용자를 등록합니다.",
        operationId = "register"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201", description = "회원가입 성공",
            content = @Content(
                mediaType = "*/*",
                schema = @Schema(implementation = UserDto.class))
        ),
        @ApiResponse(
            responseCode = "409", description = "이메일 중복",
            content = @Content(
                mediaType = "*/*",
                schema = @Schema(implementation = UserDto.class))
        ),
        @ApiResponse(
            responseCode = "400", description = "잘못된 요청 (입력값 검증 실패)",
            content = @Content(
                mediaType = "*/*",
                schema = @Schema(implementation = UserDto.class))
        ),
        @ApiResponse(
            responseCode = "500", description = "서버 내부 오류",
            content = @Content(
                mediaType = "*/*",
                schema = @Schema(implementation = UserDto.class))
        )
    })
    ResponseEntity<UserDto> create(@RequestBody @Valid UserCreateRequest request);

    @PostMapping("/login")
    @Operation(
        summary = "로그인",
        description = "사용자 로그인을 처리합니다.",
        operationId = "login"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201", description = "로그인 성공",
            content = @Content(
                mediaType = "*/*",
                schema = @Schema(implementation = UserDto.class))
        ),
        @ApiResponse(
            responseCode = "401", description = "로그인 실패 (이메일 또는 비밀번호 불일치)",
            content = @Content(
                mediaType = "*/*",
                schema = @Schema(implementation = UserDto.class))
        ),
        @ApiResponse(
            responseCode = "400", description = "잘못된 요청 (입력값 검증 실패)",
            content = @Content(
                mediaType = "*/*",
                schema = @Schema(implementation = UserDto.class))
        ),
        @ApiResponse(
            responseCode = "500", description = "서버 내부 오류",
            content = @Content(
                mediaType = "*/*",
                schema = @Schema(implementation = UserDto.class))
        )
    })
    ResponseEntity<UserDto> login(@Valid @RequestBody UserLoginRequest userLoginRequest);

    @GetMapping("/{userId}")
    @Operation(
        summary = "사용자 상세 정보 조회",
        description = "사용자 ID로 상세 정보를 조회합니다.",
        operationId = "getUser",
        security = @SecurityRequirement(name = "CustomHeaderAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", description = "사용자 정보 조회 성공",
            content = @Content(
                mediaType = "*/*",
                schema = @Schema(implementation = UserDto.class))
        ),
        @ApiResponse(
            responseCode = "404", description = "사용자 정보 없음",
            content = @Content(
                mediaType = "*/*",
                schema = @Schema(implementation = UserDto.class))
        ),
        @ApiResponse(
            responseCode = "500", description = "서버 내부 오류",
            content = @Content(
                mediaType = "*/*",
                schema = @Schema(implementation = UserDto.class))
        )
    })
    ResponseEntity<UserDto> find(
        @PathVariable @Parameter(description = "사용자 ID", required = true) UUID userId
    );

    @DeleteMapping("/{userId}")
    @Operation(
        summary = "사용자 논리 삭제",
        description = "본인이 작성한 사용자을 논리적으로 삭제합니다.",
        operationId = "deleteUser",
        security = @SecurityRequirement(name = "CustomHeaderAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "사용자 삭제 성공"),
        @ApiResponse(responseCode = "404", description = "사용자 정보 없음"),
        @ApiResponse(responseCode = "403", description = "사용자 삭제 권한 없음"),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    ResponseEntity<UserDto> deleted(
        @PathVariable @Parameter(description = "사용자 ID", required = true) UUID userId
    );

    @DeleteMapping("/{userId}/hard")
    @Operation(
        summary = "사용자 물리 삭제",
        description = "사용자을 물리적으로 삭제합니다.",
        operationId = "permanentDeleteUser",
        security = @SecurityRequirement(name = "CustomHeaderAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "사용자 삭제 성공"),
        @ApiResponse(responseCode = "404", description = "사용자 정보 없음"),
        @ApiResponse(responseCode = "403", description = "사용자 삭제 권한 없음"),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    ResponseEntity<Void> hardDelete(
        @PathVariable @Parameter(description = "사용자 ID", required = true) UUID userId
    );

    @PatchMapping("/{userId}")
    @Operation(
        summary = "사용자 정보 수정",
        description = "사용자의 닉네임을 수정합니다.",
        operationId = "updateUser",
        security = @SecurityRequirement(name = "CustomHeaderAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", description = "사용자 정보 수정 성공",
            content = @Content(
                mediaType = "*/*",
                schema = @Schema(implementation = UserDto.class))
        ),
        @ApiResponse(
            responseCode = "400", description = "잘못된 요청 (입력값 검증 실패)",
            content = @Content(
                mediaType = "*/*",
                schema = @Schema(implementation = UserDto.class))
        ),
        @ApiResponse(
            responseCode = "404", description = "사용자 정보 없음",
            content = @Content(
                mediaType = "*/*",
                schema = @Schema(implementation = UserDto.class))
        ),
        @ApiResponse(
            responseCode = "403", description = "사용자 정보 수정 권한 없음",
            content = @Content(
                mediaType = "*/*",
                schema = @Schema(implementation = UserDto.class))
        ),
        @ApiResponse(
            responseCode = "500", description = "서버 내부 오류",
            content = @Content(
                mediaType = "*/*",
                schema = @Schema(implementation = UserDto.class))
        )
    })
    ResponseEntity<UserDto> update(
        @PathVariable @Parameter(description = "사용자 ID", required = true) UUID userId,
        @RequestBody @Valid UserUpdateRequest request
    );

    //후에 반환 dto 변경
    @GetMapping("/power")
    @Operation(
        summary = "인기 사용자 목록 조회",
        description = "기간별 인기 사용자 목록을 조회합니다",
        operationId = "getPopularUsers"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", description = "인기 사용자 목록 조회 성공",
            content = @Content(
                mediaType = "*/*",
                schema = @Schema(implementation = PopularReviewDto.class))
        ),
        @ApiResponse(
            responseCode = "400", description = "잘못된 요청 (랭킹 기간 오류, 정렬 방향 오류)",
            content = @Content(
                mediaType = "*/*",
                schema = @Schema(implementation = PopularReviewDto.class))
        ),
        @ApiResponse(
            responseCode = "500", description = "서버 내부 오류",
            content = @Content(
                mediaType = "*/*",
                schema = @Schema(implementation = PopularReviewDto.class))
        )
    })
    ResponseEntity<CursorPageResponse<PopularReviewDto>> getPopularUsers(
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

//        혹은,
//        @Parameter(
//            description = """
//                파워 유저 목록 조회용 DTO<br>
//                - **period**: 랭킹 기간 (DAILY, WEEKLY, MONTHLY, ALL_TIME, 기본값: DAILY)<br>
//                - **direction**: 정렬 방향 (ASC, DESC, 기본값: ASC)<br>
//                - **cursor**: 커서 페이지네이션을 커서<br>
//                - **after**: createdAt 기준 보조 커서<br>
//                - **limit**: 페이지 크기 (기본값: 50)
//                """
//        )@ModelAttribute @Valid PowerUserGetRequest request;
    );

}
