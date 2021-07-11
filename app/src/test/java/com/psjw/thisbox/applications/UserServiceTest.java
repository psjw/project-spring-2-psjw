package com.psjw.thisbox.applications;


import com.github.dozermapper.core.DozerBeanMapperBuilder;
import com.github.dozermapper.core.Mapper;
import com.psjw.thisbox.domain.Role;
import com.psjw.thisbox.domain.RoleRepository;
import com.psjw.thisbox.domain.User;
import com.psjw.thisbox.domain.UserRepository;
import com.psjw.thisbox.dto.UserModificationData;
import com.psjw.thisbox.dto.UserRegistrationData;
import com.psjw.thisbox.exceptions.UserEmailDuplicationException;
import com.psjw.thisbox.exceptions.UserNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.nio.file.AccessDeniedException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@DisplayName("UserService")
class UserServiceTest {

    private PasswordEncoder passwordEncoder;
    private UserService userService;
    private Mapper mapper;
    private UserRepository userRepository = mock(UserRepository.class);
    private RoleRepository roleRepository = mock(RoleRepository.class);

    @BeforeEach
    void setUp() {
        mapper = DozerBeanMapperBuilder.buildDefault();
        passwordEncoder = new BCryptPasswordEncoder();
        userService = new UserService(userRepository, mapper, passwordEncoder, roleRepository);
    }

    @Nested
    @DisplayName("findAllUsers 메서드는")
    class Describe_findAllUsers {
        private List<User> foundAllUsers;
        private User foundUser;

        @BeforeEach
        void setFindAllUsers() {
            UserRegistrationData userRegistrationData
                    = UserRegistrationData.builder()
                    .userEmail("valid@email.com")
                    .userPassword("validPassword")
                    .build();

            foundUser = mapper.map(userRegistrationData, User.class);

            foundAllUsers = List.of(foundUser);

            given(userRepository.findAll())
                    .willReturn(foundAllUsers);

        }

        @Test
        @DisplayName("모든 사용자 리스트를 반환한다.")
        void findAllusers() {
            List<User> foundUsers = userService.findAllUsers();
            assertThat(foundUsers).isEqualTo(Arrays.asList(foundUser));
        }
    }

    @Nested
    @DisplayName("findUser 메서드는")
    class Describe_findUser {

        @Nested
        @DisplayName("사용자 아이디가 올바르다면")
        class Context_valid_findUser {
            private Long validUserId = 1L;
            private String validEmail = "valid@email.com";
            private String validName = "validName";
            private String validPassword = "validPassword";
            private User foundUser;

            @BeforeEach
            void setUpValidFIndUser() {
                foundUser = User.builder()
                        .userId(validUserId)
                        .userEmail(validEmail)
                        .userName(validName)
                        .userPassword(validPassword)
                        .build();

                given(userRepository.findByUserIdAndDeletedIsFalse(eq(validUserId)))
                        .willReturn(Optional.of(foundUser));
            }

            @Test
            @DisplayName("사용자 정보를 반환한다.")
            void findUserWithRightUserId() {
                User user = userService.findUser(validUserId);
                assertThat(user.getUserName())
                        .isEqualTo(foundUser.getUserName());
                assertThat(user.getUserEmail())
                        .isEqualTo(foundUser.getUserEmail());
            }
        }

        @Nested
        @DisplayName("사용자 아이디가 올바르지 않다면")
        class Context_invalid_findUser {
            private Long invalidUserId = 1L;

            @BeforeEach
            void setUpInvalidFindUser() {

                given(userRepository.findByUserId(eq(invalidUserId)))
                        .willThrow(new UserNotFoundException(invalidUserId));
            }

            @Test
            @DisplayName("UserNotFoundException을 던진다")
            void findUserWithWrongUserId() {
                assertThatThrownBy(() -> {
                    userService.findUser(invalidUserId);
                }).isInstanceOf(UserNotFoundException.class);
            }
        }
    }

    @Nested
    @DisplayName("createUser 메서드는")
    class Describe_createUser {
        @Nested
        @DisplayName("올바른 사용자 정보를 전달한다면")
        class Context_valid_createUser {
            private Long validUserId = 1L;
            private String validUserEmail = "valid@email.com";
            private String validPassword = "validPassword";
            private String validUserName = "validUserName";
            private String validUserPhone = "01000000000";
            private User validUser;

