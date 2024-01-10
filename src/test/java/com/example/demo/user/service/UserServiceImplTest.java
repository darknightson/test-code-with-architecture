package com.example.demo.user.service;

import com.example.demo.user.domain.User;
import com.example.demo.user.domain.UserCreate;
import com.example.demo.user.domain.UserStatus;
import com.example.demo.user.service.port.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @InjectMocks
    private UserServiceImpl userService;

    @Mock
    UserRepository userRepository;

    @Test
    void 유저_ID로_ACTIVE_유저를_조회할_수_있다() {

        // given
        Long id = 1l;

        User user = User.builder()
                .id(1l)
                .email("anthony.son@kakaoent.com")
                .nickname("anthony")
                .build();

        // when
        when(userRepository.findByIdAndStatus(id, UserStatus.ACTIVE)).thenReturn(Optional.of(user));
        User result = userService.getById(id);

        // then
        assertThat(result.getNickname()).isEqualTo(user.getNickname());
        assertThat(result.getEmail()).isEqualTo(user.getEmail());

    }

    @Test
    void 유저_이메일로_유저를_조회할_수_있다() {

        // given
        String email = "anthony.son@kakaoent.com";
        User user = User.builder()
                .id(1l)
                .email("anthony.son@kakaoent.com")
                .nickname("anthony")
                .build();

        // when
        when(userRepository.findByEmailAndStatus(email, UserStatus.ACTIVE)).thenReturn(Optional.of(user));
        User result = userService.getByEmail(email);

        // then
        assertThat(result.getNickname()).isEqualTo(user.getNickname());
        assertThat(result.getEmail()).isEqualTo(user.getEmail());

    }
}