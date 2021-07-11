package com.psjw.thisbox.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@DisplayName("User")
class UserTest {
    private PasswordEncoder passwordEncoder;


    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
    }

    @DisplayName("changeWith 메서드는 사용자 정보를 변경한다.")
    @Test
    void changeWithPassword() {

        User user = User.builder().build();
        user.changeWith(User.builder()
                .userEmail("chageEmail@email.com")
                .userName("changeName")
                .userPhone("010xxxxxxxxx")
                .userId(1L)
                .build());

        assertThat(user.getUserEmail())
                .isEqualTo("chageEmail@email.com");
        assertThat(user.getUserName())
                .isEqualTo("changeName");
        assertThat(user.getUserPhone())
                .isEqualTo("010xxxxxxxxx");
        assertThat(user.getUpdateById())
                .isEqualTo(1L);
        assertThat(user.getUpdateDate()).isNotNull();
    }

    @Test
    @DisplayName("changePassword 메서드는 패스워드를 변경한다.")
    void changePassword() {
        User user = User.builder().build();
        user.changePassword("changePassword",passwordEncoder);
        assertThat(user.getUserPassword()).isNotEmpty();
    }

    @Test
    @DisplayName("isWithdrawal 메서드는 회원정보의 탈퇴 여부를 변경 한다.")
    void isWithdrawal(){
        User user = User.builder().build();
        assertThat(user.isDeleted()).isFalse();
        user.isWithdrawal();
        assertThat(user.isDeleted()).isTrue();
    }

    @Nested
    @DisplayName("authenticate 메서드는")
    class Describe_authenticate{

        @Test
        @DisplayName("authenticate 메서드는 Password를 비교한다.")
        void authenticate(){
            User user = User.builder().build();
            user.changePassword("validPassword",passwordEncoder);

            assertThat(user.authenticate(
                    "validPassword", passwordEncoder))
                    .isTrue();

            assertThat(user.authenticate(
                    "wrongPassword", passwordEncoder))
                    .isFalse();
        }

        @Test
        @DisplayName("탈퇴한 유저는 비교되지 않지 않는다.")
        void authenticateWithWithdrawal(){
            User user = User.builder()
                    .deleted(true)
                    .build();
            user.changePassword("WithdrawalPassword",passwordEncoder);
            assertThat(user.authenticate(
                    "WithdrawalPassword", passwordEncoder)).isFalse();
        }
    }


    @Nested
    @DisplayName("updateUserInfo 메서드는")
    class Describe_updateUserInfo{

        @DisplayName("createById 정보가 없으면 생성일에 대한 정보는 존재하지 않는다.")
        @Test
        void updateUserInfoWithoutCreateById(){
            User user = User.builder().build();
            user.updateUserInfo(null, 1L);
            assertThat(user.getCreateDate()).isNull();
            assertThat(user.getCreateById()).isNull();
        }

        @DisplayName("생성일과 수정일에 대한 정보가 변경된다.")
        @Test
        void updateUserInfoWithCreateById(){
            User user = User.builder().build();
            user.updateUserInfo(1L, 1L);
            assertThat(user.getCreateDate()).isNotNull();
            assertThat(user.getCreateById()).isEqualTo(1L);
            assertThat(user.getUpdateDate()).isNotNull();
            assertThat(user.getUpdateById()).isEqualTo(1L);
        }
    }

}