            @BeforeEach
            void setUpValidCreateUser() {

                validUser = User.builder()
                        .userId(validUserId)
                        .userEmail(validUserEmail)
                        .userPhone(validUserPhone)
                        .userName(validUserName)
                        .build();

                validUser.changePassword(validPassword, passwordEncoder);

                given(userRepository.findByUserEmail(validUserEmail))
                        .willReturn(Optional.of(validUser));

                given(userRepository.save(any(User.class)))
                        .willReturn(validUser);

            }

            @Test
            @DisplayName("사용자 정보를 저장후 반환한다.")
            void validCreateUser() {
                UserRegistrationData userRegistrationData = UserRegistrationData.builder()
                        .userEmail(validUserEmail)
                        .userPassword(validPassword)
                        .userPhone(validUserPhone)
                        .userName(validUserName)
                        .build();

                User createdUser = userService.createUser(userRegistrationData);
                assertThat(createdUser).isNotNull();
                assertThat(createdUser.getUserId()).isEqualTo(validUserId);
                assertThat(createdUser.getUserEmail()).isEqualTo(validUserEmail);
                assertThat(createdUser.getUserName()).isEqualTo(validUserName);
                assertThat(createdUser.getUserPhone()).isEqualTo(validUserPhone);
                assertThat(createdUser.authenticate(validPassword, passwordEncoder))
                        .isTrue();
                verify(userRepository).save(any(User.class));
                verify(roleRepository).save(any(Role.class));

            }
        }

        @Nested
        @DisplayName("중복된 이메일을 입력한다면")
        class Context_invalid_createUser {
            private String invalidUserEmail = "invalid@email.com";

            @BeforeEach
            void setUpInvalidCreateUser() {
                given(userRepository.findByUserEmail(invalidUserEmail))
                        .willThrow(new UserEmailDuplicationException(invalidUserEmail));
            }

            @Test
            @DisplayName("UserEmailDuplicationException을 던진다.")
            void invalidCreateUserDuplicateEmail() {
                UserRegistrationData userRegistrationData = UserRegistrationData.builder()
                        .userEmail(invalidUserEmail)
                        .build();
                assertThatThrownBy(() -> {
                    userService.createUser(userRegistrationData);
                }).isInstanceOf(UserEmailDuplicationException.class);

                verify(userRepository).findByUserEmail(invalidUserEmail);
            }
        }
    }

    @Nested
    @DisplayName("updateUser 메서드는")
    class Describe_updateUser {
        @Nested
        @DisplayName("올바른 사용자 아이디를 전달하면")
        class Context_valid_updateUser {
            private Long validUserId = 1L;
            private String validUserEmail = "valid@email.com";
            private String validUserName = "validUserName";
            private String validUserPhone = "01000000000";
            private User validUser;

            @BeforeEach
            void setUpValidUpdateUser() {
                validUser = User.builder()
                        .userId(validUserId)
                        .userEmail(validUserEmail)
                        .userPhone(validUserPhone)
                        .userName(validUserName)
                        .build();

                given(userRepository.findByUserIdAndDeletedIsFalse(eq(validUserId)))
                        .willReturn(Optional.of(validUser));

            }

            @Test
            @DisplayName("사용자 정보를 변경한다.")
            void updateUserWithExistedId() throws AccessDeniedException {
                String userEmail = "udpate@email.com";
                String userName = "updateUserName";
                String userPhone = "01011111111";
                Long userId = 1L;

                UserModificationData userModificationData
                        = UserModificationData.builder()
                        .userName(userEmail)
                        .userName(userName)
                        .userPhone(userPhone)
                        .build();

                User updatedUser = userService.updateUser(
                        validUserId, userModificationData, userId);

                assertThat(updatedUser.getUserId())
                        .isEqualTo(userId);
                assertThat(updatedUser.getUserName())
                        .isEqualTo(userName);
                assertThat(updatedUser.getUserPhone())
                        .isEqualTo(userPhone);
                verify(userRepository).findByUserIdAndDeletedIsFalse(eq(userId));
            }
        }

