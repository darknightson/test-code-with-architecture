package com.example.demo.user.domain;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.example.demo.common.domain.exception.CertificationCodeNotMatchedException;
import com.example.demo.common.service.port.ClockHolder;
import com.example.demo.common.service.port.UuidHolder;
import com.example.demo.mock.TestClockHolder;
import com.example.demo.mock.TestUuidHolder;
import com.example.demo.user.controller.port.UserService;
import com.example.demo.user.service.CertificationService;
import com.example.demo.user.service.UserServiceImpl;
import com.example.demo.user.service.port.UserRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.Optional;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
public class UserTest {

    @InjectMocks
    private UserServiceImpl userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CertificationService certificationService;

    @Mock
    private UuidHolder uuidHolder;

    @Spy
    private ClockHolder clockHolder = new TestClockHolder(2l);


    @Test
    public void UserCreate_객체로_생성할_수_있다() {
        // given
        UserCreate userCreate = UserCreate.builder()
                .email("anthony.son@kakaoent.com")
                .nickname("anthony")
                .address("seoul")
                .build();

        // when
        String uuid = UUID.randomUUID().toString();
        User user = User.from(userCreate, new TestUuidHolder(uuid));

        // then
        assertThat(user.getEmail()).isEqualTo(userCreate.getEmail());
        assertThat(user.getNickname()).isEqualTo(userCreate.getNickname());
        assertThat(user.getAddress()).isEqualTo(userCreate.getAddress());
        assertThat(user.getStatus()).isEqualTo(UserStatus.PENDING);
        assertThat(user.getCertificationCode()).isEqualTo(uuid);

    }

    @Test
    public void UserUpdate_객체로_데이터를_업데이트_할_수_있다() {

        // given
        String uuid = UUID.randomUUID().toString();

        User user = User.builder()
                .id(1L)
                .email("anthony.son@kakaoent.com")
                .nickname("anthony.son")
                .address("seoul")
                .build();

        UserUpdate userUpdate = UserUpdate.builder()
                .nickname("nick")
                .address("address")
                .build();

        // when
        when(userRepository.findByIdAndStatus(1L, UserStatus.ACTIVE)).thenReturn(Optional.ofNullable(user));

        when(uuidHolder.random()).thenReturn(uuid);

        when(userRepository.save(any(User.class))).thenReturn(user); // 이 부분에서 스텁

        // 스텁 완료 테스트 진행
        User result = userService.update(1L, userUpdate);

        // then
        assertThat(result.getNickname()).isEqualTo(userUpdate.getNickname());

    }

    @Test
    public void 로그인을_할_수_있고_로그인시_마지막_로그인_시간이_변경된다() {

        Long id  = 1l;
        Long clock  = clockHolder.millis();
        User user = User.builder()
                .id(id)
                .email("anthony.son@kakaoent.com")
                .nickname("anthony.son")
                .lastLoginAt(clock)
                .build();

        when(userRepository.findById(id)).thenReturn(Optional.ofNullable(user));

        userService.login(id);

        // domain 소형 테스트

        ClockHolder clockHolder = new TestClockHolder(3l);

        User loginUser = user.login(clockHolder);

        Assertions.assertThat(loginUser.getLastLoginAt()).isEqualTo(3l);
    }

    @Test
    public void 유효한_인증_코드로_계정을_활성화_할_수_있다() {
        User user = User.builder()
                .id(1l)
                .email("anthony.son@kakaoent.com")
                .nickname("anthony.son")
                .certificationCode("1234")
                .build();

        User certificate = user.certificate("1234");

        assertThat(certificate.getStatus()).isEqualTo(UserStatus.ACTIVE);
        assertThat(certificate.getCertificationCode()).isEqualTo("1234");
    }

    @Test
    public void 잘못된_인증_코드로_계정을_활성화_하려하면_에러를_던진다() {
        User user = User.builder()
                .id(1l)
                .email("anthony.son@kakaoent.com")
                .nickname("anthony.son")
                .certificationCode("12341")
                .build();

        assertThatThrownBy(
                () -> user.certificate("1234")
        ).isInstanceOf(CertificationCodeNotMatchedException.class);
    }
}
