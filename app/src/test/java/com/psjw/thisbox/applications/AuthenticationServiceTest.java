package com.psjw.thisbox.applications;

import com.psjw.thisbox.domain.Role;
import com.psjw.thisbox.domain.RoleRepository;
import com.psjw.thisbox.domain.User;
import com.psjw.thisbox.domain.UserRepository;
import com.psjw.thisbox.exceptions.InvalidTokenException;
import com.psjw.thisbox.exceptions.LoginFailException;
import com.psjw.thisbox.utils.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@DisplayName("AuthenticationService")
class AuthenticationServiceTest {
    private static final String SECRET_KEY = "12345678901234567890123456789012";
    private static final String VALID_TOKEN = "eyJhbGciOiJIUzI1NiJ9.eyJ1c2VySWQiOjF9" +
            ".ZZ3CUl0jxeLGvQ1Js5nG2Ty5qGTlqai5ubDMXZOdaDk";
    private static final String INVALID_TOKEN = VALID_TOKEN+"INVALID";

    private AuthenticationService authenticationService;

    private UserRepository userRepository = mock(UserRepository.class);
    private RoleRepository roleRepository = mock(RoleRepository.class);
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        JwtUtil jwtUtil = new JwtUtil(SECRET_KEY);
        passwordEncoder = new BCryptPasswordEncoder();
        authenticationService = new AuthenticationService(
                userRepository, roleRepository, jwtUtil, passwordEncoder);

        given(roleRepository.findAllByUserId(1L))
                .willReturn(Arrays.asList(new Role("USER")));
        given(roleRepository.findAllByUserId(1L))
                .willReturn(Arrays.asList(new Role("USER"),new Role("ADMIN")));

    }

    @Nested
    @DisplayName("login 메서드는")
    class Describe_login {

        @Nested
        @DisplayName("올바른 이메일과 암호를 넣으면")
        class Context_valid_login {
            private String validEmail = "valid@email.com";
            private String validPassword = "validPassword";
            private Long validUserId = 1L;
            private User validUser;

            @BeforeEach
            void setUpdValidLogin() {
                validUser = User.builder()
                        .userId(validUserId)
                        .userEmail(validEmail)
                        .build();
                validUser.changePassword(validPassword, passwordEncoder);
                given(userRepository.findByUserEmail(
                        eq(validEmail)))
                        .willReturn(Optional.of(validUser));
            }

            @Test
            @DisplayName("토큰을 반환한다.")
            void loginRightEmailAndPassword() {
                String accessToken = authenticationService.login(validEmail, validPassword);
                assertThat(accessToken).isEqualTo(VALID_TOKEN);
                verify(userRepository).findByUserEmail(validEmail);
            }
        }

        @Nested
        @DisplayName("올바르지 않은 ")
        class Conetxt_invalid_login {
            private String inValidEmail = "invalid@email.com";
            private String inValidPassword = "invalidPassword";
            private String validEmail = "valid@email.com";
            private String validPassword = "validPassword";

            @Test
            @DisplayName("이메일을 넣으면 LoginFailException을 던진다.")
            void loginWithWrongEmail() {
                assertThatThrownBy(()
                        -> authenticationService.login(inValidEmail, validPassword))
                        .isInstanceOf(LoginFailException.class);
            }

            @Test
            @DisplayName("패스워드를 넣으면 LoginFailException을 던진다.")
            void loginWithWrongPassword() {
                assertThatThrownBy(()
                        -> authenticationService.login(validEmail, inValidPassword))
                        .isInstanceOf(LoginFailException.class);
            }
        }
    }

    @Nested
    @DisplayName("parseToken 메서드는")
    class Describe_parseToken {
        @Nested
        @DisplayName("올바른 토큰을 전달하면")
        class Context_valid_parseToken {
            private Long validUserId = 1L;

            @Test
            @DisplayName("사용자 아이디를 반환한다.")
            void parseTokenWIthValidToken() {
                Long userId = authenticationService.parseToken(VALID_TOKEN);
                assertThat(userId).isEqualTo(validUserId);
            }
        }

        @Nested
        @DisplayName("올바르지 않은 토큰을 복호화하면")
        class Context_invalid_parseToken {

            @Test
            @DisplayName("InvalidToken Exception을 던진다.")
            void parseTokenWithInvalidToken(){
                assertThatThrownBy(() -> {
                    authenticationService.parseToken(INVALID_TOKEN);
                }).isInstanceOf(InvalidTokenException.class);
            }
        }

        @ParameterizedTest(name = "{index} => ''{0}'' 을 복호화 하면 InvalidTokenException을 던진다.")
        @ValueSource(strings = {" "})
        @NullAndEmptySource
        void parseTokenWithEmptyToken(String value){
            assertThatThrownBy(() -> {
                authenticationService.parseToken(value);
            }).isInstanceOf(InvalidTokenException.class);
        }
    }

    @Test
    @DisplayName("사용자에 대한 Role을 조회 가능하다.")
    void getRoles(){
        assertThat(authenticationService.roles(1L).stream()
                .map(Role::getName)
                .collect(Collectors.toList())
        ).isEqualTo(Arrays.asList("USER","ADMIN"));
    }

}