        @Nested
        @DisplayName("전달한 사용자 아이디가")
        class Context_invalid_updateUser {
            private Long validUserId = 1L;
            private Long invalidUserId = 100L;
            private Long deletedUserId = 200L;
            private Long notFoundUserId = 300L;
            private UserModificationData userModificationData;
            private String validUserName = "validUserName";
            private String validUserPhone = "01000000000";

            @BeforeEach
            void setUpInvalidUpdateUser() {
                userModificationData = UserModificationData.builder()
                        .userName(validUserName)
                        .userPhone(validUserPhone)
                        .build();

                given(userRepository.findByUserIdAndDeletedIsFalse(invalidUserId))
                        .willThrow(new UserNotFoundException(invalidUserId));

                given(userRepository.findByUserIdAndDeletedIsFalse(notFoundUserId))
                        .willThrow(new UserNotFoundException(notFoundUserId));
            }

            @Test
            @DisplayName("삭제됬다면 UserNotFoundException을 전달한다.")
            void updateUserWithDeletedId() {
                assertThatThrownBy(() -> {
                    userService.updateUser(deletedUserId
                            , userModificationData, deletedUserId);
                }).isInstanceOf(UserNotFoundException.class);
            }

            @Test
            @DisplayName("다른 사용자의 아이디라면 AccessDeniedException을 전달한다.")
            void updateUserWithoutId() {
                assertThatThrownBy(() -> {
                    userService.updateUser(validUserId
                            , userModificationData, invalidUserId);
                }).isInstanceOf(AccessDeniedException.class);
            }

            @Test
            @DisplayName("존재하지 않는다면 UserNotFoundException을 전달한다.")
            void updateUserWithNotExistedId() {
                assertThatThrownBy(() -> {
                    userService.updateUser(notFoundUserId
                            , userModificationData, notFoundUserId);
                }).isInstanceOf(UserNotFoundException.class);
            }
        }
    }

    @Nested
    @DisplayName("deleteUser 메서드는")
    class Describe_deleteUser {
        @Nested
        @DisplayName("올바른 사용자 아이디를 전달하면")
        class Context_valid_deleteUser {
            private Long validUserId = 1L;
            private String validUserEmail = "valid@email.com";
            private String validUserName = "validUserName";
            private String validUserPhone = "01000000000";
            private User validUser;

            @BeforeEach
            void setUpValidDeleteUser() {

                validUser = User.builder()
                        .userId(validUserId)
                        .userEmail(validUserEmail)
                        .userPhone(validUserPhone)
                        .userName(validUserName)
                        .build();
                given(userRepository.findByUserIdAndDeletedIsFalse(validUserId))
                        .willReturn(Optional.of(validUser));
            }

            @Test
            @DisplayName("사용자 정보를 삭제한다.")
            void deleteUserWithExistedUserId() {
                User deletedUser = userService.deleteUser(validUserId);
                assertThat(deletedUser.isDeleted()).isTrue();
            }

        }

        @Nested
        @DisplayName("요청한 사용자 아이디가")
        class Context_invalid_deleteUser {
            private Long deletedUserId = 200L;
            private Long notFoundUserId = 300L;

            //존재하지 않는 아이디
            //이미 삭제된 아이디
            @BeforeEach
            void setUpInvalidDeleteUser() {
                given(userRepository.findByUserIdAndDeletedIsFalse(deletedUserId))
                        .willThrow(new UserNotFoundException(deletedUserId));

                given(userRepository.findByUserIdAndDeletedIsFalse(notFoundUserId))
                        .willThrow(new UserNotFoundException(notFoundUserId));
            }

            @Test
            @DisplayName("존재하지 않는다면 UserNotFoundException을 전달한다.")
            void deleteUserWithNotExistedId() {
                assertThatThrownBy(() -> {
                    userService.deleteUser(notFoundUserId);
                }).isInstanceOf(UserNotFoundException.class);
            }

            @Test
            @DisplayName("이미 삭제됬다면 UserNotFoundException을 전달한다.")
            void deleteUserWithdeletedId() {
                assertThatThrownBy(() -> {
                    userService.deleteUser(deletedUserId);
                }).isInstanceOf(UserNotFoundException.class);
            }
        }
    }
}

