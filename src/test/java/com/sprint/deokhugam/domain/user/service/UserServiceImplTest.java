package com.sprint.deokhugam.domain.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sprint.deokhugam.domain.user.dto.data.UserDto;
import com.sprint.deokhugam.domain.user.dto.request.UserCreateRequest;
import com.sprint.deokhugam.domain.user.dto.request.UserLoginRequest;
import com.sprint.deokhugam.domain.user.dto.request.UserUpdateRequest;
import com.sprint.deokhugam.domain.user.entity.User;
import com.sprint.deokhugam.domain.user.exception.DuplicateEmailException;
import com.sprint.deokhugam.domain.user.exception.InvalidUserRequestException;
import com.sprint.deokhugam.domain.user.exception.UserNotFoundException;
import com.sprint.deokhugam.domain.user.mapper.UserMapper;
import com.sprint.deokhugam.domain.user.repository.UserRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
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

    private User user;
    private UserDto userDto;
    private UUID userId;

    @BeforeEach
    void setUp() {
        user = new User("testUser@test.com", "testUser", "test1234!", false);
        userDto = UserDto.builder()
                .id(UUID.randomUUID())
                .nickname(user.getNickname())
                .email(user.getEmail())
                .isDeleted(false)
                .build();
        userId = user.getId();
    }

    @Test
    void 회원가입을_하면_회원이_저장된다() {
        // given
        UserCreateRequest request = new UserCreateRequest("userEmail@test.com", "testUser", "test1234!");

        given(userRepository.existsByEmail(anyString())).willReturn(false);
        given(userMapper.toEntity(any())).willReturn(user);
        given(userRepository.save(any(User.class))).willReturn(user);
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
    void 중복_이메일로_회원가입_시_예외_발생한다() {
        // given
        UserCreateRequest request = new UserCreateRequest("duplicate@example.com", "testuser", "password");
        given(userRepository.existsByEmail(request.email())).willReturn(true);

        // when & then
        assertThatThrownBy(() -> userService.createUser(request))
                .isInstanceOf(DuplicateEmailException.class)
                .hasMessageContaining("이미 존재하는 이메일입니다.");
    }


    @Test
    void 존재하는_유저_조회시_성공적으로_조회한다() {
        UUID userId = user.getId();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userMapper.toDto(user)).thenReturn(
                UserDto.builder()
                        .id(userId)
                        .email(user.getEmail())
                        .nickname(user.getNickname())
                        .build()
        );

        UserDto result = userService.findUser(userId);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(userId);
        assertThat(result.email()).isEqualTo(user.getEmail());
        assertThat(result.nickname()).isEqualTo(user.getNickname());
    }

    @Test
    void 존재하지_않는_유저_조회시_예외가_발생한다() {
        UUID id = UUID.randomUUID();
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findUser(id))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("존재하지 않는 사용자 입니다.");
    }

    @Test
    void 존재하는_사용자로_로그인시_성공한다() {
        UserLoginRequest request = new UserLoginRequest(user.getEmail(), user.getPassword());
        when(userRepository.findByEmail(user.getEmail()))
                .thenReturn(Optional.of(user));
        when(userMapper.toDto(user))
                .thenReturn(userDto);

        UserDto result = userService.loginUser(request);

        assertThat(result).isNotNull();
        assertThat(result.email()).isEqualTo(user.getEmail());
        assertThat(result.nickname()).isEqualTo(user.getNickname());
    }

    @Test
    void 존재하지_않는_이메일로_로그인시_예외가_발생한다() {
        // given
        UserLoginRequest request = new UserLoginRequest("nonexistent@test.com", user.getPassword());
        when(userRepository.findByEmail("nonexistent@test.com"))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userService.loginUser(request))
                .isInstanceOf(InvalidUserRequestException.class)
                .hasMessageContaining("해당하는 이메일은 존재하지 않습니다.");
    }

    @Test
    void 잘못된_비밀번호로_로그인시_예외가_발생한다() {
        // given
        UserLoginRequest request = new UserLoginRequest(user.getEmail(), "wrongPassword");
        when(userRepository.findByEmail(user.getEmail()))
                .thenReturn(Optional.of(user));

        // when & then
        assertThatThrownBy(() -> userService.loginUser(request))
                .isInstanceOf(InvalidUserRequestException.class)
                .hasMessageContaining("비밀번호가 일치하지 않습니다.");
    }

    @Test
    void null_요청으로_로그인시_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> userService.loginUser(null))
                .isInstanceOf(InvalidUserRequestException.class);
    }

    @Test
    void 닉네임_수정에_성공한다() {
        // given
        String newNickname = "updatedNickName";

        UserUpdateRequest request = UserUpdateRequest.builder()
                .nickname(newNickname)
                .build();

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        UserDto expectedDto = UserDto.builder()
                .id(userId)
                .email(user.getEmail())
                .nickname(newNickname)
                .build();

        when(userMapper.toDto(user))
                .thenReturn(expectedDto);

        // when
        UserDto result = userService.updateUserNickName(request, userId);

        // then
        assertThat(result.nickname()).isEqualTo(newNickname);
        assertThat(result.id()).isEqualTo(userId);
        assertThat(result.email()).isEqualTo(user.getEmail());

        verify(userRepository).findById(userId);
        verify(userMapper).toDto(user);
    }

    @Test
    void null값으로_닉네임_수정할_시_예외가_발생한다() {

        assertThatThrownBy(() -> userService.updateUserNickName(null, userId))
                .isInstanceOf(InvalidUserRequestException.class);
    }

    @Test
    void 논리삭제_상태인_사용자를_물리삭제하면_성공한다() {
        // given
        user.softDelete(); // 논리 삭제 상태로 변경
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // when
        userService.hardDeleteUser(userId);

        // then

        verify(userRepository).findById(userId);
        verify(userRepository).delete(user);
    }

    @Test
    void 논리삭제되지_않은_사용자_물리삭제_시_예외가_발생한다() {
        // given
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // when & then
        assertThatThrownBy(() -> userService.hardDeleteUser(userId))
                .isInstanceOf(InvalidUserRequestException.class);

        verify(userRepository).findById(userId);
        verify(userRepository, never()).delete(user); //userRepository.delete 가 실행하지 않았음을 검증
    }


    @Test
    void 논리삭제_물리삭제중_사용자_없을_경우_예외가_발생한다
            () {
        // given
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // then
        assertThatThrownBy(() -> userService.deleteUser(userId))
                .isInstanceOf(UserNotFoundException.class);

        assertThatThrownBy(() -> userService.hardDeleteUser(userId))
                .isInstanceOf(UserNotFoundException.class);
    }

}