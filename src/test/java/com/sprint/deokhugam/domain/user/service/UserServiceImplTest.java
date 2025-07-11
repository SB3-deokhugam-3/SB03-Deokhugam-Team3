package com.sprint.deokhugam.domain.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

import com.sprint.deokhugam.domain.user.dto.data.UserDto;
import com.sprint.deokhugam.domain.user.dto.request.UserCreateRequest;
import com.sprint.deokhugam.domain.user.entity.User;
import com.sprint.deokhugam.domain.user.mapper.UserMapper;
import com.sprint.deokhugam.domain.user.repository.UserRepository;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void 회원가입을_하면_회원이_저장된다() {
        // given
        UserCreateRequest request = new UserCreateRequest("userEmail@test.com", "testUser", "test1234!");

        given(userRepository.existsByEmail(anyString())).willReturn(false);
        given(userMapper.toEntity(any())).willReturn(new User("userEmail@test.com", "testUser", "test1234!"));
        given(userRepository.save(any(User.class))).willReturn(new User("userEmail@test.com", "testUser", "test1234!"));
        given(userMapper.toDto(any(User.class))).willReturn(
                UserDto.builder()
                        .email("userEmail@test.com")
                        .nickname("testUser")
                        .id(UUID.randomUUID())
                        .build()
        );

        // when
        UserDto result = userService.createUser(request);

        // then
        assertThat(result).isNotNull();
        assertThat(result.nickname()).isEqualTo("testUser");
        assertThat(result.email()).isEqualTo("userEmail@test.com");
    }

    @Test
    void 중복_이메일로_회원가입_시도_시_회원가입을_실패한다() {
        // given
        UserCreateRequest request = new UserCreateRequest("duplicate@example.com", "testuser",
                "password");

        //request.email 존재한다고 가정 ->  repo true 반환
        given(userRepository.existsByEmail(request.email())).willReturn(true);

        // when & then
        assertThatThrownBy(() -> userService.createUser(request))
                .hasMessageContaining("이미 존재하는 이메일입니다.");
    }

    @Test
    void null_요청으로_회원가입_시도_시_예외가_발생한다() {
        // given
        UserCreateRequest request = null;

        // when & then
        assertThatThrownBy(() -> userService.createUser(request))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 빈_이메일로_회원가입_시도_시_예외가_발생한다() {
        // given
        UserCreateRequest request = new UserCreateRequest("", "testUser", "test1234!");

        // when & then
        assertThatThrownBy(() -> userService.createUser(request))
                .hasMessageContaining("이메일은 필수로 입력해주셔야 합니다.");
    }

    @Test
    void 빈_닉네임으로_회원가입_시도_시_예외가_발생한다() {
        // given
        UserCreateRequest request = new UserCreateRequest("testuser@test.com", "", "test1234!");

        // when & then
        assertThatThrownBy(() -> userService.createUser(request))
                .hasMessageContaining("닉네임은 필수로 입력해주셔야 합니다.");
    }

    @Test
    void 빈_비밀번호로_회원가입_시도_시_예외가_발생한다() {
        // given
        UserCreateRequest request = new UserCreateRequest("testuser@test.com", "testUser", "");

        // when & then
        assertThatThrownBy(() -> userService.createUser(request))
                .hasMessageContaining("비밀번호는 필수로 입력해주셔야 합니다.");
    }

}

