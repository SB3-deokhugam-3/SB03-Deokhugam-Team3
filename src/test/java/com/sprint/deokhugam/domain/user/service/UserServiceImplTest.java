package com.sprint.deokhugam.domain.user.service;

import static org.assertj.core.api.Assertions.assertThat;
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

}