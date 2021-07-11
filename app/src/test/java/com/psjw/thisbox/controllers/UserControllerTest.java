package com.psjw.thisbox.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.psjw.thisbox.applications.AuthenticationService;
import com.psjw.thisbox.applications.UserService;
import com.psjw.thisbox.domain.Role;
import com.psjw.thisbox.domain.User;
import com.psjw.thisbox.dto.UserModificationData;
import com.psjw.thisbox.dto.UserRegistrationData;
import com.psjw.thisbox.exceptions.UserEmailDuplicationException;
import com.psjw.thisbox.exceptions.UserNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.file.AccessDeniedException;
import java.util.Arrays;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(UserController.class)
@AutoConfigureRestDocs
class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private AuthenticationService authenticationService;

    private PasswordEncoder passwordEncoder;
    private ObjectMapper objectMapper;

    private static final String SECRET_KEY = "12345678901234567890123456789012";
    private static final String USER_VALID_TOKEN = "eyJhbGciOiJIUzI1NiJ9.eyJ1c2VySWQiOjF9" +
            ".ZZ3CUl0jxeLGvQ1Js5nG2Ty5qGTlqai5ubDMXZOdaDk"; //1L
    private static final String ADMIN_VALID_TOKEN = "eyJhbGciOiJIUzI1NiJ9.eyJ1c2VySWQiOjEwMH0" +
            ".TyN9pyFfKE5exvxTmvgnk1WvQJQUhgq2aMIa5cmXQFI"; //2L
    private static final String OTHER_VALID_TOKEN = "eyJhbGciOiJIUzI1NiJ9.eyJ1c2VySWQiOjEwMX0" +
            ".97rv_U5E4Yeh_fL2RkD1nrl8IFybT9ylgnGXJPm5oR0"; //101L


    private static final String USER_INVALID_TOKEN = USER_VALID_TOKEN + "INVALID";
    private static final String ADMIN_INVALID_TOKEN = ADMIN_VALID_TOKEN + "INVALID";
    private static final String BEARER = "Bearer ";

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        objectMapper = new ObjectMapper();
    }

    @Nested
    @DisplayName("getUsers 메서드는")
    class Describe_getUsers {
        @Nested
        @DisplayName("사용자 목록이 있다면")
        class Context_valid_getUsers {
            private Long validUserId = 2L;
            private String validUserEmail = "valid@email.com";
            private String validUserPassword = "validPassword";
            User foundUser;

            @BeforeEach
            void setUpContextvalidGetUesrs() {
                foundUser = User.builder()
                        .userId(validUserId)
                        .userEmail(validUserEmail)
                        .build();

                foundUser.changePassword(validUserPassword, passwordEncoder);

                given(userService.findAllUsers())
                        .willReturn(Arrays.asList(
                                foundUser
                        ));

                given(authenticationService.parseToken(ADMIN_VALID_TOKEN))
                        .willReturn(validUserId);

                given(authenticationService.roles(validUserId))
                        .willReturn(Arrays.asList(new Role("ADMIN")));

            }

            @Test
            @DisplayName("사용자 목록을 응답한다.")
            void findAllUsersValid() throws Exception {
                mockMvc.perform(get("/users")
                        .accept(MediaType.APPLICATION_JSON_UTF8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", BEARER + ADMIN_VALID_TOKEN))
                        .andExpect(status().isOk())
                        .andExpect(content().string(containsString(foundUser.getUserEmail())));
            }
        }

        @Nested
        @DisplayName("요청한 사용자 정보가")
        class Context_invalid_findAllUsers {
            private Long unAuthUserId = 1L;
            private String unAuthUserEmail = "unAuth@email.com";
            private String unAuthUserPassword = "unAuthPassword";
            User foundUser;

            @BeforeEach
            void setUpInvalidFindAllUsers() {
                foundUser = User.builder()
                        .userId(unAuthUserId)
                        .userEmail(unAuthUserEmail)
                        .build();

                foundUser.changePassword(unAuthUserPassword, passwordEncoder);

                given(userService.findAllUsers())
                        .willReturn(Arrays.asList(
                                foundUser
                        ));
                given(authenticationService.parseToken(USER_VALID_TOKEN))
                        .willReturn(unAuthUserId);

                given(authenticationService.roles(unAuthUserId))
                        .willReturn(Arrays.asList(new Role("USER")));
            }

            @Test
            @DisplayName("권한이 없다면 Http Status 403을 응답한다.")
            void findAllUsersWithoutAdminRole() throws Exception {
                mockMvc.perform(get("/users")
                        .accept(MediaType.APPLICATION_JSON_UTF8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", BEARER + USER_VALID_TOKEN))
                        .andExpect(status().isForbidden());
            }


            @Test
            @DisplayName("Access Token이 없다면 HttpStatus 401을 응답한다.")
            void findAllUsersWithoutAccessToken() throws Exception {
                mockMvc.perform(get("/users")
                        .accept(MediaType.APPLICATION_JSON_UTF8)
                        .contentType(MediaType.APPLICATION_JSON))
                        .andExpect(status().isUnauthorized());
            }

            @Test
            @DisplayName("다른 사용자 Access Token이 없다면 HttpStatus 403을 응답한다.")
            void findAllUsersWithoutOtherAccessTOken() throws Exception {
                mockMvc.perform(get("/users")
                        .accept(MediaType.APPLICATION_JSON_UTF8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", BEARER + OTHER_VALID_TOKEN))
                        .andExpect(status().isForbidden());
            }
        }
    }

    @Nested
    @DisplayName("getUser 메서드는")
    class Describe_getUser {
        @Nested
        @DisplayName("올바른 사용자 아이디를 전달한다면")
        class Context_valid_getUser {
            private Long validUserId = 2L;
            private String validUserEmail = "valid@email.com";
            private String validUserName = "validName";
            private String validPhone = "01000000000";
            private String validUserPassword = "validPassword";
            private User foundUser;

            @BeforeEach
            void setUpContextValidGetUser() {
                foundUser = User.builder()
                        .userId(validUserId)
                        .userName(validUserName)
                        .userEmail(validUserEmail)
                        .userPhone(validPhone)
                        .build();

                foundUser.changePassword(validUserPassword, passwordEncoder);

                given(authenticationService.parseToken(USER_VALID_TOKEN))
                        .willReturn(validUserId);

                given(userService.findUser(eq(validUserId)))
                        .willReturn(foundUser);

                given(authenticationService.roles(validUserId))
                        .willReturn(Arrays.asList(new Role("ADMIN")));

            }

            @Test
            @DisplayName("사용자 정보를 응답한다.")
            void findUserWithId() throws Exception {
                mockMvc.perform(get("/users/" + validUserId)
                        .accept(MediaType.APPLICATION_JSON_UTF8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", BEARER + USER_VALID_TOKEN)
                ).andExpect(status().isOk())
                        .andExpect(content()
                                .string(containsString(validUserEmail)));
            }
        }

        @Nested
        @DisplayName("요청한 사용자 아이디가 ")
        class Context_invalid_findUser {
            private Long notfoundUserId = 100L;
            private Long validUserId = 2L;
            private String invalidUserEmail = "notfound@email.com";
            private String invalidUserName = "invalidName";
            private String invalidPhone = "01000000000";
            private String invalidUserPassword = "invalidPassword";
            private User foundUser;

            @BeforeEach
            void setUpInvalidFindUser() {
                foundUser = User.builder()
                        .userId(notfoundUserId)
                        .userName(invalidUserName)
                        .userEmail(invalidUserEmail)
                        .userPhone(invalidPhone)
                        .build();

                foundUser.changePassword(invalidUserPassword, passwordEncoder);

                given(userService.findUser(eq(notfoundUserId)))
                        .willThrow(new UserNotFoundException(notfoundUserId));

                given(authenticationService.parseToken(USER_VALID_TOKEN))
                        .willReturn(validUserId);

                given(authenticationService.roles(validUserId))
                        .willReturn(Arrays.asList(new Role("USER")));

            }

            @Test
            @DisplayName("존재하지 않는다면 Http Status 404 던진다.")
            void getUserWithNotExistedUserId() throws Exception {
                mockMvc.perform(get("/users/" + notfoundUserId)
                        .accept(MediaType.APPLICATION_JSON_UTF8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", BEARER + USER_VALID_TOKEN)
                ).andExpect(status().isNotFound());
            }

            @Test
            @DisplayName("Access Token이 없다면 HttpStatus 401을 응답한다.")
            void getUserWithoutAccessToken() throws Exception {
                mockMvc.perform(get("/users/" + validUserId)
                        .accept(MediaType.APPLICATION_JSON_UTF8)
                        .contentType(MediaType.APPLICATION_JSON)
                ).andExpect(status().isUnauthorized());
            }

            @Test
            @DisplayName("다른 사용자 Access Token을 사용하면 HttpStatus 403을 응답한다.")
            void getUserWithOtherAccessToken() throws Exception {
                mockMvc.perform(get("/users/" + validUserId)
                        .accept(MediaType.APPLICATION_JSON_UTF8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", BEARER + OTHER_VALID_TOKEN)
                ).andExpect(status().isForbidden());
            }
        }
    }

    @Nested
    @DisplayName("createUser 메서드는")
    class Describe_createUser {
        @Nested
        @DisplayName("올바른 사용자 정보를 등록하면")
        class Context_valid_createUser {
            private Long validUserId = 1L;
            private String validUserEmail = "valid@email.com";
            private String validUserName = "validName";
            private String validPhone = "01000000000";
            private String validUserPassword = "validPassword";
            private User savedUser;
            private UserRegistrationData requestUserRegistrationData;
            private String requestUserRegistrationDataJSON;

            @BeforeEach
            void setUpContextValidCreateUser() throws JsonProcessingException {
                savedUser = User.builder()
                        .userId(validUserId)
                        .userEmail(validUserEmail)
                        .userPhone(validPhone)
                        .userName(validUserName)
                        .build();

                requestUserRegistrationData = UserRegistrationData.builder()
                        .userEmail(validUserEmail)
                        .userPhone(validPhone)
                        .userName(validUserName)
                        .userPassword(validUserPassword)
                        .build();

                requestUserRegistrationDataJSON
                        = objectMapper.writeValueAsString(requestUserRegistrationData);

                given(userService.createUser(any(UserRegistrationData.class)))
                        .willReturn(savedUser);

                given(authenticationService.parseToken(USER_VALID_TOKEN))
                        .willReturn(validUserId);

                given(authenticationService.roles(validUserId))
                        .willReturn(Arrays.asList(new Role("USER")));
            }

            @Test
            @DisplayName("저장한 사용자 정보를 응답한다.")
            void createUserSuccess() throws Exception {
                mockMvc.perform(post("/users")
                        .accept(MediaType.APPLICATION_JSON_UTF8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", BEARER + USER_VALID_TOKEN)
                        .content(requestUserRegistrationDataJSON)
                ).andExpect(status().isCreated())
                        .andExpect(content().string(containsString(validUserEmail)))
                        .andExpect(content().string(containsString(validUserName)))
                        .andExpect(content().string(containsString(validPhone)));
            }
        }

        @Nested
        @DisplayName("요청한 사용자 정보가")
        class Context_invalid_createUser {
            private Long validUserId = 1L;
            private String validUserEmail = "notfound@email.com";
            private String validUserName = "invalidName";
            private String invalidPhone = "010XXXXXXXXX";
            private String duplicaionUserEmail = "notfound@email.com";
            private UserRegistrationData requestUserRegistrationData;
            private String requestUserRegistrationDataJSON;
            private UserRegistrationData requestInvalidPhoneUserRegistrationData;
            private String requestInvalidPhoneUserRegistrationDataJSON;
            private UserRegistrationData requestExistedUserRegistrationData;
            private String requestExistedUserRegistrationDataDataJSON;


            @BeforeEach
            void setUpInvalidCreateUser() throws JsonProcessingException {
                requestUserRegistrationData = UserRegistrationData.builder()
                        .userEmail("")
                        .userPhone("")
                        .userName("")
                        .userPassword("")
                        .build();

                requestUserRegistrationDataJSON
                        = objectMapper.writeValueAsString(requestUserRegistrationData);


                requestInvalidPhoneUserRegistrationData = UserRegistrationData.builder()
                        .userEmail(validUserEmail)
                        .userPhone(validUserEmail)
                        .userName(validUserName)
                        .userPassword(invalidPhone)
                        .build();

                requestInvalidPhoneUserRegistrationDataJSON
                        = objectMapper.writeValueAsString(requestUserRegistrationData);

                requestExistedUserRegistrationData = UserRegistrationData.builder()
                        .userEmail(duplicaionUserEmail)
                        .userPhone(validUserEmail)
                        .userName(validUserName)
                        .userPassword(invalidPhone)
                        .build();

                requestExistedUserRegistrationDataDataJSON
                        = objectMapper.writeValueAsString(requestExistedUserRegistrationData);

                given(userService.createUser(requestExistedUserRegistrationData))
                        .willThrow(new UserEmailDuplicationException(duplicaionUserEmail));

            }

            @Test
            @DisplayName("올바르지 않다면 Http Status 400을 응답한다.")
            void createUserWithInvalidAttribute() throws Exception {
                mockMvc.perform(post("/users")
                        .accept(MediaType.APPLICATION_JSON_UTF8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", BEARER + USER_VALID_TOKEN)
                        .content(requestUserRegistrationDataJSON)
                ).andExpect(status().isBadRequest());
            }


            @Test
            @DisplayName("핸드폰 번호가 올바르지 않다면 Http Status 400을 응답한다.")
            void createUserWithInvalidPhoneAttribute() throws Exception {
                mockMvc.perform(post("/users")
                        .accept(MediaType.APPLICATION_JSON_UTF8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", BEARER + USER_VALID_TOKEN)
                        .content(requestInvalidPhoneUserRegistrationDataJSON)
                ).andExpect(status().isBadRequest());
            }

            @Test
            @DisplayName("이미 등록된 이메일이라면 Http Status 400을 응답한다.")
            void createUserWithInvalidExsitedEmail() throws Exception {
                mockMvc.perform(post("/users")
                        .accept(MediaType.APPLICATION_JSON_UTF8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", BEARER + USER_VALID_TOKEN)
                        .content(requestExistedUserRegistrationDataDataJSON)
                ).andExpect(status().isBadRequest());
            }
        }
    }

    @Nested
    @DisplayName("updateUser 메서드는")
    class Describe_updateUser {
        @Nested
        @DisplayName("올바른 사용자 정보로 요청하면")
        class Context_valid_updateUser {
            private Long validUserId = 1L;
            private Long requestUserId = 1L;
            private String validUserEmail = "valid@email.com";
            private String validUserName = "validName";
            private String validPhone = "01000000000";
            private String validUserPassword = "validPassword";
            private UserModificationData requestModificationData;
            private String requestModificationDataJson;

            @BeforeEach
            void setUpValidUpdateUser() throws JsonProcessingException, AccessDeniedException {
                requestModificationData = UserModificationData.builder()
                        .userPhone(validPhone)
                        .userName(validUserName)
                        .userPassword(validUserPassword)
                        .build();

                requestModificationDataJson
                        = objectMapper.writeValueAsString(requestModificationData);

                given(userService.updateUser(
                        eq(validUserId), any(UserModificationData.class), eq(requestUserId)))
                        .will(invocation -> {
                            Long id = invocation.getArgument(0);
                            UserModificationData modificationData = invocation.getArgument(1);
                            return User.builder()
                                    .userId(validUserId)
                                    .userEmail(validUserEmail)
                                    .userName(modificationData.getUserName())
                                    .userPhone(modificationData.getUserPhone())
                                    .build();
                        });
                given(authenticationService.parseToken(USER_VALID_TOKEN))
                        .willReturn(validUserId);

                given(authenticationService.roles(validUserId))
                        .willReturn(Arrays.asList(new Role("USER")));
            }

            @Test
            @DisplayName("사용자 정보를 수정한다.")
            void updateUserWithValidAttribute() throws Exception {
                mockMvc.perform(patch("/users/" + requestUserId)
                        .accept(MediaType.APPLICATION_JSON_UTF8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestModificationDataJson)
                        .header("Authorization", BEARER + USER_VALID_TOKEN)
                ).andExpect(status().isOk())
                        .andExpect(content().string(containsString(validUserEmail)))
                        .andExpect(content().string(containsString(validPhone)));
            }
        }

        @Nested
        @DisplayName("요청한 사용자 정보가")
        class Context_invalid_updateUser {
            private Long validUserId = 1L;
            private String validUserName = "validName";
            private String validPhone = "01000000000";
            private String validUserPassword = "validPassword";
            private UserModificationData requestInvalidModificationData;
            private String requestInvalidModificationDataJson;
            private UserModificationData requestModificationData;
            private String requestModificationDataJson;

            @BeforeEach
            void setUpValidUpdateUser() throws JsonProcessingException, AccessDeniedException {
                requestInvalidModificationData = UserModificationData.builder()
                        .userPhone("")
                        .userName("")
                        .userPassword("")
                        .build();

                requestInvalidModificationDataJson
                        = objectMapper.writeValueAsString(requestInvalidModificationData);


                requestModificationData = UserModificationData.builder()
                        .userPhone(validPhone)
                        .userName(validUserName)
                        .userPassword(validUserPassword)
                        .build();

                requestModificationDataJson
                        = objectMapper.writeValueAsString(requestModificationData);

                given(userService.updateUser(
                        eq(validUserId), any(UserModificationData.class), eq(validUserId)))
                        .willThrow(new UserNotFoundException(validUserId));

                given(authenticationService.parseToken(USER_VALID_TOKEN))
                        .willReturn(validUserId);

                given(authenticationService.roles(validUserId))
                        .willReturn(Arrays.asList(new Role("USER")));
            }

            @Test
            @DisplayName("올바르지 않다면 HttpStatus 400을 응답한다.")
            void updateUserWithoutAttribute() throws Exception {
                mockMvc.perform(patch("/users/" + validUserId)
                        .accept(MediaType.APPLICATION_JSON_UTF8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", BEARER + USER_VALID_TOKEN)
                        .content(requestInvalidModificationDataJson)
                ).andExpect(status().isBadRequest());
            }

            @Test
            @DisplayName("아이디가 없다면 HttpStatus 400을 응답한다.")
            void updateUserWithNotExistedId() throws Exception {
                mockMvc.perform(patch("/users/" + validUserId)
                        .accept(MediaType.APPLICATION_JSON_UTF8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", BEARER + USER_VALID_TOKEN)
                        .content(requestModificationDataJson)
                ).andExpect(status().isNotFound());
            }

            @Test
            @DisplayName("Access Token이 없다면 HttpStatus 401을 응답한다.")
            void updateUserWithoutAccessToken() throws Exception {
                mockMvc.perform(patch("/users/" + validUserId)
                        .accept(MediaType.APPLICATION_JSON_UTF8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestModificationDataJson)
                ).andExpect(status().isUnauthorized());
            }

            @Test
            @DisplayName("다른 사용자 Access Token을 사용하면 HttpStatus 403을 응답한다.")
            void findAllUsersWithoutOtherAccessTOken() throws Exception {
                mockMvc.perform(get("/users/" + validUserId)
                        .accept(MediaType.APPLICATION_JSON_UTF8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", BEARER + OTHER_VALID_TOKEN))
                        .andExpect(status().isForbidden());
            }
        }
    }

    @Nested
    @DisplayName("deleteUser 메서드는")
    class Describe_deleteUser {
        @Nested
        @DisplayName("올바른 사용자 아이디를 전달한다면")
        class Context_valid_deleteUser {
            private Long validUserId = 1L;
            private String validUserEmail = "valid@email.com";
            private String validUserName = "validName";
            private String validPhone = "01000000000";
            private User deletedUser;

            @BeforeEach
            void setUpValidDeleteUser() {
                deletedUser = User.builder()
                        .userId(validUserId)
                        .userName(validUserName)
                        .userEmail(validUserEmail)
                        .userPhone(validPhone)
                        .build();

                given(userService.deleteUser(eq(validUserId)))
                        .willReturn(deletedUser);

                given(authenticationService.parseToken(USER_VALID_TOKEN))
                        .willReturn(validUserId);

                given(authenticationService.roles(validUserId))
                        .willReturn(Arrays.asList(new Role("USER")));
            }

            @Test
            @DisplayName("사용자 정보를 삭제 한다.")
            void deleteUserWithExistedUserId() throws Exception {
                mockMvc.perform(delete("/users/" + validUserId)
                        .accept(MediaType.APPLICATION_JSON_UTF8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", BEARER + USER_VALID_TOKEN)
                ).andExpect(status().isOk());
            }
        }

        @Nested
        @DisplayName("요청한 사용자 정보가")
        class Context_invalid_deleteUser {
            private Long validUserId = 1L;
            private Long notFoundUserId = 2L;
            private String validUserName = "validName";
            private String validPhone = "01000000000";
            private String validUserPassword = "validPassword";

            @BeforeEach
            void setUpValidUpdateUser() throws JsonProcessingException, AccessDeniedException {

                given(userService.deleteUser(notFoundUserId))
                        .willThrow(new UserNotFoundException(notFoundUserId));

                given(authenticationService.parseToken(ADMIN_VALID_TOKEN))
                        .willReturn(notFoundUserId);

                given(authenticationService.roles(notFoundUserId))
                        .willReturn(Arrays.asList(new Role("USER")));

                given(authenticationService.parseToken(USER_VALID_TOKEN))
                        .willReturn(validUserId);

                given(authenticationService.roles(validUserId))
                        .willReturn(Arrays.asList(new Role("USER")));
            }


            @Test
            @DisplayName("아이디가 없다면 HttpStatus 400을 응답한다.")
            void updateUserWithNotExistedId() throws Exception {
                mockMvc.perform(delete("/users/" + notFoundUserId)
                        .accept(MediaType.APPLICATION_JSON_UTF8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", BEARER + ADMIN_VALID_TOKEN)
                ).andExpect(status().isNotFound());
            }

            @Test
            @DisplayName("Access Token이 없다면 HttpStatus 401을 응답한다.")
            void updateUserWithoutAccessToken() throws Exception {
                mockMvc.perform(delete("/users/" + validUserId)
                        .accept(MediaType.APPLICATION_JSON_UTF8)
                        .contentType(MediaType.APPLICATION_JSON)
                ).andExpect(status().isUnauthorized());
            }

            @Test
            @DisplayName("다른 사용자 Access Token을 사용하면 HttpStatus 403을 응답한다.")
            void findAllUsersWithoutOtherAccessTOken() throws Exception {
                mockMvc.perform(delete("/users/" + validUserId)
                        .accept(MediaType.APPLICATION_JSON_UTF8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", BEARER + OTHER_VALID_TOKEN))
                        .andExpect(status().isForbidden());
            }
        }
    }
}
