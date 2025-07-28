package com.sprint.deokhugam.domain.notification.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sprint.deokhugam.domain.book.entity.Book;
import com.sprint.deokhugam.domain.notification.dto.data.NotificationDto;
import com.sprint.deokhugam.domain.notification.dto.request.NotificationGetRequest;
import com.sprint.deokhugam.domain.notification.entity.Notification;
import com.sprint.deokhugam.domain.notification.exception.InvalidNotificationRequestException;
import com.sprint.deokhugam.domain.notification.mapper.NotificationMapper;
import com.sprint.deokhugam.domain.notification.repository.NotificationRepository;
import com.sprint.deokhugam.domain.review.entity.Review;
import com.sprint.deokhugam.domain.user.entity.User;
import com.sprint.deokhugam.domain.user.exception.InvalidUserRequestException;
import com.sprint.deokhugam.global.dto.response.CursorPageResponse;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private NotificationMapper notificationMapper;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    private User mockUser1;
    private User mockUser2;
    private Book mockBook;
    private Review mockReview;
    private Instant now;

    @BeforeEach
    void setUp() {
        now = Instant.now();

        mockUser1 = User.builder()
            .email("test@example.com")
            .nickname("tester")
            .password("password123")
            .build();
        ReflectionTestUtils.setField(mockUser1, "id", UUID.randomUUID());

        mockUser2 = User.builder()
            .email("test@example.com")
            .nickname("tester")
            .password("password123")
            .build();
        ReflectionTestUtils.setField(mockUser2, "id", UUID.randomUUID());

        mockBook = Book.builder()
            .title("테스트책")
            .author("작가")
            .publisher("출판사")
            .publishedDate(now.atZone(java.time.ZoneId.systemDefault()).toLocalDate())
            .description("책 설명")
            .isbn("1234567890123")
            .rating(4.0)
            .build();

        mockReview = Review.builder()
            .rating(5)
            .content("좋은 책입니다.")
            .user(mockUser1)
            .book(mockBook)
            .build();
        ReflectionTestUtils.setField(mockUser1, "id", UUID.randomUUID());
    }

    @Test
    void 정상_요청_시_알림_목록을_반환한다() {
        // given
        UUID userId = UUID.randomUUID();
        UUID notificationId = UUID.randomUUID();
        UUID reviewId = UUID.randomUUID();
        Instant now = Instant.now();
        int limit = 3;

        Notification entity = Notification.builder()
            .content("테스트 알림")
            .build();

        NotificationDto dto = NotificationDto.builder()
            .id(notificationId)
            .userId(userId)
            .reviewId(reviewId)
            .content("테스트 알림")
            .confirmed(false)
            .createdAt(now)
            .updatedAt(now)
            .build();

        CursorPageResponse<Notification> fakeResponse = new CursorPageResponse<>(
            List.of(entity),
            "nextCursor",
            now.toString(),
            1,
            null,
            true
        );

        NotificationGetRequest request = new NotificationGetRequest(
            userId,
            null,
            null,
            now,
            limit
        );

        when(notificationRepository.findByUserIdWithCursor(userId, now, null, limit))
            .thenReturn(fakeResponse);
        when(notificationMapper.toDto(entity)).thenReturn(dto);

        // when
        CursorPageResponse<NotificationDto> response = notificationService.getNotifications(
            request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.content()).hasSize(1);
        assertThat(response.content().get(0).content()).isEqualTo("테스트 알림");
        assertThat(response.nextCursor()).isEqualTo("nextCursor");
        assertThat(response.hasNext()).isTrue();
    }

    @Test
    void markAllAsRead_호출되면_Repository메서드가_정확히_실행된다() {
        // given
        UUID userId = UUID.randomUUID();

        // when
        notificationService.markAllAsRead(userId);

        // then
        verify(notificationRepository, times(1)).markAllAsReadByUserId(userId);
    }

    @Test
    void 알림_생성_성공() {
        // given
        String content = "새로운 알림";
        boolean isConfirmed = false;
        UUID userId = mockUser2.getId();
        UUID reviewId = mockReview.getId();

        Notification fakeNotification = Notification.builder()
            .user(mockUser2)
            .review(mockReview)
            .content(content)
            .isConfirmed(isConfirmed)
            .build();

        NotificationDto expectedDto = NotificationDto.builder()
            .userId(userId)
            .reviewId(reviewId)
            .content(content)
            .confirmed(isConfirmed)
            .createdAt(now)
            .updatedAt(now)
            .build();

        when(notificationRepository.save(any(Notification.class))).thenReturn(fakeNotification);
        when(notificationMapper.toDto(fakeNotification)).thenReturn(expectedDto);

        // when
        Optional<NotificationDto> result = notificationService.create(mockUser2, mockReview, content,
            isConfirmed);

        // then
        assertThat(result).isPresent();
        NotificationDto dto = result.get();

        assertThat(dto.content()).isEqualTo(content);
        assertThat(dto.confirmed()).isFalse();

        verify(notificationRepository).save(any(Notification.class));
        verify(notificationMapper).toDto(fakeNotification);
    }

    @Test
    void 자기_글에_대한_알림은_생성되지_않는다() {
        // given
        // mockUser가 mockReview 작성자와 같음 (자기 글)
        String content = "자기 글 알림";
        boolean isConfirmed = false;

        // when
        Optional<NotificationDto> result = notificationService.create(mockUser1, mockReview, content, isConfirmed);

        // then
        assertThat(result).isEmpty();
        verify(notificationRepository, times(0)).save(any(Notification.class));
        verify(notificationMapper, times(0)).toDto(any(Notification.class));
    }

    @Test
    void user값이_null이면_알림_생성_실패한다() {
        // when & then
        assertThatThrownBy(() -> notificationService.create(null, mockReview, "내용", false))
            .isInstanceOf(InvalidUserRequestException.class)
            .hasMessageContaining("null 값이 들어왔습니다.");
    }

    @Test
    void review값이_null이면_알림_생성_실패한다() {
        // when & then
        assertThatThrownBy(() -> notificationService.create(mockUser1, null, "내용", false))
            .isInstanceOf(InvalidUserRequestException.class)
            .hasMessageContaining("null 값이 들어왔습니다.");
    }

    @Test
    void content값이_null이면_알림_생성_실패한다() {
        // when & then
        assertThatThrownBy(() -> notificationService.create(mockUser1, mockReview, null, false))
            .isInstanceOf(InvalidUserRequestException.class)
            .hasMessageContaining("null 값이 들어왔습니다.");
    }

    @Test
    void 알림_ID로_확인여부를_업데이트한다() {
        // given
        UUID notificationId = UUID.randomUUID();

        Notification mockNotification = Notification.builder()
            .user(mockUser1)
            .review(mockReview)
            .content("알림 내용")
            .isConfirmed(false)
            .build();

        when(notificationRepository.findById(notificationId))
            .thenReturn(Optional.of(mockNotification));

        // when
        notificationService.updateNotification(notificationId);

        // then
        verify(notificationRepository).findById(notificationId);
        assertThat(mockNotification.isConfirmed()).isTrue();
    }

    @Test
    void 존재하지_않는_알림ID면_예외를_던진다() {
        // given
        UUID fakeId = UUID.randomUUID();
        when(notificationRepository.findById(fakeId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> notificationService.updateNotification(fakeId))
            .isInstanceOf(InvalidNotificationRequestException.class)
            .hasMessageContaining("해당 알림은 존재하지 않습니다.");
    }
}