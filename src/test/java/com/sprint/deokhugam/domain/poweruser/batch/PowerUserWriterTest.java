package com.sprint.deokhugam.domain.poweruser.batch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.sprint.deokhugam.domain.poweruser.batch.writer.PowerUserWriter;
import com.sprint.deokhugam.domain.poweruser.entity.PowerUser;
import com.sprint.deokhugam.domain.poweruser.service.PowerUserService;
import com.sprint.deokhugam.domain.user.entity.User;
import com.sprint.deokhugam.global.enums.PeriodType;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.item.Chunk;

@ExtendWith(MockitoExtension.class)
@DisplayName("PowerUserWriter 테스트")
class PowerUserWriterTest {

    @Mock
    private PowerUserService powerUserService;

    @InjectMocks
    private PowerUserWriter writer;

    @Test
    void write_정상_저장() throws Exception {
        // given
        List<PowerUser> powerUsers = createTestPowerUsers();
        Chunk<PowerUser> chunk = new Chunk<>(powerUsers);

        // when
        writer.write(chunk);

        // then
        verify(powerUserService).replacePowerUsers(powerUsers);
    }

    @Test
    void write_빈_청크_처리() throws Exception {
        // given
        Chunk<PowerUser> emptyChunk = new Chunk<>(Collections.emptyList());

        // when
        writer.write(emptyChunk);

        // then
        verify(powerUserService, never()).replacePowerUsers(any());
    }

    @Test
    void write_예외_발생_시_전파() throws Exception {
        // given
        List<PowerUser> powerUsers = createTestPowerUsers();
        Chunk<PowerUser> chunk = new Chunk<>(powerUsers);
        doThrow(new RuntimeException("저장 실패"))
            .when(powerUserService).replacePowerUsers(any());

        // when
        Throwable thrown = catchThrowable(() -> writer.write(chunk));

        // then
        assertThat(thrown).isInstanceOf(RuntimeException.class);
        assertThat(thrown.getMessage()).isEqualTo("저장 실패");
    }

    private List<PowerUser> createTestPowerUsers() {
        User testUser1 = User.builder()
            .email("test1@example.com")
            .nickname("테스트유저1")
            .password("password")
            .build();

        User testUser2 = User.builder()
            .email("test2@example.com")
            .nickname("테스트유저2")
            .password("password")
            .build();

        PowerUser powerUser1 = PowerUser.builder()
            .user(testUser1)
            .period(PeriodType.DAILY)
            .rank(1L)
            .score(100.0)
            .reviewScoreSum(80.0)
            .likeCount(10L)
            .commentCount(5L)
            .build();

        PowerUser powerUser2 = PowerUser.builder()
            .user(testUser2)
            .period(PeriodType.DAILY)
            .rank(2L)
            .score(90.0)
            .reviewScoreSum(70.0)
            .likeCount(8L)
            .commentCount(4L)
            .build();

        return List.of(powerUser1, powerUser2);
    }
}