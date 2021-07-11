package com.psjw.thisbox.utils;

import com.psjw.thisbox.exceptions.InvalidTokenException;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("JwtUtil")
class JwtUtilTest {
    private JwtUtil jwtUtil;
    private static final String SECRET_KEY = "12345678901234567890123456789012";
    private static final String VALID_TOKEN = "eyJhbGciOiJIUzI1NiJ9.eyJ1c2VySWQiOjF9" +
            ".ZZ3CUl0jxeLGvQ1Js5nG2Ty5qGTlqai5ubDMXZOdaDk";
    private static final String INVALID_TOKEN = VALID_TOKEN+"INVALID";
    private Long userId = 1L;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil(SECRET_KEY);
    }

    @Test
    @DisplayName("사용자 아이디로 토큰을 생성한다.")
    public void valid_create_token() {
        String token = jwtUtil.encode(userId);
        assertThat(token).isEqualTo(VALID_TOKEN);
    }

    @Test
    @DisplayName("토큰을 복호화 하면 사용자 아이디를 반환한다.")
    public void valid_decode_token() {
        Claims claims = jwtUtil.decode(VALID_TOKEN);
        Long claimUserId = claims.get("userId", Long.class);
        assertThat(claimUserId).isEqualTo(userId);
    }

    @Test
    @DisplayName("잘못된 토큰을 복호화 하면 InvalidTokenException을 던진다.")
    public void invalid_decode_token_exception() {
        assertThatThrownBy(() -> {
                    jwtUtil.decode(INVALID_TOKEN);
                }
        ).isInstanceOf(InvalidTokenException.class);
    }

    @ParameterizedTest(name = "{index} => ''{0}'' 을 복호화 하면 InvalidTokenException을 던진다.")
    @ValueSource(strings = {" "})
    @NullAndEmptySource
    public void invalid_empty_decode_token_exception(String value){
        assertThatThrownBy(() -> {
            jwtUtil.decode(value);
        }).isInstanceOf(InvalidTokenException.class);
    }
}
