package com.psjw.thisbox.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.psjw.thisbox.applications.AuthenticationService;
import com.psjw.thisbox.dto.SessionRequestData;
import com.psjw.thisbox.exceptions.LoginFailException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("SessionController")
@WebMvcTest(SessionController.class)
class SessionControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthenticationService authenticationService;

    private ObjectMapper objectMapper = new ObjectMapper();

    private static final String VALID_TOKEN = "eyJhbGciOiJIUzI1NiJ9.eyJ1c2VySWQiOjF9" +
            ".ZZ3CUl0jxeLGvQ1Js5nG2Ty5qGTlqai5ubDMXZOdaDk";

    @Nested
    @DisplayName("login 메서드는")
    class Describe_login {
        @Nested
        @DisplayName("올바른 사용자 정보를 입력한다면")
        class Context_valid_create_accessToken {
            private String userEmail = "test@gmail.com";
            private String userPassword = "test";
            private SessionRequestData sessionRequestData;
            private String sessionRequestDataJson;

            @BeforeEach
            void setUp() throws JsonProcessingException {
                sessionRequestData = SessionRequestData.builder()
                        .userEmail(userEmail)
                        .userPassword(userPassword)
                        .build();
                given(authenticationService.login(userEmail, userPassword))
                        .willReturn(VALID_TOKEN);

                sessionRequestDataJson = objectMapper.writeValueAsString(sessionRequestData);

            }

            @Test
            @DisplayName("토큰을 반환한다.")
            void validLoginByEmailAndPassword() throws Exception {
                mockMvc.perform(post("/session")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(sessionRequestDataJson))
                        .andExpect(status().isCreated())
                        .andExpect(content().string(containsString(VALID_TOKEN)));
            }
        }

        @Nested
        @DisplayName("올바르지 않은")
        class Context_invalid_create_accessToken {
            private String userEmail = "test@gmail.com";
            private String userPassword = "test";
            private String wrongUserEmail = "wrong@gmail.com";
            private String wrongUserPassword = "wrong";
            private SessionRequestData sessionRequestWrongEmailData;
            private SessionRequestData sessionRequestWrongPasswordData;
            private String sessionRequestWrongEmailDataJson;
            private String sessionRequestWrongPasswordDataJson;

            @BeforeEach
            void setUp() throws JsonProcessingException {
                sessionRequestWrongEmailData = SessionRequestData.builder()
                        .userEmail(userEmail)
                        .userPassword(wrongUserPassword)
                        .build();

                sessionRequestWrongEmailDataJson
                        = objectMapper.writeValueAsString(sessionRequestWrongEmailData);

                sessionRequestWrongPasswordData = SessionRequestData.builder()
                        .userEmail(wrongUserEmail)
                        .userPassword(userPassword)
                        .build();

                sessionRequestWrongPasswordDataJson
                        = objectMapper.writeValueAsString(sessionRequestWrongPasswordData);

                given(authenticationService.login(wrongUserEmail, userPassword))
                        .willThrow(new LoginFailException(userEmail));

                given(authenticationService.login(userEmail, wrongUserPassword))
                        .willThrow(new LoginFailException(userEmail));
            }

            @Test
            @DisplayName("이메일을 입력한다면 Http 상태코드 401을 반환한다.")
            void invalid_login_by_wrong_email() throws Exception {
                mockMvc.perform(post("/session")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(sessionRequestWrongEmailDataJson))
                        .andExpect(status().isBadRequest());
            }


            @Test
            @DisplayName("패스워드를 입력한다면 Http 상태코드 401을 반환한다.")
            void invalid_login_by_wrong_password() throws Exception {
                mockMvc.perform(post("/session")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(sessionRequestWrongPasswordDataJson))
                        .andExpect(status().isBadRequest());
            }

        }
    }

}
